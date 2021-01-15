package me.kamilki.safepass.util;

import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String newLoginToken() {
        String token;

        do {
            token = newToken(64);
        } while (SafePass.USER_SESSIONS.containsKey(token));

        return token;
    }

    public static String newEntryID(final Database database) {
        String entryID;

        do {
            entryID = newToken(20);
        } while (database.safeEntryExists(entryID));

        return entryID;
    }

    public static String newResetToken(final Database database) {
        String restoreToken;

        do {
            restoreToken = newToken(64);
        } while (database.resetTokenExists(restoreToken));

        return restoreToken;
    }

    public static String newVerificationToken(final Database database) {
        String verificationToken;

        do {
            verificationToken = newToken(64);
        } while (database.verificationTokenExists(verificationToken));

        return verificationToken;
    }

    public static String newToken(final int length) {
        final byte[] randomBytes = new byte[(int) (length / 8.0D * 6.0D)];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private TokenGenerator() {}

}
