package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A single training unit as part of a training section within a running plan.
 * The movement types can be adjusted and saved.
 * Example: 20 min run
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 * @see  RunningPlanEntry
 *
 */
public class RunningUnit extends PersistentObject {

    @Id
    private NitriteId uuid;
    private boolean isCompleted;
    private long duration; // duration in minutes
    private MovementType movementType;

    /**
     * Default constructor for reflection and database management.
     */
    public RunningUnit() {
        super();
        duration = 0;
        movementType = new MovementType();
        isCompleted = false;
    }

    /**
     * Create a running unit of a plan entry.
     *
     * @param duration of the unit
     * @param movementType of the unit
     */
    public RunningUnit(int duration, @NotNull MovementType movementType) {
        this.duration = duration;
        this.movementType = movementType;
        this.isCompleted = false;
    }

    /**
     * Get the type of movement for the unit.
     *
     * @return Type of movement for the unit
     */
    public MovementType getMovementType() {
        return movementType;
    }

    /**
     * Set the type of movement for the unit.
     *
     * @param movementType type of movement for the unit
     */
    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    /**
     * Get a flag to determine if unit is completed.
     *
     * @return  A flag to determine if unit is completed
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Set a flag to determine if unit is completed.
     *
     * @param completed A flag to determine if unit is completed
     */
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    /**
     * Get the duration of the unit.
     *
     * @return The duration of the unit
     */
    public long getDuration() {
        return duration;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("isCompleted", isCompleted);
        document.put("duration", duration);

        if (movementType != null) {
            Document movementTypeDocument = movementType.write(mapper);
            document.put("movementType", movementTypeDocument);
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (NitriteId) document.get("uuid");
            isCompleted = (boolean) document.get("isCompleted");
            duration = (long) document.get("duration");

            Document movementTypeDocument = (Document) document.get("movementType");
            if (movementTypeDocument != null) {
                MovementType movementType = new MovementType();
                movementType.read(mapper, movementTypeDocument);
                this.movementType = movementType;
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningUnit that = (RunningUnit) o;
        return duration == that.duration && uuid.equals(that.uuid) && movementType.equals(that.movementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, duration, movementType);
    }

    @Override
    public NitriteId getUUID() {
        return uuid;
    }
}
