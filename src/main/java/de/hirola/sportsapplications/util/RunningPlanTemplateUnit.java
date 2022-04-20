package de.hirola.sportsapplications.util;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Mapping object for RunningPlanUnit to import from JSON.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public class RunningPlanTemplateUnit {

    public final int week; // week of the training segment in the week
    public final int day; // day of the training segment in the week
    public final String[] units; // array of individual training sections in the form {"duration:style"},{duration:style"}, ..

    /**
     * Default constructor for import from json.
     */
    public RunningPlanTemplateUnit() {
        this.week = 1;
        this.day = 1;
        this.units = new String[2];
    }
}