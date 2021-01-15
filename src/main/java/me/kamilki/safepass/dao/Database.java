package me.kamilki.safepass.dao;

import me.kamilki.safepass.entity.HistoryEntry;
import me.kamilki.safepass.entity.SafeEntry;
import me.kamilki.safepass.entity.User;

import java.util.List;
import java.util.Optional;

public interface Database {

    Optional<User> getUser(final String username);
    String getUsername(final int userID);
    int getUserID(final String username);
    int getUserID(final String token, final String table);
    boolean saveUser(final String username, final String password, final String verificationToken);

    boolean addLoginHistory(final int userID, final String ip, final String browser, final long time);
    List<HistoryEntry> getLoginHistory(final int userID);

    void verifyUser(final int userID);
    boolean verificationTokenExists(final String verificationToken);

    void addPasswordReset(final int userID, final String resetToken, final long expiryTime);
    void removePasswordReset(final String resetToken);
    boolean resetTokenExists(final String resetToken);
    boolean resetTokenExpired(final String resetToken);
    void changePassword(final int userID, final String password);

    boolean safeEntryExists(final String entryID);
    String getEncryptedPassword(final String entryID, final int userID);
    Optional<SafeEntry> getSafeEntry(final String entryID);
    List<SafeEntry> getUserSafeEntries(final int userID, final byte[] decryptionKey);
    void saveSafeEntry(final String entryID, final String website, final String login, final String password, final int userID,
                       final byte[] encryptionKey);


}
