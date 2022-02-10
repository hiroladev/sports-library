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
 * @since 0.0.1
 */
public class RunningPlanTemplate {
    // Name des Templates
    public final String name;
    // Eine paar kurze Anmerkungen der Vorlage.
    public final String remarks;
    // Nummer für Reihenfolge der Laufpläne
    public final int orderNumber;
    public final boolean isTemplate;
    // Array von einzelnen Trainingsabschnitten in der Form **{"Dauer:Stil"},{Dauer:Stil"}**, ...
    // Die Zuordnung Stil zu Geschwindigkeit kann jeder User selbst treffen, auch eigene Stile sind möglich.
    public final List<RunningPlanTemplateUnit> trainingUnits;

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

    public String getName() {
        return name;
    }

    public String getRemarks() {
        return remarks;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public List<RunningPlanTemplateUnit> getTrainingUnits() {
        return trainingUnits;
    }
}
