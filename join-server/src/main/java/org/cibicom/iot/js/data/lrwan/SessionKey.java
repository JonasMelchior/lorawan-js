package org.cibicom.iot.js.data.lrwan;

import org.cibicom.iot.js.service.lrwan.JoinReqFailedExc;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SessionKey {
    // test key for ESP32_SX1276: 00000000000000000000000706050407
    // test key for glamos field tester: 2F6DE60C81548C210C50C4B408E68162
    public static String testAppKey = "00000000000000000000000706050407";
    public static String testNwkKey = "0000000000032000190AC00706050407";
    public static String testJSIntKey = "00000032003200000000000706050407";
    public static String testJSEncKey = "000980000000000000DB000706050407";


    public static byte[] generateAppSKey1_0(String key, String joinNonce, String netId, String devNonce) throws JoinReqFailedExc {
        StringBuilder plaintext = new StringBuilder("02" + joinNonce + netId + devNonce);
        while (plaintext.length() % 32 != 0) {
            plaintext.append("0");
        }

        try {
            return generateSessionKey(Hex.decodeHex(key), Hex.decodeHex(plaintext.toString()));
        } catch (DecoderException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    public static byte[] generateAppSKey1_1(String key, String joinNonce, String joinEUI, String devNonce) throws JoinReqFailedExc {
        StringBuilder plaintext = new StringBuilder("02" + joinNonce + joinEUI + devNonce);
        while (plaintext.length() % 32 != 0) {
            plaintext.append("0");
        }

        try {
            return generateSessionKey(Hex.decodeHex(key), Hex.decodeHex(plaintext.toString()));
        } catch (DecoderException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    public static byte[] generateNwkSKey1_0(String key, String joinNonce, String netId, String devNonce) throws JoinReqFailedExc {
        StringBuilder plaintext = new StringBuilder("01" + joinNonce + netId + devNonce);
        while (plaintext.length() % 32 != 0) {
            plaintext.append("0");
        }

        try {
            return generateSessionKey(Hex.decodeHex(key), Hex.decodeHex(plaintext.toString()));
        } catch (DecoderException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    public static byte[] generateNwkSKey1_1(NetworkSessionKeyType networkSessionKeyType, String key, String joinNonce, String joinEUI, String devNonce) throws JoinReqFailedExc {
        StringBuilder plaintext;
        switch (networkSessionKeyType) {
            case FORWARDING -> plaintext = new StringBuilder("01");
            case SERVING -> plaintext = new StringBuilder("03");
            case NETWORK -> plaintext = new StringBuilder("04");
            default -> throw new JoinReqFailedExc("NetworkSessionKeyType not valid");
        }

        plaintext.append(joinNonce).append(joinEUI).append(devNonce);

        while (plaintext.length() % 32 != 0) {
            plaintext.append("0");
        }

        try {
            return generateSessionKey(Hex.decodeHex(key), Hex.decodeHex(plaintext.toString()));
        } catch (DecoderException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }

    private static byte[] generateSessionKey(byte[] key, byte[] plaintext) throws JoinReqFailedExc {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }
}
