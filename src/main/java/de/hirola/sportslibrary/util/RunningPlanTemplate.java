package de.hirola.sportslibrary.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Mapping object for RunningPlan to import from JSON.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class RunningPlanTemplate {
    public final String name; // name of the template
    public final String remarks; // remarks of the template
    public final int orderNumber; // number for the order of training
    public boolean isTemplate; // setting true while initial app import
    public final List<RunningPlanTemplateUnit> trainingUnits; // training units

    /**
     * Default constructor for import from json.
     */
    public RunningPlanTemplate() {
        this.name = "";
        this.remarks = "";
        this.orderNumber = 0;
        this.isTemplate = false;
        this.trainingUnits = new ArrayList<>();
    }

    public RunningPlanTemplate(String name, String remarks, int orderNumber, List<RunningPlanTemplateUnit> trainingUnits) {
        this.name = name;
        this.remarks = remarks;
        this.orderNumber = orderNumber;
        this.isTemplate = true;
        this.trainingUnits = trainingUnits;
    }

    public RunningPlanTemplate(String name, String remarks, int orderNumber, boolean isTemplate, List<RunningPlanTemplateUnit> trainingUnits) {
        this.name = name;
        this.remarks = remarks;
        this.orderNumber = orderNumber;
        this.isTemplate = isTemplate;
        this.trainingUnits = trainingUnits;
    }
}
