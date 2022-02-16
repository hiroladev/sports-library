package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.database.ListMapper;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.DateUtil;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.NitriteMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * @since 0.0.1
 */
public class Track extends PersistentObject {

    @org.dizitart.no2.objects.Id
    private NitriteId uuid;
    private String name = ""; // name of track
    private String description = ""; // short description
    private Date importDate = Date.from(Instant.now());
    private long startTimeInMilli = -1;
    private long stopTimeInMilli = -1;
    private double distance = -1.0;
    private double averageSpeed = -1.0;
    private double altitudeDifference = -1;
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
    public Track(String name, String description, long startTimeInMilli, long stopTimeInMilli, @Nullable List<LocationData> locations) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "No description available.");
        this.startTimeInMilli = startTimeInMilli;
        this.stopTimeInMilli = stopTimeInMilli;
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
        //TODO: if start time equal -1, get the time form last location
        return startTimeInMilli;
    }

    /**
     * Set the start time of the track in milliseconds to UTC Time.
     *
     * @param startTimeInMilli of the track
     */
    public void setStartTimeInMilli(long startTimeInMilli) {
        this.startTimeInMilli = startTimeInMilli;
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
     * Set the stop time of the track in milliseconds to UTC Time.
     *
     * @param stopTimeInMilli of the track.
     */
    public void setStopTimeInMilli(long stopTimeInMilli) {
        this.stopTimeInMilli = stopTimeInMilli;
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
            uuid = (NitriteId) document.get("uuid");
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
            } catch (ClassCastException | SportsLibraryException exception) {
                //TODO: logging?
                locations = new ArrayList<>();
            }
        }
    }
    
    @Override
    public NitriteId getUUID() {
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
                && startTimeInMilli == track.startTimeInMilli
                && stopTimeInMilli == track.stopTimeInMilli;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name, description, startTimeInMilli, stopTimeInMilli);
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
