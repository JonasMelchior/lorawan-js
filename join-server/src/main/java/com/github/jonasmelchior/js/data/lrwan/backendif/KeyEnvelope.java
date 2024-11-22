package com.github.jonasmelchior.js.data.lrwan.backendif;

import com.github.jonasmelchior.js.service.lrwan.JoinReqFailedExc;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class KeyEnvelope {
    private String kEKLabel;
    private String aESKey;

    public KeyEnvelope(String key) {
        this.aESKey = key;
    }

    public KeyEnvelope(String key, String kekLabel, Key kek) throws JoinReqFailedExc {
        this.aESKey = key;
        this.kEKLabel = kekLabel;
        try {
            Cipher cipher = Cipher.getInstance("AESKW");
            cipher.init(Cipher.WRAP_MODE, kek);
            byte[] wrappedSessionKey = cipher.wrap(new SecretKeySpec(Hex.decodeHex(key), "AES"));
            this.aESKey = Hex.encodeHexString(wrappedSessionKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | DecoderException |
                 IllegalBlockSizeException e) {
            throw new JoinReqFailedExc(e.getMessage());
        }
    }


    public static Key unwrap(String wrappedKey, Key kek) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AESKW");
            cipher.init(Cipher.UNWRAP_MODE, kek);
            return cipher.unwrap(Hex.decodeHex(wrappedKey), "AES", Cipher.SECRET_KEY);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public String getkEKLabel() {
        return kEKLabel;
    }

    public void setkEKLabel(String kEKLabel) {
        this.kEKLabel = kEKLabel;
    }

    public String getaESKey() {
        return aESKey;
    }

    public String getaESKey(Key kek) {
        return this.aESKey;
//        try {
//            Cipher cipher = Cipher.getInstance("AESKW");
//            cipher.init(Cipher.UNWRAP_MODE, kek);
//            return Hex.encodeHexString(cipher.unwrap(Hex.decodeHex(this.aESKey), "AES", Cipher.SECRET_KEY).getEncoded());
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | DecoderException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void setaESKey(String aESKey) {
        this.aESKey = aESKey;
    }
}
