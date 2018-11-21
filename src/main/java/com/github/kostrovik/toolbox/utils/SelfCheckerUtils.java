package com.github.kostrovik.toolbox.utils;

import com.github.kostrovik.toolbox.models.Version;
import com.github.kostrovik.useful.utils.FileSystemUtil;
import com.github.kostrovik.useful.utils.InstanceLocatorUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class SelfCheckerUtils {
    private static Logger logger = InstanceLocatorUtil.getLocator().getLogger(SelfCheckerUtils.class.getName());
    private FileSystemUtil fsUtil;

    public SelfCheckerUtils() {
        this.fsUtil = new FileSystemUtil();
    }

    /**
     * Ищет в корне приложения файл с версией. В случае если такой файл найден то читает из него первую строку.
     * Если строка содержит версию в нужном формате то сравнивает ее с той что получена от сервера. Возвращает
     * флаг соответсвия версии клиента и сервера.
     * <p>
     * Во всех случаях когда не возможно прочитать файл с версией либо разобрать версию из строки, возвращает FALSE.
     *
     * @param actualVersion the actual version
     *
     * @return the boolean
     */
    public boolean amIActual(Version actualVersion) {
        Path versionFile = getVersionFilePath();
        if (Objects.nonNull(versionFile) && Files.exists(versionFile)) {
            try (BufferedReader reader = Files.newBufferedReader(versionFile, Charset.forName("UTF-8"))) {
                String versionLine = reader.readLine();
                if (Objects.nonNull(versionLine) && !versionLine.trim().isEmpty()) {
                    Version version = Version.parseVersion(versionLine);
                    return version.compareTo(actualVersion) == 0;
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ошибка чтения файла версии.", e);
            }
        }
        return false;
    }

    private Path getVersionFilePath() {
        Path fileDirectory = fsUtil.getCurrentDirectory(SelfCheckerUtils.class);
        return Paths.get(fileDirectory.toString(), "version.txt");
    }
}
