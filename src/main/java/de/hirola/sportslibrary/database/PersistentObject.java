package de.hirola.sportslibrary.database;

import com.onyx.persistence.ManagedEntity;
import com.onyx.persistence.annotations.Entity;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A layer to abstract the used data management library.
 * Objects of this type can be managed in the data store.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
@Entity
public abstract class PersistentObject extends ManagedEntity {
    public abstract String getUUID();
}
