package com.github.kostrovik.toolbox.utils;

import com.github.kostrovik.useful.utils.LoggerConfigImpl;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class ApplicationLogger extends LoggerConfigImpl {
    @Override
    protected String prepareLogDirectoryName() {
        return "update_logs";
    }
}
