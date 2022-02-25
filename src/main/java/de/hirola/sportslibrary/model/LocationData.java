package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.DateUtil;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;

import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An object to store location data.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public class LocationData extends PersistentObject {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private long timeStamp; // UTC time of this location, in milliseconds since epoch (January 1, 1970).
    private String provider;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;

    /**
     * Default constructor for reflection and database management.
     */
    public LocationData() {
        timeStamp = DateUtil.getTimeStampFromNow();
        provider = "https://www.countrycoordinate.com"; // Neustadt in Sachsen
        latitude = 51.023639;
        longitude = 14.213444;
        altitude = 0.0;
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
        provider = "Unknown";
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = 0.0;
        this.speed = 0.0;
    }

    /**
     * Creates a location object.
     *
     * @param timeStamp of the location
     * @param provider source of the location
     * @param latitude coordinate for the location
     * @param longitude coordinate for the location
     * @param altitude of the location
     * @param speed of the location
     */
    public LocationData(long timeStamp, String provider,
                        double latitude, double longitude, double altitude, double speed) {
        this.timeStamp = timeStamp;
        this.provider = provider;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
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

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("timeStamp", timeStamp);
        document.put("provider", provider);
        document.put("latitude", latitude);
        document.put("longitude", longitude);
        document.put("altitude", altitude);
        document.put("speed", speed);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            timeStamp = (long) document.get("timeStamp");
            provider = (String) document.get("provider");
            latitude = (double) document.get("latitude");
            longitude = (double) document.get("longitude");
            altitude = (double) document.get("altitude");
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
        return Objects.hash(super.hashCode(), timeStamp, provider, latitude, longitude, altitude, speed);
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    

}