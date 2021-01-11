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

        ctx.result(EncryptionUtil.decrypt(encryptedPassword, key));
    }

    public static void servePasswordChangePage(final Context ctx, final Database database) {
        final String restoreToken = ctx.queryParam("restoreToken");
        if (restoreToken == null || restoreToken.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final int userID = database.getUserID(restoreToken, "pass_resets");
        if (userID == -1) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("username", database.getUsername(userID));

        ctx.sessionAttribute("restoreToken", restoreToken);
        ctx.render("/html/restorePassword.html", model);
    }

    public static void handlePasswordChangePost(final Context ctx, final Database database) {
        final String restoreToken = ctx.sessionAttribute("restoreToken");
        if (restoreToken == null || restoreToken.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
            return;
        }

        final int userID = database.getUserID(restoreToken, "pass_resets");
        if (userID == -1) {
            LoginController.redirectLogin(ctx, "loginRedirect", "/manager");
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

        ctx.sessionAttribute("restoreToken", null);
        LoginController.redirectLogin(ctx, "passwordReset", true);
    }

    public static void servePasswordChangeRequestPage(final Context ctx, final Database database) {
        ctx.render("/html/requestRestore.html");
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

        final String restoreToken = TokenGenerator.newRestoreToken(database);
        database.addPasswordRestore(userID, restoreToken);

        System.out.println("Sending restore password link to " + username);
        System.out.println("https://127.0.0.1/restorePassword?restoreToken=" + restoreToken);
        System.out.println("http://127.0.0.1:8080/restorePassword?restoreToken=" + restoreToken);

        LoginController.redirectLogin(ctx, "changeRequest", true);
    }

    private PasswordController() {}

}
