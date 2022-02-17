package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.DateUtil;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
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
 * @since 0.0.1
 *
 */
@Indices({
        @Index(value = "uuid", type = IndexType.Unique)
})
public class Training extends PersistentObject {

    @Id
    private NitriteId nitriteId;
    private String uuid = UUIDFactory.generateUUID();
    private String name;
    private String remarks;
    private long duration; // in minutes
    private double distance; // if -1 then calculate from track
    private double altitudeDifference; // if -1 then calculate from track
    private double averageSpeed; // if -1 then calculate from track
    private Date trainingDate;
    private TrainingType trainingType; // biking, running, ...
    private Track track; // track of the training

    /**
     * Default constructor for reflection, JSON import
     * and database management.
     */
    public Training() {
        name = "Training";
        remarks = "";
        distance = -1.0;
        altitudeDifference = -1.0;
        averageSpeed = -1.0;
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable TrainingType trainingType,
                    @NotNull Track track, @Nullable LocalDate trainingDate) {
        this.name = name;
        // if null set default
        this.remarks = Objects.requireNonNullElse(remarks, "");
        this.trainingType = Objects.requireNonNullElseGet(trainingType, TrainingType::new);
        this.track = track;
        if (trainingDate == null) {
            this.trainingDate = DateUtil.getDateFromNow();
        } else {
            this.trainingDate = DateUtil.getDateFromLocalDate(trainingDate);
        }
        // get the values from track
        distance = track.getDistance();
        altitudeDifference = track.getAverageSpeed();
        averageSpeed = track.getAverageSpeed();
    }

    public Training(@NotNull String name, @Nullable String remarks, @Nullable TrainingType trainingType,
                    @Nullable LocalDate trainingDate, @Nullable Track track,
                    double distance, double averageSpeed, double altitudeDifference) {
        this.name = name;
        // if null set default
        this.remarks = Objects.requireNonNullElse(remarks, "");
        // if null set default
        this.trainingType = Objects.requireNonNullElseGet(trainingType, TrainingType::new);
        this.track = track; // can be null
        if (trainingDate == null) {
            this.trainingDate = DateUtil.getDateFromNow();
        } else {
            this.trainingDate = DateUtil.getDateFromLocalDate(trainingDate);
        }
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
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("duration", duration);
        document.put("distance", distance);
        document.put("altitudeDifference", altitudeDifference);
        document.put("averageSpeed", averageSpeed);
        document.put("trainingDate", trainingDate);

        if (trainingType != null) {
            Document trainingTypeDocument = trainingType.write(mapper);
            document.put("trainingType", trainingTypeDocument);
        }

        if (track != null) {
            Document trackDocument = track.write(mapper);
            document.put("track", trackDocument);
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            nitriteId = NitriteId.createId((Long) document.get("nitriteId"));
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            duration = (long) document.get("duration");
            distance = (double) document.get("distance");
            altitudeDifference = (double) document.get("altitudeDifference");
            averageSpeed = (double) document.get("averageSpeed");
            trainingDate = (Date) document.get("trainingDate");

            Document trainingTypeDocument = (Document) document.get("trainingType");
            if (trainingTypeDocument != null) {
                TrainingType trainingType = new TrainingType();
                trainingType.read(mapper, trainingTypeDocument);
                this.trainingType = trainingType;
            }

            Document trackDocument = (Document) document.get("track");
            if (trackDocument != null) {
                Track track = new Track();
                track.read(mapper, trackDocument);
                this.track = track;
            }
        }
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

    @Override
    public NitriteId getNitriteId() {
        return nitriteId;
    }
}
