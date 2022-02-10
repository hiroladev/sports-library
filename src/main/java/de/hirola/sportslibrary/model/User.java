package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.time.Instant;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Der Nutzer der App, also der Läufer.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
@Entity
public class User extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
     @Attribute
    private String firstName;
    @Attribute
    private String lastName;
    @Attribute
    private String emailAddress;
    @Attribute
    private Date birthday; // required to calculate the heart rate
    @Attribute
    private int gender; // required to calculate the heart rate
     @Attribute
    private int trainingLevel; // from Global
    @Attribute
    private int maxPulse; // calculate with birthday and gender
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = RunningPlan.class,
            inverse = "relationAttributeForUserToRunningPlan",
            cascadePolicy = CascadePolicy.SAVE)
    private RunningPlan activeRunningPlan; // current training

    /**
     * Default constructor for reflection.
     */
    public User() {
        super();
        firstName = "";
        lastName = "Athlete";
        emailAddress = "";
        birthday = Date.from(Instant.now());
        gender = 0;
        trainingLevel = 0;
        maxPulse = 0;
        activeRunningPlan = null;
    }

    /**
     * Get the first name of the user.
     *
     * @return The first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name of the user.
     *
     * @param firstName of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the last name of the user.
     *
     * @return The last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name of the user.
     *
     * @param lastName of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the email address of the user.
     *
     * @return The first name of the user
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Set the email address of the user.
     * The address will be not validate.
     *
     * @param emailAddress of the user
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Get the birthday of the user.
     * The year ist need to calculate the max pulse.
     *
     * @return The birthday of the user
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Set the birthday of the user.
     *
     * @param birthday of the user
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * Get the gender of the user.
     *
     * @return The gender of the user
     * @see Global
     */
    public int getGender() {
        return gender;
    }

    /**
     * Set the gender of the user.
     * The gender ist need to calculate the max pulse.
     *
     * @param gender of the user
     */
    public void setGender(int gender) {
        if (Global.TrainingParameter.genderValues.containsKey(gender)) {
            this.gender = gender;
        }
    }

    /**
     * Get the training level of user.
     *
     * @return The training level of the user
     * @see Global
     */
    public int getTrainingLevel() {
        return trainingLevel;
    }

    /**
     * Set the training level of the user.
     *
     * @param trainingLevel of the user
     */
    public void setTrainingLevel(int trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    /**
     * Get the max pulse of user.
     *
     * @return The max pulse of the user
     */
    public int getMaxPulse() {
        return maxPulse;
    }

    /**
     * Set the max pulse of the user.
     *
     * @param maxPulse of the user
     */
    public void setMaxPulse(int maxPulse) {
        this.maxPulse = maxPulse;
    }

    /**
     * Get the first name of user.
     *
     * @return The first name of the user
     */
    @Nullable
    public RunningPlan getActiveRunningPlan() {
        return activeRunningPlan;
    }

    /**
     * Set the active training plan.
     *
     * @param activeRunningPlan wich the user want to train now
     */
    public void setActiveRunningPlan(RunningPlan activeRunningPlan) {
        this.activeRunningPlan = activeRunningPlan;
    }

    @Override
    public boolean equals(Object o) {
        // gleiche User-ID und gleiche E-Mail-Adresse = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return uuid.equals(user.uuid) && Objects.equals(emailAddress, user.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, emailAddress);
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}
