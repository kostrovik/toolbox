package com.github.kostrovik.toolbox.utils;

import com.github.kostrovik.toolbox.models.Version;
import com.github.kostrovik.useful.utils.FileSystemUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class DirectoryUtils {
    private Pattern versionDirectoryPattern = Pattern.compile("version_[a-zA-Z_0-9\\-.]+");
    private FileSystemUtil fsUtil;

    public DirectoryUtils() {
        this.fsUtil = new FileSystemUtil();
    }

    public boolean hasVersionName(String directoryName) {
        Matcher matcher = versionDirectoryPattern.matcher(directoryName);
        return matcher.matches();
    }

    public boolean matchVersionName(String directoryName, Version version) {
        Matcher matcher = versionDirectoryPattern.matcher(directoryName);
        boolean result = matcher.matches();

        String[] nameGroups = directoryName.split("version_");
        if (nameGroups.length < 2) {
            return false;
        }
        Version groupVersion = Version.parseVersion(nameGroups[1]);

        int test = groupVersion.compareTo(version);

        return result && test == 0;
    }

    public Path getVersionsPath() throws IOException {
        Path fileDirectory = fsUtil.getCurrentDirectory(DirectoryUtils.class);

        if (!fileDirectory.getParent().getFileName().toString().equalsIgnoreCase("versions")) {
            Path versionsPath = Paths.get(fileDirectory.toString(), "versions");
            fsUtil.createPath(versionsPath);
            return versionsPath;
        }

        return fileDirectory.getParent();
    }
}