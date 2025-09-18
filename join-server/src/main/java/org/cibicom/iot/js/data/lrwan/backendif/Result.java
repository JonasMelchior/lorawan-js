package org.cibicom.iot.js.data.lrwan.backendif;

public class Result {
    private String resultCode;
    private String description;

    public static final String contentErrSenderId = "SenderID is not Hex encoded in ASCII format";
    public static final String contentErrReceiverId = "ReceiverID is not Hex encoded in ASCII format";
    public static final String contentErrDevEUI = "DevEUI is not Hex encoded in ASCII format";
    public static final String contentErrDevAddr = "DevAddr is not Hex encoded in ASCII format";
    public static final String contentErrDLSettings = "DLSettings is not Hex encoded in ASCII format";
    public static final String contentErrPHYPayload = "PHYPayload is not Hex encoded in ASCII format";
    public static final String contentErrMessageType = "Wrong message type - expected JoinReq";

    public static final String lengthErrSenderId = "SenderID is not 3 bytes long (6 hex characters)";
    public static final String lengthErrReceiverId = "ReceiverID is not 8 bytes long (16 hex characters)";
    public static final String lengthErrDevEUI = "DevEUI is not 8 bytes long (16 hex characters)";
    public static final String lengthErrDevAddr = "DevAddr is not 4 bytes long (8 hex characters)";
    public static final String lengthErrDLSettings = "DLSettings is not 1 byte long (2 hex characters)";
    public static final String lengthErrPHYPayload = "PHYPayload is not 18 bytes (46 hex characters)";
    public static final String lengthErrSessionKeyId = "Session key is above 16 characters";

    public static final String nullErrManFields1_0 = "Mandatory field(s) not provided for ProtocolVersion 1.0";
    public static final String nullErrManFields1_1 = "Mandatory field(s) not provided for ProtocolVersion 1.1";

    public static final String wrongProtVersionErr = "ProtocolVersion is not valid";

    public static final String jsonParsingFailedErr = "JSON parsing failed due to syntax errors";

    public Result(String resultCode, String description) {
        this.resultCode = resultCode;
        this.description = description;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
