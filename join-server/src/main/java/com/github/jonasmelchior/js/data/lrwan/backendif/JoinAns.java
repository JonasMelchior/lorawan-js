package com.github.jonasmelchior.js.data.lrwan.backendif;

import com.github.jonasmelchior.js.service.lrwan.JoinReqFailedExc;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class JoinAns {
    private String protocolVersion;
    private String senderID;
    private String receiverID;
    private String transactionID;
    private String messageType;
    // Only used in LoRaWAN backend spec. 1.1
    private String receiverNSID;
    private String PHYPayload;
    private Result result;
    private Integer lifeTime;
    private KeyEnvelope sNwkSIntKey;
    private KeyEnvelope fNwkSIntKey;
    private KeyEnvelope nwkSEncKey;
    private KeyEnvelope nwkSKey;
    private KeyEnvelope appSKey;
    private String sessionKeyID;

    public JoinAns(String protocolVersion, String senderID, String receiverID, String transactionID, String PHYPayload, Result result, Integer lifeTime, String sessionKeyID) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transactionID = transactionID;
        this.messageType = "JoinAns";
        this.PHYPayload = PHYPayload;
        this.PHYPayload = this.PHYPayload.toUpperCase();
        this.result = result;
        this.lifeTime = lifeTime;
        this.sessionKeyID = sessionKeyID;
    }

    public void encryptPHYPayload1_0(String appKey, String MHDR) throws JoinReqFailedExc {
        // Calculate the MIC
        try {
            Mac cmac = Mac.getInstance("AESCMAC", new BouncyCastleFipsProvider());
            cmac.init(new SecretKeySpec(Hex.decodeHex(appKey), "AES"));
            byte[] mac = cmac.doFinal(Hex.decodeHex(this.PHYPayload));
            byte[] mic = new byte[4];
            System.arraycopy(mac, 0, mic, 0, 4);
            this.PHYPayload += Hex.encodeHexString(mic);
        } catch (NoSuchAlgorithmException | DecoderException | InvalidKeyException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }

        // Encrypt in decryption mode
        try {
            // No need to do padding since already a multiple of 16 octets
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Hex.decodeHex(appKey), "AES"));
            // Encrypt Join-acc without MHDR
            String joinAccEnc = Hex.encodeHexString(cipher.doFinal(Hex.decodeHex(this.PHYPayload.substring(2))));
            // Reconstruct PHYPayload - Insert MHDR and append encrypted PHYPayload
            this.PHYPayload = MHDR + joinAccEnc;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | DecoderException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    public void encryptPHYPayload1_1(String jSIntKey, String nwkKey, String MHDR) throws JoinReqFailedExc {
        // Calculate the MIC
        try {
            Mac cmac = Mac.getInstance("AESCMAC", new BouncyCastleFipsProvider());
            cmac.init(new SecretKeySpec(Hex.decodeHex(jSIntKey), "AES"));
            byte[] mac = cmac.doFinal(Hex.decodeHex(this.PHYPayload));
            byte[] mic = new byte[4];
            System.arraycopy(mac, 0, mic, 0, 4);
            this.PHYPayload += Hex.encodeHexString(mic);
        } catch (NoSuchAlgorithmException | DecoderException | InvalidKeyException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }

        // Encrypt in decryption mode
        try {
            // No need to do padding since already a multiple of 16 octets
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Hex.decodeHex(nwkKey), "AES"));
            // Encrypt Join-acc without MHDR
            String joinAccEnc = Hex.encodeHexString(cipher.doFinal(Hex.decodeHex(this.PHYPayload.substring(2))));
            // Reconstruct PHYPayload´ - Insert MHDR and append encrypted PHYPayload
            this.PHYPayload = MHDR + joinAccEnc;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | DecoderException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    public JoinAns(String protocolVersion, String senderID, String receiverID, String transactionID, String messageType) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.transactionID = transactionID;
        this.messageType = messageType;
    }

    public JoinAns() {
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
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

    public String getPHYPayload() {
        return PHYPayload;
    }

    public void setPHYPayload(String PHYPayload) {
        this.PHYPayload = PHYPayload;
    }

    public Integer getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(Integer lifeTime) {
        this.lifeTime = lifeTime;
    }

    public String getSessionKeyID() {
        return sessionKeyID;
    }

    public void setSessionKeyID(String sessionKeyID) {
        this.sessionKeyID = sessionKeyID;
    }

    public KeyEnvelope getsNwkSIntKey() {
        return sNwkSIntKey;
    }

    public void setsNwkSIntKey(KeyEnvelope sNwkSIntKey) {
        this.sNwkSIntKey = sNwkSIntKey;
    }

    public KeyEnvelope getfNwkSIntKey() {
        return fNwkSIntKey;
    }

    public void setfNwkSIntKey(KeyEnvelope fNwkSIntKey) {
        this.fNwkSIntKey = fNwkSIntKey;
    }

    public KeyEnvelope getNwkSEncKey() {
        return nwkSEncKey;
    }

    public void setNwkSEncKey(KeyEnvelope nwkSEncKey) {
        this.nwkSEncKey = nwkSEncKey;
    }

    public KeyEnvelope getNwkSKey() {
        return nwkSKey;
    }

    public void setNwkSKey(KeyEnvelope nwkSKey) {
        this.nwkSKey = nwkSKey;
    }

    public KeyEnvelope getAppSKey() {
        return appSKey;
    }

    public void setAppSKey(KeyEnvelope appSKey) {
        this.appSKey = appSKey;
    }

    public String getReceiverNSID() {
        return receiverNSID;
    }

    public void setReceiverNSID(String receiverNSID) {
        this.receiverNSID = receiverNSID;
    }
}
