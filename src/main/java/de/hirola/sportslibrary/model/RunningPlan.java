package de.hirola.sportslibrary.model;

import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.database.ListMapper;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.DateUtil;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Represents a running plan. Plans are included as templates,
 * which I kindly took from his with the permission of
 * Mr. Christian Zangl ([lauftipps.ch](https://lauftipps.ch/trainingsplaene/alle-trainingsplaene-auf-einen-blick)).
 * Users can create their own plans.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 *
 */

public class RunningPlan extends PersistentObject implements Comparable<RunningPlan> {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private String name;
    private String remarks;
    // Order number of plan, "Build-Up Training" starts with a low-numbered run plan
    private int orderNumber;
    private Date startDate;
    private boolean isTemplate; // templates must be not changed
    private List<RunningPlanEntry> entries; // training day with different units

    /**
     * Default constructor for reflection and database management.
     */
    public RunningPlan() {
        name = "";
        remarks = "";
        orderNumber = 0;
        startDate = DateUtil.getDateFromNow();
        // start day is monday
        adjustStartDate();
        entries = new ArrayList<>();
        isTemplate = false;
    }

    /**
     * Creates a running plan.
     *
     * @param name of plan
     * @param remarks of plan
     * @param orderNumber of plan
     * @param entries of plan
     * @param isTemplate can the plan be changed or deleted
     */
    public RunningPlan(@NotNull String name, @Nullable String remarks, int orderNumber, @NotNull List<RunningPlanEntry> entries, boolean isTemplate) {
        this.name = name;
        this.remarks = Objects.requireNonNullElse(remarks, "No description available.");
        this.orderNumber = orderNumber;
        this.entries = entries;
        this.isTemplate = isTemplate;
        startDate = DateUtil.getDateFromNow();
        adjustStartDate();
    }

    /**
     * Get the name of the plan.
     *
     * @return Name of the plan.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the plan.
     *
     * @param name of the plan
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the remarks of the plan.
     *
     * @return Remarks of the plan.
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Set the remarks of the plan.
     *
     * @param remarks of the plan
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Get the training order number of the plan.
     *
     * @return Order number of the plan.
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Set the orderNumber of the plan.
     * Determines the order of training.
     *
     * @param orderNumber of the plan
     */
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Get the start date of the plan.
     *
     * @return start date of the plan.
     */
    public LocalDate getStartDate() {
        return DateUtil.getLocalDateFromDate(startDate);
    }

    /**
     * Set the start date of the plan.
     *
     * @param startDate of the plan
     */
    public void setStartDate(LocalDate startDate) {
        // change the date only if running plan not active
        this.startDate = DateUtil.getDateFromLocalDate(startDate);
        if (!isActive()) {
            // correct start day (is monday)
            adjustStartDate();
        }
    }

    /**
     * Get the entries (days) of the plan.
     *
     * @return Entries (days) of the plan.
     * @see RunningPlanEntry
     */
    public List<RunningPlanEntry> getEntries() {
        return entries;
    }

    /**
     * Add a training entry to the plan.
     *
     * @param runningPlanEntry to be added
     * @see RunningPlanEntry
     */
    public void addEntry(RunningPlanEntry runningPlanEntry) {
        if (runningPlanEntry == null) {
            return;
        }
        if (entries == null) {
            entries = new ArrayList<>();
        }
        if (!entries.contains(runningPlanEntry)) {
            entries.add(runningPlanEntry);
        }
    }

    /**
     * Set all running units as uncompleted.
     */
    public void setUncompleted() {
        for (RunningPlanEntry entry : entries) {
            List<RunningUnit> units = entry.getRunningUnits();
            for (RunningUnit unit : units) {
                unit.setCompleted(false);
            }
        }
    }

    /**
     * Set a running unit as completed.
     *
     * @param completedUnit to set the state of completed
     */
    public void completeUnit(@NotNull RunningUnit completedUnit) {
        for (RunningPlanEntry entry: entries) {
            Optional<RunningUnit> optionalRunningUnit = entry.getRunningUnits()
                    .stream()
                    .filter(runningUnit -> runningUnit.equals(completedUnit))
                    .findFirst();
            // set completed
            optionalRunningUnit.ifPresent(runningUnit -> runningUnit.setCompleted(true));
        }
    }

    /**
     * Get a flag, if the plan is a template.
     * Templates must be not changed or deleted.
     *
     * @return A flag to determine if plan a template
     */
    public boolean isTemplate() {
        return isTemplate;
    }

    /**
     * Get the (rounded) percentage of completed training sessions.
     *
     * @return The (rounded) percentage of completed training sessions
     */
    public int percentCompleted() {
        int countOfEntries = entries.size();
        int completed = 0;

        if (countOfEntries > 0) {
            for (RunningPlanEntry entry : entries) {
                if (entry.isCompleted()) {
                    completed++;
                }
            }
            completed = (completed * 100) / countOfEntries;
        }
        return completed;
    }

    /**
     * Total duration for all training segments in minutes.
     *
     * @return Duration for all training units in minutes
     */
    public long getDuration() {
        return entries.stream().map(RunningPlanEntry::getDuration).reduce(0L, Long::sum);
    }

    /**
     * Indicates whether the schedule is selected and used.
     * The value is true if at least one session has been completed.
     *
     * @return A flag to determine if plan is active in training.
     * @see User
     */
    public boolean isActive() {
        return entries.stream().anyMatch(RunningPlanEntry::isCompleted);
    }

    /**
     * Indicates whether the run plan has already been completed, i.e. has been carried out.
     * Automatically true if all sessions have been completed.
     *
     * @return A flag to determine if the plan is completed
     */
    public boolean isCompleted() {
        return entries.stream().allMatch(RunningPlanEntry::isCompleted);
    }

    /**
     * Returns all training units (all days) of the given week.
     *
     * @param forWeek week for which the units are to be determined
     * @return List of all training sessions for the given week
     * @see RunningPlanEntry
     */
    public List<RunningPlanEntry> runningPlanEntriesForWeek(int forWeek) {
        List<RunningPlanEntry> runningPlanEntries = new ArrayList<>();
        if (forWeek <= 0 || entries.size() == 0) {
            //  ungültige Woche, keine Einträge = leere Liste
            return runningPlanEntries;
        }
        //  Einträge der Woche ermitteln
        for (RunningPlanEntry entry : entries) {
            if (entry.getWeek() == forWeek) {
                runningPlanEntries.add(entry);
            }
        }
        return runningPlanEntries;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("remarks", remarks);
        document.put("orderNumber", orderNumber);
        document.put("startDate", startDate);
        document.put("isTemplate", isTemplate);

        if (entries != null) {
            document.put("entries", ListMapper.toDocumentsList(mapper, entries));
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            remarks = (String) document.get("remarks");
            orderNumber = (int) document.get("orderNumber");
            startDate = (Date) document.get("startDate");
            isTemplate = (boolean) document.get("isTemplate");

            try {
                @SuppressWarnings("unchecked")
                List<Document> objectsDocument = (List<Document>) document.get("entries");
                entries = ListMapper.toElementsList(mapper, objectsDocument, RunningPlanEntry.class);
            } catch (ClassCastException | SportsLibraryException exception) {
                //TODO: logging?
                entries = new ArrayList<>();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Name = Objekt ist identisch
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RunningPlan that = (RunningPlan) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name);
    }

    @Override
    public int compareTo(@NotNull RunningPlan other) {
        // sort by order number
        return Integer.compare(this.orderNumber, other.orderNumber);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    // start day is monday
    private void adjustStartDate() {
        LocalDate today = DateUtil.getLocalDateFromNow();
        LocalDate actualStartDate = DateUtil.getLocalDateFromDate(startDate);
        if (actualStartDate.isBefore(today)) {
            // start day is in the past
            startDate = DateUtil.getDateFromLocalDate(today);
        }
        DayOfWeek dayOfWeek = actualStartDate.getDayOfWeek();
        if (dayOfWeek != DayOfWeek.MONDAY) {
            // from Tuesday the start date is next Monday
            long daysToAdd = 8 - dayOfWeek.getValue();
            actualStartDate = actualStartDate.plusDays(daysToAdd);
            startDate = DateUtil.getDateFromLocalDate(actualStartDate);
        }
    }

}
