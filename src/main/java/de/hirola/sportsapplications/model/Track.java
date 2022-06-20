package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.database.ListMapper;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.DateUtil;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

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
    private String name; // name of track
    private String description; // short description, can be null
    private String remarks; // remarks, e.g. metadata from gpx, can be null
    private Date importDate;
    private long startTimeInMilli = -1; // in utc epoch millis
    private long stopTimeInMilli = -1; // in utc epoch millis
    private long duration = -1; // in minutes
    private double distance = -1.0;
    private double averageSpeed = -1.0;
    private double elevationDifference = -1.0;
    private List<LocationData> locationData; // list of tracking data

    /**
     * Default constructor for reflection and database management.
     */
    public Track() {
        locationData = new ArrayList<>();
    }

    /**
     * Create a track to start recording.
     *
     * @param name of track
     * @param description of track
     * @param startTimeInMilli of track
     */
    public Track(@NotNull String name, @Null String description, long startTimeInMilli) {
        this.name = name;
        this.description = description;
        this.startTimeInMilli = startTimeInMilli;
        this.locationData = new ArrayList<>();
    }

    /**
     * Create a track to start recording.
     *
     * @param name of track
     * @param description of track
     * @param locationData of track
     */
    public Track(@NotNull String name, @Null String description, @Null List<LocationData> locationData) {
        this.name = name;
        this.description = description;
        this.locationData = Objects.requireNonNullElseGet(locationData, ArrayList::new);
    }

    /**
     * Create a recorded and completed track. The locationData of the track can be empty.
     *
     * @param name of track
     * @param description of track
     * @param startTimeInMilli of track
     * @param stopTimeInMilli of track
     * @param locationData of track
     */
    public Track(@NotNull String name, @Null String description,
                 long startTimeInMilli, long stopTimeInMilli, double distance,
                 @Null List<LocationData> locationData) {
        this.name = name;
        this.description = description;
        this.startTimeInMilli = startTimeInMilli;
        this.stopTimeInMilli = stopTimeInMilli;
        this.distance = distance;
        this.locationData = Objects.requireNonNullElseGet(locationData, ArrayList::new);
        // calculate duration and speed
        calculateValues();
    }

    /**
     * Create an imported track. The start and end time is determined from the locationData.
     * If no import date given, the current date will be set.
     *
     * @param name of track
     * @param description of track
     * @param importDate of track
     * @param locationData of track
     */
    public Track(@NotNull String name, @Null String description, @Null LocalDate importDate,
                 @NotNull List<LocationData> locationData) {
        this.name = name;
        this.description = description;
        this.importDate = DateUtil.getDateFromLocalDate(importDate);
        this.locationData = locationData;
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
     * @param locationData of track
     */
    public Track(@NotNull String name, @Null String description, @Null LocalDate importDate,
                 long startTimeInMilli, long stopTimeInMilli, double avg, double distance, @NotNull List<LocationData> locationData) {
        this.name = name;
        this.description = description;
        this.importDate = DateUtil.getDateFromLocalDate(importDate);
        this.startTimeInMilli = startTimeInMilli;
        this.stopTimeInMilli = stopTimeInMilli;
        this.averageSpeed = avg;
        this.distance = distance;
        this.locationData = locationData;
    }

    /**
     * Get the name the track.
     *
     * @return The name of the track.
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
     * @return The name of the track.
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
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
     * Get remarks for the track, e.g. metadata from a imported gpx file.
     *
     * @return The remarks of the track.
     */
    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    /**
     * Set remarks for the track.
     *
     * @param remarks of the track
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
     * @return The stop time of the track.
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
     * If not set (-1), the duration will calculate from the locationData.
     *
     * @return The distance of the track in meters.
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
     * the individual speeds of the locationData.
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
     * from the individual altitude difference of the locationData.
     *
     * @return The average speed of the track in km/h.
     */
    public double getElevationDifference() {
        //TODO: if -1, calculate from locationData
        return elevationDifference;
    }

    /**
     * Set the altitudeDifference of the track in meter.
     *
     * @param elevationDifference of the track in meter
     */
    public void setElevationDifference(double elevationDifference) {
        this.elevationDifference = elevationDifference;
    }

    /**
     * Get the import date of the track.
     *
     * @return The import date of the track.
     */
    public Optional<LocalDate> getImportDate() {
        return Optional.ofNullable(DateUtil.getLocalDateFromDate(importDate));
    }

    /**
     * Get the locationData of the track.
     *
     * @return The locationData of the track
     */
    public List<LocationData> getLocations() {
        if (locationData == null) {
            locationData = new ArrayList<>();
        }
        return locationData;
    }

    /**
     * Add a list of locationData to the track.
     * Any existing list will be overwritten.
     *
     * @param locationData to be added to the track.
     */
    public void setLocations(@NotNull List<LocationData> locationData) {
        this.locationData = locationData;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("description", description);
        document.put("remarks", remarks);
        document.put("importDate", importDate);
        document.put("startTimeInMilli", startTimeInMilli);
        document.put("stopTimeInMilli", stopTimeInMilli);
        document.put("distance", distance);
        document.put("averageSpeed", averageSpeed);
        document.put("elevationDifference", elevationDifference);

        if (locationData != null) {
            document.put("locationData", ListMapper.toDocumentsList(mapper, locationData));
        }


        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            description = (String) document.get("description");
            remarks = (String) document.get("remarks");
            importDate = (Date) document.get("importDate");
            startTimeInMilli = (long) document.get("startTimeInMilli");
            stopTimeInMilli = (long) document.get("stopTimeInMilli");
            distance = (double) document.get("distance");
            averageSpeed = (double) document.get("averageSpeed");
            elevationDifference = (double) document.get("elevationDifference");

            try {
                @SuppressWarnings("unchecked")
                List<Document> objectsDocument = (List<Document>) document.get("locationData");
                locationData = ListMapper.toElementsList(mapper, objectsDocument, LocationData.class);
            } catch (Exception ignore) {
                locationData = new ArrayList<>();
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
