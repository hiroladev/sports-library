package de.hirola.sportslibrary.util;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Index of available running plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningPlanTemplateFile {

    public final String name;
    public final String fileName; // name of json file

    /**
     * Default constructor for import from json.
     */
    public RunningPlanTemplateFile() {
        this.name = "";
        this.fileName = "";
    }
}
