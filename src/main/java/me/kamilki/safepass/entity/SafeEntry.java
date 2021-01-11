package me.kamilki.safepass.entity;

public final class SafeEntry {

    private final String id;
    private final String login;
    private final String password;
    private final String website;
    private final int userID;

    public SafeEntry(final String id, final String login, final String password, final String website, final int userID) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.website = website;
        this.userID = userID;
    }

    public String getId() {
        return this.id;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public String getWebsite() {
        return this.website;
    }

    public int getUserID() {
        return this.userID;
    }

}