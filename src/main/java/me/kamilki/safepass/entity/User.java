package me.kamilki.safepass.entity;

public class User {

    private final int id;
    private final String username;
    private final String hashedPassword;
    private final boolean verified;

    public User(final int id, final String username, final String hashedPassword, final boolean verified) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.verified = verified;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public boolean isVerified() {
        return this.verified;
    }

}
