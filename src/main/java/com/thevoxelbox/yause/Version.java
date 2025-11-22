package com.thevoxelbox.yause;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static final Pattern versionPattern = Pattern.compile("^([0-9]+)(\\.([0-9]+))?(\\.([0-9]+))?$");
    private int value = 0;
    private int major = 0;
    private int minor = 0;
    private int revision = 0;

    public Version() {
    }

    public Version(int major, int minor, int revision) {
        this.setValue(major, minor, revision);
    }

    public Version(String version) {
        Matcher versionMatcher = versionPattern.matcher(version.trim());
        if (versionMatcher.matches()) {
            int major = Integer.parseInt(versionMatcher.group(1));
            int minor = versionMatcher.group(3) != null ? Integer.parseInt(versionMatcher.group(3)) : 0;
            int revision = versionMatcher.group(5) != null ? Integer.parseInt(versionMatcher.group(5)) : 0;
            this.setValue(major, minor, revision);
        }
    }

    private void setValue(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.value = (this.major << 16) + (this.minor << 8) + this.revision;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", this.major, this.minor, this.revision);
    }

    public boolean isGreaterThan(Version other) {
        return this.value > other.value;
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        return this.value - other.value;
    }
}
