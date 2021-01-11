package me.kamilki.safepass.util;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public final class ModelUtil {

    public static Map<String, Object> createModel(final Context ctx, final String... attributes) {
        final Map<String, Object> model = new HashMap<>();

        for (final String attribute : attributes) {
            model.put(attribute, ctx.sessionAttribute(attribute));
            ctx.sessionAttribute(attribute, null);
        }

        return model;
    }

    private ModelUtil() {}

}
