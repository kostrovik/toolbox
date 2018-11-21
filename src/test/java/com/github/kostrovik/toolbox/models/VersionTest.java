package com.github.kostrovik.toolbox.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class VersionTest {
    @Test
    void parseVersionTest1() {
        String versionString = "0_1_2";
        Version version = Version.parseVersion(versionString);

        assertEquals(0, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());

        versionString = "20_1_2";
        version = Version.parseVersion(versionString);

        assertEquals(20, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());

        versionString = "0.1.2";
        version = Version.parseVersion(versionString);

        assertEquals(0, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());

        versionString = "20.1.2";
        version = Version.parseVersion(versionString);

        assertEquals(20, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());
    }

    @Test
    void parseVersionTest2() {
        String versionString = "1_1_2.4";
        Version version = Version.parseVersion(versionString);

        assertEquals(1, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());

        versionString = "1.1.2.4";
        version = Version.parseVersion(versionString);

        assertEquals(1, version.getMajor());
        assertEquals(1, version.getMinor());
        assertEquals(2, version.getPatch());
    }

    @Test
    void parseWrongVersionTest() {
        String versionString = "1_1a_2";

        Executable tester = () -> Version.parseVersion(versionString);

        assertThrows(IllegalArgumentException.class, tester, "Не верный формат номера версии.");

        String versionString1 = "1.1a.2";

        Executable tester1 = () -> Version.parseVersion(versionString1);

        assertThrows(IllegalArgumentException.class, tester1, "Не верный формат номера версии.");
    }

    @Test
    void compareVersionTest() {
        String lowVersionString = "1_1_2";
        String heightVersionString = "1_1_3";

        Version lowVersion = Version.parseVersion(lowVersionString);
        Version heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1_1_2";
        heightVersionString = "1_2_2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1_1_2";
        heightVersionString = "2_1_2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1_1_2";
        heightVersionString = "1_1_2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(0, heightVersion.compareTo(lowVersion));

        lowVersionString = "1.1.2";
        heightVersionString = "1.1.3";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1.1.2";
        heightVersionString = "1.2.2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1.1.2";
        heightVersionString = "2.1.2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(1, heightVersion.compareTo(lowVersion));
        assertEquals(-1, lowVersion.compareTo(heightVersion));

        lowVersionString = "1.1.2";
        heightVersionString = "1.1.2";

        lowVersion = Version.parseVersion(lowVersionString);
        heightVersion = Version.parseVersion(heightVersionString);

        assertEquals(0, heightVersion.compareTo(lowVersion));
    }
}