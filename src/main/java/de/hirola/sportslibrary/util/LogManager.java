package de.hirola.sportslibrary.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Layer for logging of library. Encapsulated the use of logging tools.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public final class LogManager {

    private static LogManager instance;
    private boolean isDebugMode;
    private String logDirString;
    private boolean isLoggingEnabled;

    /**
     * Get an instance of logger.
     *
     * @param packageName of the app using this logger
     * @return The logger object for logging.
     */
    public static LogManager getInstance(@NotNull String packageName) {
        if (instance == null) {
            instance = new LogManager(packageName);
        }
        return instance;
    }

    /**
     * Get a flag to determine, whether errors should be logged.
     * Can only be true, if logging is enabled.
     *
     * @return The flag to determine, whether errors should be logged.
     */
    public boolean isDebugMode() {
        return isDebugMode && isLoggingEnabled;
    }

    /**
     * Set the debug mode on or off.
     *
     * @param debugMode a flag to set debug mode on or off
     */
    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    /**
     * Get the content of the log file as string.
     * If logging to file disabled or an error occurred while getting the content from file,
     * an empty string will be returned.
     *
     * @return The content of log file as string.
     */
    @NotNull
    public String getLogContent() {
        if (isLoggingEnabled) {
            try (LineIterator it = FileUtils.lineIterator(FileUtils.getFile(logDirString), "UTF-8")) {
                StringBuilder stringBuilder = new StringBuilder();
                while (it.hasNext()) {
                    stringBuilder.append(it.nextLine());
                }
                return  stringBuilder.toString();
            } catch (IOException exception) {
                if (isLoggingEnabled) {
                    String message = "Error occurred while getting content from log file.";
                    Logger.debug(message, exception);
                }
                return "";
            }
        }
        return "";
    }

    private LogManager(@NotNull String packageName) {
        // build the database name from package name
        if (!packageName.contains(".")) {
            // a primitive check for valid package name
            isLoggingEnabled = false;
            isDebugMode = false;
            return;
        }
        // build the path, determine if android or jvm
        // see https://developer.android.com/reference/java/lang/System#getProperties()
        try {
            String vendor = System.getProperty("java.vm.vendor"); // can be null
            if (vendor != null) {
                if (vendor.equals("The Android Project")) {
                    // path for local database on Android
                    logDirString = "/data/data"
                            + File.separatorChar
                            + packageName;
                } else {
                    //  path for local database on JVM
                    String userHomeDir = System.getProperty("user.home");
                    logDirString = userHomeDir
                            + File.separatorChar
                            + packageName;
                }
                // set the property for the rolling file logger
                System.setProperty("tinylog.directory", logDirString);
            } else {
                isLoggingEnabled = false;
            }
        } catch (Exception exception){
            isLoggingEnabled = false;
            System.out.println("Logging is disable.");
        }
    }
}
