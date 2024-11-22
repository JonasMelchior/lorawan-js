package com.github.jonasmelchior.js.service.device.keys;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.keys.*;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.service.device.IDevKeyIdService;
import com.github.jonasmelchior.js.service.device.IKeyCredentialService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.SymmetricSecretKey;
import org.bouncycastle.crypto.fips.FipsAES;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.fips.FipsSymmetricKeyGenerator;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;


public class KeyHandler {
    private final IKeyCredentialService keyCredentialService;
    private final IDevKeyIdService devIdService;
    KeyStore keyStore;
    private static final String keystoreFilePathCredentials = System.getProperty("keystore_filepath_credentials");
    private static final String keystoreFilePathRKeys = System.getProperty("keystore_filepath_rkeys");
    private static final String keystoreFilePathSKeys = System.getProperty("keystore_filepath_skeys");

    public KeyHandler(IKeyCredentialService KeyCredentialService, IDevKeyIdService devIdService) {
        this.keyCredentialService = KeyCredentialService;
        this.devIdService = devIdService;
        CryptoServicesRegistrar.setSecureRandom(
                FipsDRBG.SHA512_HMAC.fromEntropySource(
                                new BasicEntropySourceProvider(new SecureRandom(), true))
                        .build(null, true));
        try {
            this.keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
        } catch (KeyStoreException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    // Used to store root key with new credential
    public void storeRootKey(KeySpec keySpecs, String credentialIdentifier, String password, User user) throws RootKeyPersistenceException, CredentialPersistenceException {
        initCredential(new ArrayList<>(List.of(keySpecs)), credentialIdentifier, password, user);
        storeRootKey(new ArrayList<>(List.of(keySpecs)), password);
    }

    // Used to store root keys with new credential
    public void storeRootKeys(List<KeySpec> keySpecs, String credentialIdentifier, String password, User user) throws RootKeyPersistenceException, CredentialPersistenceException {
        initCredential(keySpecs, credentialIdentifier, password, user);
        storeRootKey(keySpecs, password);
    }

    // Used to store root key with existing credential
    public void storeRootKey(KeySpec keySpecs, KeyCredential keyCredential, User user) throws RootKeyPersistenceException {
        updateCredential(new ArrayList<>(List.of(keySpecs)), keyCredential);
        storeRootKey(new ArrayList<>(List.of(keySpecs)), getCredential(keyCredential.getIdentifier()));
    }

    // Used to store root keys with existing credential
    public void storeRootKeys(List<KeySpec> keySpecs, KeyCredential keyCredential, User user) throws RootKeyPersistenceException {
        updateCredential(keySpecs, keyCredential);
        storeRootKey(keySpecs, getCredential(keyCredential.getIdentifier()));
    }

    public Optional<KeyCredential> getKeyCredential(String identifier, User owner) {
        return keyCredentialService.findByIdentifierAndByOwner(identifier, owner);
    }

    public void updateSessionKeys(String oldDevEUI, String newDevEUI) throws RootKeyPersistenceException {
        Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(oldDevEUI);
        if (devKeyId.isEmpty()) {
            throw new RootKeyPersistenceException("Couldn't retrieve key information for DevEUI " + oldDevEUI);
        }
    }

    // Only applies when storing AppKey for LoRaWAN 1.0 devices
    public void updateRootKey(String oldDevEUI, KeySpec keySpec, MACVersion fromMACVersion, MACVersion toMACVersion) throws RootKeyPersistenceException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(oldDevEUI);
        if (devKeyId.isEmpty()) {
            throw new RootKeyPersistenceException("Couldn't retrieve key information for DevEUI " + oldDevEUI);
        }

        boolean deleteSessionKeys = !oldDevEUI.equals(keySpec.getIdentifier());
        deleteKSKey(devKeyId.get(), deleteSessionKeys);

        if (fromMACVersion.equals(MACVersion.LORAWAN_1_1) &&
                (toMACVersion == MACVersion.LORAWAN_1_0 || toMACVersion == MACVersion.LORAWAN_1_0_1 ||
                        toMACVersion == MACVersion.LORAWAN_1_0_2 || toMACVersion == MACVersion.LORAWAN_1_0_3 ||
                        toMACVersion == MACVersion.LORAWAN_1_0_4)) {
            devKeyId.get().getKsKeySpecs()
                    .removeIf( ksKeySpec ->
                            ksKeySpec.getKeyType().equals(KeyType.AppKey1_1) ||
                                    ksKeySpec.getKeyType().equals(KeyType.NwkKey1_1)
                    );
            devKeyId.get().getKsKeySpecs().add(new KsKeySpec(
                    KeyType.AppKey1_0,
                    "appkey1_0;" + keySpec.getIdentifier()
            ));
        }

        // Update devKeyId with potential new DevEUI along with KsKeySpecs in case DevEUI is different.
        // Note that the MAC version might not always change
        updateDevKeyIdAndKsKeySpecs(devKeyId.get(), keySpec.getIdentifier());

        Optional<KeyCredential> keyCredential = devIdService.findCredentialByDevEUI(keySpec.getIdentifier());
        if (keyCredential.isPresent()) {
            storeRootKey(new ArrayList<>(List.of(keySpec)), getCredential(keyCredential.get().getIdentifier()));
        }
    }

    // Only applies when storing AppKey and NwkKey for LoRaWAN 1.1 devices
    public void updateRootKeys(String oldDevEUI, List<KeySpec> keySpecs, MACVersion fromMACVersion, MACVersion toMACVersion) throws RootKeyPersistenceException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(oldDevEUI);
        if (devKeyId.isEmpty()) {
            throw new RootKeyPersistenceException("Couldn't retrieve key information for DevEUI " + oldDevEUI);
        }

        boolean deleteSessionKeys = !oldDevEUI.equals(keySpecs.get(0).getIdentifier());
        deleteKSKey(devKeyId.get(), deleteSessionKeys);

        if (fromMACVersion.equals(MACVersion.LORAWAN_1_0) && toMACVersion.equals(MACVersion.LORAWAN_1_1)) {
            devKeyId.get().getKsKeySpecs()
                    .removeIf( ksKeySpec ->
                            ksKeySpec.getKeyType().equals(KeyType.AppKey1_0)
                    );
            List<KsKeySpec> ksKeySpecs = new ArrayList<>();
            for (KeySpec keySpec : keySpecs) {
                String alias = "";
                switch (keySpec.getKeyType()) {
                    case AppKey1_1 -> alias = "appkey1_1;" + keySpec.getIdentifier();
                    case NwkKey1_1 -> alias = "nwkkey1_1;" + keySpec.getIdentifier();
                }
                ksKeySpecs.add(new KsKeySpec(keySpec.getKeyType(), alias));
            }
            devKeyId.get().getKsKeySpecs().addAll(ksKeySpecs);
        }

        // Update devKeyId with potential new DevEUI along with KsKeySpecs in case DevEUI is different.
        // Note that the MAC version might not always change
        updateDevKeyIdAndKsKeySpecs(devKeyId.get(), keySpecs.get(0).getIdentifier());

        Optional<KeyCredential> keyCredential = devIdService.findCredentialByDevEUI(keySpecs.get(0).getIdentifier());
        if (keyCredential.isPresent()) {
            storeRootKey(keySpecs, getCredential(keyCredential.get().getIdentifier()));
        }
    }

    private void updateDevKeyIdAndKsKeySpecs(DevKeyId devKeyId, String devEUI) {
        devKeyId.setDevEUI(devEUI);
        for (KsKeySpec ksKeySpec : devKeyId.getKsKeySpecs()) {
            String[] alias = ksKeySpec.getAlias().split(";");
            ksKeySpec.setAlias(alias[0] + ";" +devEUI);
        }
        devIdService.save(devKeyId);
    }

    public void storeSessionKeys(List<KeySpec> keySpecs, Boolean isInitialSession) throws SessionKeyPersistenceException {
        Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(keySpecs.get(0).getIdentifier());
        if (devKeyId.isEmpty()) {
            return;
        }

        if (isInitialSession) {
            for (KeySpec keySpec : keySpecs) {
                String alias = "";
                switch (keySpec.getKeyType()) {
                    case AppSKey -> alias = "appskey;" + keySpec.getIdentifier();
                    case NwkSKey -> alias = "nwkskey;" + keySpec.getIdentifier();
                    case FNwkSIntKey -> alias = "fnwksintkey;" + keySpec.getIdentifier();
                    case SNwkSIntKey -> alias = "snwksintkey;" + keySpec.getIdentifier();
                    case NwkSEncKey -> alias = "nwksenckey;" + keySpec.getIdentifier();
                }
                devKeyId.get().getKsKeySpecs().add(new KsKeySpec(keySpec.getKeyType(), alias));
            }
            devIdService.save(devKeyId.get());
        }

        storeSessionKeys(keySpecs, getCredential(devKeyId.get().getKeyCredential().getIdentifier()));
    }

    public void deleteKey(Device device) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(device.getDevEUI());
        if (devKeyId.isPresent()) {
            deleteKSKey(devKeyId.get(), true);
            devIdService.delete(devKeyId.get());
        }
    }

    public void deleteKeys(List<Device> devices) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        List<DevKeyId> devKeyIds = devIdService.findAllByDevEUI(devices.stream().map(Device::getDevEUI).toList());
        deleteKSKeys(devKeyIds);
        devIdService.deleteAll(devKeyIds);
    }

    private void deleteKSKey(DevKeyId devKeyId, boolean deleteSessionKeys) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_rkeys"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());

            for (KsKeySpec ksKeySpec : devKeyId.getKsKeySpecs()) {
                if ((ksKeySpec.getKeyType() == KeyType.AppSKey ||
                        ksKeySpec.getKeyType() == KeyType.NwkSKey ||
                        ksKeySpec.getKeyType() == KeyType.NwkSEncKey ||
                        ksKeySpec.getKeyType() == KeyType.FNwkSIntKey ||
                        ksKeySpec.getKeyType() == KeyType.SNwkSIntKey) &&
                        !deleteSessionKeys) {
                    continue;
                }
                keyStore.deleteEntry(ksKeySpec.getAlias());
            }

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_rkeys"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
    }

    private void deleteKSKeys(List<DevKeyId> devKeyIds) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_rkeys"));
        keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());

        for (DevKeyId devKeyId : devKeyIds) {
            for (KsKeySpec ksKeySpec : devKeyId.getKsKeySpecs()) {
                keyStore.deleteEntry(ksKeySpec.getAlias());
            }
        }

        OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_rkeys"));
        keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
        outputStream.close();
    }

    public Key retrieveAppKey1_0(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.AppKey1_0, false);
    }

    public Key retrieveAppKey1_1(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.AppKey1_1, false);
    }


    public Key retrieveNwkKey1_1(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.NwkKey1_1, false);
    }

    public Key retrieveAppSKey(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.AppSKey, true);
    }

    public Key retrieveNwkSKey(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.NwkSKey, true);
    }

    public Key retrieveNwkSEncKey(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.NwkSEncKey, true);
    }

    public Key retrieveFNwkSIntKey(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.FNwkSIntKey, true);
    }

    public Key retrieveSNwkSIntKey(String devEUI) throws RootKeyRetrievalException {
        return retrieveKey(devEUI, KeyType.SNwkSIntKey, true);
    }

    private Key retrieveKey(String devEUI, KeyType keyType, Boolean isSession) throws RootKeyRetrievalException {
        try {
            Optional<DevKeyId> devKeyId = devIdService.findByDevEUI(devEUI);
            if (devKeyId.isEmpty()) {
                return null;
            }
            else {
                String password = getCredential(devKeyId.get().getKeyCredential().getIdentifier());

                InputStream inputStream;
                if (isSession) {
                    inputStream = new FileInputStream(System.getProperty("keystore_filepath_skeys"));
                }
                else {
                    inputStream = new FileInputStream(System.getProperty("keystore_filepath_rkeys"));
                }

                keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
                return keyStore.getKey(devKeyId.get().getKsKeySpecs().stream().
                        filter(ksKeySpec -> ksKeySpec.getKeyType().equals(keyType)).
                        findFirst().get().getAlias(), password.toCharArray());
            }
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | CertificateException |
                 IOException e) {
            throw new RootKeyRetrievalException(e.getMessage());
        }
    }

    private void updateCredential(List<KeySpec> keySpecs, KeyCredential keyCredential) {
        Set<DevKeyId> devKeyIds = new HashSet<>();

        Map<String, List<KsKeySpec>> devKeyKsKeySpecs = new HashMap<>();

        for (KeySpec keySpec : keySpecs) {
            KsKeySpec ksKeySpec = new KsKeySpec(keySpec.getKeyType());
            switch (keySpec.getKeyType()) {
                case AppKey1_0 -> ksKeySpec.setAlias("appkey1_0;" + keySpec.getIdentifier());
                case AppKey1_1 -> ksKeySpec.setAlias("appkey1_1;" + keySpec.getIdentifier());
                case NwkKey1_1 -> ksKeySpec.setAlias("nwkkey1_1;"  + keySpec.getIdentifier());
                case AppSKey -> ksKeySpec.setAlias("appskey;" + keySpec.getIdentifier());
                case NwkSKey -> ksKeySpec.setAlias("nwkskey;" + keySpec.getIdentifier());
                case FNwkSIntKey -> ksKeySpec.setAlias("fnwksintkey;"  + keySpec.getIdentifier());
                case SNwkSIntKey -> ksKeySpec.setAlias("snwksintkey;" + keySpec.getIdentifier());
                case NwkSEncKey -> ksKeySpec.setAlias("nwksenckey;" + keySpec.getIdentifier());
            }
            if (!devKeyKsKeySpecs.containsKey(keySpec.getIdentifier())) {
                devKeyKsKeySpecs.put(keySpec.getIdentifier(), new ArrayList<>(List.of(ksKeySpec)));
            }
            else {
                devKeyKsKeySpecs.get(keySpec.getIdentifier()).add(ksKeySpec);
            }
        }

        for (Map.Entry<String, List<KsKeySpec>> entry : devKeyKsKeySpecs.entrySet()) {
            devKeyIds.add(new DevKeyId(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        devKeyIds.forEach( devKeyId ->  devKeyId.setKeyCredential(keyCredential) );
        devIdService.saveAll(devKeyIds);

        keyCredentialService.save(keyCredential);
    }

    private void initCredential(List<KeySpec> keySpecs, String credentialIdentifier, String password, User user) throws RootKeyPersistenceException, CredentialPersistenceException {
        FipsSymmetricKeyGenerator<SymmetricSecretKey> keyGen =
                new FipsAES.KeyGenerator(256, CryptoServicesRegistrar.getSecureRandom());
        SymmetricSecretKey key = keyGen.generateKey();
        SecretKeySpec aesKey = new SecretKeySpec(key.getKeyBytes(), "AES");

        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_credentials"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            keyStore.setKeyEntry(credentialIdentifier, aesKey, System.getProperty("master_pwd").toCharArray(), null);

            inputStream.close();

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_credentials"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();

            Set<DevKeyId> devKeyIds = new HashSet<>();
            Map<String, List<KsKeySpec>> devKeyKsKeySpecs = new HashMap<>();

            for (KeySpec keySpec : keySpecs) {
                KsKeySpec ksKeySpec = new KsKeySpec(keySpec.getKeyType());
                switch (keySpec.getKeyType()) {
                    case AppKey1_0 -> ksKeySpec.setAlias("appkey1_0;" + keySpec.getIdentifier());
                    case AppKey1_1 -> ksKeySpec.setAlias("appkey1_1;" + keySpec.getIdentifier());
                    case NwkKey1_1 -> ksKeySpec.setAlias("nwkkey1_1;"  + keySpec.getIdentifier());
                }
                if (!devKeyKsKeySpecs.containsKey(keySpec.getIdentifier())) {
                    devKeyKsKeySpecs.put(keySpec.getIdentifier(), new ArrayList<>(List.of(ksKeySpec)));
                }
                else {
                    devKeyKsKeySpecs.get(keySpec.getIdentifier()).add(ksKeySpec);
                }
            }

            for (Map.Entry<String, List<KsKeySpec>> entry : devKeyKsKeySpecs.entrySet()) {
                devKeyIds.add(new DevKeyId(
                        entry.getKey(),
                        entry.getValue()
                ));
            }

            byte[] ivBytes = new byte[16];
            CryptoServicesRegistrar.getSecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            KeyCredential keyCredential = new KeyCredential(
                    credentialIdentifier,
                    Hex.encodeHexString(generateEncCredential(aesKey, password, iv)),
                    Hex.encodeHexString(iv.getIV()),
                    user
            );

            keyCredentialService.save(keyCredential);

            devKeyIds.forEach( devKeyId ->  devKeyId.setKeyCredential(keyCredential) );
            devIdService.saveAll(devKeyIds);

        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new CredentialPersistenceException(e.getMessage());
        }
    }

    private void storeSessionKeys(List<KeySpec> keySpecs, String password) throws SessionKeyPersistenceException {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_skeys"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            for (KeySpec keySpec : keySpecs) {
                SecretKeySpec key = new SecretKeySpec(Hex.decodeHex(keySpec.getKey()), "AES");
                String alias = "";
                switch (keySpec.getKeyType()) {
                    case AppSKey -> alias = "appskey;" + keySpec.getIdentifier();
                    case NwkSKey -> alias = "nwkskey;" + keySpec.getIdentifier();
                    case FNwkSIntKey -> alias = "fnwksintkey;"  + keySpec.getIdentifier();
                    case SNwkSIntKey -> alias = "snwksintkey;" + keySpec.getIdentifier();
                    case NwkSEncKey -> alias = "nwksenckey;" + keySpec.getIdentifier();
                }
                keyStore.setKeyEntry(alias, key, password.toCharArray(), null);
            }

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_skeys"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());

            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | DecoderException e) {
            throw new SessionKeyPersistenceException(e.getMessage());
        }
    }

    private void storeRootKey(List<KeySpec> keySpecs, String password) throws RootKeyPersistenceException {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_rkeys"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            for (KeySpec keySpec : keySpecs) {
                SecretKeySpec key = new SecretKeySpec(Hex.decodeHex(keySpec.getKey()), "AES");
                String alias = "";
                switch (keySpec.getKeyType()) {
                    case AppKey1_0 -> alias = "appkey1_0;" + keySpec.getIdentifier();
                    case AppKey1_1 -> alias = "appkey1_1;" + keySpec.getIdentifier();
                    case NwkKey1_1 -> alias = "nwkkey1_1;"  + keySpec.getIdentifier();
                }
                keyStore.setKeyEntry(alias, key, password.toCharArray(), null);

            }
            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_rkeys"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());

            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | DecoderException e) {
            throw new RootKeyPersistenceException(e.getMessage());
        }
    }


    public String getCredential(String identifier) {
        try {
            Optional<KeyCredential> optionalKeyCredential = keyCredentialService.findById(identifier);

            if (optionalKeyCredential.isPresent()) {
                InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_credentials"));
                keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());

                Key key = keyStore.getKey(identifier, System.getProperty("master_pwd").toCharArray());
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Hex.decodeHex(optionalKeyCredential.get().getIv())));
                return new String(cipher.doFinal(Hex.decodeHex(optionalKeyCredential.get().getCredential())));
            }

            return null;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 KeyStoreException | NoSuchPaddingException | InvalidKeyException | DecoderException |
                 IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateEncCredential(SecretKeySpec key, String pwd, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            return cipher.doFinal(pwd.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public void storeKek(String kek, String alias) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            SecretKeySpec aesKey = new SecretKeySpec(Hex.decodeHex(kek), "AES");
            keyStore.setKeyEntry(alias, aesKey, System.getProperty("master_pwd").toCharArray(), null);

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | DecoderException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void storeKeks(String kek, List<String> aliases) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            for (String alias : aliases) {
                SecretKeySpec aesKey = new SecretKeySpec(Hex.decodeHex(kek), "AES");
                keyStore.setKeyEntry(alias, aesKey, System.getProperty("master_pwd").toCharArray(), null);
            }

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | DecoderException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateKek(String kek, String oldAlias, String alias) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            if (keyStore.isKeyEntry(oldAlias)) {
                keyStore.deleteEntry(oldAlias);
            }
            SecretKeySpec aesKey = new SecretKeySpec(Hex.decodeHex(kek), "AES");
            keyStore.setKeyEntry(alias, aesKey, System.getProperty("master_pwd").toCharArray(), null);

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | DecoderException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteKek(String alias) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            if (keyStore.isKeyEntry(alias)) {
                keyStore.deleteEntry(alias);
            }

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteKeks(List<String> aliases) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            for (String alias : aliases) {
                if (keyStore.isKeyEntry(alias)) {
                    keyStore.deleteEntry(alias);
                }
            }

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRandomKek() {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            FipsSymmetricKeyGenerator<SymmetricSecretKey> keyGen =
                    new FipsAES.KeyGenerator(256, CryptoServicesRegistrar.getSecureRandom());
            SymmetricSecretKey key = keyGen.generateKey();
            SecretKeySpec aesKey = new SecretKeySpec(key.getKeyBytes(), "AES");

            keyStore.setKeyEntry("kek", aesKey, System.getProperty("master_pwd").toCharArray(), null);

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException |
                 KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void setKek(String kek) {
        try {
            InputStream inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            SecretKeySpec aesKey = new SecretKeySpec(Hex.decodeHex(kek), "AES");
            keyStore.setKeyEntry("kek", aesKey, System.getProperty("master_pwd").toCharArray(), null);

            OutputStream outputStream = new FileOutputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.store(outputStream, System.getProperty("master_pwd").toCharArray());
            outputStream.close();
        } catch (CertificateException | IOException | NoSuchAlgorithmException | DecoderException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public Key getKek(String alias) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(System.getProperty("keystore_filepath_kek"));
            keyStore.load(inputStream, System.getProperty("master_pwd").toCharArray());
            inputStream.close();

            return keyStore.getKey(alias, System.getProperty("master_pwd").toCharArray());
        } catch (CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 KeyStoreException e) {
            e.printStackTrace();
            return null;
        }

    }
}
