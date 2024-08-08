package com.github.jonasmelchior.js.service.lrwan;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.SessionState;
import com.github.jonasmelchior.js.data.lrwan.backendif.*;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.google.gson.*;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Hex;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Optional;

public class AppSKeyReqHandler {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    private DeviceService deviceService;
    private DeviceKeyHandler deviceKeyHandler;
    private AppSKeyReqLogService appSKeyReqLogService;

    public AppSKeyReqHandler(
            DeviceService deviceService,
            KeyCredentialService keyCredentialService,
            DevKeyIdService devKeyIdService,
            AppSKeyReqLogService appSKeyReqLogService
    ) {
        this.deviceService = deviceService;
        this.deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devKeyIdService
                )
        );
        this.appSKeyReqLogService = appSKeyReqLogService;
    }

    public AppSKeyAns processAppSKeyReq(String appSKeyReqJSon) {
        AppSKeyReq appSKeyReq;

        try {
            appSKeyReq = gson.fromJson(appSKeyReqJSon, AppSKeyReq.class);
        } catch (JsonSyntaxException e) {
            AppSKeyAns appSKeyAnsErr = getMandatoryAppSKeyAns(new AppSKeyReq());
            appSKeyAnsErr.setResult(new Result(
                    "MalformedMessage",
                    Result.jsonParsingFailedErr + ": " + e.getMessage()
            ));
            return appSKeyAnsErr;
        }

        AppSKeyAns appSKeyAnsErr = getMandatoryAppSKeyAns(appSKeyReq);

        Pair<String, String> validationErrorResult = Validator.validateAppSKeyReq(appSKeyReq);
        if (validationErrorResult != null) {
            appSKeyAnsErr.setResult(new Result(
                    validationErrorResult.a,
                    validationErrorResult.b
            ));
            return appSKeyAnsErr;
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI(appSKeyReq.getDevEUI());
        if (optionalDevice.isEmpty()) {
            appSKeyAnsErr.setResult(new Result(
                    "UnknownDevEUI", "DevEUI " + appSKeyAnsErr.getDevEUI() + " is not registered on JS"
            ));
            return appSKeyAnsErr;
        }

        Device device = optionalDevice.get();

        AppSKeyReqLog appSKeyReqLog = new AppSKeyReqLog(
                device,
                appSKeyReqJSon,
                LocalDateTime.now()
        );

        if (device.getSessionStatus().getState() != SessionState.ACTIVE) {
            appSKeyAnsErr.setResult(new Result(
                    "Other", "Device " + appSKeyReq.getDevEUI() + " has not been activated yet"
            ));
            postProcess(appSKeyReqLog, appSKeyAnsErr, false);
            return appSKeyAnsErr;
        }

        if (Integer.parseInt(device.getSessionStatus().getSessionKeyId()) != Integer.parseInt(appSKeyReq.getSessionKeyID())) {
            appSKeyAnsErr.setResult(new Result(
                    "Other", "Specified sessionKeyID is not correct for DevEUI " + appSKeyReq.getDevEUI()
            ));
            postProcess(appSKeyReqLog, appSKeyAnsErr, false);
            return appSKeyAnsErr;
        }

        AppSKeyAns appSKeyAns = new AppSKeyAns(
                appSKeyReq.getProtocolVersion(),
                appSKeyReq.getReceiverID(),
                appSKeyReq.getSenderID(),
                appSKeyReq.getTransactionID(),
                appSKeyReq.getDevEUI(),
                appSKeyReq.getSessionKeyID()
        );
        if (appSKeyReq.getProtocolVersion().equals("1.1")) {
            appSKeyAns.setReceiverNSID(appSKeyReq.getSenderNSID());
        }

        try {
            Key kek = deviceKeyHandler.getKek();
            Key appSKey = deviceKeyHandler.getAppSKey(appSKeyReq.getDevEUI());
            appSKeyAns.setResult(new Result(
                    "Success", "AppSKey successfully retrieved"
            ));
            appSKeyAns.setAppSKey(new KeyEnvelope(new String(Hex.encodeHex(appSKey.getEncoded())).toUpperCase(), kek));
            postProcess(appSKeyReqLog, appSKeyAns, true);
        } catch (JoinReqFailedExc e) {
            appSKeyAns.setResult(new Result(
                    "Other", "Failed to retrieve AppSKey on JS"
            ));
            postProcess(appSKeyReqLog, appSKeyAns, false);
            return appSKeyAns;
        }

        return appSKeyAns;
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

    private void postProcess(AppSKeyReqLog appSKeyReqLog, AppSKeyAns appSKeyAns, Boolean isRetrieved) {
        appSKeyReqLog.setAppSKeyAns(gsonExclKey.toJson(appSKeyAns));
        appSKeyReqLog.setSuccess(isRetrieved);
        appSKeyReqLogService.save(appSKeyReqLog);
    }

    private AppSKeyAns getMandatoryAppSKeyAns(AppSKeyReq appSKeyReq) {
        AppSKeyAns appSKeyAns = new AppSKeyAns();
        if (appSKeyReq.getProtocolVersion() != null) {
            appSKeyAns.setProtocolVersion(appSKeyReq.getProtocolVersion());
        }
        else {
            appSKeyAns.setProtocolVersion("");
        }

        if (appSKeyReq.getReceiverID() != null) {
            appSKeyAns.setSenderID(appSKeyReq.getReceiverID());
        }
        else {
            appSKeyAns.setSenderID("");
        }

        if (appSKeyReq.getSenderID() != null) {
            appSKeyAns.setReceiverID(appSKeyReq.getSenderID());
        }
        else {
            appSKeyAns.setReceiverID("");
        }

        if (appSKeyReq.getTransactionID() != null) {
            appSKeyAns.setTransactionID(appSKeyReq.getTransactionID());
        }
        else {
            appSKeyAns.setTransactionID("");
        }
        if (appSKeyReq.getDevEUI() != null) {
            appSKeyAns.setDevEUI(appSKeyReq.getDevEUI());
        }
        else {
            appSKeyAns.setDevEUI("");
        }
        if (appSKeyReq.getSessionKeyID() != null) {
            appSKeyAns.setSessionKeyId(appSKeyReq.getSessionKeyID());
        }
        else {
            appSKeyAns.setSessionKeyId("");
        }

        if (appSKeyReq.getProtocolVersion() != null && appSKeyReq.getProtocolVersion().equals("1.1")) {
            if (appSKeyReq.getSenderNSID() != null) {
                appSKeyAns.setReceiverNSID(appSKeyReq.getSenderNSID());
            }
            else {
                appSKeyAns.setReceiverNSID("");
            }
        }

        appSKeyAns.setMessageType("AppSKeyAns");
        return appSKeyAns;
    }
}
