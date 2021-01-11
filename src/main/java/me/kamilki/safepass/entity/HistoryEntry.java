package me.kamilki.safepass.entity;

import java.text.SimpleDateFormat;

public final class HistoryEntry {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");

    private final String ip;
    private final String browser;
    private final String time;
    private final long pureTime;

    public HistoryEntry(final String ip, final String browser, final long time) {
        this.ip = ip;
        this.browser = browser;
        this.time = DATE_FORMAT.format(time);
        this.pureTime = time;
    }

    public String getIp() {
        return this.ip;
    }

    public String getBrowser() {
        return this.browser;
    }

    public String getTime() {
        return this.time;
    }

    public long getPureTime() {
        return this.pureTime;
    }

}
