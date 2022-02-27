package de.hirola.sportslibrary.database;

import de.hirola.sportslibrary.database.PersistentObject;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Delegate for datastore events. Update the ui without view model.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public interface DatastoreDelegate {
    void didObjectAdded(PersistentObject persistentObject);
    void didObjectUpdated(PersistentObject persistentObject);
    void didObjectRemoved(PersistentObject persistentObject);
}
