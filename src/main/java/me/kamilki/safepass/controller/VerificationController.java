package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.dao.Database;

public final class VerificationController {

    public static void handleVerify(final Context ctx, final Database database) {
        final String verificationToken = ctx.queryParam("verificationToken");
        if (verificationToken == null || verificationToken.isEmpty()) {
            ctx.redirect("/");
            return;
        }

        final int userID = database.getUserID(verificationToken, "verifications");
        if (userID == -1) {
            ctx.redirect("/");
            return;
        }

        database.verifyUser(userID);
        LoginController.redirectLogin(ctx, "emailVerified", true);
    }

    private VerificationController() {}

}
