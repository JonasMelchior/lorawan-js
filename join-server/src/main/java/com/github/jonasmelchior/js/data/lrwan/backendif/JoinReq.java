package com.github.jonasmelchior.js.data.lrwan.backendif;

import com.github.jonasmelchior.js.service.lrwan.JoinReqFailedExc;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinReq {
    private String protocolVersion;
    private String senderID;
    private String receiverID;
    private String transactionID;
    private String messageType;
    private String senderNSID;
    private String senderToken;
    private String receiverToken;
    private String MACVersion;
    private String PHYPayload;
    private String devEUI;
    private String devAddr;
    private String dLSettings;
    private Integer rxDelay;
    private String cFList;

    public JoinReq(String protocolVersion, String senderID, String receiverID, String transactionID, String messageType, String senderNSID, String senderToken, String receiverToken, String MACVersion, String PHYPayload, String devEUI, String devAddr, String dLSettings, int rxDelay, String cFList) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transactionID = transactionID;
        this.messageType = messageType;
        this.senderNSID = senderNSID;
        this.senderToken = senderToken;
        this.receiverToken = receiverToken;
        this.MACVersion = MACVersion;
        this.PHYPayload = PHYPayload;
        this.devEUI = devEUI;
        this.devAddr = devAddr;
        this.dLSettings = dLSettings;
        this.rxDelay = rxDelay;
        this.cFList = cFList;
    }

    public boolean checkMic(Key key) throws JoinReqFailedExc {
        try {
            Mac cmac = Mac.getInstance("AESCMAC", new BouncyCastleFipsProvider());
            cmac.init(key);
            byte[] mac = cmac.doFinal(Hex.decodeHex(this.PHYPayload.substring(0, this.PHYPayload.length() - 8)));
            byte[] mic = new byte[4];
            System.arraycopy(mac, 0, mic, 0, 4);
            return this.PHYPayload.substring(this.PHYPayload.length() - 8).equalsIgnoreCase(Hex.encodeHexString(mic));
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }


    // Validate message. That is, to check that mandatory fields are not null and verify lengths and contents.
    // ProtocolVersion and PHYPayload is not checked since this is checked in JoinProcessor after a call to this method
    public Pair<String, String> validate() {
        Pair<String, String> notNullErr = validateNotNull();
        if (notNullErr != null) {
            return notNullErr;
        }

        Pair<String, String> lengthErr = validateLength();
        if (lengthErr != null) {
            return lengthErr;
        }

        Pair<String, String> contentErr = validateContent();
        if (contentErr != null) {
            return contentErr;
        }

        return null;
    }

    private Pair<String, String> validateContent() {
        if (!isHexadecimal(this.senderID)) {
            return new Pair<>("MalformedMessage", Result.contentErrSenderId);
        }
        if (!isHexadecimal(this.receiverID)) {
            return new Pair<>("MalformedMessage", Result.contentErrReceiverId);
        }
        if (!isHexadecimal(this.devEUI)) {
            return new Pair<>("MalformedMessage", Result.contentErrDevEUI);
        }
        if (!isHexadecimal(this.devAddr)) {
            return new Pair<>("MalformedMessage", Result.contentErrDevAddr);
        }
        if (!isHexadecimal(this.dLSettings)) {
            return new Pair<>("MalformedMessage", Result.contentErrDLSettings);
        }
        if (!isHexadecimal(this.PHYPayload)) {
            return new Pair<>("MalformedMessage", Result.contentErrPHYPayload);
        }
        if (!this.messageType.equals("JoinReq")) {
            return new Pair<>("MalformedMessage", Result.contentErrMessageType);
        }

        return null;
    }

    private Pair<String, String> validateLength() {
        if (this.senderID.length() != 6) {
            return new Pair<>("MalformedMessage", Result.lengthErrSenderId);
        }
        if (this.receiverID.length() != 16) {
            return new Pair<>("MalformedMessage", Result.lengthErrReceiverId);
        }
        // check address lenghts
        if (this.devEUI.length() != 16) {
            return new Pair<>("MalformedMessage", Result.lengthErrDevEUI);
        }
        if (this.devAddr.length() != 8) {
            return new Pair<>("MalformedMessage", Result.lengthErrDevAddr);
        }
        // must be 1 byte in hex representation.
        if (this.dLSettings.length() != 2) {
            return new Pair<>("MalformedMessage", Result.lengthErrDLSettings);
        }
        if (this.PHYPayload.length() != 46) {
            return new Pair<>("FrameSizeError", Result.lengthErrPHYPayload);
        }

        return null;
    }

    private Pair<String, String> validateNotNull() {
        boolean fieldsNotNull = this.protocolVersion != null && this.senderID != null && this.receiverID != null &&
                this.transactionID != null && this.messageType != null && this.getMACVersion() != null &&
                this.PHYPayload != null && this.devEUI != null && this.devAddr != null && this.dLSettings != null &&
                this.rxDelay != null;
        boolean senderNSIdNotNull = this.senderNSID != null;
        if (this.protocolVersion == null) {
            return null;
        }
        else {
            if (this.protocolVersion.equals("1.0")) {
                if (fieldsNotNull) {
                    return null;
                }
                else {
                    return new Pair<>("MalformedMessage", Result.nullErrManFields1_0);
                }
            }
            else if (this.protocolVersion.equals("1.1")){
                if (fieldsNotNull & senderNSIdNotNull) {
                    return null;
                }
                else {
                    return new Pair<>("MalformedMessage", Result.nullErrManFields1_1);
                }
            }
            else {
                return new Pair<>("InvalidProtocolVersion", Result.wrongProtVersionErr);
            }
        }
    }

    private boolean isHexadecimal(String input) {
        Pattern pattern = Pattern.compile("^[0-9a-fA-F]+$");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public JoinReq() {
    }

    public String getJoinEUI() {
        return PHYPayload.substring(2, 18);
    }

    public String getDevNonce (){
        return PHYPayload.substring(34, 38);
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSenderNSID() {
        return senderNSID;
    }

    public void setSenderNSID(String senderNSID) {
        this.senderNSID = senderNSID;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

    public String getReceiverToken() {
        return receiverToken;
    }

    public void setReceiverToken(String receiverToken) {
        this.receiverToken = receiverToken;
    }

    public String getMACVersion() {
        return MACVersion;
    }

    public void setMACVersion(String MACVersion) {
        this.MACVersion = MACVersion;
    }

    public String getPHYPayload() {
        return PHYPayload;
    }

    public void setPHYPayload(String PHYPayload) {
        this.PHYPayload = PHYPayload;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public String getDevAddr() {
        return devAddr;
    }

    public void setDevAddr(String devAddr) {
        this.devAddr = devAddr;
    }

    public String getdLSettings() {
        return dLSettings;
    }

    public void setdLSettings(String dLSettings) {
        this.dLSettings = dLSettings;
    }

    public Integer getRxDelay() {
        return rxDelay;
    }

    public void setRxDelay(Integer rxDelay) {
        this.rxDelay = rxDelay;
    }

    public String getcFList() {
        return cFList;
    }

    public void setcFList(String cFList) {
        this.cFList = cFList;
    }

    @Override
    public String toString() {
        return "JoinReq{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", senderID='" + senderID + '\'' +
                ", receiverID='" + receiverID + '\'' +
                ", transactionID='" + transactionID + '\'' +
                ", messageType='" + messageType + '\'' +
                ", senderNSID='" + senderNSID + '\'' +
                ", senderToken='" + senderToken + '\'' +
                ", receiverToken='" + receiverToken + '\'' +
                ", macVersion='" + MACVersion + '\'' +
                ", phyPayload='" + PHYPayload + '\'' +
                ", devEUI='" + devEUI + '\'' +
                ", devAddr='" + devAddr + '\'' +
                ", dLSettings='" + dLSettings + '\'' +
                ", rxDelay=" + rxDelay +
                ", cFList='" + cFList + '\'' +
                '}';
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
