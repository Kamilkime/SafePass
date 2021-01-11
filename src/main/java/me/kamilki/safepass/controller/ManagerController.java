package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.entity.SafeEntry;
import me.kamilki.safepass.util.EncryptionUtil;
import me.kamilki.safepass.util.TokenGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ManagerController {

    public static void serveManagerPage(final Context ctx, final Database database) {
        final String userToken = ctx.sessionAttribute("userToken");
        final int userID = SafePass.USER_SESSIONS.get(userToken);

        final Map<String, Object> model = new HashMap<>();
        model.put("username", database.getUsername(userID));
        model.put("entries", database.getUserSafeEntries(userID, SafePass.USER_ENCRYPTION.get(userToken)));

        ctx.render("/html/manager.html", model);
    }

    public static void serveEditPage(final Context ctx, final Database database) {
        final String entryID = ctx.queryParam("entryID");
        if (entryID == null || entryID.isEmpty()) {
            ctx.redirect("/manager");
            return;
        }

        final Map<String, Object> model = new HashMap<>();

        if ("new".equalsIgnoreCase(entryID)) {
            model.put("newEntry", true);
            model.put("website", "");
            model.put("login", "");
            model.put("password", "");

            ctx.sessionAttribute("entryID", TokenGenerator.newEntryID(database));
        } else {
            final Optional<SafeEntry> safeEntryOptional = database.getSafeEntry(entryID);
            if (!safeEntryOptional.isPresent()) {
                ctx.redirect("/manager");
                return;
            }

            final SafeEntry safeEntry = safeEntryOptional.get();
            final String userToken = ctx.sessionAttribute("userToken");
            final int userID = SafePass.USER_SESSIONS.get(userToken);

            if (safeEntry.getUserID() != userID) {
                ctx.redirect("/manager");
                return;
            }

            final byte[] decryptionKey = SafePass.USER_ENCRYPTION.get(userToken);
            if (decryptionKey == null) {
                LoginController.redirectLogin(ctx, "loginRedirect", ctx.path());
                return;
            }

            model.put("newEntry", false);
            model.put("website", EncryptionUtil.decrypt(safeEntry.getWebsite(), decryptionKey));
            model.put("login", EncryptionUtil.decrypt(safeEntry.getLogin(), decryptionKey));
            model.put("password", EncryptionUtil.decrypt(safeEntry.getPassword(), decryptionKey));

            ctx.sessionAttribute("entryID", safeEntry.getId());
        }

        ctx.render("/html/editEntry.html", model);
    }

    public static void handleEditPost(final Context ctx, final Database database) {
        final String entryID = ctx.sessionAttribute("entryID");
        if (entryID == null || entryID.length() != 20) {
            ctx.redirect("/manager");
            return;
        }

        final String website = ctx.formParam("website");
        if (website == null || website.isEmpty()) {
            ctx.redirect("/manager");
            return;
        }

        final String login = ctx.formParam("login");
        if (login == null || login.isEmpty()) {
            ctx.redirect("/manager");
            return;
        }

        final String password = ctx.formParam("password");
        if (password == null || password.isEmpty()) {
            ctx.redirect("/manager");
            return;
        }

        final String userToken = ctx.sessionAttribute("userToken");
        final int userID = SafePass.USER_SESSIONS.get(userToken);

        final Optional<SafeEntry> safeEntryOptional = database.getSafeEntry(entryID);
        if (safeEntryOptional.isPresent() && safeEntryOptional.get().getUserID() != userID) {
            ctx.redirect("/manager");
            return;
        }

        final byte[] encryptionKey = SafePass.USER_ENCRYPTION.get(userToken);
        if (encryptionKey == null) {
            ctx.redirect("/manager");
            return;
        }

        database.saveSafeEntry(entryID, website, login, password, userID, encryptionKey);

        ctx.sessionAttribute("entryID", null);
        ctx.redirect("/manager");
    }

    private ManagerController() {}

}
