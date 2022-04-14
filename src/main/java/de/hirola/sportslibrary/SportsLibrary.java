package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.DatabaseManager;
import de.hirola.sportslibrary.database.DatastoreDelegate;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.*;
import de.hirola.sportslibrary.util.LogManager;
import de.hirola.sportslibrary.util.RunningPlanTemplate;
import de.hirola.sportslibrary.util.TemplateLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Initializes the library environment.
 * Sets up local data storage and loads program defaults.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public final class SportsLibrary implements DatastoreDelegate {

    private final LogManager logManager;
    private final DataRepository dataRepository;
    private List<DatastoreDelegate> delegates;
    private final User appUser;

    /**
     * Create a new library objekt for data management.
     *
     * @param packageName of app using this library
     * @param application on Android needed
     * @throws SportsLibraryException if library could not initialize
     * @see SportsLibraryApplication
     */
    public SportsLibrary(@NotNull String packageName,
                         @Nullable SportsLibraryApplication application) throws SportsLibraryException {
        // initialize the logManager
        logManager = LogManager.getInstance(packageName, Global.DEBUG_MODE);
        // lokalen Datenspeicher mit dem Namen der App anlegen / öffnen
        DatabaseManager databaseManager = DatabaseManager.getInstance(packageName, logManager);
        dataRepository = new DataRepository(databaseManager, this, logManager);
        // bei neu angelegtem Datenspeicher diesen mit initialen Werten befüllen
        if (dataRepository.isEmpty()) {
            TemplateLoader templateLoader = new TemplateLoader(dataRepository, application, logManager);
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
    }

    /**
     * Get the local datastore for the library.
     *
     * @return The local datastore for the library
     * @see DataRepository
     */
    public DataRepository getDataRepository() {
        return dataRepository;
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
        TemplateLoader templateLoader = new TemplateLoader(dataRepository, logManager);
        templateLoader.exportRunningPlanToJSON(runningPlan, exportDir);
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
}
