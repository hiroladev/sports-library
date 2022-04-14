package de.hirola.sportslibrary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Layer for logging of library. Encapsulated the use of logging tools.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
final class LogManager {

    private static LogManager instance;
    private final File loggingDirectoy;
    private final boolean isDebugMode;
    private boolean isLoggingEnabled;

    /**
     * Get an instance of logger.
     *
     * @param loggingDirectoy of the app using this logger
     * @param isDebugMode of logging on or off
     * @return The logger object for logging.
     */
    public static LogManager getInstance(@NotNull File loggingDirectoy, boolean isDebugMode) {
        if (instance == null) {
            instance = new LogManager(loggingDirectoy, isDebugMode);
        }
        return instance;
    }

    /**
     * Get a flag to determine, if (file) logging is enabled.
     *
     * @return The flag to determine, if logging to file enabled.
     */
    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    /**
     * Get a flag to determine, whether errors should be logged.
     * Can only be true, if (file) logging is enabled.
     *
     * @return The flag to determine, whether errors should be logged.
     */
    public boolean isDebugMode() {
        return isDebugMode && isLoggingEnabled;
    }

    /**
     * Get the content of all log files from the app directory.
     * The creation date is defined as a key for each log file content.
     * If logging to file disabled or an error occurred while getting the content from file,
     * a null value will be returned.
     *
     * @return A map containing the creation date and content of each log file.
     */
    @Nullable
    public List<LogContent> getLogContent() {
        if (isLoggingEnabled) {
            List<LogContent> logContentList = new ArrayList<>();
            Collection<File> logFiles = FileUtils.listFiles(
                    loggingDirectoy,
                    new String[]{"log"},
                    false);
            for (File logFile : logFiles) {
                try (LineIterator it = FileUtils.lineIterator(FileUtils.getFile(logFile), "UTF-8")) {
                    // get the creation time
                    BasicFileAttributes attr = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
                    FileTime logFileCreationTime = attr.creationTime();
                    // get the content of the file
                    StringBuilder stringBuilder = new StringBuilder();
                    while (it.hasNext()) {
                        stringBuilder.append(it.nextLine());
                        stringBuilder.append("\n");
                    }
                    LocalDate creationDate = logFileCreationTime
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    LogContent logContent = new LogContent();
                    logContent.creationDate = creationDate;
                    logContent.contentString = stringBuilder.toString();
                    logContentList.add(logContent);
                } catch (IOException exception) {
                    if (isLoggingEnabled) {
                        String message = "Error occurred while getting content from log file.";
                        Logger.debug(message, exception);
                    }
                    return null;
                }
            }
            return logContentList;
        }
        return null;
    }

    private LogManager(@NotNull File loggingDirectoy, boolean isDebugMode) {
        this.loggingDirectoy = loggingDirectoy;
        this.isDebugMode = isDebugMode;
        try {
            // set the property for the rolling file logger
            System.setProperty("tinylog.directory", loggingDirectoy.getPath());
        } catch (SecurityException exception) {
            isLoggingEnabled = false;
        }
    }

    /**
     * A helper class for the content of log files.
     */
    public static final class LogContent {
        public LocalDate creationDate;
        public String contentString;
    }
}
