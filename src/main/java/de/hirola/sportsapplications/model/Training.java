package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.DateUtil;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
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
 * @since v0.1
 *
 */
public class Training extends PersistentObject implements Comparable<Training> {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private String name;
    private String remarks;
    private long duration; // in minutes
    private double distance; // if -1 then calculate from track
    private double altitudeDifference; // if -1 then calculate from track
    private double averageSpeed; // if -1 then calculate from track
    private Date trainingDate;
    private UUID trainingTypeUUID; // biking, running, ...
    private UUID trackUUID; // track of the training, can be null

    /**
     * Default constructor for reflection, JSON import
     * and database management.
     */
    public Training() {
        name = "Training";
        remarks = "";
        duration = -1L;
        distance = -1.0;
        altitudeDifference = -1.0;
        averageSpeed = -1.0;
        trackUUID = null;
        trainingDate = DateUtil.getDateFromNow();
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable LocalDate trainingDate,
                    @Nullable UUID trainingTypeUUID, @Nullable UUID trackUUID) {
        this();
        this.name = name;
        if (remarks != null) {
            this.remarks = remarks;
        }
        if (trainingDate != null) {
            this.trainingDate = DateUtil.getDateFromLocalDate(trainingDate);
        }
        this.trainingTypeUUID = trainingTypeUUID; // can be null
        this.trackUUID = trackUUID; // can be null
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable LocalDate trainingDate,
                    long duration, double distance, double averageSpeed, double altitudeDifference,
                    @Nullable UUID trainingTypeUUID, @Nullable UUID trackUUID) {
        this.name = name;
        if (remarks != null) {
            this.remarks = remarks;
        }
        if (trainingDate != null) {
            this.trainingDate = DateUtil.getDateFromLocalDate(trainingDate);
        }
        this.trainingTypeUUID = trainingTypeUUID; // can be null
        this.trackUUID = trackUUID; // can be null
        this.duration = duration;
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
    @Nullable
    public UUID getTrainingTypeUUID() {
        return trainingTypeUUID;
    }

    /**
     * Set the type of the training.
     *
     * @param trainingTypeUUID of the training
     */
    public void setTrainingTypeUUID(UUID trainingTypeUUID) {
        this.trainingTypeUUID = trainingTypeUUID;
    }

    /**
     * Get the date of the training.
     *
     * @return The date of training
     */
    public LocalDate getTrainingDate() {
        return DateUtil.getLocalDateFromDate(trainingDate);
    }

    /**
     * Set the date of the training.
     *
     * @param trainingDate of the training
     */
    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = DateUtil.getDateFromLocalDate(trainingDate);
    }

    /**
     * Get the track of the training.
     *
     * @return The track of training, can be null
     */
    @Nullable
    public UUID getTrackUUID() {
        return trackUUID;
    }

    /**
     * Set the track of the training.
     *
     * @param trackUUID of the training
     */
    public void setTrackUUID(UUID trackUUID) {
        this.trackUUID = trackUUID;
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
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("duration", duration);
        document.put("distance", distance);
        document.put("altitudeDifference", altitudeDifference);
        document.put("averageSpeed", averageSpeed);
        document.put("trainingDate", trainingDate);
        document.put("trainingTypeUUID", trainingTypeUUID);
        document.put("trackUUID", trackUUID);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            duration = (long) document.get("duration");
            distance = (double) document.get("distance");
            altitudeDifference = (double) document.get("altitudeDifference");
            averageSpeed = (double) document.get("averageSpeed");
            trainingDate = (Date) document.get("trainingDate");
            trainingTypeUUID = (UUID) document.get("trainingTypeUUID");
            trackUUID = (UUID) document.get("trackUUID");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Training that = (Training) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    @Override
    public int compareTo(@NotNull Training other) {
        // sort by date
        LocalDate dateFromThis = DateUtil.getLocalDateFromDate(trainingDate);
        LocalDate dateFromOther = other.getTrainingDate();
        if (dateFromThis.isBefore(dateFromOther)) {
            return -1;
        }
        if (dateFromThis.isAfter(dateFromOther)) {
            return 1;
        }
        return 0;
    }

}
