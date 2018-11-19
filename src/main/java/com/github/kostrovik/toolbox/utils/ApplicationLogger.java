package com.github.kostrovik.toolbox.utils;

import com.github.kostrovik.toolbox.formatters.LogMessageFormatter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class ApplicationLogger {
    private static volatile FileHandler FILE_HANDLER;

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        createLoggerFile();

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(FILE_HANDLER);

        return logger;
    }

    private static void createLoggerFile() {
        if (FILE_HANDLER == null) {
            synchronized (ApplicationLogger.class) {
                if (FILE_HANDLER == null) {
                    try {
                        URI applicationDirectory = ApplicationLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI();

                        if (Paths.get(applicationDirectory).getParent().toString().equals("/")) {
                            applicationDirectory = URI.create(System.getProperty("java.home"));
                        }

                        Path logPath = Paths.get(applicationDirectory.getPath() + "/logs", String.format("%s.log", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

                        if (Files.notExists(logPath.getParent())) {
                            Files.createDirectory(logPath.getParent());
                        }

                        FILE_HANDLER = new FileHandler(logPath.toString(), true);
                        FILE_HANDLER.setLevel(Level.ALL);
                        FILE_HANDLER.setFormatter(new LogMessageFormatter());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
