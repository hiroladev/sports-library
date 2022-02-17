package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
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
 * An object represents the type of training, currently bike and running training.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 *
 */
@Indices({
        @Index(value = "uuid", type = IndexType.Unique),
        @Index(value = "name", type = IndexType.Unique)
})
public class TrainingType extends PersistentObject {

    @Id
    private NitriteId nitriteId;
    private String uuid = UUIDFactory.generateUUID();
    private String name;
    private String imageName; // image for the kind of training
    private String remarks;
    private double speed;

    /**
     * Default constructor for reflection and database management.
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
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("imageName", imageName);
        document.put("remarks", remarks);
        document.put("speed", speed);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            nitriteId = NitriteId.createId((Long) document.get("nitriteId"));
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            imageName = (String) document.get("imageName");
            remarks = (String) document.get("remarks");
            speed = (double) document.get("speed");
        }
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

    @Override
    public NitriteId getNitriteId() {
        return nitriteId;
    }
}

