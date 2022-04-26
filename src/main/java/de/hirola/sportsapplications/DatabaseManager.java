package de.hirola.sportsapplications;

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
 * This library use the <a href="https://github.com/nitrite/nitrite-java">Nitrite</a> embedded database.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
class DatabaseManager {

    private static DatabaseManager instance;
    private final Nitrite database;

    /**
     * Get an instance of database manager.
     *
     * @param databaseDirectory of the library
     * @return An instance of the database manager.
     * @throws SportsLibraryException if an error occurred while creating or opening the database
     */
    public static DatabaseManager getInstance(@NotNull File databaseDirectory) throws SportsLibraryException {
        if (instance == null) {
            instance = new DatabaseManager(databaseDirectory);
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

    private DatabaseManager(@NotNull File databaseDirectory) throws SportsLibraryException {
        try {
            // Nitrite by default compacts the database file before close.
            // If compaction is enabled chunks will be moved next to each other.
            // Disabling compaction will increase the performance during database close.
                String databasePath = buildDatabasePath(databaseDirectory);
                database = Nitrite.builder()
                        .filePath(databasePath)
                        .disableAutoCompact()
                        .openOrCreate();

        } catch (NitriteIOException exception) {
            throw new SportsLibraryException("Could not determine the runtime environment. Database is null: "
                    + exception.getMessage());
        }
    }

    private String buildDatabasePath(@NotNull File databaseDirectory) {
        // build the database name from package name
        String packageName = Global.LIBRARY_PACKAGE_NAME;
        int beginIndex = packageName.lastIndexOf('.') + 1;
        int endIndex = packageName.length();
        String databaseName = packageName.substring(beginIndex, endIndex) + ".db";
        return databaseDirectory.getPath() + File.separatorChar + databaseName;
    }

}
