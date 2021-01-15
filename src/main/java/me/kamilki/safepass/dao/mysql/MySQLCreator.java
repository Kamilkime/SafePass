package me.kamilki.safepass.dao.mysql;

public final class MySQLCreator {

    public static void prepareTables(final MySQLConnection connection) {
        connection.update("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username TEXT NOT NULL, " +
                "password TEXT NOT NULL, verified TINYINT(1) NOT NULL DEFAULT 0) ENGINE = INNODB;");
        connection.update("CREATE TABLE IF NOT EXISTS verifications (user_id INT PRIMARY KEY, token VARCHAR(64) NOT NULL) ENGINE = INNODB;");
        connection.update("CREATE TABLE IF NOT EXISTS pass_resets (user_id INT PRIMARY KEY, token VARCHAR(64) NOT NULL, " +
                "expires BIGINT NOT NULL) ENGINE = INNODB;");
        connection.update("CREATE TABLE IF NOT EXISTS safe_entries (id VARCHAR(20) PRIMARY KEY, login TEXT NOT NULL, password TEXT " +
                "NOT NULL, website TEXT NOT NULL, user_id INT NOT NULL) ENGINE = INNODB;");
        connection.update("CREATE TABLE IF NOT EXISTS history (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, ip VARCHAR(15) " +
                "NOT NULL, browser TEXT NOT NULL, time BIGINT NOT NULL) ENGINE = INNODB;");
    }

    private MySQLCreator() {}

}
