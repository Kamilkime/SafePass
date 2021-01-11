package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.entity.User;
import me.kamilki.safepass.util.HashUtil;
import me.kamilki.safepass.util.ModelUtil;
import me.kamilki.safepass.util.TokenGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class LoginController {

    private static final long LOGIN_DELAY = 5000L;
    private static final String[] LOGIN_ATTRIBUTES = new String[]{"logout", "registered", "loginDelay", "loginFailed", "changeRequest",
            "passwordReset", "passwordNotReset", "emailVerified", "emailNotVerified"};

    private static final Map<String, Long> loginDelay = new HashMap<>();

    public static void serveLoginPage(final Context ctx) {
        final String userToken = ctx.sessionAttribute("userToken");
        if (userToken != null && !userToken.isEmpty() && SafePass.USER_SESSIONS.containsKey(userToken)) {
            ctx.redirect("/manager");
            return;
        }

        ctx.render("/html/login.html", ModelUtil.createModel(ctx, LOGIN_ATTRIBUTES));
    }

    public static void handleLoginPost(final Context ctx, final Database database) {
        final long lastLoginAttempt = loginDelay.getOrDefault(ctx.ip(), 0L);
        loginDelay.put(ctx.ip(), System.currentTimeMillis());

        final long timeLeft = (lastLoginAttempt + LOGIN_DELAY) - System.currentTimeMillis();
        if (timeLeft > 0) {
            LoginController.redirectLogin(ctx, "loginDelay", Math.round(Math.ceil(timeLeft / 1000.0D)));
            return;
        }

        final String password = ctx.formParam("password");
        if (password == null || password.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginFailed", true);
            return;
        }

        final String username = ctx.formParam("username");
        if (username == null || username.isEmpty()) {
            LoginController.redirectLogin(ctx, "loginFailed", true);
            return;
        }

        final Optional<User> userOptional = database.getUser(username);
        if (!userOptional.isPresent()) {
            LoginController.redirectLogin(ctx, "loginFailed", true);
            return;
        }

        final User user = userOptional.get();
        if (!HashUtil.checkPassword(password, user.getHashedPassword())) {
            LoginController.redirectLogin(ctx, "loginFailed", true);
            return;
        }

        if (!user.isVerified()) {
            LoginController.redirectLogin(ctx, "emailNotVerified", true);
            return;
        }

        if (!database.addLoginHistory(user.getId(), ctx.ip(), ctx.userAgent(), System.currentTimeMillis())) {
            LoginController.redirectLogin(ctx, "loginFailed", true);
            return;
        }

        final String userToken = TokenGenerator.newLoginToken();
        ctx.sessionAttribute("userToken", userToken);

        SafePass.USER_SESSIONS.put(userToken, user.getId());
        SafePass.USER_ENCRYPTION.put(userToken, HashUtil.hashPasswordSHA256(password));

        String redirect = ctx.sessionAttribute("loginRedirect");
        if (redirect == null || redirect.isEmpty()) {
            redirect = "/manager";
        }

        ctx.sessionAttribute("loginRedirect", null);
        ctx.redirect(redirect);
    }

    public static void handleLogout(final Context ctx) {
        final String userToken = ctx.sessionAttribute("userToken");
        if (userToken == null || userToken.isEmpty()) {
            ctx.redirect("/login");
            return;
        }

        SafePass.USER_SESSIONS.remove(userToken);
        SafePass.USER_ENCRYPTION.remove(userToken);

        ctx.sessionAttribute("userToken", null);
        LoginController.redirectLogin(ctx, "logout", true);
    }

    public static void redirectLogin(final Context ctx, final String redirectReason, final Object redirectValue) {
        ctx.sessionAttribute(redirectReason, redirectValue);
        ctx.redirect("/login");
    }

    public static void checkLogin(final Context ctx) {
        final String userToken = ctx.sessionAttribute("userToken");
        if (userToken == null || userToken.isEmpty() || !SafePass.USER_SESSIONS.containsKey(userToken)) {
            redirectLogin(ctx, "loginRedirect", ctx.path());
        }
    }

    private LoginController() {}

}
