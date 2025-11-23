package com.theyausebox.yause.client;

/**
 * Client-side cache for the latest server-sent playtime value (ticks).
 */
public class ClientPlaytimeCache {
    private static volatile long playTicks = -1L;
    private static volatile long lastReadMs = -1L;

    public static void setPlayTicks(long ticks, long receiveMs) {
        playTicks = ticks;
        lastReadMs = receiveMs;
    }

    public static long getPlayTicks() { return playTicks; }
    public static long getLastReadMs() { return lastReadMs; }
    public static boolean hasPlaytime() { return playTicks >= 0L; }

    public static void clear() {
        playTicks = -1L;
        lastReadMs = -1L;
    }

    private ClientPlaytimeCache() {}
}
