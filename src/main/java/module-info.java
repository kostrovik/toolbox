module com.github.kostrovik.toolbox {
    requires java.logging;
    requires com.github.kostrovik.http.client;
    requires com.github.kostrovik.useful.utils;

    exports com.github.kostrovik.toolbox.common;
    exports com.github.kostrovik.toolbox.dictionaries;

    uses com.github.kostrovik.useful.interfaces.LoggerConfigInterface;

    provides com.github.kostrovik.useful.interfaces.LoggerConfigInterface with com.github.kostrovik.toolbox.utils.ApplicationLogger;
}