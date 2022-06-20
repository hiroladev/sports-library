package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import javax.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A single training unit as part of a training section within a running plan.
 * The movement types can be adjusted and saved.
 * Example: 20 min run
 *
 * A running unit cannot exist without a running plan.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 * @see  RunningPlanEntry
 *
 */

public class RunningUnit extends PersistentObject {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private boolean isCompleted;
    private long duration; // duration in minutes
    private MovementType movementType;
    private String typeOfRunString; // for flex plans
    private int lowerPulseLimit;
    private int upperPulseLimit;
    private long pace; //  running speed minutes / kilometer in seconds

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
        lowerPulseLimit = 0;
        upperPulseLimit = 0;
        pace = 0L;
        isCompleted = false;
    }

    /**
     * Create a running unit without a movement type.
     * Required for flex plans (iCAL import).
     *
     * @param typeOfRunString with running instructions.
     * @param pace of the running unit
     * @param lowerPulseLimit of the running unit
     * @param upperPulseLimit of the running unit
     */
    public RunningUnit(@NotNull String typeOfRunString, long pace, int lowerPulseLimit, int upperPulseLimit) {
        this.typeOfRunString = typeOfRunString;
        this.pace = pace;
        this.lowerPulseLimit = lowerPulseLimit;
        this.upperPulseLimit = upperPulseLimit;
        this.duration = 0;
        isCompleted = false;
    }

    /**
     * Get the type of movement for the unit.
     *
     * @return Type of movement for the unit
     */
    public Optional<MovementType> getMovementType() {
        return Optional.ofNullable(movementType);
    }

    /**
     * Set the type of movement for the unit.
     *
     * @param movementType type of movement for the unit
     */
    public void setMovementType(@NotNull MovementType movementType) {
        this.movementType = movementType;
    }

    /**
     * Get a string for the type(s) of movement while running.
     * Required for flex plans (iCAL import).
     *
     * @return A string with running instructions.
     */
    public Optional<String> getTypeOfRunString() {
        return Optional.ofNullable(typeOfRunString);
    }

    /**
     * Set a string for the type(s) of movement while running.
     * Required for flex plans (iCAL import).
     *
     * @param typeOfRunString with running instructions
     */
    public void setTypeOfRunString(@NotNull String typeOfRunString) {
        this.typeOfRunString = typeOfRunString;
    }

    /**
     *  Get the duration of the unit in minutes.
     *
     * @return The duration of the unit in minutes.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the duration of the running unit.
     *
     * @param duration of running unit in minutes
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Returns the lower limit of the heart rate range during exercise.
     *
     * @return The lower limit of the heart rate range during exercise.
     */
    public int getLowerPulseLimit() {
        return lowerPulseLimit;
    }

    /**
     * Set the lower limit of the heart rate range during exercise.
     *
     * @param lowerPulseLimit of the heart rate range during exercise.
     */
    public void setLowerPulseLimit(int lowerPulseLimit) {
        this.lowerPulseLimit = lowerPulseLimit;
    }

    /**
     * Returns the upper limit of the heart rate range during exercise.
     *
     * @return The upper limit of the heart rate range during exercise.
     */
    public int getUpperPulseLimit() {
        return upperPulseLimit;
    }

    /**
     * Returns the upper limit of the heart rate range during exercise.
     *
     * @param upperPulseLimit of the heart rate range during exercise
     */
    public void setUpperPulseLimit(int upperPulseLimit) {
        this.upperPulseLimit = upperPulseLimit;
    }

    /**
     * Get the pace for the running unit.
     *
     * @return The pace of the running unit in seconds for 1 kilometer.
     */
    public long getPace() {
        return pace;
    }

    /**
     * Set the pace for the running unit.
     *
     * @param pace of the running unit in seconds for 1 kilometer
     */
    public void setPace(long pace) {
        this.pace = pace;
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
     * @param completed A flag to determine if unit is completed.
     */
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("isCompleted", isCompleted);
        document.put("duration", duration);
        document.put("typeOfRunString", typeOfRunString);
        document.put("lowerPulseLimit", lowerPulseLimit);
        document.put("upperPulseLimit", upperPulseLimit);
        document.put("pace", pace);

        if (movementType != null) {
            Document movementTypeDocument = movementType.write(mapper);
            document.put("movementType", movementTypeDocument);
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            isCompleted = (boolean) document.get("isCompleted");
            duration = (long) document.get("duration");
            lowerPulseLimit = (int) document.get("lowerPulseLimit");
            upperPulseLimit = (int) document.get("upperPulseLimit");
            pace = (long) document.get("pace");

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
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, duration, movementType);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

}
