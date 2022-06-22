package de.hirola.sportsapplications.util;

import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.sportsapplications.model.RunningPlanEntry;
import de.hirola.sportsapplications.model.RunningUnit;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.validate.ValidationException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An util class for managing iCAL files.
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
     * @throws SportsLibraryException if the iCAL file not read or the data could not be imported.
     * @throws ValidationException has the file not a valid format
     */
    public static void importICAL(@NotNull SportsLibrary sportsLibrary, @NotNull File importFile)
            throws SportsLibraryException, ValidationException {
        RunningPlan runningPlan = loadRunningPlanFromICAL(sportsLibrary, importFile);
        sportsLibrary.add(runningPlan);
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
        final File parentDirectory = exportFile.getParentFile();
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

    public static RunningPlan loadRunningPlanFromICAL(@NotNull SportsLibrary sportsLibrary, @NotNull File importFile)
            throws SportsLibraryException, ValidationException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        if (importFile.exists()) {
            if (importFile.isFile() && importFile.canRead()) {
                try {
                    // reading the iCAL file using the ical4j library
                    final FileInputStream fin = new FileInputStream(importFile.getPath());
                    final CalendarBuilder builder = new CalendarBuilder();
                    final Calendar runningPlanCalendar = builder.build(fin);
                    runningPlanCalendar.validate();
                    final ComponentList<CalendarComponent> components = runningPlanCalendar.getComponents();
                    final int numberOfEvents = components.size(); // should be equal to the number of running plan entries
                    // get the name and remarks for the running plan
                    // e.g. "10-km-Trainingsplan Flex10 (lauftipps.ch/LT273)"
                    String name = applicationResources.getString("ical.event.runningplan.name");
                    String remarks = applicationResources.getString("ical.event.runningplan.remarks");
                    if (numberOfEvents > 0) {
                        final VEvent event = (VEvent) components.get(0);
                        event.validate();
                        final String description = event.getDescription().getValue();
                        final int startIndexOfRemarks =  description.indexOf('(');
                        final int startIndexOfNewLine =  description.indexOf('\n');
                        if (startIndexOfNewLine > -1 && startIndexOfRemarks > - 1) {
                            name = description.substring(0, startIndexOfRemarks - 1); // "10-km-Trainingsplan Flex10"
                            remarks = description.substring(startIndexOfRemarks + 1, startIndexOfNewLine - 1); // "lauftipps.ch/LT273"
                        } else if (startIndexOfNewLine > -1 ){
                            name = description.substring(0, startIndexOfNewLine); // "10-km-Trainingsplan Flex10 (lauftipps.ch/LT273)"
                        }
                    } else {
                        throw new SportsLibraryException("The iCAL file contains no events.");
                    }
                    // try to extract the data from event
                    final List<RunningPlanEntry> entries = new ArrayList<>();
                    for (CalendarComponent component: components) {
                        final VEvent event = (VEvent) component;
                        event.validate();
                        entries.add(getDataFromEvent(sportsLibrary, event));
                    }
                    if (entries.size() != numberOfEvents) {
                        // import failed - number running plan entries must be same as number of events
                        if (sportsLibrary.isDebugMode()) {
                            sportsLibrary.debug("The number of running plan entries is not equal the numbers of events.");
                        }
                        throw new SportsLibraryException("Error while importing the iCAl. The parsing was not correct.");
                    }
                    // create running plan
                    return new RunningPlan(name, remarks, 99, entries, false);
                } catch (ParserException exception) {
                    String errorMessage = "Error while loading the iCAL.";
                    if (sportsLibrary.isDebugMode()) {
                        sportsLibrary.debug(exception, "iCAL parsing error");
                    }
                    throw new SportsLibraryException(errorMessage + " " + exception.getMessage());
                } catch (IOException exception) {
                    String errorMessage = "Error while reading the import file.";
                    if (sportsLibrary.isDebugMode()) {
                        sportsLibrary.debug(exception, errorMessage);
                    }
                    throw new SportsLibraryException();
                }
            } else {
                throw new SportsLibraryException("The file " + importFile + " is not a file or could not be read.");
            }
        } else {
            throw new SportsLibraryException("The file " + importFile + " does not exist.");
        }
    }

    private static RunningPlanEntry getDataFromEvent(SportsLibrary sportsLibrary, VEvent event) {
        /*
        typical format - info in description:
        WOCHE 1 (ab 06.06.2022)
        ----------
        Lauf #2: > Intervalltraining: 10 min EL, IV, 10 min laDL [----> v0.1]
        Lauf #2: > IV= 3 x 3 min (5-km-Tempo), dazwischen 4 min TP [----> v0.1]
        Dauer: 37 min [----> v0.1]
        Puls: 2a: 115 bis 121
        Puls: 2b: 162 bis 168
        Tempo 2a: 08:59 min|km
        Tempo 2b: 05:57 min|km
        Distanz: 4.6 km [----> v0.1]
        ----------
        */
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        int day = 0, week = 0;
        long duration = 0L;
        double distance = 0.0;
        final String description = event.getDescription().getValue();
        final Summary eventTitle = event.getSummary();

        // get the week and day for the running entry
        try {
            eventTitle.validate();
            final String eventTitleValue = eventTitle.getValue(); // e. g. Lauftraining: Woche 1 - Lauf Nummer: 1
            final int indexOfWeek = eventTitleValue.indexOf(Global.ICALPattern.WEEK_PATTERN);
            final int indexOfDay = eventTitleValue.indexOf(Global.ICALPattern.DAY_PATTERN);
            try {
                week = Integer.parseInt(eventTitleValue.substring(indexOfWeek
                        + Global.ICALPattern.WEEK_PATTERN.length() + 1, indexOfDay -1));
            } catch(NumberFormatException exception) {
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug(exception, "Could not parse the week from summary. Set default value.");
                }
            }
            try {
                day = Integer.parseInt(eventTitleValue.substring(indexOfDay
                        + Global.ICALPattern.DAY_PATTERN.length() + 1));
            } catch(NumberFormatException exception) {
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug(exception, "Could not parse the day from summary. Set default value.");
                }
            }
        } catch (ValidationException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug(exception, "Event summary not valid. Set default values for week and day.");
            }
        }

        // get the duration of running plan entry (in minutes) - the first occurrences of "Dauer:"
        int startIndexOfDuration = description.indexOf(Global.ICALPattern.DURATION_PATTERN);
        if (startIndexOfDuration > -1) {
            int startIndexOfNewLine =  description.indexOf('\n', startIndexOfDuration);
            if (startIndexOfNewLine > -1) {
                final String completeDurationString =
                        description.substring(startIndexOfDuration + Global.ICALPattern.DURATION_PATTERN.length(),
                                startIndexOfNewLine - 4); // -4: ' min'
                try {
                    duration = Integer.parseInt(completeDurationString);
                } catch(NumberFormatException exception) {
                    // e.g. "Dauer: Einlaufen|Auslaufen: 20 min || Wettkampf: 63 min"
                    // try to extract from string
                    int startIndexOfDurationSeparator = 0;
                    while (true) {
                        startIndexOfDurationSeparator = completeDurationString.indexOf(":", startIndexOfDurationSeparator);
                        int startIndexOfMinutes = completeDurationString.indexOf("min", startIndexOfDurationSeparator);
                        if (startIndexOfDurationSeparator == -1 || startIndexOfMinutes == -1) {
                            break;
                        }
                        final String durationString = completeDurationString.substring(startIndexOfDurationSeparator + 2, startIndexOfMinutes - 1);
                        try {
                            duration += Integer.parseInt(durationString);
                            startIndexOfDurationSeparator+= 1;
                        } catch (NumberFormatException exception1) {
                            if (sportsLibrary.isDebugMode()) {
                                sportsLibrary.debug(exception, "Could not parse the duration from description. Set default value.");
                            }
                            break;
                        }
                    }
                }
            }
        }
        // get the distance of running plan entry (in kilometer) - the first occurrences of "Distanz:"
        int startIndexOfDistance = description.indexOf(Global.ICALPattern.DISTANCE_PATTERN);
        if (startIndexOfDistance > -1) {
            int startIndexOfNewLine =  description.indexOf('\n', startIndexOfDistance);
            if (startIndexOfNewLine > -1) {
                final String distanceString = description.substring(startIndexOfDistance + Global.ICALPattern.DISTANCE_PATTERN.length(),
                        startIndexOfNewLine - 3); // -3: ' km'
                try {
                    distance = Double.parseDouble(distanceString);
                } catch(NumberFormatException exception) {
                    if (sportsLibrary.isDebugMode()) {
                        sportsLibrary.debug(exception, "Could not parse the distance from description. Set default value.");
                    }
                }
            }
        }

        // create running plan units from description
        final List<RunningUnit> runningUnits = new ArrayList<>();
        // get the type of running string - occurrences of "Lauf:", can span multiple lines
        // get the (complete) pulse string - occurrences of e.g. "Puls: 2a: 115 bis 121"
        // get the (complete) pace string - occurrences of e.g. "Tempo 2a: 08:59 min|km"
        int startIndexOfNewLineForRunningString = 0, startIndexOfNewLineForPulseString = 0, startIndexOfNewLineForPaceString = 0;
        String typeOfRunningString, completePulseString, completePaceString;
        while (true) {
            final RunningUnit runningUnit = new RunningUnit();
            // running information
            int startIndexOfTypeOfRunningString = description.indexOf(Global.ICALPattern.TYPE_OF_RUNNING_STRING_PATTERN, startIndexOfNewLineForRunningString);
            startIndexOfNewLineForRunningString =  description.indexOf('\n', startIndexOfTypeOfRunningString);
            if (startIndexOfTypeOfRunningString > -1 && startIndexOfNewLineForRunningString > -1) {
                typeOfRunningString = description.substring(startIndexOfTypeOfRunningString, startIndexOfNewLineForRunningString);
                runningUnit.setRunningInfos(typeOfRunningString);
                // get the next occurrences of "Lauf:"
                startIndexOfNewLineForRunningString+=1;
            } else {
                break;
            }
            // pulse information
            int startIndexOfCompletePulseString = description.indexOf(Global.ICALPattern.PULSE_PATTERN, startIndexOfNewLineForPulseString);
            startIndexOfNewLineForPulseString =  description.indexOf('\n', startIndexOfCompletePulseString);

            if (startIndexOfCompletePulseString > -1 && startIndexOfNewLineForPulseString > -1) {
                completePulseString = description.substring(startIndexOfCompletePulseString, startIndexOfNewLineForPulseString);
                final Number[] pulseValues = getPulseFromString(completePulseString); // 0... lower, 1... upper
                runningUnit.setLowerPulseLimit(pulseValues[0].intValue());
                runningUnit.setUpperPulseLimit(pulseValues[1].intValue());
                // get the next occurrences of "Puls:"
                startIndexOfNewLineForPulseString+=1;
            }
            // pace information
            int startIndexOfCompletePaceString = description.indexOf(Global.ICALPattern.PACE_PATTERN, startIndexOfNewLineForPaceString);
            startIndexOfNewLineForPaceString =  description.indexOf('\n', startIndexOfCompletePaceString);

            if (startIndexOfCompletePaceString > -1 && startIndexOfNewLineForPaceString > -1) {
                completePaceString = description.substring(startIndexOfCompletePaceString, startIndexOfNewLineForPaceString);
                runningUnit.setPace(getPaceFromString(completePaceString));
                // get the next occurrences of "Puls:"
                startIndexOfNewLineForPaceString+=1;
            }
            // add to the list
            runningUnits.add(runningUnit);
        }

        // get the training day from event
        final DtStart eventStartDate = event.getStartDate();
        final Date date = eventStartDate.getDate();
        final TimeZone timeZone = eventStartDate.getTimeZone();
        final LocalDate runningDate = date.toInstant().atZone(timeZone.toZoneId()).toLocalDate();
        // if the entry contains only on unit - set the duration from entry to unit
        if (runningUnits.size() == 1) {
            runningUnits.get(0).setDuration(duration);
        }
        // create a running plan entry
        return new RunningPlanEntry(week, day, runningDate, duration, distance, description, runningUnits);
    }

    private static Number[] getPulseFromString(@NotNull String completePulseString) {
        // get the pulse from string e.g. "Puls: 2a: 115 bis 121"
        final Number[] pulseValues = new Number[2];
        int indexOfPulseString = completePulseString.indexOf(Global.ICALPattern.PULSE_PATTERN);
        if (indexOfPulseString == -1) { // pulse string was not found
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
        String pulseValuesString;
        int startIndexOfOfPulseValues = completePulseString.lastIndexOf(":") + 2;
        if (startIndexOfOfPulseValues > 0) {
            pulseValuesString = completePulseString.substring(startIndexOfOfPulseValues);
        } else {
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
        int startIndexOfPulseSeparatorPattern = pulseValuesString.indexOf(Global.ICALPattern.PULSE_SEPARATOR_PATTERN);
        if (startIndexOfPulseSeparatorPattern == -1) {
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
        final String lowerPulseLimitString = pulseValuesString.substring(0,startIndexOfPulseSeparatorPattern - 1);
        final String upperPulseLimitString = pulseValuesString.substring(startIndexOfPulseSeparatorPattern
                + Global.ICALPattern.PULSE_SEPARATOR_PATTERN.length()
                + 1);
        try {
            pulseValues[0] = Integer.parseInt(lowerPulseLimitString);
            pulseValues[1] = Integer.parseInt(upperPulseLimitString);
            return pulseValues;
        } catch (NumberFormatException exception) {
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
    }

    private static long getPaceFromString(@NotNull String completePaceString) {
        // get the pace from string e.g. "Tempo 2a: 08:59 min|km"
        int indexOfPaceString = completePaceString.indexOf(Global.ICALPattern.PACE_PATTERN);
        if (indexOfPaceString == -1) { // pace string was not found
            return 0L;
        }
        int startIndexOfOfPaceValue = completePaceString.indexOf(":") + 1; // 08:59 min|km
        int endIndexOfOfPaceValue = completePaceString.indexOf(Global.ICALPattern.PACE_UNIT_PATTERN);
        if (startIndexOfOfPaceValue > 0 && endIndexOfOfPaceValue > -1) {
            final String paceString = completePaceString.substring(startIndexOfOfPaceValue, endIndexOfOfPaceValue - 1); // 08:59
            try {
                // 08:59 -> convert in seconds
                final int separatorIndex = paceString.indexOf(":");
                final String minutesString = paceString.substring(1, separatorIndex);
                final String secondsString = paceString.substring(separatorIndex + 1);
                long paceInSeconds = Long.parseLong(minutesString) * 60;
                paceInSeconds += Long.parseLong(secondsString) % 60;
                return paceInSeconds;
            } catch (NumberFormatException exception) {
                return 0L;
            }
        }
        return 0L;
    }
}
