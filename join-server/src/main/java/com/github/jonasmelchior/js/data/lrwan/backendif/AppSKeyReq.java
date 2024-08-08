package com.github.jonasmelchior.js.data.lrwan.backendif;

public class AppSKeyReq {
    private String protocolVersion;
    private String senderID;
    private String receiverID;
    private String transactionID;
    private String messageType;
    private String senderNSID;
    private String senderToken;
    private String receiverToken;
    private String devEUI;
    private String sessionKeyID;

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

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public String getSessionKeyID() {
        return sessionKeyID;
    }

    public void setSessionKeyID(String sessionKeyID) {
        this.sessionKeyID = sessionKeyID;
    }
}
