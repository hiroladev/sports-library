package de.hirola.sportslibrary.database;

import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.util.Logger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A layer to abstract the used data management library.
 * We use the <a href="https://onyx.dev/products#embedded">Onyx</a> embedded database.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class DatabaseManager {

    private final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;
    private static Logger logger;
    private Nitrite database;

    /**
     * Get an instance of database manager.
     *
     * @param packageName of the using app or library, used for the database name
     * @return An instance of the database manager.
     */
    public static DatabaseManager getInstance(@NotNull String packageName) {
        if (instance == null) {
            logger = Logger.getInstance(packageName);
            instance = new DatabaseManager(packageName);
        }
        return instance;
    }

    /**
     * Get the manager to handle with data, e.g. add, update or delete.
     * Can be null if while initialize an error occurred. The errors
     * are logged.
     *
     * @return The manger for data management
     */
    @Nullable
    public Nitrite getDatabase() {
        return database;
    }

    private DatabaseManager(@NotNull String packageName) {
        try {
            // Nitrite by default compacts the database file before close.
            // If compaction is enabled chunks will be moved next to each other.
            // Disabling compaction will increase the performance during database close.
                String databasePath = initializeDatabasePath(packageName);
                database = Nitrite.builder()
                        .filePath(databasePath)
                        .disableAutoCompact()
                        .openOrCreate();

        } catch (SportsLibraryException | NitriteIOException exception){
            logger.log(Logger.ERROR, TAG,"Could not determine the runtime environment. Database is null.", exception);
        }
    }

    private String initializeDatabasePath(@NotNull String packageName) throws SportsLibraryException {
        String databasePath;
        // build the database name from package name
        if (!packageName.contains(".")) {
            // a primitive check for valid package name
            throw new SportsLibraryException("Not a valid package name.");
        }
        int beginIndex = packageName.lastIndexOf('.') + 1;
        int endIndex = packageName.length();
        String databaseName = packageName.substring(beginIndex, endIndex);
        // build the path, determine if android or jvm
        // see https://developer.android.com/reference/java/lang/System#getProperties()
        try {
            String vendor = System.getProperty("java.vm.vendor"); // can be null
            if (vendor != null) {
                if (vendor.equals("The Android Project")) {
                    // path for local database on Android
                    databasePath = "/data/data"
                            + File.separatorChar
                            + packageName
                            + File.separatorChar
                            + databaseName
                            + File.separatorChar
                            + databaseName + ".db";
                } else {
                    //  path for local database on JVM
                    String userHomeDir = System.getProperty("user.home");
                    databasePath = userHomeDir
                            + File.separatorChar
                            + databaseName
                            + File.separatorChar
                            + databaseName + ".db";
                }
            } else {
                String errorMessage = "Could not determine the runtime environment.";
                logger.log(Logger.ERROR, TAG,errorMessage, null);
                throw new SportsLibraryException(errorMessage);
            }
        } catch (SecurityException exception){
            String errorMessage = "Could not determine the runtime environment.";
            logger.log(Logger.ERROR, TAG,errorMessage, exception);
            throw new SportsLibraryException(errorMessage + ": " + exception.getCause().getMessage());
        }
        return databasePath;
    }

}
