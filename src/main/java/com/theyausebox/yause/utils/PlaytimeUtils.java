package com.theyausebox.yause.utils;

import java.util.Date;

public final class PlaytimeUtils {
    private PlaytimeUtils() {}

    public static Date convertTicksToDate(long ticks) {
        return new Date(ticks * 50L);
    }

    public static long ticksToSeconds(long ticks) {
        return ticks / 20L;
    }

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

    public static String formatPlaytimeFromTicks(long ticks) {
        long seconds = ticksToSeconds(ticks);
        return formatDurationSeconds(seconds);
    }
}
