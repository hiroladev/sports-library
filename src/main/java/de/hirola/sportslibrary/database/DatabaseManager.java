package de.hirola.sportslibrary.database;

import com.onyx.exception.InitializationException;
import com.onyx.exception.OnyxException;
import com.onyx.persistence.factory.PersistenceManagerFactory;
import com.onyx.persistence.factory.impl.EmbeddedPersistenceManagerFactory;
import com.onyx.persistence.manager.PersistenceManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A layer to abstract the used data management library.
 * We use the <a href="https://onyx.dev/products#embedded">Onyx</a> embedded database.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */

public class DatabaseManager {

    private final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;
    private static Logger logger;
    private PersistenceManagerFactory factory = null;
    private PersistenceManager persistenceManager = null;

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
     * Get the manager to handle with data, e.g. save, update or delete.
     * Can be null if while initialize an error occurred. The errors
     * are logged.
     *
     * @return The manger for data management
     */
    @Nullable
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * Delete all objects from the database.
     */
    public void clearAll() {
        if (persistenceManager != null) {
            for (Class<? extends PersistentObject> type : Global.PERSISTENT_CLASSES_LIST) {
                try {
                    List<? extends PersistentObject> allObjectsFromType = persistenceManager.list(type);
                    persistenceManager.deleteEntities(allObjectsFromType);
                    String logMessage = allObjectsFromType.size()
                            + " objects from type "
                            + type.getSimpleName()
                            + " deleted.";
                    logger.log(Logger.DEBUG, TAG, logMessage, null);
                } catch (OnyxException exception) {
                    String errorMessage = "Error occurred while deleting all objects from type "
                            + type;
                    logger.log(Logger.ERROR, TAG, errorMessage, exception);
                }
            }
        }
    }

    /**
     * Close the database.
     */
    public void close() {
        if (factory != null) {
            factory.close();
        }
    }

    private DatabaseManager(@NotNull String packageName) {
        try {
                String databaseName = initializeDatabasePath(packageName);
                factory = new EmbeddedPersistenceManagerFactory(databaseName);
                factory.initialize();

                persistenceManager = factory.getPersistenceManager();

        } catch (SportsLibraryException | OnyxException exception){
            logger.log(Logger.ERROR, TAG,"Could not determine the runtime environment. Manager is null.", exception);
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
