package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A training session with the bike, the tour is imported as a GPX using an existing recording,
 * saved as a route and visualized with MapKit.
 * The user can store additional information for each training session.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 *
 */
@Entity
public class Training extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private String name;
    @Attribute
    private String remarks;
    @Attribute
    private long duration; // in minutes
    @Attribute
    private double distance; // if -1 then calculate from track
    @Attribute
    private double altitudeDifference; // if -1 then calculate from track
    @Attribute
    private double averageSpeed; // if -1 then calculate from track
    @Attribute
    private Date trainingDate;
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = TrainingType.class,
            inverse = "relationAttributeForTrainingToTrainingType",
            cascadePolicy = CascadePolicy.NONE)
    private TrainingType trainingType; // biking, running, ...
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = Track.class,
            inverse = "relationAttributeForTrainingToTrack",
            cascadePolicy = CascadePolicy.SAVE)
    private Track track; // track of the training

    /**
     * Default constructor for import from json.
     */
    public Training() {
        name = "Training";
        remarks = "";
        distance = -1.0;
        altitudeDifference = -1.0;
        averageSpeed = -1.0;
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable TrainingType trainingType,
                    @NotNull Track track, @Nullable Date trainingDate) {
        this.name = name;
        // if null set default
        this.remarks = Objects.requireNonNullElse(remarks, "");
        this.trainingType = Objects.requireNonNullElseGet(trainingType, TrainingType::new);
        this.track = track;
        // if null set default
        this.trainingDate = Objects.requireNonNullElseGet(trainingDate, () -> Date.from(Instant.now()));
        // get the values from track
        distance = track.getDistance();
        altitudeDifference = track.getAverageSpeed();
        averageSpeed = track.getAverageSpeed();
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable TrainingType trainingType,
                    @Nullable Date trainingDate, @Nullable Track track,
                    double distance, double averageSpeed, double altitudeDifference) {
        this.name = name;
        // if null set default
        this.remarks = Objects.requireNonNullElse(remarks, "");
        // if null set default
        this.trainingType = Objects.requireNonNullElseGet(trainingType, TrainingType::new);
        this.track = track; // can be null
        this.trainingDate = Objects.requireNonNullElseGet(trainingDate, () -> Date.from(Instant.now()));
        this.distance = distance;
        this.averageSpeed= averageSpeed;
        this.altitudeDifference = altitudeDifference;
    }

    /**
     * Get the name of the training.
     *
     * @return The name of training
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the training.
     *
     * @param name of the training
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the remarks of the training.
     *
     * @return The remarks of training
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Set the remarks of the training.
     *
     * @param remarks of the training
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Get the type of the training.
     *
     * @return The type of training
     */
    public TrainingType getTrainingType() {
        return trainingType;
    }

    /**
     * Set the type of the training.
     *
     * @param trainingType of the training
     */
    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    /**
     * Get the date of the training.
     *
     * @return The date of training
     */
    public Date getTrainingDate() {
        return trainingDate;
    }

    /**
     * Set the date of the training.
     *
     * @param trainingDate of the training
     */
    public void setTrainingDate(Date trainingDate) {
        this.trainingDate = trainingDate;
    }

    /**
     * Get the track of the training.
     *
     * @return The track of training, can be null
     */
    @Nullable
    public Track getTrack() {
        return track;
    }

    /**
     * Set the track of the training.
     *
     * @param track of the training
     */
    public void setTrack(Track track) {
        this.track = track;
    }

    /**
     * Get the duration of the training.
     *
     * @return The duration of training in minutes
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the duration of the training.
     *
     * @param duration of the training in minutes
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Get the distance of the training.
     *
     * @return The distance of training in meters
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance of the training.
     *
     * @param distance of the training in meters
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Get the name of the training.
     *
     * @return The altitude difference of training in meters
     */
    public double getAltitudeDifference() {
        return altitudeDifference;
    }

    /**
     * Set the altitude difference of training.
     *
     * @param altitudeDifference of training in meters
     */
    public void setAltitudeDifference(double altitudeDifference) {
        this.altitudeDifference = altitudeDifference;
    }

    /**
     * Get the average speed of the training.
     *
     * @return The average speed of training in km/h
     */
    public double getAverageSpeed() {
        return averageSpeed;
    }

    /**
     * Set the average speed of the training.
     *
     * @param averageSpeed of the training in km/h
     */
    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Training training = (Training) o;
        return name.equals(training.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name);
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}
