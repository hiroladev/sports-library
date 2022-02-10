package de.hirola.sportslibrary.database;

import com.onyx.exception.OnyxException;
import com.onyx.persistence.manager.PersistenceManager;
import com.onyx.persistence.query.Query;
import com.onyx.persistence.query.QueryCriteria;
import com.onyx.persistence.query.QueryCriteriaOperator;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Adds a persistence layer, encapsulating the actual data storage technology used.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */
public final class DataRepository {

    private final String TAG = DataRepository.class.getSimpleName();

    private final Logger logger = Logger.getInstance(null);
    private final PersistenceManager persistenceManager; // we use the Onxy Embedded Persistence Manager

    /**
     * Create the local datastore access layer.
     *
     * @param appName of the app using this library
     */
    public DataRepository(@Nullable String appName) {
        persistenceManager = DatabaseManager.getInstance(appName).getPersistenceManager(); // can be null
    }

    /**
     * Get a flag to determine if the datastore is empty.
     * Some templates are required, which must be imported at the first start.
     * If an error occurred while determine the state, the result is true.
     *
     * @return A flag to determine if the datastore is empty
     */
    public boolean isEmpty() {
        // the datastore is "empty" if there are no movement and training types or running plans (yet)
        int emptyValueCounter = 0;
        QueryCriteria criteria1 = new QueryCriteria("uuid", QueryCriteriaOperator.EQUAL, "*");
        Query query1 = new Query(MovementType.class, criteria1);

        QueryCriteria criteria2 = new QueryCriteria("uuid", QueryCriteriaOperator.EQUAL, "*");
        Query query2 = new Query(TrainingType.class, criteria1);

        QueryCriteria criteria23= new QueryCriteria("uuid", QueryCriteriaOperator.EQUAL, "*");
        Query query3 = new Query(RunningPlan.class, criteria1);

        try {
            List<MovementType> result1 = persistenceManager.executeQuery(query3);
            List<MovementType> result2 = persistenceManager.executeQuery(query3);
            List<MovementType> result3 = persistenceManager.executeQuery(query3);
            if (result1.isEmpty() && result2.isEmpty() && result3.isEmpty() ){
                return true;
            }

        } catch (OnyxException exception) {
            logger.log(Logger.DEBUG, TAG, "Error occurred while searching.", exception);
            return true;
        }
        return false;
    }

    /**
     * Get a flag to determine if the datastore is open.
     *
     * @return A flag to determine if the datastore is open.
     */
    public boolean isOpen() {
        return persistenceManager != null;

    }

    /**
     * Add a new or save an existing object.
     *
     * @param object to be added
     * @throws SportsLibraryException if an error occurred while adding
     */
    public void save(PersistentObject object) throws SportsLibraryException {
        // Onyx does not differentiate between updating or inserting entities.
        // If an entity with a matching primary key already exists,
        // Onyx Database will assume you are updating the entity specified and overwrite the existing record.
        try {
            persistenceManager.saveEntity(object);
        } catch (OnyxException exception) {
            logger.log(Logger.DEBUG, TAG, "Saving the object with id " + object.getUUID() + " failed.", exception);
            throw new SportsLibraryException(exception);
        }
    }

    /**
     * Removes an existing object from the local datastore.
     *
     * @param object to be removed
     * @throws SportsLibraryException if an error occurred while removing
     */
    public void delete(PersistentObject object) throws SportsLibraryException {
        try {
            persistenceManager.deleteEntity(object);
        } catch (OnyxException exception) {
            logger.log(Logger.DEBUG, TAG, "Deleting the object with id " + object.getUUID() + " failed.", exception);
            throw new SportsLibraryException(exception);
        }
    }

    @Nullable
    public PersistentObject findByUUID(@NotNull Class<? extends PersistentObject> withType, @NotNull String uuid) {
        try {
            return persistenceManager.findById(withType, uuid);
        } catch (OnyxException exception) {
            logger.log(Logger.DEBUG, TAG, "Error while searching an object from type "
                    + withType + " and id " + uuid,  exception);
        }
        return null;
    }

    /**
     * Get all objects with a given type. If an error occurred while finding
     * the objects, the list is empty too.
     * Errors will be logged.
     *
     * @param fromType of object to get
     * @return A list of objects with the given type. The list can be empty.
     */
    public List<? extends PersistentObject> findAll(Class<? extends PersistentObject> fromType)  {
        List<? extends PersistentObject> results = new ArrayList<>();
        try {
            results = persistenceManager.list(fromType);
            // check and correct the start date if running plan not active
            for (PersistentObject persistentObject : results) {
                if (persistentObject instanceof RunningPlan) {
                    RunningPlan runningPlan = (RunningPlan) persistentObject;
                    if (!runningPlan.isCompleted() || !runningPlan.isActive()) {
                        Date startDate = runningPlan.getStartDate();
                        LocalDate actualStartDate = Instant.ofEpochMilli(startDate.getTime())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        LocalDate today = LocalDate.now(ZoneId.systemDefault());
                        if (actualStartDate.isBefore(today) || actualStartDate.isEqual(today)) {
                            // the method adjust the start day automatically
                            runningPlan.setStartDate(java.sql.Date.valueOf(today));
                            try {
                                // save the corrected start date to local data store
                                persistenceManager.saveEntity(runningPlan);
                            } catch (OnyxException exception) {
                                logger.log(Logger.DEBUG, TAG, "Saving the new start day of running plan with id "
                                        + runningPlan.getUUID() + " failed.", exception);
                            }
                        }
                    }
                }
            }

        } catch (OnyxException exception) {
            logger.log(Logger.DEBUG, TAG, "Error occurred while searching all objects from type " + fromType.getSimpleName(), exception);
        }
        return results;
    }
}
