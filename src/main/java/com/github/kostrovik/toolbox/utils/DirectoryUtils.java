package com.github.kostrovik.toolbox.utils;

import com.github.kostrovik.toolbox.models.Version;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class DirectoryUtils {
    private static Logger logger = ApplicationLogger.getLogger(DirectoryUtils.class.getName());
    private Pattern versionDirectoryPattern = Pattern.compile("version_[a-zA-Z_0-9\\-.]+");

    public void copyDirectory(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                logger.log(Level.INFO, "Создание раздела. {0}", destination);
                boolean created = destination.mkdir();
                if (created) {
                    logger.log(Level.INFO, "Раздел создан.");
                } else {
                    logger.log(Level.SEVERE, "Раздел не создан.");
                }
            }

            String[] files = source.list();
            if (Objects.nonNull(files)) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyDirectory(srcFile, destFile);
                }
            }
        } else {
            try {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.log(Level.INFO, "Файл скопирован. {0}", destination);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Файл не скопирован. ", e);
            }
        }
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

    public void removeTempDirectories(Path root) {
        try (Stream<Path> testDir = Files.list(root)) {
            testDir.forEach(path -> {
                if (hasVersionName(path.toString())) {
                    removeDirectories(path);
                }
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка удаления директории файла. ", e);
        }
    }

    private void removeDirectories(Path directory) {
        if (Objects.nonNull(directory)) {
            try (Stream<Path> filesStream = Files.walk(directory)) {
                filesStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ошибка удаления директории или файла. ", e);
            }
        }
    }

    public Path getVersionsPath() {
        try {
            URI fileDirectory = DirectoryUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path rootPath;
            if (Paths.get(fileDirectory).getParent().toString().equals("/")) {
                fileDirectory = URI.create(System.getProperty("java.home"));
                rootPath = Paths.get(fileDirectory.getPath());
            } else {
                rootPath = Paths.get(fileDirectory.getPath()).getParent();
            }

            return rootPath.getParent();
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Ошибка поиска директории с версиями приложения.", e);
        }
        return null;
    }

    public void setPermissionsOnDirectory(Path directory, String pasixPermissions) {
        try (Stream<Path> testDir = Files.list(directory)) {
            testDir.forEach(path -> {
                try {
                    Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(pasixPermissions));
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка установки прав для файла. ", e);
                }
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения директории. ", e);
        }
    }
}