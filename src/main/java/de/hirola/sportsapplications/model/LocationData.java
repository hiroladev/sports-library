package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.DateUtil;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;

import java.util.Objects;
import java.util.Optional;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An object to store location data.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class LocationData extends PersistentObject {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private long timeStamp; // UTC time of this location, in milliseconds since epoch (January 1, 1970).
    private String gpsFix;
    private double latitude;
    private double longitude;
    private double elevation;
    private double speed;

    /**
     * Default constructor for reflection and database management.
     */
    public LocationData() {
        timeStamp = DateUtil.getTimeStampFromNow();
        // Neustadt in Sachsen (Germany)
        latitude = 51.023639;
        longitude = 14.213444;
        elevation = 0.0;
        speed = 0.0;
    }

    /**
     * Creates a location object with coordinates.
     *
     * @param latitude coordinate for the location
     * @param longitude coordinate for the location
     */
    public LocationData(double latitude, double longitude) {
        timeStamp = DateUtil.getTimeStampFromNow();
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = 0.0;
        this.speed = 0.0;
    }

    /**
     * Creates a location object.
     *
     * @param timeStamp of the location
     * @param gpsFix of the location
     * @param latitude coordinate for the location
     * @param longitude coordinate for the location
     * @param elevation of the location
     * @param speed of the location
     */
    public LocationData(long timeStamp, String gpsFix,
                        double latitude, double longitude, double elevation, double speed) {
        this.timeStamp = timeStamp;
        this.gpsFix = gpsFix;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.speed = speed;
    }

    /**
     * Get the time stamp of the location.
     *
     * @return The time stamp of the location in milliseconds since epoch.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Set the time stamp of the location.
     *
     * @param timeStamp of the location in milliseconds since epoch.
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Get the Position detection type (none, 2d, 3d, dgps, pps, ..) of the location.
     *
     * @return The fix of the location.
     */
    public Optional<String> getGpsFix() {
        return Optional.ofNullable(gpsFix);
    }

    /**
     * Set the GPS fix of the location.
     *
     * @param gpsFix of the location.
     */
    public void setGpsFix(String gpsFix) {
        this.gpsFix = gpsFix;
    }

    /**
     * Get the latitude of the location.
     *
     * @return latitude of the location
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude of the location.
     *
     * @param latitude The latitude of the location.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude of the location.
     *
     * @return longitude of the location
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude of the location.
     *
     * @param longitude The longitude of the location.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the elevation of the location.
     *
     * @return The elevation of the location.
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Set the elevation of the location.
     *
     * @param elevation The elevation of the location.
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    /**
     * Get the speed of the location.
     *
     * @return The speed of the location.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the speed of the location.
     *
     * @param speed The speed of the location.
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("timeStamp", timeStamp);
        document.put("provider", gpsFix);
        document.put("latitude", latitude);
        document.put("longitude", longitude);
        document.put("elevation", elevation);
        document.put("speed", speed);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            timeStamp = (long) document.get("timeStamp");
            gpsFix = (String) document.get("provider");
            latitude = (double) document.get("latitude");
            longitude = (double) document.get("longitude");
            elevation = (double) document.get("elevation");
            speed = (double) document.get("speed");

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LocationData that = (LocationData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timeStamp, gpsFix, latitude, longitude, elevation, speed);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    

}