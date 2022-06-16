package de.hirola.sportsapplications.util;

import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.RunningPlan;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.validate.ValidationException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A util class for managing GPX files-
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 *
 */
public final class ICALManager {

    /**
     * Import a running plan in iCAL format from <a href="https://lauftipps.ch/">laufplan.ch</a>
     * and add it to the local datastore.
     *
     * @param sportsLibrary in which the track import should become
     * @param importFile with data in iCAL format
     * @throws IOException if the iCAL file not read or the data could not be imported.
     * @throws ValidationException has the file not a valid format
     */
    public static void importICAL(@NotNull SportsLibrary sportsLibrary, @NotNull File importFile)
            throws IOException, ValidationException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        if (importFile.exists()) {
            if (importFile.isFile() && importFile.canRead()) {
                try {
                    // reading the iCAL file using the ical4j library
                    FileInputStream fin = new FileInputStream(importFile.getPath());
                    CalendarBuilder builder = new CalendarBuilder();
                    Calendar runningPlanCalendar = builder.build(fin);
                    runningPlanCalendar.validate();
                    ComponentList<CalendarComponent> components = runningPlanCalendar.getComponents();
                    for (CalendarComponent component: components) {
                        VEvent event = (VEvent) component;
                        System.out.println(event.getStartDate().getDate().toString());
                        System.out.println(event.getEndDate().getDate().toString());
                        System.out.println(event.getSummary());
                    }
                    // creating a running plan from calendar

                    // add to the local datastore

                } catch (ParserException exception) {
                    throw new IOException("Error while loading the iCAL.", exception);
                }
            } else {
                throw new IOException("The file " + importFile + " is not a file or could not be read.");
            }
        } else {
            throw new IOException("The file " + importFile + " does not exist.");
        }
    }

    /**
     * Export a given running plan from the sports library to a iCAL file.
     * Not implemented yet.
     *
     * @param runningPlan to be exported
     * @param exportFile for the iCAL
     * @throws IOException if the export failed
     */
    public static void exportICAL(@NotNull RunningPlan runningPlan,
                                  @NotNull File exportFile) throws IOException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        File parentDirectory = exportFile.getParentFile();
        if (parentDirectory.exists()) {
            if (parentDirectory.isDirectory() && parentDirectory.canWrite()) {
                throw new IOException("Sorry - Not implemented yet.");
            } else {
                throw new IOException("The file " + exportFile + " is not a file or is not writeable.");
            }
        } else {
            throw new IOException("The file " + exportFile + " does not exist.");
        }
    }
}
