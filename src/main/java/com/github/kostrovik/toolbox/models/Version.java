package com.github.kostrovik.toolbox.models;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class Version implements Comparable<Version> {
    private int major;
    private int minor;
    private int patch;
    private static final Pattern versionPattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(.*)*");

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static Version parseVersion(String version) {
        Matcher matcher = versionPattern.matcher(version);
        if (matcher.matches()) {
            return new Version(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        }
        throw new IllegalArgumentException("Не верный формат номера версии.");
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(Version o) {
        if (o.getMajor() > getMajor()) {
            return -1;
        }

        int result = 0;
        if (o.getMajor() < getMajor()) {
            result = 1;
        }

        if (o.getMinor() > getMinor()) {
            return -1;
        }

        if (o.getMinor() < getMinor()) {
            result = 1;
        }

        if (o.getPatch() > getPatch()) {
            return -1;
        }
        if (o.getPatch() < getPatch()) {
            return 1;
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return major == version.major &&
                minor == version.minor &&
                patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
