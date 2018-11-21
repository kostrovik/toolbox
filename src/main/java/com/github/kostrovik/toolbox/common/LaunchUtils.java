package com.github.kostrovik.toolbox.common;

import com.github.kostrovik.http.client.common.HttpClient;
import com.github.kostrovik.http.client.common.HttpClientAnswer;
import com.github.kostrovik.http.client.common.HttpRequest;
import com.github.kostrovik.toolbox.dictionaries.LaunchEvents;
import com.github.kostrovik.toolbox.models.Version;
import com.github.kostrovik.toolbox.utils.DirectoryUtils;
import com.github.kostrovik.toolbox.utils.SelfCheckerUtils;
import com.github.kostrovik.useful.interfaces.Callable;
import com.github.kostrovik.useful.interfaces.Listener;
import com.github.kostrovik.useful.utils.FileSystemUtil;
import com.github.kostrovik.useful.utils.InstanceLocatorUtil;
import com.github.kostrovik.useful.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class LaunchUtils {
    private static Logger logger = InstanceLocatorUtil.getLocator().getLogger(LaunchUtils.class.getName());
    private static final String OS_TYPE = System.getProperty("os.name");
    private HttpClient httpClient;
    private Version actualVersion;
    private DirectoryUtils directoryUtils;
    private FileSystemUtil fsUtil;
    private SelfCheckerUtils selfCheckerUtils;
    private ZipUtil zipUtils;
    private Path appDir;
    private String checkVersionApi;
    private String downloadVersionApi;

    private Map<LaunchEvents, Callable<EventObject>> events;

    public LaunchUtils(String updateServerAddress, String checkVersionApi, String downloadVersionApi) {
        this.httpClient = new HttpClient(updateServerAddress);
        this.directoryUtils = new DirectoryUtils();
        this.fsUtil = new FileSystemUtil();
        this.selfCheckerUtils = new SelfCheckerUtils();
        this.zipUtils = new ZipUtil();

        Objects.requireNonNull(checkVersionApi);
        this.checkVersionApi = checkVersionApi;

        Objects.requireNonNull(downloadVersionApi);
        this.downloadVersionApi = downloadVersionApi;

        this.events = new HashMap<>();
    }

    public void setEventCallback(LaunchEvents event, Callable<EventObject> callback) {
        if (Objects.nonNull(callback)) {
            events.put(event, callback);
        }
    }

    public void checkVersion(Callable<EventObject> callback) throws IOException {
        if (events.containsKey(LaunchEvents.BEFORE_CHECK_VERSION)) {
            events.get(LaunchEvents.BEFORE_CHECK_VERSION).call(new EventObject("Получение актуальной версии."));
        }
        actualVersion = getActualVersion();

        boolean normalLaunch = false;
        normalLaunch = selfCheckerUtils.amIActual(actualVersion);
        if (events.containsKey(LaunchEvents.AFTER_CHECK_VERSION)) {
            String checkResult = normalLaunch ? "Версия актуальна." : "Версия не поддерживается.";
            events.get(LaunchEvents.AFTER_CHECK_VERSION).call(new EventObject(checkResult));
        }

        if (!normalLaunch) {
            findVersionDirectory();
            if (Objects.isNull(appDir)) {
                downloadVersion();
            } else {
                launchApplication(appDir);
                System.exit(0);
            }
        } else {
            callback.call(new EventObject("Запуск приложения"));
        }
    }

    private void downloadVersion() {
        if (events.containsKey(LaunchEvents.BEFORE_DOWNLOAD_VERSION)) {
            events.get(LaunchEvents.BEFORE_DOWNLOAD_VERSION).call(new EventObject("Загрузка необходимой версии."));
        }

        HttpRequest request = new HttpRequest(httpClient);
        try {
            Path appArchive = Paths.get(directoryUtils.getVersionsPath().toString(), "version_" + actualVersion.toString(), "app.zip");

            if (events.containsKey(LaunchEvents.DOWNLOAD_PROGRESS)) {
                request.downloadWithProgressListener(
                        new URL(httpClient.getServerAddress() + downloadVersionApi),
                        appArchive,
                        getDownloadListener(appArchive),
                        new Listener<>() {
                            @Override
                            public void handle(Double result) {
                                events.get(LaunchEvents.DOWNLOAD_PROGRESS).call(new EventObject(result));
                            }

                            @Override
                            public void error(Throwable error) {
                                logger.log(Level.SEVERE, "Ошибка загрузки файла.", error);
                            }
                        }
                );
            } else {
                request.download(
                        new URL(httpClient.getServerAddress() + downloadVersionApi),
                        appArchive,
                        getDownloadListener(appArchive)
                );
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Ошибка создания URL для загрузки файла.", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка доступа к директории с версиями.", e);
        }
    }

    private Listener<File> getDownloadListener(Path appArchive) {
        return new Listener<>() {
            @Override
            public void handle(File result) {
                try {
                    if (events.containsKey(LaunchEvents.AFTER_DOWNLOAD_VERSION)) {
                        events.get(LaunchEvents.AFTER_DOWNLOAD_VERSION).call(new EventObject("Архив приложения загружен."));
                    }

                    if (events.containsKey(LaunchEvents.BEFORE_UNZIP_VERSION)) {
                        events.get(LaunchEvents.BEFORE_UNZIP_VERSION).call(new EventObject("Распаковка архива."));
                    }

                    zipUtils.setListener(new Listener<>() {
                        @Override
                        public void handle(String result) {
                            if (events.containsKey(LaunchEvents.UNZIP_PROGRESS)) {
                                events.get(LaunchEvents.UNZIP_PROGRESS).call(new EventObject(result));
                            }
                        }

                        @Override
                        public void error(Throwable error) {
                            logger.log(Level.SEVERE, "Ошибка распаковки архива.", error);
                        }
                    });

                    zipUtils.extract(appArchive, appArchive.getParent());

                    if (events.containsKey(LaunchEvents.AFTER_UNZIP_VERSION)) {
                        events.get(LaunchEvents.AFTER_UNZIP_VERSION).call(new EventObject("Архив распакован."));
                    }

                    launchApplication(appArchive.getParent());
                    Files.deleteIfExists(appArchive);
                    System.exit(0);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка распаковки архива.", e);
                }
            }

            @Override
            public void error(Throwable error) {
                logger.log(Level.SEVERE, "Ошибка загрузки файла.", error);
            }
        };
    }

    private void launchApplication(Path versionPath) {
        String applicationLauncher = versionPath.toString();
        ProcessBuilder builder = new ProcessBuilder();

        logger.log(Level.INFO, "Путь к файлу запуска: {0}", applicationLauncher);
        try {
            if (isWindows()) {
                builder.command(applicationLauncher + File.separator + "launcher.exe");
                builder.directory(new File(applicationLauncher));
                builder.start();
            } else {
                fsUtil.setPermissionsOnDirectory(Paths.get(versionPath.toString(), "bin"), "rwxr-xr-x");
                builder.command("sh", applicationLauncher + File.separator + "bin" + File.separator + "ApplicationLauncher");
                builder.directory(new File(applicationLauncher + File.separator + "bin"));
                builder.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка запуска приложения.", e);
        }
    }

    private boolean isWindows() {
        return OS_TYPE.toLowerCase().startsWith("windows");
    }

    private Version getActualVersion() throws IOException {
        HttpRequest request = new HttpRequest(httpClient);
        request.GET(checkVersionApi);
        try {
            request.build();
            HttpClientAnswer answer = request.getResult();

            Map<String, String> details = (Map<String, String>) answer.getDetails();
            return Version.parseVersion(details.get("version"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка проверки версии.", e);
            throw e;
        }
    }

    private void findVersionDirectory() {
        try {
            Path versionsPath = directoryUtils.getVersionsPath();
            if (Objects.nonNull(versionsPath)) {
                File versionsDirectory = new File(versionsPath.toString());
                try (Stream<Path> files = Files.list(Paths.get(versionsDirectory.getAbsolutePath()))) {
                    files.forEach(file -> {
                        if (directoryUtils.matchVersionName(file.getFileName().toString(), actualVersion)) {
                            appDir = file;
                        }
                    });
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка чтения директории.", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения директории.", e);
        }
    }
}