package de.hirola.sportslibrary.util;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Index of available running plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
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

    /**
     * Create an object which represents a json file with a template for import.
     *
     * @param name of running plan
     * @param fileName of template (json) file which contains the content of running plan
     */
    public RunningPlanTemplateFile(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    /**
     * Get the name of running plan.
     *
     * @return The name of the running plan
     */
    public String getName() {
        return name;
    }

    /**
     * Get the file name of template (json) file.
     *
     * @return The file name of template (json) file
     */
    public String getFileName() {
        return fileName;
    }
}
