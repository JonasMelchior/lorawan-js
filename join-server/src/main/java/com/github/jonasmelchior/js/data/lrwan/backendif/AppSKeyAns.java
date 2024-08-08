package com.github.jonasmelchior.js.data.lrwan.backendif;

public class AppSKeyAns {
    private String protocolVersion;
    private String senderID;
    private String receiverID;
    private String transactionID;
    private String messageType;
    private String receiverNSID;
    private String senderToken;
    private String receiverToken;
    private Result result;
    private String devEUI;
    private KeyEnvelope appSKey;
    private String sessionKeyId;

    public AppSKeyAns() {
    }

    public AppSKeyAns(String protocolVersion, String senderID, String receiverID, String transactionID, String devEUI, String sessionKeyId) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transactionID = transactionID;
        this.messageType = "AppSKeyAns";
        this.devEUI = devEUI;
        this.sessionKeyId = sessionKeyId;
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

    public String getReceiverNSID() {
        return receiverNSID;
    }

    public void setReceiverNSID(String receiverNSID) {
        this.receiverNSID = receiverNSID;
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

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public KeyEnvelope getAppSKey() {
        return appSKey;
    }

    public void setAppSKey(KeyEnvelope appSKey) {
        this.appSKey = appSKey;
    }

    public String getSessionKeyId() {
        return sessionKeyId;
    }

    public void setSessionKeyId(String sessionKeyId) {
        this.sessionKeyId = sessionKeyId;
    }
}
