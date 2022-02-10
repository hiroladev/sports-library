package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.FetchPolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;

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
@Entity
public class RunningUnit extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private boolean isCompleted;
    @Attribute
    private final long duration; // duration in minutes
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = MovementType.class,
            inverse = "relationAttributeForMovementTypeToRunningPlanUnit", // mapping attribute in MovementType
            cascadePolicy = CascadePolicy.NONE)
    private MovementType movementType;
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = RunningPlanEntry.class,
            inverse = "runningUnits",
            cascadePolicy = CascadePolicy.ALL,
            fetchPolicy = FetchPolicy.LAZY)
    private RunningPlanEntry relationAttributeForRunningPlanEntryToRunningPlanUnit; // defined only for modelling the relationship 1:m

    /**
     * Default constructor for reflection.
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
    public RunningUnit(int duration, MovementType movementType) {
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
    public String getUUID() {
        return uuid;
    }
}
