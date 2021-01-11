package me.kamilki.safepass;

import io.javalin.Javalin;
import me.kamilki.safepass.controller.*;
import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.dao.mysql.MySQLDatabase;

import java.util.HashMap;
import java.util.Map;

// user: safepass
// pass: dd*AkUXxH@997T!L!BwMBw6G4

public final class SafePass {

    public static final Map<String, Integer> USER_SESSIONS = new HashMap<>();
    public static final Map<String, byte[]> USER_ENCRYPTION = new HashMap<>();
    public static final Map<String, String> CSRF_PROTECTION = new HashMap<>();

    public static void main(final String[] args) {
        final Javalin app = Javalin.create(config -> config.addStaticFiles("/static")).start(8080);
        final Database database = new MySQLDatabase("127.0.0.1", "3306", "safepass", "false", "safepass", "dd*AkUXxH@997T!L!BwMBw6G4");

        app.get("/", ctx -> ctx.render("/html/index.html"));

        app.get("/login", LoginController::serveLoginPage);
        app.post("/login", ctx -> LoginController.handleLoginPost(ctx, database));
        app.get("/logout", LoginController::handleLogout);

        app.get("/register", RegisterController::serveRegisterPage);
        app.post("/register", ctx -> RegisterController.handleRegisterPost(ctx, database));

        app.get("/verify", ctx -> VerificationController.handleVerify(ctx, database));

        app.get("/requestRestore", ctx -> PasswordController.servePasswordChangeRequestPage(ctx, database));
        app.post("/requestRestore", ctx -> PasswordController.handlePasswordChangeRequestPost(ctx, database));

        app.get("/restorePassword", ctx -> PasswordController.servePasswordChangePage(ctx, database));
        app.post("/restorePassword", ctx -> PasswordController.handlePasswordChangePost(ctx, database));

        app.before("/loginHistory", LoginController::checkLogin);
        app.get("/loginHistory", ctx -> HistoryController.serveHistoryPage(ctx, database));

        app.before("/manager", LoginController::checkLogin);
        app.get("/manager", ctx -> ManagerController.serveManagerPage(ctx, database));

        app.before("/getPassword", LoginController::checkLogin);
        app.get("/getPassword", ctx -> PasswordController.getPassword(ctx, database));

        app.before("/editEntry", LoginController::checkLogin);
        app.get("/editEntry", ctx -> ManagerController.serveEditPage(ctx, database));
        app.post("/editEntry", ctx -> ManagerController.handleEditPost(ctx, database));

        app.error(404, ctx -> ctx.redirect("/"));
    }

    private SafePass() {}

}
