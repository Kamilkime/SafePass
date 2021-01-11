package me.kamilki.safepass.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public final class EncryptionUtil {

    public static String encrypt(final String password, final byte[] key) {
        try {
            final Base64.Encoder base64Encoder = java.util.Base64.getEncoder();
            final SecretKeySpec cipherKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
            return base64Encoder.encodeToString(cipher.doFinal(password.getBytes()));
        } catch (final Exception exception) {
            return "";
        }
    }

    public static String decrypt(final String password, final byte[] key) {
        try {
            final Base64.Decoder base64Decoder = java.util.Base64.getDecoder();
            final SecretKeySpec cipherKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, cipherKey);
            return new String(cipher.doFinal(base64Decoder.decode(password)));
        } catch (final Exception exception) {
            return "";
        }
    }

    private EncryptionUtil() {}

}
