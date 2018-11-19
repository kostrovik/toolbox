package com.github.kostrovik.toolbox.formatters;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class LogMessageFormatter extends SimpleFormatter {
    @Override
    public String format(LogRecord record) {
        String result = super.format(record);
        result = result + "\n";
        return result;
    }
}
