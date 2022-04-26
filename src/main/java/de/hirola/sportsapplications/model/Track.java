package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.database.ListMapper;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.DateUtil;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
 * @since v0.1
 */

public class Track extends PersistentObject {

    @org.dizitart.no2.objects.Id
    private String uuid = UUIDFactory.generateUUID();
    private String name = ""; // name of track
    private String description = ""; // short description
    private Date importDate = Date.from(Instant.now());
    private long startTimeInMilli = -1; // in utc epoch millis
    private long stopTimeInMilli = -1; // in utc epoch millis
    private long duration = -1; // in minutes
    private double distance = -1.0;
    private double averageSpeed = -1.0;
    private double altitudeDifference = -1.0;
    private List<LocationData> locations; // list of tracking data

    /**
     * Default constructor for reflection and database management.
     */
    public Track() {
        locations = new ArrayList<>();
    }

    /**
     * Create a track to start recording.
     *
     * @param name of track
     * @param description of track
     * @param startTimeInMilli of track
     */
    public Track(@NotNull String name, @Nullable String description, long startTimeInMilli) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        this.startTimeInMilli = startTimeInMilli;
        this.locations = new ArrayList<>();
    }

    /**
     * Create a track to start recording.
     *
     * @param name of track
     * @param description of track
     * @param locations of track
     */
    public Track(@NotNull String name, @Nullable String description, @Nullable List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        this.locations = Objects.requireNonNullElseGet(locations, ArrayList::new);
    }

    /**
     * Create a recorded and completed track. The location of the tack can be empty.
     *
     * @param name of track
     * @param description of track
     * @param startTimeInMilli of track
     * @param stopTimeInMilli of track
     * @param locations of track, can be null
     */
    public Track(String name, String description,
                 long startTimeInMilli, long stopTimeInMilli, double distance,
                 @Nullable List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        this.startTimeInMilli = startTimeInMilli;
        this.stopTimeInMilli = stopTimeInMilli;
        this.distance = distance;
        this.locations = Objects.requireNonNullElseGet(locations, ArrayList::new);
        // calculate duration and speed
        calculateValues();
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
    public Track(@NotNull String name, @Nullable String description, @Nullable LocalDate importDate,
                 @NotNull List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        if (importDate == null) {
            this.importDate = DateUtil.getDateFromNow();
        } else {
            this.importDate = DateUtil.getDateFromLocalDate(importDate);
        }
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
     * @param startTimeInMilli of track
     * @param stopTimeInMilli of track
     * @param avg of track
     * @param distance of track
     * @param locations of track
     */
    public Track(@NotNull String name, @Nullable String description, @Nullable LocalDate importDate,
                 long startTimeInMilli, long stopTimeInMilli, double avg, double distance, @NotNull List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        if (importDate == null) {
            this.importDate = DateUtil.getDateFromNow();
        } else {
            this.importDate = DateUtil.getDateFromLocalDate(importDate);
        }
        this.startTimeInMilli = startTimeInMilli;
        this.stopTimeInMilli = stopTimeInMilli;
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
    public long getStartTimeInMilli() {
        return startTimeInMilli;
    }

    /**
     * Get the stop time the track in milliseconds to UTC Time
     * or -1 if no time is set.
     *
     * @return The stop time of the track
     */
    public long getStopTimeInMilli() {
        //TODO: if stop time equal -1, get the time form last location
        return stopTimeInMilli;
    }

    /**
     * Get the duration of a track in minutes.
     *
     * @return The calculated duration of the track in minutes
     *          or - 1 if the duration could not calculate.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the duration of the track.
     *
     * @param duration of track in minutes
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Get the distance of the track in meters.
     * If not set (-1), the duration will calculate from the locations.
     *
     * @return The distance of the track in meters
     */
    public double getDistance() {
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
        if (averageSpeed == -1) {
            return 0.0;
        }
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
    public LocalDate getImportDate() {
        return DateUtil.getLocalDateFromDate(importDate);
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

    /**
     * Add a list of locations to the track.
     * Any existing list will be overwritten.
     *
     * @param locations to be added to the track.
     */
    public void setLocations(@NotNull List<LocationData> locations) {
        this.locations = locations;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("importDate", importDate);
        document.put("startTimeInMilli", startTimeInMilli);
        document.put("stopTimeInMilli", stopTimeInMilli);
        document.put("distance", distance);
        document.put("averageSpeed", averageSpeed);
        document.put("altitudeDifference", altitudeDifference);

        if (locations != null) {
            document.put("locations", ListMapper.toDocumentsList(mapper, locations));
        }


        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            importDate = (Date) document.get("importDate");
            startTimeInMilli = (long) document.get("startTimeInMilli");
            stopTimeInMilli = (long) document.get("stopTimeInMilli");
            distance = (double) document.get("distance");
            averageSpeed = (double) document.get("averageSpeed");
            altitudeDifference = (double) document.get("altitudeDifference");

            try {
                @SuppressWarnings("unchecked")
                List<Document> objectsDocument = (List<Document>) document.get("locations");
                locations = ListMapper.toElementsList(mapper, objectsDocument, LocationData.class);
            } catch (Exception ignore) {
                locations = new ArrayList<>();
            }
        }
    }
    
    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Track that = (Track) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name, description, startTimeInMilli, stopTimeInMilli);
    }

    private void calculateValues() {
        if (duration == -1) {
            if (startTimeInMilli > 0 && stopTimeInMilli > 0) {
                try {
                    // calculate to minutes
                    Instant startTime = Instant.ofEpochMilli(startTimeInMilli);
                    Instant stopTime = Instant.ofEpochMilli(stopTimeInMilli);
                    Duration durationTime = Duration.between(startTime, stopTime);
                    long durationInSeconds = Math.abs(durationTime.getSeconds());
                    // minimale value is 1 minute
                    if (durationInSeconds < 60) {
                        duration = 1;
                    } else {
                        duration = durationInSeconds / 60;
                    }
                } catch (Exception exception) {
                    // we could not calculate
                    duration = -1;
                }
            }
        }
        if (averageSpeed == -1.0) {
            if (distance > 0 && duration > 0) {
                // distance in m, duration in sec
                averageSpeed = (distance / duration / 60) * 3.6;
            }
        }
    }

    /**
     * A wrapper class for the id of a track.
     * Can be used while handling track recording on Android.
     * The id will be created with the primary key in a local sqlite database.
     */
    public static final class Id {
        private final long id;
        private boolean isRecording;

        /**
         * Create an instance of the id, which represents the (temporary) id
         * of a track. The recording flag is set to <b>true</b>.
         *
         * @param id of track while recording
         */
        public Id(long id) {
            this.id = id;
            isRecording = true;
        }

        /**
         * Get the (temporary) id of a track.
         *
         * @return The (temporary) id of a track.
         */
        public long getId() {
            return id;
        }

        /**
         * Get a flag to determine if a track is being recorded.
         *
         * @return A flag to determine the recording state.
         */
         public boolean isRecording() {
            return isRecording;
        }

        /**
         * Sets a flag to determine if a track is being recorded.
         *
         * @param recording a flag to determine the recording state
         */
        public void setRecording(boolean recording) {
            isRecording = recording;
        }

        @Override
        public String toString() {
            return "Track.ID: " + id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id1 = (Id) o;
            return id == id1.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
