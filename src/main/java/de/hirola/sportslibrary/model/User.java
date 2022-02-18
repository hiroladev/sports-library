package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.Global;
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

import java.time.LocalDate;
import java.time.ZoneId;
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
@Indices({
        @Index(value = "uuid", type = IndexType.Unique),
        @Index(value = "emailAddress", type = IndexType.Unique)
})
public class User extends PersistentObject {

    @Id
    private NitriteId nitriteId;
    private String uuid = UUIDFactory.generateUUID();
    private String firstName;
    private String lastName;
    private String emailAddress = UUIDFactory.generateEMailAddress();
    private Date birthday; // required to calculate the heart rate
    private int gender; // required to calculate the heart rate
    private int trainingLevel; // from Global
    private int maxPulse; // calculate with birthday and gender
    private RunningPlan activeRunningPlan; // current training

    /**
     * Default constructor for reflection and database management.
     */
    public User() {
        firstName = "";
        lastName = "Athlete";
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
     * Get the birthday of the user. The year ist need to calculate the max pulse.
     *
     * @return The birthday of the user
     */
    public LocalDate getBirthday() {
        return birthday
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * Set the birthday of the user.
     *
     * @param birthday of the user
     */
    public void setBirthday(@NotNull LocalDate birthday) {
        this.birthday = Date
                .from(birthday.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
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

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("firstName", firstName);
        document.put("lastName", lastName);
        document.put("emailAddress", emailAddress);
        document.put("birthday", birthday);
        document.put("gender", gender);
        document.put("trainingLevel", trainingLevel);
        document.put("maxPulse", maxPulse);

        if (activeRunningPlan != null) {
            Document activeRunningPlanDocument = activeRunningPlan.write(mapper);
            document.put("activeRunningPlan", activeRunningPlanDocument);
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            nitriteId = document.getId();
            uuid = (String) document.get("uuid");
            firstName = (String) document.get("firstName");
            lastName = (String) document.get("lastName");
            emailAddress = (String) document.get("emailAddress");
            birthday = (Date) document.get("birthday");
            gender = (int) document.get("gender");
            trainingLevel = (int) document.get("trainingLevel");
            maxPulse = (int) document.get("maxPulse");

            Document activeRunningPlanDocument = (Document) document.get("activeRunningPlan");
            if (activeRunningPlanDocument != null) {
                RunningPlan activeRunningPlan = new RunningPlan();
                activeRunningPlan.read(mapper, activeRunningPlanDocument);
                this.activeRunningPlan = activeRunningPlan;
            }
        }
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

    @Override
    public NitriteId getNitriteId() {
        return nitriteId;
    }
}
