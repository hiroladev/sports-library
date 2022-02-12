package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.FetchPolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
@Entity
public class MovementType extends PersistentObject {

    @Attribute
    @Identifier
    private final String key;  // token for the type of moving, e.g. L is running, must be unique
    @Attribute
    private final String stringForKey; // the text for the token
    private String colorKeyString; // the color for the token, dynamically on different platforms
    @Attribute
    private double speed; // speed of movement type in km/h
    @Attribute
    private double pace; // pace of movement type, user defined in relation to the speed
    @Relationship(type = RelationshipType.ONE_TO_MANY,
            inverseClass = RunningUnit.class,
            inverse = "movementType",
            cascadePolicy = CascadePolicy.NONE,
            fetchPolicy = FetchPolicy.LAZY)
    private List<RunningUnit> associatedRunningUnits; // used only for modelling relations

    /**
     * Default constructor for reflection and database management.
     */
    public MovementType() {
        super();
        key = UUIDFactory.generateUUID();
        stringForKey = UUIDFactory.generateUUID();
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
        super();
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
    public String getUUID() {
        return key;
    }
}
