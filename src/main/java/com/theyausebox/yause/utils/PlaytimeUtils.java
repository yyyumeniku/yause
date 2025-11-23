package com.theyausebox.yause.utils;

import java.util.Date;

/**
 * Utility helpers for converting Minecraft ticks to time/date and formatting
 * human-friendly playtime strings. Adapted from ideas in Simple-World-Timer
 * (foxahead/Simple-World-Timer) but simplified for the project's needs.
 */
public final class PlaytimeUtils {
    private PlaytimeUtils() {}

    // Convert Minecraft ticks (1 tick = 50ms) into a java.util.Date.
    public static Date convertTicksToDate(long ticks) {
        return new Date(ticks * 50L);
    }

    // Convert ticks to seconds (ticks are game ticks, often 20 ticks = 1 second for many stats)
    public static long ticksToSeconds(long ticks) {
        return ticks / 20L;
    }

    // Format a duration given in seconds into a short human-friendly string.
    // e.g. 93785 -> "1d 2h 3m 5s"
    public static String formatDurationSeconds(long seconds) {
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
            if (hours > 0) sb.append(' ').append(hours).append("h");
            if (minutes > 0) sb.append(' ').append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else if (hours > 0) {
            sb.append(hours).append("h");
            if (minutes > 0) sb.append(' ').append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else if (minutes > 0) {
            sb.append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else {
            sb.append(secs).append("s");
        }

        return sb.toString();
    }

    // Convenience wrapper: format ticks directly into a human-friendly string.
    public static String formatPlaytimeFromTicks(long ticks) {
        long seconds = ticksToSeconds(ticks);
        return formatDurationSeconds(seconds);
    }
}
