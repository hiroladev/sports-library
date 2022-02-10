package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.User;
import de.hirola.sportslibrary.util.Logger;
import de.hirola.sportslibrary.util.TemplateLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Initializes the library environment.
 * Sets up local data storage and loads program defaults.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public final class SportsLibrary implements DatastoreDelegate {

    private final String TAG = Logger.class.getSimpleName();

    private final Logger logger = Logger.getInstance(null);
    private final DataRepository dataRepository;
    private List<DatastoreDelegate> delegates;
    private final User appUser;

    /**
     * Create a new library objekt for data management.
     *
     * @param appName of app using this library
     * @param application on Android needed
     * @throws SportsLibraryException if library could not initialize
     * @see SportsLibraryApplication
     */
    public SportsLibrary(@Nullable String appName,
                         @Nullable SportsLibraryApplication application) throws SportsLibraryException {
        // lokalen Datenspeicher mit dem Namen der App anlegen / öffnen
        dataRepository = new DataRepository(appName);
        // bei neu angelegtem Datenspeicher diesen mit initialen Werten befüllen
        if (dataRepository.isEmpty()) {
            TemplateLoader templateLoader = new TemplateLoader(dataRepository, application);
            // alle Templates in Datenspeicher laden
            templateLoader.loadAllFromJSON();
        }
        // create or load the App user
        List<? extends PersistentObject> users = dataRepository.findAll(User.class);
        if (users.size() > 1) {
            // not good
            if (Global.APP_DEBUG_MODE) {
                logger.log(Logger.DEBUG, TAG, "More as one user in the app.", null);
            }
        }
        if (users.isEmpty()) {
            // create the App user
            appUser = new User();
            dataRepository.save(appUser);
        } else {
            // set the App user
            PersistentObject persistentObject = users.get(0);
            if (persistentObject instanceof User) {
                appUser = (User) persistentObject;
            } else {
                if (Global.APP_DEBUG_MODE) {
                    appUser = new User();
                    logger.log(Logger.DEBUG, TAG, "Couldn't get the user from datastore.", null);
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
