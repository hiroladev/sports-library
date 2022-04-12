package de.hirola.sportslibrary.util;

import java.util.UUID;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Creates UUID for the objects, needed as key in datastore
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 *
 */
public final class UUIDFactory {
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 15);
    }
    public static String generateTrainingType() {
        return "Trainingstype~"
                + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);
    }
    public static String generateEMailAddress() {
        return "app.user@"
                + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 5)
                + ".de";
    }
}
