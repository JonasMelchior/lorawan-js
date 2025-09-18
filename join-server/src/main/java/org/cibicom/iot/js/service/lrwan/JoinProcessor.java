package org.cibicom.iot.js.service.lrwan;

import com.google.gson.*;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.cibicom.iot.js.controller.JSRestController;
import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.data.device.JoinLog;
import org.cibicom.iot.js.data.device.SessionState;
import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.KeyType;
import org.cibicom.iot.js.data.lrwan.LifeTimeKey;
import org.cibicom.iot.js.data.lrwan.NetworkSessionKeyType;
import org.cibicom.iot.js.data.lrwan.SessionKey;
import org.cibicom.iot.js.data.lrwan.backendif.*;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import org.cibicom.iot.js.service.device.keys.KeyHandler;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class JoinProcessor {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    // Exclude 'lifetime' field from serialization
                    return f.getName().equals("lifeTime");
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    private DeviceService deviceService;
    private DeviceKeyHandler deviceKeyHandler;
    private JoinLogService joinLogService;
    Logger logger = LoggerFactory.getLogger(JoinProcessor.class);

    public JoinProcessor(DeviceService deviceService,
                         KeyCredentialService keyCredentialService,
                         DevKeyIdService devKeyIdService,
                         JoinLogService joinLogService
    ){
        this.deviceService = deviceService;
        this.deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devKeyIdService
                ),
                joinLogService
        );
        this.joinLogService = joinLogService;
    }

    public JoinAns processJoinReq(String joinReqJSon) {
        JoinReq joinReq;

        try {
            joinReq = gson.fromJson(joinReqJSon, JoinReq.class);
        } catch (JsonSyntaxException e) {
            JoinAns joinAnsParseErr = getMandatoryJoinAns(new JoinReq());
            joinAnsParseErr.setResult(new Result(
                    "MalformedMessage",
                    Result.jsonParsingFailedErr + ": " + e.getMessage()
            ));
            return joinAnsParseErr;
        }

        JoinAns joinAnsErr = getMandatoryJoinAns(joinReq);

        Pair<String, String> validationErrorResult = Validator.validateJoinReq(joinReq);
        if (validationErrorResult != null) {
            joinAnsErr.setResult(new Result(
                    validationErrorResult.a,
                    validationErrorResult.b
            ));
            return joinAnsErr;
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI(joinReq.getDevEUI());
        if (optionalDevice.isEmpty()) {
            joinAnsErr.setResult(new Result(
                    "UnknownDevEUI", "DevEUI " + joinReq.getDevEUI() + " is not registered on JS"
            ));
            return joinAnsErr;
        }

        // We can only consider to store a JoinLog after the previous checks, since this ensures
        // a device is populated with a valid devEUI and the JoinReq JSON is OK
        Device device = optionalDevice.get();

        JoinLog joinLog = new JoinLog(
                device,
                joinReqJSon,
                LocalDateTime.now()
        );

        // Retrieve keys from key stores. Important to do only once, since costly computationally
        Key appKey = null;
        Key nwkKey = null;
        if (joinReq.getMACVersion().contains("1.0")) {
            appKey = deviceKeyHandler.getAppKey1_0(joinReq.getDevEUI());
        }
        else if (joinReq.getMACVersion().contains("1.1")) {
            appKey = deviceKeyHandler.getAppKey1_1(joinReq.getDevEUI());
            nwkKey = deviceKeyHandler.getNwkKey1_1(joinReq.getDevEUI());
        }

        try {
            Key key = null;
            if (joinReq.getMACVersion().contains("1.0")) {
                key = appKey;
            }
            else if (joinReq.getMACVersion().equals("1.1")) {
                key = nwkKey;
            }
            if (!joinReq.checkMic(key)) {
                joinAnsErr.setResult(new Result(
                        "MICFailed",
                        "MIC could not be verified\n"
                ));
                postProcess(joinLog, joinAnsErr, false);
                return joinAnsErr;
            }
        } catch (JoinReqFailedExc e) {
            joinAnsErr.setResult(new Result(
                    "JoinReqFailed",
                    "Failed to process JoinReq - couldn't calculate MIC of Join-request\n" + e.getMessage()
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        if (optionalDevice.get().getSessionStatus().getUsedDevNonces().contains(joinReq.getDevNonce())) {
            joinAnsErr.setResult(new Result(
                    "FrameReplayed", "DevNone " + joinReq.getDevNonce() + " for devEUI " + joinReq.getDevEUI()  + " has already been used in a previous JoinReq"
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        int joinNonceDecimal = Integer.parseInt(device.getSessionStatus().getLastJoinNonce(), 16);
        joinNonceDecimal++;
        String newJoinNonce = String.format("%06X", joinNonceDecimal).toUpperCase(); // Pad with leading zeros if necessary

        int sessionKeyIdDecimal = Integer.parseInt(device.getSessionStatus().getSessionKeyId(), 16);
        sessionKeyIdDecimal++;
        String newSessionKeyId = String.format("%016X", sessionKeyIdDecimal).toUpperCase();

        Integer newSessionNum = device.getSessionStatus().getSessionNum() + 1;

        byte[] appSKey = new byte[0]; // LoRaWAN 1.0.x and 1.1
        byte[] nwkSKey = new byte[0]; // LoRaWAN 1.0.x
        byte[] fNwkSIntKey = new byte[0]; // LoRaWAN 1.1
        byte[] sNwkSIntKey = new byte[0]; // LoRaWAN 1.1
        byte[] nwkSEncKey = new byte[0]; // LoRaWAN 1.1

        // Data types used in PHYPayload - little endian encoded.
        // Note, that MHDR, DLSettign and rxDelay are both 1 byte, so no need to convert
        // Fields are left as little endian during encryption and MIC check
        String newJoinNonceLE;
        String netIDLE;
        String devAddrLE;
        String cFListLE = null;
        try {
            newJoinNonceLE = Hex.encodeHexString(convertToLittleEndian(Hex.decodeHex(newJoinNonce)));
            netIDLE = Hex.encodeHexString(convertToLittleEndian(Hex.decodeHex(joinReq.getSenderID())));
            devAddrLE = Hex.encodeHexString(convertToLittleEndian(Hex.decodeHex(joinReq.getDevAddr())));
            if (joinReq.getcFList() != null) {
                cFListLE = Hex.encodeHexString(convertToLittleEndian(Hex.decodeHex(joinReq.getcFList())));
            }
        } catch (DecoderException e) {
            joinAnsErr.setResult(new Result(
                    "JoinReqFailed", "Failed to process JoinReq\n" + e.getMessage()
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        try {
            if (joinReq.getMACVersion().contains("1.0")) {
                if (appKey != null) {
                    String appKeyS = Hex.encodeHexString(appKey.getEncoded());
                    appSKey = SessionKey.generateAppSKey1_0(appKeyS, newJoinNonceLE, netIDLE, joinReq.getDevNonce());
                    nwkSKey = SessionKey.generateNwkSKey1_0(appKeyS, newJoinNonceLE, netIDLE, joinReq.getDevNonce());
                }
                else {
                    joinAnsErr.setResult(new Result(
                            "UnknownDevEUI",
                            "No root keys associated (or missing) with DevEUI " + joinReq.getDevEUI()
                    ));
                    postProcess(joinLog, joinAnsErr, false);
                    return joinAnsErr;
                }
            }
            else if (joinReq.getMACVersion().equals("1.1")){
                if (appKey != null && nwkKey != null) {
                    appSKey = SessionKey.generateAppSKey1_1(Hex.encodeHexString(appKey.getEncoded()), newJoinNonceLE, joinReq.getJoinEUI(), joinReq.getDevNonce());
                    fNwkSIntKey = SessionKey.generateNwkSKey1_1(NetworkSessionKeyType.FORWARDING, Hex.encodeHexString(nwkKey.getEncoded()), newJoinNonceLE, joinReq.getJoinEUI(), joinReq.getDevNonce());
                    sNwkSIntKey = SessionKey.generateNwkSKey1_1(NetworkSessionKeyType.SERVING, Hex.encodeHexString(nwkKey.getEncoded()), newJoinNonceLE, joinReq.getJoinEUI(), joinReq.getDevNonce());
                    nwkSEncKey = SessionKey.generateNwkSKey1_1(NetworkSessionKeyType.NETWORK, Hex.encodeHexString(nwkKey.getEncoded()), newJoinNonceLE, joinReq.getJoinEUI(), joinReq.getDevNonce());
                }
                else {
                    joinAnsErr.setResult(new Result(
                            "UnknownDevEUI",
                            "No root keys associated (or missing) with DevEUI " + joinReq.getDevEUI()
                    ));
                    postProcess(joinLog, joinAnsErr, false);
                    return joinAnsErr;
                }
            }
        } catch (JoinReqFailedExc e) {
            joinAnsErr.setResult(new Result(
                    "JoinReqFailed",
                    "Failed to process JoinReq - couldn't generate appSKey and nwkSKey\n" + e.getMessage()
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        String rxDelayHex  = String.valueOf(Hex.encodeHex(new byte[]{joinReq.getRxDelay().byteValue()}));

        byte joinReqMHDRByte = 0;
        try {
            joinReqMHDRByte = Hex.decodeHex(joinReq.getPHYPayload())[0];
        } catch (DecoderException e) {
            joinAnsErr.setResult(new Result(
                    "JoinReqFailed", "Failed to process JoinReq\n" + e.getMessage()
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        // MHDR format: 7..5 (MType), 4..2 (RFU), 1..0 (Major)
        // 0x1F: 00011111
        // 0x20: 00100000
        // Clear 3 MSB and change to 001 while maintaining 5 LSB
        byte joinAccMHDRByte = (byte) ((joinReqMHDRByte & 0x1F) | 0x20);
        String joinAccMHDR = Hex.encodeHexString(new byte[]{joinAccMHDRByte});
        String phyPayload = joinAccMHDR + newJoinNonceLE + netIDLE + devAddrLE + joinReq.getdLSettings() + rxDelayHex;

        // CFList is optional. Append if present
        if (joinReq.getcFList() != null) {
            phyPayload += cFListLE;
        }

        JoinAns joinAns = new JoinAns(
                joinReq.getProtocolVersion(),
                //TODO - change this to the actual ID of this join server (JoinEUI)
                joinReq.getReceiverID(),
                joinReq.getSenderID(),
                joinReq.getTransactionID(),
                phyPayload.toUpperCase(),
                new Result("Success", "JoinReq processed successfully and session keys have been derived"),
                0,
                newSessionKeyId
        );
        if (joinReq.getProtocolVersion().equals("1.1")) {
            joinAns.setReceiverNSID(joinReq.getSenderNSID());
        }
        try {
            //Note: encryptPHYPayload functions can throw JoinReqFailed
            if (joinReq.getMACVersion().contains("1.0")) {
                assert appKey != null;
                joinAns.encryptPHYPayload1_0(
                        Hex.encodeHexString(appKey.getEncoded()),
                        joinAccMHDR
                );

                if (device.getKekEnabled()) {
                    Key kek = deviceKeyHandler.getKek(joinReq.getDevEUI());
                    String kekLabel = device.getKekLabel();

                    if (device.getForwardAppSKeyToNS()) {
                        joinAns.setAppSKey(new KeyEnvelope(new String(Hex.encodeHex(appSKey)).toUpperCase(), kekLabel, kek));
                    }
                    joinAns.setNwkSKey(new KeyEnvelope(new String(Hex.encodeHex(nwkSKey)).toUpperCase(), kekLabel, kek));
                }
                else {
                    if (device.getForwardAppSKeyToNS()) {
                        joinAns.setAppSKey(new KeyEnvelope(new String(Hex.encodeHex(appSKey)).toUpperCase()));
                    }
                    joinAns.setNwkSKey(new KeyEnvelope(new String(Hex.encodeHex(nwkSKey)).toUpperCase()));
                }

                List<KeySpec> keySpecs = new ArrayList<>(List.of(
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(appSKey), KeyType.AppSKey),
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(nwkSKey), KeyType.NwkSKey)
                ));

                Boolean isInitialSession = device.getSessionStatus().getState().equals(SessionState.INIT);
                deviceKeyHandler.storeSessionKeys(keySpecs, isInitialSession);
            }
            // Change to 'else if' in the future when newer MAC version than 1.1 is released
            else {
                assert nwkKey != null;
                joinAns.encryptPHYPayload1_1(
                        Hex.encodeHexString(Objects.requireNonNull(LifeTimeKey.deriveLifeTimeKey(
                                KeyType.JSIntKey,
                                Hex.encodeHexString(nwkKey.getEncoded()),
                                joinReq.getDevEUI()
                        ))),
                        Hex.encodeHexString(deviceKeyHandler.getNwkKey1_1(joinReq.getDevEUI()).getEncoded()),
                        joinAccMHDR
                );

                if (device.getKekEnabled()) {
                    Key kek = deviceKeyHandler.getKek(joinReq.getDevEUI());
                    String kekLabel = device.getKekLabel();

                    if (device.getForwardAppSKeyToNS()) {
                        joinAns.setAppSKey(new KeyEnvelope(new String(Hex.encodeHex(appSKey)).toUpperCase(), kekLabel, kek));
                    }
                    joinAns.setfNwkSIntKey(new KeyEnvelope(new String(Hex.encodeHex(fNwkSIntKey)).toUpperCase(), kekLabel, kek));
                    joinAns.setsNwkSIntKey(new KeyEnvelope(new String(Hex.encodeHex(sNwkSIntKey)).toUpperCase(), kekLabel, kek));
                    joinAns.setNwkSEncKey(new KeyEnvelope(new String(Hex.encodeHex(nwkSEncKey)).toUpperCase(), kekLabel, kek));
                }
                else {
                    if (device.getForwardAppSKeyToNS()) {
                        joinAns.setAppSKey(new KeyEnvelope(new String(Hex.encodeHex(appSKey)).toUpperCase()));
                    }
                    joinAns.setfNwkSIntKey(new KeyEnvelope(new String(Hex.encodeHex(fNwkSIntKey)).toUpperCase()));
                    joinAns.setsNwkSIntKey(new KeyEnvelope(new String(Hex.encodeHex(sNwkSIntKey)).toUpperCase()));
                    joinAns.setNwkSEncKey(new KeyEnvelope(new String(Hex.encodeHex(nwkSEncKey)).toUpperCase()));
                }

                List<KeySpec> keySpecs = new ArrayList<>(List.of(
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(appSKey), KeyType.AppSKey),
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(nwkSEncKey), KeyType.NwkSEncKey),
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(fNwkSIntKey), KeyType.FNwkSIntKey),
                        new KeySpec(joinReq.getDevEUI(), Hex.encodeHexString(sNwkSIntKey), KeyType.SNwkSIntKey)
                ));

                Boolean isInitialSession = device.getSessionStatus().getState().equals(SessionState.INIT);
                deviceKeyHandler.storeSessionKeys(keySpecs, isInitialSession);
            }

            // Successful JoinAns generated. Update sessions status for given device
            device.getSessionStatus().setState(SessionState.ACTIVE);
            device.getSessionStatus().setDevAddr(joinReq.getDevAddr());
            device.getSessionStatus().setLastDevNonce(joinReq.getDevNonce());
            device.getSessionStatus().setLastJoinNonce(newJoinNonce);
            device.getSessionStatus().setSessionKeyId(newSessionKeyId);
            device.getSessionStatus().setSessionNum(device.getSessionStatus().getSessionNum() + 1);
            device.getSessionStatus().getUsedDevNonces().add(joinReq.getDevNonce());
            device.getSessionStatus().getUsedJoinNonces().add(newJoinNonce);
            device.setLastJoin(LocalDateTime.now());
            deviceService.save(device, null);
            postProcess(joinLog, joinAns, true);
        } catch (JoinReqFailedExc e) {
            joinAnsErr.setResult(new Result(
                    "JoinReqFailed", "Failed to process JoinReq\n" + e.getMessage()
            ));
            postProcess(joinLog, joinAnsErr, false);
            return joinAnsErr;
        }

        return joinAns;
    }

    Gson gsonExclKey = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    // Exclude 'lifetime' field from serialization
                    return f.getName().equals("aESKey");
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    private void postProcess(JoinLog joinLog, JoinAns joinAns, Boolean isProcessed) {
        joinLog.setJoinAns(gsonExclKey.toJson(joinAns));
        joinLog.setSuccess(isProcessed);
        joinLogService.save(joinLog);
    }

    // Used in case of errors in JoinReq
    private JoinAns getMandatoryJoinAns(JoinReq joinReq) {
        JoinAns joinAns = new JoinAns();
        if (joinReq.getProtocolVersion() != null) {
            joinAns.setProtocolVersion(joinReq.getProtocolVersion());
        }
        else {
            joinAns.setProtocolVersion("");
        }

        if (joinReq.getReceiverID() != null) {
            joinAns.setSenderID(joinReq.getReceiverID());
        }
        else {
            joinAns.setSenderID("");
        }

        if (joinReq.getSenderID() != null) {
            joinAns.setReceiverID(joinReq.getSenderID());
        }
        else {
            joinAns.setReceiverID("");
        }

        if (joinReq.getTransactionID() != null) {
            joinAns.setTransactionID(joinReq.getTransactionID());
        }
        else {
            joinAns.setTransactionID(1001010101L);
        }

        if (joinReq.getProtocolVersion() != null && joinReq.getProtocolVersion().equals("1.1")) {
            if (joinReq.getSenderNSID() != null) {
                joinAns.setReceiverNSID(joinReq.getSenderNSID());
            }
            else {
                joinAns.setReceiverNSID("");
            }
        }

        joinAns.setMessageType("JoinAns");
        return joinAns;
    }

    private byte[] convertToLittleEndian(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
        return array;
    }
}
