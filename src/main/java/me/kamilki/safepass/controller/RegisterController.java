package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.util.HashUtil;
import me.kamilki.safepass.util.ModelUtil;
import me.kamilki.safepass.util.TokenGenerator;
import org.apache.commons.validator.routines.EmailValidator;

public final class RegisterController {

    private static final String[] REGISTER_ATTRIBUTES = new String[]{"userExists", "registrationFailed"};

    public static void serveRegisterPage(final Context ctx) {
        final String userToken = ctx.sessionAttribute("userToken");
        if (userToken != null && !userToken.isEmpty() && SafePass.USER_SESSIONS.containsKey(userToken)) {
            ctx.redirect("/manager");
            return;
        }

        ctx.render("/html/register.html", ModelUtil.createModel(ctx, REGISTER_ATTRIBUTES));
    }

    public static void handleRegisterPost(final Context ctx, final Database database) {
        final String username = ctx.formParam("username");
        if (username == null || username.isEmpty() || !EmailValidator.getInstance().isValid(username)) {
            ctx.sessionAttribute("registrationFailed", true);
            ctx.redirect("/register");
            return;
        }

        final String password = ctx.formParam("password");
        if (password == null || password.isEmpty()) {
            ctx.sessionAttribute("registrationFailed", true);
            ctx.redirect("/register");
            return;
        }

        final int userID = database.getUserID(username);
        if (userID != -1) {
            ctx.sessionAttribute("userExists", true);
            ctx.redirect("/register");
            return;
        }

        final String encryptedPassword = HashUtil.hashPasswordBCrypt(password);
        if (encryptedPassword.isEmpty()) {
            ctx.sessionAttribute("registrationFailed", true);
            ctx.redirect("/register");
            return;
        }

        final String verificationToken = TokenGenerator.newVerificationToken(database);
        if (!database.saveUser(username, encryptedPassword, verificationToken)) {
            ctx.sessionAttribute("registrationFailed", true);
            ctx.redirect("/register");
            return;
        }

        System.out.println("Sending verification password link to " + username);
        System.out.println("https://127.0.0.1/verify?verificationToken=" + verificationToken);
        System.out.println("http://127.0.0.1:8080/verify?verificationToken=" + verificationToken);

        LoginController.redirectLogin(ctx, "registered", true);
    }

    private RegisterController() {}

}
