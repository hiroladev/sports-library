package de.hirola.sportslibrary.util;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Mapping object for RunningPlanUnit to import from JSON.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public class RunningPlanTemplateUnit {
    // Woche des Trainingsabschnittes in der Woche
    private final int week;
    // Tag des Trainingsabschnittes in der Woche
    private final int day;
    // Gesamtdauer des jeweiligen Trainingsabschnittes in min
    private final int duration;
    // Array von einzelnen Trainingsabschnitten in der Form {"Dauer:Stil"},{Dauer:Stil"}, ..
    private final String[] units;

    /**
     * Default constructor for import from json.
     */
    public RunningPlanTemplateUnit() {
        this.week = 1;
        this.day = 1;
        this.duration = 1;
        this.units = new String[2];
    }

    /**
     * Create a template object.
     *
     * @param week of unit
     * @param day of unit
     * @param duration of unit
     * @param units of unit
     */
    public RunningPlanTemplateUnit(int week, int day, int duration, String[] units) {
        this.week = week;
        this.day = day;
        this.duration = duration;
        this.units = units;
    }

    /**
     * Get week of unit.
     *
     * @return The week of unit, starting by 1
     */
    public int getWeek() {
        return week;
    }

    /**
     * Get day of unit.
     *
     * @return The day of unit, starting by 1 (monday)
     */
    public int getDay() {
        return day;
    }

    /**
     * Get duration of unit in minutes.
     *
     * @return The week of unit, starting by 1
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get a List of movements with durations.
     * Example: Format "L", "1" means movement type with key L and a duration of 1 minute.
     *
     * @return The week of unit, starting by 1
     */
    public String[] getUnits() {
        return units;
    }
}