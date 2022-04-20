package de.hirola.sportsapplications.database;

import de.hirola.sportsapplications.model.UUID;
import org.dizitart.no2.mapper.Mappable;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A layer to abstract the used data management library.
 * Objects of this type can be managed in the data store.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public abstract class PersistentObject implements Mappable {
    public abstract UUID getUUID();
}
