package com.github.kostrovik.toolbox.common;

import com.github.kostrovik.http.client.common.HttpClient;
import com.github.kostrovik.http.client.common.HttpClientAnswer;
import com.github.kostrovik.http.client.common.HttpRequest;
import com.github.kostrovik.http.client.interfaces.Listener;
import com.github.kostrovik.toolbox.interfaces.Callable;
import com.github.kostrovik.toolbox.models.Version;
import com.github.kostrovik.toolbox.utils.ApplicationLogger;
import com.github.kostrovik.toolbox.utils.DirectoryUtils;
import com.github.kostrovik.toolbox.utils.SelfCheckerUtils;
import com.github.kostrovik.toolbox.utils.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class LaunchUtils {
    private static Logger logger = ApplicationLogger.getLogger(LaunchUtils.class.getName());
    private static final String OS_TYPE = System.getProperty("os.name");
    private HttpClient httpClient;
    private Version actualVersion;
    private DirectoryUtils directoryUtils;
    private SelfCheckerUtils selfCheckerUtils;
    private ZipUtils zipUtils;
    private Path appDir;
    private String checkVersionApi;
    private String downloadVersionApi;

    public LaunchUtils(String updateServerAddress, String checkVersionApi, String downloadVersionApi) {
        this.httpClient = new HttpClient(updateServerAddress);
        this.directoryUtils = new DirectoryUtils();
        this.selfCheckerUtils = new SelfCheckerUtils();
        this.zipUtils = new ZipUtils();

        Objects.requireNonNull(checkVersionApi);
        this.checkVersionApi = checkVersionApi;

        Objects.requireNonNull(downloadVersionApi);
        this.downloadVersionApi = downloadVersionApi;
    }

    public void checkVersion(Callable callback) throws IOException {
        actualVersion = getActualVersion();
        boolean normalLaunch = false;
        try {
            normalLaunch = selfCheckerUtils.amIActual(actualVersion);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Ошибка проверки соответствия версии приложения.", e);
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
            callback.call();
        }
    }

    private void downloadVersion() {
        HttpRequest request = new HttpRequest(httpClient);
        try {
            Path appArchive = Paths.get(directoryUtils.getVersionsPath().toString(), "version_" + actualVersion.toString(), "app.zip");
            request.download(
                    new URL(httpClient.getServerAddress() + downloadVersionApi),
                    appArchive,
                    new Listener<>() {
                        @Override
                        public void handle(File result) {
                            try {
                                zipUtils.extract(
                                        new ZipInputStream(new FileInputStream(appArchive.toString())),
                                        appArchive.getParent()
                                );
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
                    }
            );
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Ошибка создания URL для загрузки файла.", e);
        }
    }

    private void launchApplication(Path versionPath) {
        String applicationLauncher = versionPath.toString();
        ProcessBuilder builder = new ProcessBuilder();

        logger.log(Level.INFO, "Путь к файлу запуска: {0}", applicationLauncher);
        try {
            if (isWindows()) {
                builder.command(applicationLauncher + "/launcher.exe");
                builder.directory(new File(applicationLauncher));
                builder.start();
            } else {
                directoryUtils.setPermissionsOnDirectory(Paths.get(versionPath.toString(), "bin"), "rwxr-xr-x");
                builder.command("sh", applicationLauncher + "/bin/ApplicationLauncher");
                builder.directory(new File(applicationLauncher + "/bin"));
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
        Path versionsPath = directoryUtils.getVersionsPath();
        if (Objects.nonNull(versionsPath)) {
            File versionsDirectory = new File(versionsPath.toUri());
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
    }
}