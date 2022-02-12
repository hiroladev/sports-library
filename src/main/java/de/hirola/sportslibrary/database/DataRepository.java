package de.hirola.sportslibrary.database;

import com.onyx.exception.OnyxException;
import com.onyx.persistence.manager.PersistenceManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private final DatabaseManager databaseManager;
    private final PersistenceManager persistenceManager; // we use the Onxy Embedded Persistence Manager

    /**
     * Create the local datastore access layer.
     *
     * @param appName of the app using this library
     */
    public DataRepository(@Nullable String appName) {
        databaseManager = DatabaseManager.getInstance(appName);
        persistenceManager = databaseManager.getPersistenceManager(); // can be null
    }

    /**
     * Get a flag to determine if the datastore is empty.
     * Some templates are required, which must be imported at the first start.
     * If an error occurred while determine the state, the result is true.
     *
     * @return A flag to determine if the datastore is empty
     */
    public boolean isEmpty() {
        // the datastore is "empty" if there are no movement [and training types] and running plans (yet)
        try {
            List<PersistentObject> result1 = persistenceManager.list(MovementType.class);
            List<PersistentObject> result2 = persistenceManager.list(TrainingType.class);
            List<PersistentObject> result3 = persistenceManager.list(RunningPlan.class);
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
            // Embedded objects are created or updated when saving.
            // Unfortunately, cascading delete doesn't work. To be on the safe side,
            // the embedded objects are therefore deleted independently.
            // I tried everything based on this page (https://www.onyx.dev/learn/tutorials/1/relationships/cascade-policy).
            // Without success. It sucks. Hence, this workaround:
            // first we delete the "parent" object in the database
            persistenceManager.deleteEntity(object);
            // now we need to delete all embedded objects (with exceptions)
            cascadingDeleteForObject(object);
        } catch (OnyxException exception) {
            String errorMessage = "Deleting the object with id " + object.getUUID() + " failed.";
            logger.log(Logger.ERROR, TAG, errorMessage, exception);
            // rollback
            try {
                persistenceManager.saveEntity(object);
            } catch (OnyxException exception1) {
                // now everything has gone wrong
                logger.log(Logger.ERROR, TAG, errorMessage, exception);
                throw new SportsLibraryException(errorMessage + " Cascading delete failed: " + exception.getMessage());
            }
            throw new SportsLibraryException(errorMessage + ": " + exception.getMessage());
        }
    }

    /**
     * Get an object from given type with given UUID.
     *
     * @param withType of object to find
     * @param uuid of object to find
     * @return The object from given type and the given UUID or null if the object was not found
     *         or an error occurred while searching
     */
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
                        LocalDate startDate = runningPlan.getStartDate();
                        LocalDate today = LocalDate.now(ZoneId.systemDefault());
                        if (startDate.isBefore(today) || startDate.isEqual(today)) {
                            // the method adjust the start day automatically
                            runningPlan.setStartDate(today);
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

    public void clearAll() {
        databaseManager.clearAll();
    }

    public void close() {
        databaseManager.close();
    }

    // workaround for delete embedded objects
    private void cascadingDeleteForObject(PersistentObject object) throws SportsLibraryException {
        // not all types should be deleted as embedded objects
        try {
            Field[] attributes = object.getClass().getDeclaredFields();
            for (Field attribute : attributes) {
                Class<?> clazz = attribute.getType();
                // embedded list object
                if (clazz.getSimpleName().equalsIgnoreCase("List")) {
                    // get a list element
                    Class<?> listElementClazz = ((Class<?>) ((ParameterizedType) attribute.getGenericType()).getActualTypeArguments()[0]);
                    // contains the list objects from type PersistentObject?
                    if (PersistentObject.class.isAssignableFrom(listElementClazz)) {
                        // should this type delete?
                        if (Global.CASCADING_DELETED_CLASSES.contains(listElementClazz)) {
                            // try to delete all objects from this list
                            attribute.setAccessible(true);
                            Object listAttributeObject = attribute.get(object);
                            if (listAttributeObject instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<PersistentObject> persistentObjectList = (List<PersistentObject>) listAttributeObject;
                                // the object can contain other embedded objects
                                for (PersistentObject persistentObject : persistentObjectList) {
                                    // check if the object (still) exists
                                    String uuid = persistentObject.getUUID();
                                    PersistentObject savedPersistentObject = findByUUID(persistentObject.getClass(), uuid);
                                    if (savedPersistentObject != null) {
                                        // recalls this func
                                        delete(persistentObject);
                                    }
                                }
                            }
                        }
                    }
                }
                // persistent object
                else {
                    // contains the list objects from type PersistentObject?
                    if (PersistentObject.class.isAssignableFrom(clazz)) {
                        // should this type delete?
                        if (Global.CASCADING_DELETED_CLASSES.contains(clazz)) {
                            attribute.setAccessible(true);
                            PersistentObject persistentObject = (PersistentObject) attribute.get(object);
                            String uuid = persistentObject.getUUID();
                            PersistentObject savedPersistentObject = findByUUID(persistentObject.getClass(), uuid);
                            if (savedPersistentObject != null) {
                                // recalls this func
                                delete(persistentObject);
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException exception) {
            throw new SportsLibraryException(exception);
        }
    }
}
