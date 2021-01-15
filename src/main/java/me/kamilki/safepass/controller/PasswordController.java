package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.util.EncryptionUtil;
import me.kamilki.safepass.util.HashUtil;
import me.kamilki.safepass.util.TokenGenerator;

import java.util.HashMap;
import java.util.Map;

public final class PasswordController {

    // Password reset token expires after 15 minutes
    private static final long TOKEN_EXPIRES_AFTER = 15L * 60L * 1000L;

    public static void getPassword(final Context ctx, final Database database) {
        final String userToken = ctx.sessionAttribute("userToken");
        final int userID = SafePass.USER_SESSIONS.get(userToken);

        final String entryID = ctx.queryParam("entryID");
        if (entryID == null || entryID.isEmpty()) {
            ctx.result("");
            return;
        }

        final byte[] key = SafePass.USER_ENCRYPTION.get(userToken);
        if (key == null) {
            ctx.result("");
            return;
        }

        final String encryptedPassword = database.getEncryptedPassword(entryID, userID);
        if (encryptedPassword.isEmpty()) {
            ctx.result("");
            return;
        }

        ctx.result(EncryptionUtil.decrypt(encryptedPassword, key).substring(8));
    }

    public static void servePasswordChangePage(final Context ctx, final Database database) {
        final String resetToken = ctx.queryParam("resetToken");
        if (resetToken == null || resetToken.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final int userID = database.getUserID(resetToken, "pass_resets");
        if (userID == -1) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        if (database.resetTokenExpired(resetToken)) {
            database.removePasswordReset(resetToken);
            LoginController.redirectLogin(ctx, "passwordResetExpired", true);
            return;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("username", database.getUsername(userID));

        ctx.sessionAttribute("resetToken", resetToken);
        ctx.render("/html/resetPassword.html", model);
    }

    public static void handlePasswordChangePost(final Context ctx, final Database database) {
        final String resetToken = ctx.sessionAttribute("resetToken");
        if (resetToken == null || resetToken.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final int userID = database.getUserID(resetToken, "pass_resets");
        if (userID == -1) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        if (database.resetTokenExpired(resetToken)) {
            database.removePasswordReset(resetToken);
            LoginController.redirectLogin(ctx, "passwordResetExpired", true);
            return;
        }

        final String password = ctx.formParam("password");
        if (password == null || password.isEmpty()) {
            LoginController.redirectLogin(ctx, "passwordNotReset", true);
            return;
        }

        final String encryptedPassword = HashUtil.hashPasswordBCrypt(password);
        if (encryptedPassword.isEmpty()) {
            LoginController.redirectLogin(ctx, "passwordNotReset", true);
            return;
        }

        database.changePassword(userID, encryptedPassword);

        ctx.sessionAttribute("resetToken", null);
        LoginController.redirectLogin(ctx, "passwordReset", true);
    }

    public static void servePasswordChangeRequestPage(final Context ctx) {
        ctx.render("/html/requestPasswordReset.html");
    }

    public static void handlePasswordChangeRequestPost(final Context ctx, final Database database) {
        final String username = ctx.formParam("username");
        if (username == null || username.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final int userID = database.getUserID(username);
        if (userID == -1) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final String resetToken = TokenGenerator.newResetToken(database);
        database.addPasswordReset(userID, resetToken, System.currentTimeMillis() + TOKEN_EXPIRES_AFTER);

        System.out.println("Sending restore password link to " + username);
        System.out.println("The link will be valid for 15 minutes!");
        System.out.println("https://127.0.0.1/resetPassword?resetToken=" + resetToken);
        System.out.println("http://127.0.0.1:8080/resetPassword?resetToken=" + resetToken);

        LoginController.redirectLogin(ctx, "changeRequest", true);
    }

    private PasswordController() {}

}
