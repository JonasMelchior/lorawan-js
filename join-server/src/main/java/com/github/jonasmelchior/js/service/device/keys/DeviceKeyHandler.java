package com.github.jonasmelchior.js.service.device.keys;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.SessionStatus;
import com.github.jonasmelchior.js.data.job.JobType;
import com.github.jonasmelchior.js.data.job.RunningJob;
import com.github.jonasmelchior.js.data.keys.*;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.service.device.IDevKeyIdService;
import com.github.jonasmelchior.js.service.device.IDeviceService;
import com.github.jonasmelchior.js.service.log.IAppSKeyReqLogService;
import com.github.jonasmelchior.js.service.log.IJoinLogService;
import com.github.jonasmelchior.js.service.lrwan.JoinReqFailedExc;
import com.github.jonasmelchior.js.service.utils.IRunningJobService;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceKeyHandler {
    private IDeviceService deviceService;
    private KeyHandler keyHandler;
    private IJoinLogService joinLogService;
    private IAppSKeyReqLogService appSKeyReqLogService;
    private IRunningJobService jobService;
    private IDevKeyIdService devKeyIdService;

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler, IJoinLogService joinLogService, IRunningJobService jobService, IAppSKeyReqLogService appSKeyReqLogService) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
        this.joinLogService = joinLogService;
        this.jobService = jobService;
        this.appSKeyReqLogService = appSKeyReqLogService;
    }

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler, IJoinLogService joinLogService, IRunningJobService jobService) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
        this.joinLogService = joinLogService;
        this.jobService = jobService;
    }

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler, IJoinLogService joinLogService, IAppSKeyReqLogService appSKeyReqLogService, IDevKeyIdService devKeyIdService) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
        this.joinLogService = joinLogService;
        this.appSKeyReqLogService = appSKeyReqLogService;
        this.devKeyIdService = devKeyIdService;
    }

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
    }

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler, IDevKeyIdService devKeyIdService) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
        this.devKeyIdService = devKeyIdService;
    }

    public DeviceKeyHandler(IDeviceService deviceService, KeyHandler keyHandler, IJoinLogService joinLogService) {
        this.deviceService = deviceService;
        this.keyHandler = keyHandler;
        this.joinLogService = joinLogService;
    }

    public DeviceKeyHandler(KeyHandler keyHandler, IDevKeyIdService devKeyIdService) {
        this.keyHandler = keyHandler;
        this.devKeyIdService = devKeyIdService;
    }

    // Used to initialize device and corresponding key with new credential
    public Pair<Boolean, String> init(KeySpec keySpec, String password, String credentialIdentifier, User user, Boolean exposed, MACVersion macVersion) {
        // No need to derive lifetime keys, since more than 1 KeySpec is passed in case of LoRaWAN 1.1 when initializing
        if (deviceService.findByDevEUI(keySpec.getDevEUI()).isPresent()) {
            return new Pair<>(false, "Device with DevEUI: " + keySpec.getDevEUI() + " already exists");
        }
        boolean validated = validateKeySpecs(new ArrayList<>(List.of(keySpec)), macVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on MAC Version have been specified");
        }

        try {
            this.keyHandler.storeRootKey(keySpec, credentialIdentifier, password, user);
        } catch (RootKeyPersistenceException e) {
            return new Pair<>(false, "Root key couldn't be persisted. An error occurred: " + e.getMessage());
        } catch (CredentialPersistenceException e) {
            return new Pair<>(false, "An error occured when persisting credential: " + e.getMessage());
        }

        persistDevice(keySpec, exposed, user, macVersion);

        return new Pair<>(true, "Root key stored successfully for device " + keySpec.getDevEUI());
    }

    // Used to initialize devices and corresponding keys with new credential
    // This can take a long time, if storing many keys why we need to initialize a job in the database if keySpecs > 100
    public Pair<Boolean, String> init(List<KeySpec> keySpecs, String password, String credentialIdentifier, User user, Boolean exposed, MACVersion macVersion) {
        boolean validated = validateKeySpecs(keySpecs, macVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on MAC Version have been specified");

        }

        RunningJob runningJob = null;
        boolean jobInitialized = false;
        if (keySpecs.size() > 100) {
            jobInitialized = true;
            runningJob = initRunningJob(keySpecs, user, JobType.REGISTER);
        }

        try {
            this.keyHandler.storeRootKeys(keySpecs, credentialIdentifier, password, user);
        } catch (RootKeyPersistenceException e) {
            return new Pair<>(false, "Root keys couldn't be persisted. An error occurred: " + e.getMessage());
        } catch (CredentialPersistenceException e) {
            return new Pair<>(false, "An error occured when persisting credential: " + e.getMessage());
        }

        persistDevices(keySpecs, exposed, user, macVersion);

        if (jobInitialized) {
            jobService.delete(runningJob);
        }

        return new Pair<>(true, "Root keys successfully stored for " + keySpecs.size() + " devices");
    }

    // Used to initialize device and corresponding key with existing credential
    public Pair<Boolean, String> init(KeySpec keySpec, KeyCredential keyCredential, User user, Boolean exposed, MACVersion macVersion) {
        // No need to derive lifetime keys, since more than 1 KeySpec is passed in case of LoRaWAN 1.1 when initializing
        boolean validated = validateKeySpecs(new ArrayList<>(List.of(keySpec)), macVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on MAC Version have been specified");
        }

        try {
            this.keyHandler.storeRootKey(keySpec, keyCredential, user);
        } catch (RootKeyPersistenceException e) {
            return new Pair<>(false, "Root key couldn't be persisted. An error occurred: " + e.getMessage());
        }

        persistDevice(keySpec, exposed, user, macVersion);

        return new Pair<>(true, "Root key stored successfully for device " + keySpec.getDevEUI());
    }

    // Used to initialize devices and corresponding keys with existing credential
    // This can take a long time, if storing many keys why we need to initialize a job in the database if keySpecs > 100
    public Pair<Boolean, String> init(List<KeySpec> keySpecs, KeyCredential keyCredential, User user, Boolean exposed, MACVersion macVersion) {
        boolean validated = validateKeySpecs(keySpecs, macVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on MAC Version have been specified");

        }

        RunningJob runningJob = null;
        boolean jobInitialized = false;
        if (keySpecs.size() > 100) {
            jobInitialized = true;
            runningJob = initRunningJob(keySpecs, user, JobType.REGISTER);
        }

        try {
            this.keyHandler.storeRootKeys(keySpecs, keyCredential, user);
        } catch (RootKeyPersistenceException e) {
            return new Pair<>(false, "Root keys couldn't be persisted. An error occurred: " + e.getMessage());
        }

        persistDevices(keySpecs, exposed, user, macVersion);

        if (jobInitialized) {
            jobService.delete(runningJob);
        }

        return new Pair<>(true, "Root keys successfully stored for " + keySpecs.size() + " devices");
    }

    private Boolean validateKeySpecs(List<KeySpec> keySpecs, MACVersion macVersion) {
        // Filter the list based on devEUI and create the Map
        Map<String, List<KeySpec>> filteredMap = keySpecs.stream()
                .collect(Collectors.groupingBy(KeySpec::getDevEUI));

        for (Map.Entry<String, List<KeySpec>> entry : filteredMap.entrySet()) {
            if (!isHexadecimal(entry.getKey(), 16)) {
                return false;
            }

            for (KeySpec keySpec : entry.getValue()) {
                if (!isHexadecimal(keySpec.getKey(), 32)) {
                    return false;
                }
            }

            if (macVersion == MACVersion.LORAWAN_1_0 ||
                    macVersion == MACVersion.LORAWAN_1_0_1 ||
                    macVersion == MACVersion.LORAWAN_1_0_2 ||
                    macVersion == MACVersion.LORAWAN_1_0_3 ||
                    macVersion == MACVersion.LORAWAN_1_0_4) {
                // Check for one root key
                if (entry.getValue().size() != 1) {
                    return false;
                }
                // Check it is of correct type
                else if (entry.getValue().get(0).getKeyType() != KeyType.AppKey1_0) {
                    return false;
                }
            }
            else if (macVersion == MACVersion.LORAWAN_1_1) {
                // Check for two root keys
                if (entry.getValue().size() != 2) {
                    return false;
                }
                // Check if both types exist
                else if ((entry.getValue().get(0).getKeyType() != KeyType.AppKey1_1
                        && entry.getValue().get(1).getKeyType() != KeyType.AppKey1_1) ||
                        (entry.getValue().get(0).getKeyType() != KeyType.NwkKey1_1
                        && entry.getValue().get(1).getKeyType() != KeyType.NwkKey1_1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isHexadecimal(String input, int expectedLength) {
        // Check if the input length matches the expected length
        if (input.length() != expectedLength) {
            return false;
        }

        // Regular expression to match hexadecimal characters
        String hexPattern = "^[0-9a-fA-F]+$";

        // Check if the input matches the hex pattern
        return input.matches(hexPattern);
    }

    private void persistDevice(KeySpec keySpec, Boolean exposed, User owner, MACVersion macVersion) {
        SessionStatus initSessionStatus = SessionStatus.sessionStatusInit();
        initSessionStatus.setDevEUI(keySpec.getDevEUI());

        Device device = new Device(
                keySpec.getDevEUI(),
                exposed,
                initSessionStatus,
                owner,
                macVersion
        );

        this.deviceService.save(device, false);
    }

    private void persistDevices(List<KeySpec> keySpecs, Boolean exposed, User owner, MACVersion macVersion) {
        List<Device> devices = new ArrayList<>();

        // distinct() because keySpecs might include duplicate DevEUIs in case of LoRaWAN 1.1 devices because of two root keys.
        for (KeySpec keySpec : keySpecs.stream().distinct().toList()) {
            SessionStatus initSessionStatus = SessionStatus.sessionStatusInit();
            initSessionStatus.setDevEUI(keySpec.getDevEUI());
            devices.add(new Device(
                    keySpec.getDevEUI(),
                    exposed,
                    initSessionStatus,
                    owner,
                    macVersion
            ));
        }

        this.deviceService.saveAll(devices, false);
    }

    // Only relevant for toMACVersion LoRaWAN 1.0.x
    public Pair<Boolean, String> update(String oldDevEUI, Device updatedDevice, KeySpec keySpec, MACVersion fromMACVersion, MACVersion toMACVersion) {
        boolean validated = validateKeySpecs(new ArrayList<>(List.of(keySpec)), toMACVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on provided MAC Version");
        }

        try {
            keyHandler.updateRootKey(oldDevEUI, keySpec, fromMACVersion, toMACVersion);
        } catch (RootKeyPersistenceException | CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException e) {
            return new Pair<>(false, "Root key couldn't be updated. An error occurred");
        }

        if (!oldDevEUI.equals(updatedDevice.getDevEUI())) {
            // Reset session context
            clearSession(updatedDevice);
        }

        deviceService.save(updatedDevice, true);
        return new Pair<>(true, "Device and root keys successfully updated for device " + keySpec.getDevEUI());
    }

    // Only relevant for toMACVersion LoRaWAN 1.1
    public Pair<Boolean, String> update(String oldDevEUI, Device updatedDevice, List<KeySpec> keySpecs, MACVersion fromMACVersion, MACVersion toMACVersion) {
        boolean validated = validateKeySpecs(keySpecs, toMACVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on provided MAC Version");
        }

        try {
            keyHandler.updateRootKeys(oldDevEUI, keySpecs, fromMACVersion, toMACVersion);
        } catch (RootKeyPersistenceException | CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException e) {
            return new Pair<>(false, "Root key couldn't be updated. An error occurred");
        }

        if (!oldDevEUI.equals(updatedDevice.getDevEUI())) {
            // Reset session context
            clearSession(updatedDevice);
        }

        deviceService.save(updatedDevice, true);
        return new Pair<>(true, "Device and root keys successfully updated for device " + updatedDevice.getDevEUI());
    }

    // Only relevant for toMACVersion LoRaWAN 1.0.x
    public Pair<Boolean, String> updateAuthorized(String oldDevEUI, Device updatedDevice, KeySpec keySpec, String password, MACVersion fromMACVersion, MACVersion toMACVersion) throws UpdateDeviceException, CredentialAuthenticationException {

        boolean validated = validateKeySpecs(new ArrayList<>(List.of(keySpec)), toMACVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on provided MAC Version");
        }

        String credentialIdentifier = "";
        Optional<DevKeyId> devKeyId = devKeyIdService.findByDevEUI(oldDevEUI);
        if (devKeyId.isEmpty()) {
            throw new UpdateDeviceException("Failed to fetch root key information for provided devEUI");
        }

        credentialIdentifier = devKeyId.get().getKeyCredential().getIdentifier();

        if (!password.equals(keyHandler.getCredential(credentialIdentifier))) {
            throw new CredentialAuthenticationException("Couldn't authorize update device permission with provided credential");
        }

        try {
            keyHandler.updateRootKey(oldDevEUI, keySpec, fromMACVersion, toMACVersion);
        } catch (RootKeyPersistenceException | CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException e) {
            return new Pair<>(false, "Root key couldn't be updated. An error occurred: " + e.getMessage());
        }

        if (!oldDevEUI.equals(updatedDevice.getDevEUI())) {
            // Reset session context
            clearSession(updatedDevice);
        }

        deviceService.save(updatedDevice, true);
        return new Pair<>(true, "Device and root keys successfully updated for device " + keySpec.getDevEUI());
    }

    // Only relevant for toMACVersion LoRaWAN 1.1
    public Pair<Boolean, String> updateAuthorized(String oldDevEUI, Device updatedDevice, List<KeySpec> keySpecs, String password, MACVersion fromMACVersion, MACVersion toMACVersion) throws UpdateDeviceException, CredentialAuthenticationException {
        boolean validated = validateKeySpecs(keySpecs, toMACVersion);
        if (!validated) {
            return new Pair<>(false, "Root key or devEUI couldn't be validated. Non-hex characters exist, incorrect length, or non-compatible key types depending on provided MAC Version");
        }

        Optional<DevKeyId> devKeyId = devKeyIdService.findByDevEUI(oldDevEUI);
        if (devKeyId.isEmpty()) {
            throw new UpdateDeviceException("Failed to fetch root key information for provided devEUI");
        }

        String credentialIdentifier = devKeyId.get().getKeyCredential().getIdentifier();

        if (!password.equals(keyHandler.getCredential(credentialIdentifier))) {
            throw new CredentialAuthenticationException("Couldn't authorize update device permission with provided credential");
        }

        try {
            keyHandler.updateRootKeys(oldDevEUI, keySpecs, fromMACVersion, toMACVersion);
        } catch (RootKeyPersistenceException | CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException e) {
            return new Pair<>(false, "Root keys couldn't be updated. An error occurred: " + e.getMessage());
        }

        if (!oldDevEUI.equals(updatedDevice.getDevEUI())) {
            // Reset session context
            clearSession(updatedDevice);
        }

        deviceService.save(updatedDevice, true);
        return new Pair<>(true, "Device and root keys successfully updated for device " + keySpecs.size() + " devices");
    }

    // This function should only be called by JoinProcessor, why we can throw a JoinReqFailedException, if anything goes wrong
    public Pair<Boolean, String> storeSessionKeys(List<KeySpec> keySpecs, Boolean isInitialSession) throws JoinReqFailedExc {
        try {
            this.keyHandler.storeSessionKeys(keySpecs, isInitialSession);
        } catch (SessionKeyPersistenceException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }

        return new Pair<>(true, "Session Keys successfully stored");
    }

    public Pair<Boolean, String> deleteAuthorized(Device device, String password) throws DeleteDeviceException, CredentialAuthenticationException {
        Optional<DevKeyId> devKeyId = devKeyIdService.findByDevEUI(device.getDevEUI());

        if (devKeyId.isEmpty()) {
            throw new DeleteDeviceException("Failed to fetch credential information for provided devEUI");
        }

        String credentialIdentifier = devKeyId.get().getKeyCredential().getIdentifier();

        if (!password.equals(keyHandler.getCredential(credentialIdentifier))) {
            throw new CredentialAuthenticationException("Couldn't authorize delete device permission with provided credential");
        }

        try {
            this.keyHandler.deleteKey(device);
            this.joinLogService.deleteAllByDev(device);
            this.appSKeyReqLogService.deleteAllByDev(device);
            this.deviceService.delete(device);
            return new Pair<>(true, "Successfully deleted device and keys for " + device.getDevEUI());
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            return new Pair<>(false, "Couldn't delete device and corresponding key. Reason: " + e.getMessage());
        }
    }

    public Pair<Boolean, String> delete(Device device) {
        try {
            this.keyHandler.deleteKey(device);
            this.joinLogService.deleteAllByDev(device);
            this.appSKeyReqLogService.deleteAllByDev(device);
            this.deviceService.delete(device);
            return new Pair<>(true, "Successfully deleted device and keys for " + device.getDevEUI());
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            return new Pair<>(false, "Couldn't delete device and corresponding key. Reason: " + e.getMessage());
        }
    }

    public Pair<Boolean, String> delete(List<Device> devices) {
        try {
            this.keyHandler.deleteKeys(devices);
            this.joinLogService.deleteByDeviceIn(devices);
            this.appSKeyReqLogService.deleteByDeviceIn(devices);
            this.deviceService.deleteAll(devices);
            return new Pair<>(true, "Successfully deleted " + devices.size() + " devices");
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            return new Pair<>(false, "Couldn't delete device and corresponding key. Reason: " + e.getMessage());
        }
    }

    public List<KeySpec> getRootKeysDTOAuthorized(String devEUI, String password) throws RootKeyRetrievalException, CredentialAuthenticationException {
        String credentialIdentifier = "";
        Optional<DevKeyId> devKeyId = devKeyIdService.findByDevEUI(devEUI);
        if (devKeyId.isEmpty()) {
            throw new RootKeyRetrievalException("Failed to fetch root key information for provided devEUI");
        }

        credentialIdentifier = devKeyId.get().getKeyCredential().getIdentifier();

        if (!password.equals(keyHandler.getCredential(credentialIdentifier))) {
            throw new CredentialAuthenticationException("Couldn't authorize root key retrieval with provided credential");
        }

        List<KeySpec> rootKeys = new ArrayList<>();

        for (KsKeySpec ksKeySpec : devKeyId.get().getKsKeySpecs()) {
            switch (ksKeySpec.getKeyType()) {
                case AppKey1_0 ->
                        rootKeys.add(new KeySpec(Hex.encodeHexString(getAppKey1_0(devEUI).getEncoded()), KeyType.AppKey1_0));
                case AppKey1_1 ->
                        rootKeys.add(new KeySpec(Hex.encodeHexString(getAppKey1_1(devEUI).getEncoded()), KeyType.AppKey1_1));
                case NwkKey1_1 ->
                        rootKeys.add(new KeySpec(Hex.encodeHexString(getNwkKey1_1(devEUI).getEncoded()), KeyType.NwkKey1_1));
            }
        }

        return rootKeys;
    }

    public KeyCredential validateCredential(String credentialID, String password, User owner) throws CredentialAuthenticationException {
        Optional<KeyCredential> keyCredential = keyHandler.getKeyCredential(credentialID, owner);
        if (keyCredential.isPresent()) {
            if (password.equals(keyHandler.getCredential(credentialID))) {
                return keyCredential.get();
            }
            else {
                throw new CredentialAuthenticationException("Credential exists and provided password is not correct");
            }
        }
        else {
            return null;
        }
    }

    private void clearSession(Device device) {
        device.setSessionStatus(SessionStatus.sessionStatusInit());
        joinLogService.deleteAllByDev(device);
        appSKeyReqLogService.deleteAllByDev(device);
    }


    public Key getAppKey1_0(String devEUI) {
        try {
            return keyHandler.retrieveAppKey1_0(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Key getAppKey1_1(String devEUI) {
        try {
            return keyHandler.retrieveAppKey1_1(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Key getNwkKey1_1(String devEUI) {
        try {
            return keyHandler.retrieveNwkKey1_1(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Key getAppSKey(String devEUI) {
        try {
            return keyHandler.retrieveAppSKey(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Key getNwkSKey(String devEUI) {
        try {
            return keyHandler.retrieveNwkSKey(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Key getFNwkSIntKey(String devEUI) {
        try {
            return keyHandler.retrieveFNwkSIntKey(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Key getSNwkSIntKey(String devEUI) {
        try {
            return keyHandler.retrieveSNwkSIntKey(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Key getNwkSEncKey(String devEUI) {
        try {
            return keyHandler.retrieveNwkSEncKey(devEUI);
        } catch (RootKeyRetrievalException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCredential(String identifier) {
        return keyHandler.getCredential(identifier);
    }

    private RunningJob initRunningJob(List<KeySpec> keySpecs, User user, JobType jobType) {
        RunningJob runningJob = new RunningJob(
                JobType.REGISTER,
                LocalDateTime.now(),
                user,
                (int) keySpecs.stream()
                        .map(KeySpec::getDevEUI)
                        .distinct()
                        .count()
        );
        jobService.save(runningJob);

        return runningJob;
    }

    public void rotateKek() {
        keyHandler.setRandomKek();
    }

    public void setKek(String kek) {
        keyHandler.setKek(kek);
    }

    public Key getKek() {
        return keyHandler.getKek();
    }
}
