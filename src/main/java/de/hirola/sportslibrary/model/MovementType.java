package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Object for managing movement types. The user can make his own settings.
 * When you start the app for the first time, some movement types are imported
 * into the data store using JSON.
 * The unique identifier of an object from type is the key. The key corresponds with
 * a key in the JSON.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 *
 */
@Indices({
       @Index(value = "key", type = IndexType.Unique)
})
public class MovementType extends PersistentObject {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private String key;  // token for the type of moving, e.g. L is running, must be unique
    private String stringForKey; // the text for the token
    private String colorKeyString; // the color for the token, dynamically on different platforms
    private double speed; // speed of movement type in km/h
    private double pace; // pace of movement type, user defined in relation to the speed

    /**
     * Default constructor for reflection and database management.
     */
    public MovementType() {
        key = UUIDFactory.generateUUID();
        stringForKey = "";
        this.colorKeyString = Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR;
        speed = 0.0;
        pace = 0.0;
    }

    /**
     * Creates a movement type object.
     *
     * @param key of the type
     * @param stringForKey token for the type
     * @param colorKeyString color for the token
     * @param speed for the type
     * @param pace for the type
     */
    public MovementType(@NotNull String key, @NotNull String stringForKey, @Nullable String colorKeyString,
                        double speed, double pace) {
        this.key = key;
        this.stringForKey = stringForKey;
        this.colorKeyString = Objects.requireNonNullElse(colorKeyString, Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR);
        this.speed = speed;
        this.pace = pace;
    }

    /**
     * Get the token for the movement type.
     *
     * @return Token for the movement type
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the token for the movement type.
     *
     * @return Token for the movement type
     */
    public String getStringForKey() {
        return stringForKey;
    }


    /**
     * Get the color string for the token of the movement type.
     *
     * @return Color string for the token
     */
    public String getColorKeyString() {
        return colorKeyString;
    }

    /**
     * Set the color for the token.
     *
     * @param colorKeyString of token
     */
    public void setColorKeyString(String colorKeyString) {
        this.colorKeyString = colorKeyString;
    }

    /**
     * Get the speed for the movement type.
     *
     * @return Token for the movement type
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the speed for the movement type.
     *
     * @param speed of movement type
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Get the pace for the movement type.
     *
     * @return Pace for the movement type
     */
    public double getPace() {
        return pace;
    }

    /**
     * Set the pace for the speed of the movement type.
     *
     * @param pace of movement type
     */
    public void setPace(double pace) {
        this.pace = pace;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("key", key);
        document.put("stringForKey", stringForKey);
        document.put("colorKeyString", colorKeyString);
        document.put("speed", speed);
        document.put("pace", pace);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            key = (String) document.get("key");
            stringForKey = (String) document.get("stringForKey");
            colorKeyString = (String) document.get("colorKeyString");
            speed = (double) document.get("speed");
            pace = (double) document.get("pace");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        // gleicher Schlüssel = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovementType that = (MovementType) o;
        return key.equals(that.key); // key must be unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, stringForKey, colorKeyString);
    }

    @Override
    public UUID getUUID() {
        return new UUID(key);
    }

    
}
