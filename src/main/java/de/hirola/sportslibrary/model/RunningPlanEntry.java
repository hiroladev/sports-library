package de.hirola.sportslibrary.model;

import com.onyx.persistence.annotations.Attribute;
import com.onyx.persistence.annotations.Entity;
import com.onyx.persistence.annotations.Identifier;
import com.onyx.persistence.annotations.Relationship;
import com.onyx.persistence.annotations.values.CascadePolicy;
import com.onyx.persistence.annotations.values.FetchPolicy;
import com.onyx.persistence.annotations.values.RelationshipType;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.util.UUIDFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An entry in the running plan contains the day and the respective training sections for the individual weeks.
 * An example:
 * Week: 3, Day: 1 (Monday), 7 min total, 2 min run, 3 min slow walk, 2 min run
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
@Entity
public class RunningPlanEntry extends PersistentObject {

    @Attribute
    @Identifier
    private final String uuid = UUIDFactory.generateUUID();
    @Attribute
    private final int week; // number of week, begins with 1
    @Attribute
    private final int day; // day of week, begins with 1 (monday)
    @Relationship(type = RelationshipType.ONE_TO_MANY,
            inverseClass = RunningUnit.class,
            inverse = "relationAttributeForRunningPlanEntryToRunningPlanUnit",
            cascadePolicy = CascadePolicy.ALL,
            fetchPolicy = FetchPolicy.LAZY)
    private final List<RunningUnit> runningUnits; // units if training day
    @Relationship(type = RelationshipType.MANY_TO_ONE,
            inverseClass = RunningPlan.class,
            inverse = "entries",
            cascadePolicy = CascadePolicy.ALL,
            fetchPolicy = FetchPolicy.LAZY)
    private RunningPlan relationAttributeForRunningPlanToRunningPlanEntry; // defined only for modelling the relationship 1:m

    /**
     * Default constructor for reflection.
     */
    public RunningPlanEntry() {
        super();
        day = 1;
        week = 1;
        runningUnits = new ArrayList<>();
    }

    /**
     * Create a running plan entry.
     *
     * @param day of entry
     * @param week of entry
     * @param runningUnits of entry
     * @see RunningUnit
     */
    public RunningPlanEntry (int day, int week, @NotNull ArrayList<RunningUnit> runningUnits) {
        this.day = day;
        this.week = week;
        this.runningUnits = runningUnits;
    }

    /**
     * Get the (rounded) percentage of completed training sessions.
     *
     * @return The (rounded) percentage of completed training sessions
     */
    public int getDay() {
        return day;
    }

    /**
     * Get the (rounded) percentage of completed training sessions.
     *
     * @return The (rounded) percentage of completed training sessions
     */
    public int getWeek() {
        return week;
    }

    /**
     * Get the (rounded) percentage of completed training sessions.
     *
     * @return The (rounded) percentage of completed training sessions
     */
    public List<RunningUnit> getRunningUnits() {
        return runningUnits;
    }

    /**
     * The total duration of the training session in minutes,
     * i.e. the sum of the individual training sections.
     *
     * @return Sum of the individual training sections in minutes
     */
    public long getDuration() {
        return runningUnits.stream().map(RunningUnit::getDuration).reduce(0L, Long::sum);
    }

    /**
     * Indicates whether the run plan entry (training day) has been completed.
     * Is automatically <b>true</b> if all training sections are completed.
     *
     * @return A flag to determine if all units of entry completed
     */
    public boolean isCompleted() {
        return runningUnits.stream().allMatch(RunningUnit::isCompleted);
    }

    /**
     * (Rounded) percentage of run segments completed.
     *
     * @return Percentage of run segments completed
     */
    public int percentCompleted() {
        int countOfEntries = runningUnits.size();
        int percent = 0;
        int completed = 0;

        if (countOfEntries > 0) {
            for (RunningUnit runningUnit : runningUnits) {
                if (runningUnit.isCompleted()) {
                    completed += 1;
                }
            }
            completed = (completed * 100) / countOfEntries;
        }
        return completed;
    }

    @Override
    public boolean equals(Object o) {
        // gleicher Tag in gleicher Woche = Object ist identisch
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RunningPlanEntry that = (RunningPlanEntry) o;
        return week == that.week && day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, week, day);
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}