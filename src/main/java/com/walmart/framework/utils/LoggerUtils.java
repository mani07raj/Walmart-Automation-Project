package com.walmart.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LoggerUtils — Static Log4j2 wrapper that resolves the logger to the calling class.
 *
 * @author Maniraj
 */
public final class LoggerUtils {

    private LoggerUtils() {}

    private static Logger getCallerLogger() {
        String callerClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        return LogManager.getLogger(callerClassName);
    }

    public static void info(String message) {
        getCallerLogger().info(message);
    }

    public static void warn(String message) {
        getCallerLogger().warn(message);
    }

    public static void error(String message, Throwable throwable) {
        getCallerLogger().error(message, throwable);
    }

    public static void error(String message) {
        getCallerLogger().error(message);
    }

    public static void debug(String message) {
        getCallerLogger().debug(message);
    }
}
