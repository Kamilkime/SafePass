package me.kamilki.safepass.dao.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class MySQLConnection {

    private final HikariDataSource dataSource;

    public MySQLConnection() {
        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" +
                System.getenv("DB_NAME") + "?useSSL=false");
        hikariConfig.setUsername(System.getenv("DB_USER"));
        hikariConfig.setPassword(System.getenv("DB_PASSWORD"));

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);
        hikariConfig.addDataSourceProperty("tcpKeepAlive", true);

        hikariConfig.setLeakDetectionThreshold(60000L);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(0);
        hikariConfig.setIdleTimeout(30000L);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private Optional<Connection> getConnection() {
        try {
            return Optional.of(this.dataSource.getConnection());
        } catch (final SQLException exception) {
            return Optional.empty();
        }
    }

    public Optional<ResultSet> query(final String query, final Object... queryReplacements) {
        final Optional<Connection> connection = this.getConnection();
        if (!connection.isPresent()) {
            return Optional.empty();
        }

        try (final PreparedStatement statement = connection.get().prepareStatement(query)) {
            for (int i = 1; i <= queryReplacements.length; i++) {
                statement.setObject(i, queryReplacements[i - 1]);
            }

            return Optional.of(statement.executeQuery());
        } catch (final SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                connection.get().close();
            } catch (final SQLException ignored) {}
        }
    }

    public int update(final String update, final Object... updateReplacements) {
        final Optional<Connection> connection = this.getConnection();
        if (!connection.isPresent()) {
            return -1;
        }

        try (final PreparedStatement statement = connection.get().prepareStatement(update)) {
            for (int i = 1; i <= updateReplacements.length; i++) {
                statement.setObject(i, updateReplacements[i - 1]);
            }

            return statement.executeUpdate();
        } catch (final SQLException exception) {
            return -1;
        } finally {
            try {
                connection.get().close();
            } catch (final SQLException ignored) {}
        }
    }

}
