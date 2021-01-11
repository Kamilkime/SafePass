package me.kamilki.safepass.controller;

import io.javalin.http.Context;
import me.kamilki.safepass.SafePass;
import me.kamilki.safepass.dao.Database;

import java.util.HashMap;
import java.util.Map;

public final class HistoryController {

    public static void serveHistoryPage(final Context ctx, final Database database) {
        final String userToken = ctx.sessionAttribute("userToken");
        final int userID = SafePass.USER_SESSIONS.get(userToken);

        final Map<String, Object> model = new HashMap<>();
        model.put("username", database.getUsername(userID));
        model.put("entries", database.getLoginHistory(userID));

        ctx.render("/html/loginHistory.html", model);
    }

    private HistoryController() {}

}
