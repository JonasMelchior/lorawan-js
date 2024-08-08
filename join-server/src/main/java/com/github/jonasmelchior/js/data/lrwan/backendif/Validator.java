package com.github.jonasmelchior.js.data.lrwan.backendif;

import com.github.jonasmelchior.js.data.lrwan.ProtocolVersion;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    public static Pair<String, String> validateJoinReq(JoinReq joinReq) {
        ProtocolVersion protocolVersion = ProtocolVersion.LORAWAN_1_0;
        List<Object> mandatoryFields = new ArrayList<>(Arrays.asList(
                joinReq.getProtocolVersion(),
                joinReq.getSenderID(),
                joinReq.getReceiverID(),
                joinReq.getTransactionID(),
                joinReq.getMessageType(),
                joinReq.getMACVersion(),
                joinReq.getPHYPayload(),
                joinReq.getDevEUI(),
                joinReq.getDevAddr(),
                joinReq.getdLSettings(),
                joinReq.getRxDelay()
        ));
        if (joinReq.getProtocolVersion().equals("1.1")) {
            mandatoryFields.add(joinReq.getSenderNSID());
            protocolVersion = ProtocolVersion.LORAWAN_1_1;
        }

        Pair<String, String> notNullErr = validateNotNull(mandatoryFields, protocolVersion);
        if (notNullErr != null) {
            return notNullErr;
        }

        Pair<String, String> protocolVersionErr = validateProtocolVersion(joinReq.getProtocolVersion());
        if (protocolVersionErr != null) {
            return protocolVersionErr;
        }

        Pair<String, String> lengthErr = validateLengthJoinReq(joinReq);
        if (lengthErr != null) {
            return lengthErr;
        }

        return validateContentJoinReq(joinReq);
    }

    public static Pair<String, String> validateAppSKeyReq(AppSKeyReq appSKeyReq) {
        ProtocolVersion protocolVersion = ProtocolVersion.LORAWAN_1_0;
        List<Object> mandatoryFields = new ArrayList<>(Arrays.asList(
                appSKeyReq.getProtocolVersion(),
                appSKeyReq.getSenderID(),
                appSKeyReq.getReceiverID(),
                appSKeyReq.getTransactionID(),
                appSKeyReq.getMessageType(),
                appSKeyReq.getDevEUI(),
                appSKeyReq.getSessionKeyID()
        ));
        if (appSKeyReq.getProtocolVersion().equals("1.1")) {
            mandatoryFields.add(appSKeyReq.getSenderNSID());
            protocolVersion = ProtocolVersion.LORAWAN_1_1;
        }

        Pair<String, String> notNullErr = validateNotNull(mandatoryFields, protocolVersion);
        if (notNullErr != null) {
            return notNullErr;
        }

        Pair<String, String> protocolVersionErr = validateProtocolVersion(appSKeyReq.getProtocolVersion());
        if (protocolVersionErr != null) {
            return protocolVersionErr;
        }

        Pair<String, String> lengthErr = validateLengthAppSKeyReq(appSKeyReq);
        if (lengthErr != null) {
            return lengthErr;
        }

        return validateContentAppSKeyReq(appSKeyReq);
    }

    private static Pair<String, String> validateProtocolVersion(String protocolVersion) {
        if (!protocolVersion.equals("1.0") && !protocolVersion.equals("1.1")) {
            return new Pair<>("InvalidProtocolVersion", Result.wrongProtVersionErr);
        }

        return null;
    }

    private static Pair<String, String> validateContentJoinReq(JoinReq joinReq) {
        if (!isHexadecimal(joinReq.getSenderID())) {
            return new Pair<>("MalformedMessage", Result.contentErrSenderId);
        }
        if (!isHexadecimal(joinReq.getReceiverID())) {
            return new Pair<>("MalformedMessage", Result.contentErrReceiverId);
        }
        if (!isHexadecimal(joinReq.getDevEUI())) {
            return new Pair<>("MalformedMessage", Result.contentErrDevEUI);
        }
        if (!isHexadecimal(joinReq.getDevAddr())) {
            return new Pair<>("MalformedMessage", Result.contentErrDevAddr);
        }
        if (!isHexadecimal(joinReq.getdLSettings())) {
            return new Pair<>("MalformedMessage", Result.contentErrDLSettings);
        }
        if (!isHexadecimal(joinReq.getPHYPayload())) {
            return new Pair<>("MalformedMessage", Result.contentErrPHYPayload);
        }
        if (!joinReq.getMessageType().equals("JoinReq")) {
            return new Pair<>("MalformedMessage", Result.contentErrMessageType);
        }

        return null;
    }
    private static Pair<String, String> validateLengthJoinReq(JoinReq joinReq) {
        Pair<String, String> senderIDLengthErr = validateLengthSenderID(joinReq.getSenderID());
        if (senderIDLengthErr != null) {
            return senderIDLengthErr;
        }

        Pair<String, String> receiverIDLengthErr = validateLengthReceiverID(joinReq.getReceiverID());
        if (receiverIDLengthErr != null) {
            return receiverIDLengthErr;
        }

        Pair<String, String> devEUILengthErr = validateLengthDevEUI(joinReq.getDevEUI());
        if (devEUILengthErr != null) {
            return devEUILengthErr;
        }

        Pair<String, String> devAddrLengthErr = validateLengthDevAddr(joinReq.getDevAddr());
        if (devAddrLengthErr != null) {
            return devAddrLengthErr;
        }

        Pair<String, String> dlSettingsLengthErr = validateLengthDLSettings(joinReq.getdLSettings());
        if (dlSettingsLengthErr != null) {
            return dlSettingsLengthErr;
        }

        return validateLengthPHYPayload(joinReq.getPHYPayload());
    }

    private static Pair<String, String> validateLengthAppSKeyReq(AppSKeyReq appSKeyReq) {
        Pair<String, String> senderIDLengthErr = validateLengthSenderID(appSKeyReq.getSenderID());
        if (senderIDLengthErr != null) {
            return senderIDLengthErr;
        }

        Pair<String, String> receiverIDLengthErr = validateLengthReceiverID(appSKeyReq.getReceiverID());
        if (receiverIDLengthErr != null) {
            return receiverIDLengthErr;
        }

        Pair<String, String> devEUILengthErr = validateLengthDevEUI(appSKeyReq.getDevEUI());
        if (devEUILengthErr != null) {
            return devEUILengthErr;
        }

        return validateLengthSessionKeyID(appSKeyReq.getSessionKeyID());
    }

    private static Pair<String, String> validateContentAppSKeyReq(AppSKeyReq appSKeyReq) {
        if (!isHexadecimal(appSKeyReq.getSenderID())) {
            return new Pair<>("MalformedMessage", Result.contentErrSenderId);
        }
        if (!isHexadecimal(appSKeyReq.getReceiverID())) {
            return new Pair<>("MalformedMessage", Result.contentErrReceiverId);
        }
        if (!isHexadecimal(appSKeyReq.getDevEUI())) {
            return new Pair<>("MalformedMessage", Result.contentErrDevEUI);
        }

        return null;
    }

    private static Pair<String, String> validateNotNull(List<Object> fields, ProtocolVersion protocolVersion) {
        boolean isNull = false;
        for (Object field : fields) {
            if (field == null) {
                isNull = true;
                break;
            }
        }

        if (isNull) {
            if (protocolVersion.equals(ProtocolVersion.LORAWAN_1_0)) {
                return new Pair<>("MalformedMessage", Result.nullErrManFields1_0);
            }
            else if (protocolVersion.equals(ProtocolVersion.LORAWAN_1_1)) {
                return new Pair<>("MalformedMessage", Result.nullErrManFields1_1);
            }
        }

        return null;
    }

    private static Pair<String, String> validateLengthSenderID(String senderID) {
        if (senderID.length() != 6) {
            return new Pair<>("MalformedMessage", Result.lengthErrSenderId);
        }
        return null;
    }

    private static Pair<String, String> validateLengthReceiverID(String receiverID) {
        if (receiverID.length() != 16) {
            return new Pair<>("MalformedMessage", Result.lengthErrReceiverId);
        }
        return null;
    }
    private static Pair<String, String> validateLengthDevEUI(String devEUI) {
        if (devEUI.length() != 16) {
            return new Pair<>("MalformedMessage", Result.lengthErrDevEUI);
        }
        return null;
    }
    private static Pair<String, String> validateLengthDevAddr(String devAddr) {
        if (devAddr.length() != 8) {
            return new Pair<>("MalformedMessage", Result.lengthErrDevAddr);
        }
        return null;
    }
    private static Pair<String, String> validateLengthDLSettings(String dlSettings) {
        if (dlSettings.length() != 2) {
            return new Pair<>("MalformedMessage", Result.lengthErrDLSettings);
        }
        return null;
    }
    private static Pair<String, String> validateLengthPHYPayload(String phyPayload) {
        if (phyPayload.length() != 46) {
            return new Pair<>("FrameSizeError", Result.lengthErrPHYPayload);
        }
        return null;
    }
    private static Pair<String, String> validateLengthSessionKeyID(String sessionKeyID) {
        if (sessionKeyID.length() > 16) {
            return new Pair<>("MalformedMessage", Result.lengthErrSessionKeyId);
        }
        return null;
    }

    private static boolean isHexadecimal(String input) {
        Pattern pattern = Pattern.compile("^[0-9a-fA-F]+$");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
