package com.github.jonasmelchior.js.data.lrwan;

import com.github.jonasmelchior.js.data.keys.KeyType;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class LifeTimeKey {

    public static byte[] deriveLifeTimeKey(KeyType keyType, String key, String devEUI) {
        StringBuilder plaintext;

        if (keyType.equals(KeyType.JSIntKey)) {
            plaintext = new StringBuilder("06" + devEUI);
        }
        else if (keyType.equals(KeyType.JSEncKey)) {
            plaintext = new StringBuilder("05" + devEUI);
        }
        else {
            return null;
        }

        while (plaintext.length() % 32 != 0) {
            plaintext.append("0");
        }

        try {
            return generateLifeTimeKey(Hex.decodeHex(key), Hex.decodeHex(plaintext.toString()));
        } catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] generateLifeTimeKey(byte[] key, byte[] plaintext) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
