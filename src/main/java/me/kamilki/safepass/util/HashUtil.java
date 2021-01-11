package me.kamilki.safepass.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    public static boolean checkPassword(final String password, final String hash) {
        return BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified;
    }

    public static String hashPasswordBCrypt(final String password) {
        try {
            return BCrypt.withDefaults().hashToString(10, password.toCharArray());
        } catch (final IllegalArgumentException exception) {
            return "";
        }
    }

    public static byte[] hashPasswordSHA256(final String password) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(password.getBytes());
        } catch (final NoSuchAlgorithmException exception) {
            return new byte[0];
        }
    }

    private HashUtil() {}

}
