package de.hirola.sportsapplications.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Each object has a unique UUID.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 *
 */
public class UUID implements Serializable {

    private final String uuid;

    public UUID(@NotNull String uuid) {
        this.uuid = uuid;
    }

    public String getString() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UUID uuid1 = (UUID) o;
        return uuid.equals(uuid1.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "UUID{" +
                "uuid='" + uuid + '\'' +
                '}';
    }
}
