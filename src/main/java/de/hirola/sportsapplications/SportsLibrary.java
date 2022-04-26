package de.hirola.sportsapplications;

import de.hirola.sportsapplications.database.DatastoreDelegate;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.model.*;
import de.hirola.sportsapplications.util.LogContent;
import de.hirola.sportsapplications.util.TemplateLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Initializes the library environment.
 * Sets up local data storage and loads program defaults.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class SportsLibrary implements DatastoreDelegate {

    private static SportsLibrary instance;
    private final DataRepository dataRepository;
    private final LogManager logManager;
    private List<DatastoreDelegate> delegates;
    private final User appUser;

    /**
     * Create a singleton library objekt.
     *
     * @param libDirectory for the database and log files of library
     * @param application on Android needed
     * @throws InstantiationException if library could not initialize
     */
    public static SportsLibrary getInstance(boolean debugMode,
                                            @Nullable File libDirectory,
                                            @Nullable SportsLibraryApplication application)
            throws InstantiationException {
        if (instance == null) {
            instance = new SportsLibrary(debugMode, libDirectory, application);
        }
        return instance;
    }

    /**
     * Create or get the directory for the app files. If no package name
     * given, the package name of this library will be used to create or get
     * the directory.
     *
     * @param  packageName of the app using this library
     * @return A file object which represents the app directory for the app files, e.g. database ad logs.
     * @throws SportsLibraryException if the directory could not create or used
     */
    public static File initializeAppDirectory(@Nullable String packageName) throws SportsLibraryException {
        // build the lib directory name from package name
        String libraryDirectoryString;
        File libraryDirectory;
        if (packageName == null) {
            packageName = Global.LIBRARY_PACKAGE_NAME;
        }
        // build the path, determine if android or jvm
        // see https://developer.android.com/reference/java/lang/System#getProperties()
        try {
            String vendor = System.getProperty("java.vm.vendor"); // can be null
            if (vendor != null) {
                if (vendor.equals("The Android Project")) {
                    // path for local database on Android
                    libraryDirectoryString = "/data/data"
                            + File.separatorChar
                            + packageName;
                } else {
                    //  path for local database on JVM
                    String userHomeDir = System.getProperty("user.home");
                    libraryDirectoryString = userHomeDir
                            + File.separatorChar
                            + packageName;
                }
            } else {
                String errorMessage = "Could not determine the runtime environment.";
                Logger.debug(errorMessage);
                throw new SportsLibraryException(errorMessage);
            }
        } catch (SecurityException exception){
            String errorMessage = "Could not determine the runtime environment.";
            Logger.debug(errorMessage, exception);
            throw new SportsLibraryException(errorMessage + ": " + exception.getCause().getMessage());
        }
        // create the directory object
        libraryDirectory = new File(libraryDirectoryString);
        // validate, if the directory exists and can modify
        if (libraryDirectory.exists()
                && libraryDirectory.isDirectory()
                && libraryDirectory.canRead()
                && libraryDirectory.canExecute()
                && libraryDirectory.canWrite()) {
            return libraryDirectory;
        }
        // create the directory
        try {
            if (libraryDirectory.mkdirs()) {
                return libraryDirectory;
            } else {
                throw new SportsLibraryException("Could not create the directory " + libraryDirectoryString);
            }
        } catch (SecurityException exception) {
            throw new SportsLibraryException("Could not create the directory " + libraryDirectoryString);
        }
    }

    /**
     * Add a delegate to the library to inform about datastore events.
     *
     * @param delegate to be added
     * @see DatastoreDelegate
     */
    public void addDelegate(@NotNull DatastoreDelegate delegate) {
        if (delegates == null) {
            delegates = new ArrayList<>();
        }
        if (!delegates.contains(delegate)) {
            delegates.add(delegate);
        }
    }

    /**
     * Removes a delegate from the library. No more datastore events will be reported
     * to the delegate.
     *
     * @param delegate to be removed
     * @see DatastoreDelegate
     */
    public void removeDelegate(@NotNull DatastoreDelegate delegate) {
        delegates.remove(delegate);
    }

    /**
     * Get the user of the app, the "athlete".
     *
     * @return The unique user of the app
     */
    public @NotNull User getAppUser() {
        return appUser;
    }

    /**
     * Get the uuid of a training type.
     * If no type with given name or more than one type was found the return value is null.
     *
     * @param name of training type
     * @return The uuid of training type with given name or null.
     * @see TrainingType
     */
    @Nullable
    public UUID getUuidForTrainingType(String name) {
        List<? extends PersistentObject> results =
                dataRepository.findByAttribute("name", name, TrainingType.class);
        // no results found
        if (results.isEmpty()) {
            return null;
        }
        // more than a type was found
        if (results.size() > 1) {
            return null;
        }
        return results.get(0).getUUID();
    }

    /**
     * Returns a list of all available running plans, sorted by order number.
     * If an error occurred or not types could be found, the list is empty.
     *
     * @return A list of all available running plans, sorted by order number.
     */
    public List<RunningPlan> getRunningPlans() {
        List<RunningPlan> runningPlans = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(RunningPlan.class);
        if (persistentObjects.isEmpty()) {
            return runningPlans;
        }
        for (PersistentObject persistentObject : persistentObjects) {
            if (persistentObject instanceof RunningPlan) {
                runningPlans.add((RunningPlan) persistentObject);
            }
        }
        // sort by order number
        Collections.sort(runningPlans);
        return runningPlans;
    }

    /**
     * Returns a list of all saved trainings, sorted by training date.
     * If an error occurred or not types could be found, the list is empty.
     *
     * @return A list of all saved trainings, sorted by training date.
     */
    public List<Training> getTrainings() {
        List<Training> trainings = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(Training.class);
        if (persistentObjects.isEmpty()) {
            return trainings;
        }
        for (PersistentObject persistentObject : persistentObjects) {
            if (persistentObject instanceof Training) {
                trainings.add((Training) persistentObject);
            }
        }
        // sort by training date
        Collections.sort(trainings);
        return trainings;
    }

    /**
     * Returns a list of all available movement types.
     * If an error occurred or not types could be found, the list is empty.
     *
     * @return A list of all available movement types.
     */
    public List<MovementType> getMovementTypes() {
        List<MovementType> movementTypes = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(MovementType.class);
        if (persistentObjects.isEmpty()) {
            return movementTypes;
        }
        for (PersistentObject persistentObject : persistentObjects) {
            if (persistentObject instanceof MovementType) {
                movementTypes.add((MovementType) persistentObject);
            }
        }
        return movementTypes;
    }

    /**
     * Add a new object.
     *
     * @param object to be added
     * @throws SportsLibraryException if an error occurred while adding
     */
    public void add(@NotNull PersistentObject object) throws SportsLibraryException {
        if  (dataRepository.isOpen()) {
            dataRepository.add(object);
        }
    }

    /**
     * Save an existing object.
     *
     * @param object to be saved
     * @throws SportsLibraryException if the object not exist or an error occurred while adding
     */
    public void update(@NotNull PersistentObject object) throws SportsLibraryException {
        if  (dataRepository.isOpen()) {
            dataRepository.update(object);
        }
    }

    /**
     * Removes an existing object from the local datastore.
     *
     * @param object to be removed
     * @throws SportsLibraryException if an error occurred while removing
     */
    public void delete(@NotNull PersistentObject object) throws SportsLibraryException {
        if (dataRepository.isOpen()) {
            dataRepository.delete(object);
        }
    }

    /**
     * Get an object from given type with given UUID.
     *
     * @param withType of object to find
     * @param uuid of object to find
     * @return The object from given type and the given UUID or null if the object was not found,
     *         the datastore is not open or an error occurred while searching.
     */
    @Nullable
    public PersistentObject findByUUID(@NotNull Class<? extends PersistentObject> withType, @NotNull UUID uuid) {
        if (dataRepository.isOpen()) {
            return dataRepository.findByUUID(withType, uuid);
        }
        return null;
    }

    /**
     * Get all objects with a given type. If an error occurred while finding
     * the objects or the datastore is not open, the list is empty too.
     *
     * @param fromType of object to get
     * @return A list of objects with the given type. The list can be empty.
     */
    public List<? extends PersistentObject> findAll(Class<? extends PersistentObject> fromType) {
        if (dataRepository.isOpen()) {
            return dataRepository.findAll(fromType);
        }
        return new ArrayList<>();
    }

    /**
     * Find objects with given name of attribute and value. List can bei empty.
     *
     * @param attributeName of object
     * @param value of attribute
     * @param fromType of object to find
     * @return A list of object where the attribute contains the desired value.
     */
    public List<? extends PersistentObject> findByAttribute(@NotNull String attributeName,
                                                            @NotNull Object value,
                                                            Class<? extends PersistentObject> fromType) {
        if (dataRepository.isOpen()) {
            return dataRepository.findByAttribute(attributeName, value, fromType);
        }
        return new ArrayList<>();
    }

    /**
     * Load a running plan from a json file.
     * The return value is null if the<BR>
     * <ul>
     *      <li>file does not exist</li>
     *      <li>file contains not a valid json with a running plan</li>
     *      <li>file could not be read</li>
     * </ul>
     */
    @Nullable
    public RunningPlan loadFromJSON(@NotNull File jsonFile) {
        return null;
    }

    /**
     * Export a given running plan to a json file.
     *
     * @param runningPlan to be exported.
     * @param exportDir for the JSON export
     * @throws SportsLibraryException if the file could not be exported
     */
    public void exportToJSON(@NotNull RunningPlan runningPlan, @NotNull File exportDir) throws SportsLibraryException {
        TemplateLoader templateLoader = new TemplateLoader(this);
        templateLoader.exportRunningPlanToJSON(runningPlan, exportDir);
    }

    /**
     * Delete all objects from the database.
     */
    public void clearAll() {
        if (dataRepository.isOpen()) {
            dataRepository.clearAll();
        }
    }

    /**
     * Get a flag to determine, whether errors should be logged.
     * Can only be true, if (file) logging is enabled.
     *
     * @return The flag to determine, whether errors should be logged.
     */
    public boolean isDebugMode() {
        return logManager.isDebugMode();
    }

    /**
     * Log a given message to the log file.
     *
     * @param message to be logged
     */
    public void debug(String message) {
        Logger.debug(message);
    }

    /**
     * Log a given message and exception to the log file.
     *
     * @param message to be logged
     * @param exception to be logged
     */
    public void debug(Exception exception, String message) {
        Logger.debug(exception, message);
    }

    /**
     * Log a given message and an argument to the log file.
     *
     * @param message to be logged
     * @param argument to be logged
     */
    public void debug(String message, Object argument) {
        Logger.debug(message, argument);
    }

    /**
     * Get the content of all log files from the app directory.
     * The creation date is defined as a key for each log file content.
     * If logging to file disabled or an error occurred while getting the content from file,
     * an empty list will be returned.
     *
     * @return A list containing all available log files.
     */
    public List<LogContent> getLogContent() {
        if (logManager.isLoggingEnabled()) {
            return logManager.getLogContent();
        }
        return new ArrayList<>();
    }

    /**
     * Sends all available debug information to ...
     * Not implemented yet.
     *
     * @return <B>True</B> if the information could be sent successfully.
     */
    public boolean sendDebugLogs() {
        return false;
    }

    @Override
    public void didObjectAdded(PersistentObject persistentObject) {
        if (delegates != null) {
            for (DatastoreDelegate delegate : delegates) {
                delegate.didObjectAdded(persistentObject);
            }
        }
    }

    @Override
    public void didObjectUpdated(PersistentObject persistentObject) {
        if (delegates != null) {
            for (DatastoreDelegate delegate : delegates) {
                delegate.didObjectUpdated(persistentObject);
            }
        }
    }

    @Override
    public void didObjectRemoved(PersistentObject persistentObject) {
        if (delegates != null) {
            for (DatastoreDelegate delegate : delegates) {
                delegate.didObjectRemoved(persistentObject);
            }
        }
    }

    private SportsLibrary(boolean debugMode,
                          @Nullable File libraryDirectory,
                          @Nullable SportsLibraryApplication application) throws InstantiationException {
        try {
            // if the parameter is null, set the directory
            if (libraryDirectory == null) {
                // the directory is created in the user's home
                libraryDirectory = initializeAppDirectory(Global.LIBRARY_PACKAGE_NAME);
            }
            // initialize the logManager
            logManager = LogManager.getInstance(libraryDirectory, debugMode);
            // lokalen Datenspeicher mit dem Namen der App anlegen / öffnen
            DatabaseManager databaseManager = DatabaseManager.getInstance(libraryDirectory);
            dataRepository = new DataRepository(this, databaseManager, this);
            // bei neu angelegtem Datenspeicher diesen mit initialen Werten befüllen
            if (dataRepository.isEmpty()) {
                TemplateLoader templateLoader = new TemplateLoader(this, application);
                // alle Templates in Datenspeicher laden
                templateLoader.loadAllFromJSON();
            }
            // create or load the App user
            List<? extends PersistentObject> users = dataRepository.findAll(User.class);
            if (users.size() > 1) {
                // not good
                if (logManager.isDebugMode()) {
                    Logger.debug("More as one user in the app.");
                }
            }
            if (users.isEmpty()) {
                // create the App user
                appUser = new User();
                dataRepository.add(appUser);
            } else {
                // set the App user
                PersistentObject persistentObject = users.get(0);
                if (persistentObject instanceof User) {
                    appUser = (User) persistentObject;
                } else {
                    appUser = new User();
                    if (logManager.isDebugMode()) {
                        Logger.debug("Couldn't get the user from datastore.");
                    }
                }
            }
        } catch (SportsLibraryException exception) {
            throw new InstantiationException(exception.getMessage());
        }
    }

}
