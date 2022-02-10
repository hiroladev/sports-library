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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
@Entity
public class LocationData extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private final long timeStamp; // UTC time of this location, in milliseconds since epoch (January 1, 1970).
    @Attribute
    private final String provider;
    @Attribute
    private final double latitude;
    @Attribute
    private final double longitude;
    @Attribute
    private final double altitude;
    @Attribute
    private final double speed;
    @Relationship(type = RelationshipType.MANY_TO_ONE,
                inverseClass = Track.class,
                inverse = "locations",
                cascadePolicy = CascadePolicy.ALL,
                fetchPolicy = FetchPolicy.LAZY)
    private Track relationAttributeForTrackToLocationData; // only for modelling relations 1:m

    /**
     * Default constructor for reflection.
     */
    public LocationData() {
        timeStamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        provider = "https://www.countrycoordinate.com"; // Neustadt in Sachsen
        latitude = 51.023639;
        longitude = 14.213444;
        altitude = 0.0;
        speed = 0.0;
    }

    /**
     * Creates a location object.
     *
     * @param latitude coordinate for the location
     * @param longitude coordinate for the location
     */
    public LocationData(double latitude, double longitude) {
        timeStamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
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
    public LocationData(long timeStamp, String provider, double latitude, double longitude, double altitude, double speed) {
        this.timeStamp = timeStamp;
        this.provider = provider;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LocationData that = (LocationData) o;
        return that.timeStamp == timeStamp
                && that.provider.equals(provider)
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.altitude, altitude) == 0
                && Double.compare(that.speed, speed) == 0;
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