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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed unter the AGPL-3.0 or later.
 *
 * A track for trainings. Tracks can be imported or recorded.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
@Entity
public class Track extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private String name = ""; // name of track
    @Attribute
    private String description = ""; // short description
    @Attribute
    private Date importDate = Date.from(Instant.now());
    @Attribute
    private long startTime = -1;
    @Attribute
    private long stopTime = -1;
    @Attribute
    private double distance = -1.0;
    @Attribute
    private double averageSpeed = -1.0;
    @Attribute
    private double altitudeDifference = -1;
    @Relationship(type = RelationshipType.ONE_TO_MANY,
            inverseClass = LocationData.class,
            inverse = "relationAttributeForTrackToLocationData",
            cascadePolicy = CascadePolicy.ALL,
            fetchPolicy = FetchPolicy.LAZY)
    private List<LocationData> locations; // list of tracking data
    @Relationship(type = RelationshipType.ONE_TO_MANY,
            inverseClass = Training.class,
            inverse = "track",
            cascadePolicy = CascadePolicy.SAVE)
    private List<Training> relationAttributeForTrainingToTrack; // used only for modelling relations
    /**
     * Default constructor for reflection.
     */
    public Track() {
        locations = new ArrayList<>();
    }

    /**
     * Create a track to start recording.
     *
     * @param name of track
     * @param description of track
     * @param startTime of track
     */
    public Track(String name, String description, long startTime) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        locations = new ArrayList<>();
    }

    /**
     * Create a recorded and completed track. The location of the tack can be empty.
     *
     * @param name of track
     * @param description of track
     * @param startTime of track
     * @param stopTime of track
     * @param locations of track, can be null
     */
    public Track(String name, String description, long startTime, long stopTime, @Nullable List<LocationData> locations) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.locations = Objects.requireNonNullElseGet(locations, ArrayList::new);
        //TODO: start, stop, avg, duration if locations not null
    }

    /**
     * Create an imported track. The start and end time is determined from the locations.
     * If no import date given, the current date will be set.
     *
     * @param name of track
     * @param description of track
     * @param importDate of track
     * @param locations of track
     */
    public Track(@NotNull String name, @Nullable String description, @Nullable Date importDate,
                 @NotNull List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.importDate = Objects.requireNonNullElseGet(importDate, () -> Date.from(Instant.now()));
        this.locations = locations;
        //TODO: start and stop time, avg, speed
    }

    /**
     * Create an imported track.
     * If no import date given, the current date will be set.
     *
     * @param name of track
     * @param description of track
     * @param importDate of track
     * @param startTime of track
     * @param stopTime of track
     * @param avg of track
     * @param distance of track
     * @param locations of track
     */
    public Track(@NotNull String name, @Nullable String description, @Nullable Date importDate,
                 long startTime, long stopTime, double avg, double distance, @NotNull List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.importDate = Objects.requireNonNullElseGet(importDate, () -> Date.from(Instant.now()));
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.averageSpeed = avg;
        this.distance = distance;
        this.locations = locations;
    }

    /**
     * Get the name the track.
     *
     * @return The name of the track
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the track.
     *
     * @param name of the track.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name the track.
     *
     * @return The name of the track
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the track.
     *
     * @param description of the track.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the start time the track in milliseconds to UTC Time
     * or -1 if no time is set.
     *
     * @return The start time of the track in milliseconds to UTC Time
     */
    public long getStartTime() {
        //TODO: if start time equal -1, get the time form last location
        return startTime;
    }

    /**
     * Set the start time of the track in milliseconds to UTC Time.
     *
     * @param startTime of the track
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the stop time the track in milliseconds to UTC Time
     * or -1 if no time is set.
     *
     * @return The stop time of the track
     */
    public long getStopTime() {
        //TODO: if stop time equal -1, get the time form last location
        return stopTime;
    }

    /**
     * Set the stop time of the track in milliseconds to UTC Time.
     *
     * @param stopTime of the track.
     */
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * Get the distance of the track in meters.
     * If not set (-1), the duration will calculate from the locations.
     *
     * @return The distance of the track in meters
     */
    public double getDistance() {
        //TODO: if -1, calculate from locations
        return distance;
    }

    /**
     * Set the distance of the track in meters.
     *
     * @param distance of the track in meters
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Get the average speed of the track in km/h.
     * If not set (-1), the speed will calculate from
     * the individual speeds of the locations.
     *
     * @return The average speed of the track in km/h
     */
    public double getAverageSpeed() {
        //TODO: if 0, calculate from locations
        return averageSpeed;
    }

    /**
     * Set the average speed of the track in km/h.
     *
     * @param speed of the track in km/h
     */
    public void setAverageSpeed(double speed) {
        this.averageSpeed = speed;
    }

    /**
     * Get the altitudeDifference of the track in meter.
     * If not set (-1), the altitude difference will calculate
     * from the individual altitude difference of the locations.
     *
     * @return The average speed of the track in km/h
     */
    public double getAltitudeDifference() {
        //TODO: if -1, calculate from locations
        return altitudeDifference;
    }

    /**
     * Set the altitudeDifference of the track in meter.
     *
     * @param altitudeDifference of the track in meter
     */
    public void setAltitudeDifference(double altitudeDifference) {
        this.altitudeDifference = altitudeDifference;
    }

    /**
     * Get the import date of the track.
     *
     * @return The import date of the track
     */
    public Date getImportDate() {
        return importDate;
    }

    /**
     * Get the locations of the track.
     *
     * @return The locations of the track
     */
    public List<LocationData> getLocations() {
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return locations;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Track track = (Track) o;
        return uuid.equals(track.uuid)
                && name.equals(track.name)
                && description.equals(track.description)
                && startTime == track.startTime
                && stopTime == track.stopTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name, description, startTime, stopTime);
    }

    /**
     * A wrapper class for the id of a track.
     * Can be used while handling track recording on Android.
     * The id will be created with the primary key in a local sqlite database.
     */
    public static final class Id {
        private final long id;
        /**
         * Create an instance of the id, which represents the (temporary) id
         * of a track.
         *
         * @param id of track while recording
         */
        public Id(long id) {
            this.id = id;
        }

        /**
         * Get the (temporary) id of a track.
         *
         * @return The (temporary) id of a track.
         */
        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Track.ID: " + id;
        }
    }
}
