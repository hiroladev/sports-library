package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
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
 * An object represents the type of training, currently bike and running training.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 *
 */
@Entity
public class TrainingType extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private String name;
    @Attribute
    private String imageName; // image for the kind of training
    @Attribute
    private String remarks;
    @Attribute
    private double speed;
    @Relationship(type = RelationshipType.ONE_TO_MANY,
            inverseClass = Training.class,
            inverse = "trainingType",
            cascadePolicy = CascadePolicy.SAVE)
    private List<Training> relationAttributeForTrainingToTrainingType; // only for modelling 1:m relations

    /**
     * Default constructor for reflection.
     */
    public TrainingType() {
        super();
        name = "Training";
        //TODO: default image for JVM and Android in resources
        imageName = "training-default";
        speed = 0.0;
    }

    /**
     * Create a type of training.
     *
     * @param name of type
     * @param imageName of type
     * @param remarks of type
     * @param speed of type
     */
    public TrainingType(@NotNull String name, @Nullable String remarks, @Nullable String imageName, double speed) {
        this.name = name;
        this.remarks = Objects.requireNonNullElse(remarks, "");
        this.imageName = Objects.requireNonNullElse(imageName, "training-default");
        this.speed = speed;
    }

    /**
     * Get the name of the training type.
     *
     * @return The name of training type
     */
    public String getName() {
        return name;
    }

    /**
     * Set the last name of the training type.
     *
     * @param name of the training type.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of the image for the training type.
     *
     * @return The name of image
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Set name of the image for the training type.
     *
     * @param imageName for the training type
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Get remarks of the training type.
     *
     * @return The remarks of the training type
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Set the remarks of the training type.
     *
     * @param remarks of the training type
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Get the speed of the training type.
     *
     * @return The speed of the training type
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the speed for the training type.
     * Approximate speed of the training type in km/h.
     * The type can thus be suggested on the basis of recorded training sessions.
     *
     * @param speed of the training
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TrainingType that = (TrainingType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name);
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}

