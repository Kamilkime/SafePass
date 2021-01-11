package me.kamilki.safepass.dao.mysql;

import me.kamilki.safepass.dao.Database;
import me.kamilki.safepass.entity.HistoryEntry;
import me.kamilki.safepass.entity.SafeEntry;
import me.kamilki.safepass.entity.User;
import me.kamilki.safepass.util.EncryptionUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class MySQLDatabase implements Database {

    private final MySQLConnection mySQLConnection;

    public MySQLDatabase(final String... config) {
        this.mySQLConnection = new MySQLConnection(config);
        MySQLCreator.prepareTables(this.mySQLConnection);
    }

    @Override
    public Optional<User> getUser(final String username) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT id, password, verified FROM users WHERE " +
                "username = ?;", username);
        if (!resultSetOptional.isPresent()) {
            return Optional.empty();
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            final User user = new User(resultSet.getInt("id"), username, resultSet.getString("password"), resultSet.getBoolean("verified"));
            return Optional.of(user);
        } catch (final SQLException exception) {
            return Optional.empty();
        }
    }

    @Override
    public String getUsername(final int userID) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT username FROM users WHERE id = ?;", userID);
        if (!resultSetOptional.isPresent()) {
            return "Error fetching username";
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return "Error fetching username";
            }

            return resultSet.getString("username");
        } catch (final SQLException exception) {
            return "Error fetching username";
        }
    }

    @Override
    public int getUserID(final String username) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT id FROM users WHERE username = ?;", username);
        if (!resultSetOptional.isPresent()) {
            return -1;
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return -1;
            }

            return resultSet.getInt("id");
        } catch (final SQLException exception) {
            return -1;
        }
    }

    @Override
    public int getUserID(final String token, final String table) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query(("SELECT users.id FROM {TABLE} JOIN users ON " +
                "({TABLE}.user_id = users.id) WHERE {TABLE}.token = ?;").replace("{TABLE}", table), token);
        if (!resultSetOptional.isPresent()) {
            return -1;
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return -1;
            }

            return resultSet.getInt("id");
        } catch (final SQLException exception) {
            return -1;
        }
    }

    @Override
    public boolean saveUser(final String username, final String password, final String verificationToken) {
        int result = this.mySQLConnection.update("INSERT INTO users VALUES (NULL, ?, ?, 0);", username, password);
        if (result == -1) {
            return false;
        }

        result = this.mySQLConnection.update("INSERT INTO verifications VALUES (?, ?);", this.getUserID(username), verificationToken);
        return result != -1;
    }

    @Override
    public boolean addLoginHistory(final int userID, final String ip, final String browser, final long time) {
        return this.mySQLConnection.update("INSERT INTO history VALUES (NULL, ?, ?, ?, ?);", userID, ip, browser, time) != -1;
    }

    @Override
    public List<HistoryEntry> getLoginHistory(final int userID) {
        final List<HistoryEntry> entries = new ArrayList<>();

        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT * FROM history WHERE user_id = ?;", userID);
        if (!resultSetOptional.isPresent()) {
            return entries;
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            while (resultSet.next()) {
                entries.add(new HistoryEntry(resultSet.getString("ip"), resultSet.getString("browser"), resultSet.getLong("time")));
            }
        } catch (final SQLException exception) {
            return entries;
        }

        entries.sort(Comparator.comparingLong(HistoryEntry::getPureTime).reversed());
        return entries;
    }

    @Override
    public void verifyUser(final int userID) {
        this.mySQLConnection.update("UPDATE users SET verified = ? WHERE id = ?;", true, userID);
        this.mySQLConnection.update("DELETE FROM verifications WHERE user_id = ?;", userID);
    }

    @Override
    public boolean verificationTokenExists(final String verificationToken) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT * FROM verifications WHERE token = ?;", verificationToken);
        if (!resultSetOptional.isPresent()) {
            return true;
        }

        try {
            return resultSetOptional.get().next();
        } catch (final SQLException exception) {
            return true;
        }
    }

    @Override
    public void addPasswordRestore(final int userID, final String restoreToken) {
        this.mySQLConnection.update("INSERT INTO pass_resets VALUES (?, ?) ON DUPLICATE KEY UPDATE token = ?;", userID, restoreToken,
                restoreToken);
    }

    @Override
    public boolean restoreTokenExists(final String restoreToken) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT * FROM pass_resets WHERE token = ?;", restoreToken);
        if (!resultSetOptional.isPresent()) {
            return true;
        }

        try {
            return resultSetOptional.get().next();
        } catch (final SQLException exception) {
            return true;
        }
    }

    @Override
    public void changePassword(final int userID, final String password) {
        this.mySQLConnection.update("UPDATE users SET password = ? WHERE id = ?;", password, userID);
        this.mySQLConnection.update("DELETE FROM pass_resets WHERE user_id = ?;", userID);
        this.mySQLConnection.update("DELETE FROM safe_entries WHERE user_id = ?;", userID);
    }

    @Override
    public boolean safeEntryExists(final String entryID) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT user_id FROM safe_entries WHERE id = ?;", entryID);
        if (!resultSetOptional.isPresent()) {
            return true;
        }

        try {
            return resultSetOptional.get().next();
        } catch (final SQLException exception) {
            return true;
        }
    }

    @Override
    public String getEncryptedPassword(final String entryID, final int userID) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT password FROM safe_entries WHERE id = ? " +
                "AND user_id = ?;", entryID, userID);
        if (!resultSetOptional.isPresent()) {
            return "";
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return "";
            }

            return resultSet.getString("password");
        } catch (final SQLException exception) {
            exception.printStackTrace();
            return "";
        }
    }

    @Override
    public Optional<SafeEntry> getSafeEntry(final String entryID) {
        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT * FROM safe_entries WHERE id = ?;", entryID);
        if (!resultSetOptional.isPresent()) {
            return Optional.empty();
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(new SafeEntry(resultSet.getString("id"), resultSet.getString("login"), resultSet.getString("password"),
                    resultSet.getString("website"), resultSet.getInt("user_id")));
        } catch (final SQLException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<SafeEntry> getUserSafeEntries(final int userID, final byte[] decryptionKey) {
        final List<SafeEntry> entries = new ArrayList<>();

        if (decryptionKey == null) {
            return entries;
        }

        final Optional<ResultSet> resultSetOptional = this.mySQLConnection.query("SELECT * FROM safe_entries WHERE user_id = ?;", userID);
        if (!resultSetOptional.isPresent()) {
            return entries;
        }

        try (final ResultSet resultSet = resultSetOptional.get()) {
            while (resultSet.next()) {
                final String login = EncryptionUtil.decrypt(resultSet.getString("login"), decryptionKey);
                final String website = EncryptionUtil.decrypt(resultSet.getString("website"), decryptionKey);

                entries.add(new SafeEntry(resultSet.getString("id"), login, "", website, -1));
            }
        } catch (final SQLException exception) {
            return entries;
        }

        return entries;
    }

    @Override
    public void saveSafeEntry(final String entryID, final String website, final String login, final String password, final int userID,
                       final byte[] encryptionKey) {
        final String encryptedWebsite = EncryptionUtil.encrypt(website, encryptionKey);
        final String encryptedLogin = EncryptionUtil.encrypt(login, encryptionKey);
        final String encryptedPassword = EncryptionUtil.encrypt(password, encryptionKey);

        this.mySQLConnection.update("INSERT INTO safe_entries VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE login = ?, password = ?," +
                " website = ?, user_id = ?;", entryID, encryptedLogin, encryptedPassword, encryptedWebsite, userID, encryptedLogin,
                encryptedPassword, encryptedWebsite, userID);
    }

}
