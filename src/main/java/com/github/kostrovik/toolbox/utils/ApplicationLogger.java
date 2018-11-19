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
    private static volatile FileHandler fileHandler;

    private ApplicationLogger() {

    }

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        try {
            createLoggerFile();
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.SEVERE, "Ошибка создания файла для логирования.", e);
        }


        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(fileHandler);

        return logger;
    }

    private static void createLoggerFile() throws IOException, URISyntaxException {
        if (fileHandler == null) {
            synchronized (ApplicationLogger.class) {
                if (fileHandler == null) {
                    URI applicationDirectory = ApplicationLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI();

                    if (Paths.get(applicationDirectory).getParent().toString().equals("/")) {
                        applicationDirectory = URI.create(System.getProperty("java.home"));
                    } else {
                        applicationDirectory = Paths.get(applicationDirectory).getParent().toUri();
                    }

                    Path logPath = Paths.get(applicationDirectory.getPath() + "/update_logs", String.format("%s.log", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

                    if (Files.notExists(logPath.getParent())) {
                        Files.createDirectory(logPath.getParent());
                    }

                    fileHandler = new FileHandler(logPath.toString(), true);
                    fileHandler.setLevel(Level.ALL);
                    fileHandler.setFormatter(new LogMessageFormatter());
                }
            }
        }
    }
}
