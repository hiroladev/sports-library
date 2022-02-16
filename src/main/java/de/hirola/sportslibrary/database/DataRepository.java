package de.hirola.sportslibrary.database;

import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.util.Logger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

    private final Logger logger;
    private final Nitrite database; // we use Nitrite database

    /**
     * Create the local datastore access layer.
     *
     * @param databaseManager of this library
     */
    public DataRepository(@NotNull DatabaseManager databaseManager, Logger logger) {
        this.logger = logger;
        database = databaseManager.getDatabase(); // can be null
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
        if (isOpen()) {
            if (!database.hasRepository(MovementType.class) &&
                    !database.hasRepository(TrainingType.class) &&
                            !database.hasRepository(RunningPlan.class)) {
                return true;
            }
            ObjectRepository<MovementType> movementTypeRepository = database.getRepository(MovementType.class);
            Cursor<MovementType> movementTypeCursor = movementTypeRepository.find(ObjectFilters.ALL);
            if (movementTypeCursor.size() == 0) {
                return true;
            }
            ObjectRepository<TrainingType> trainingTypeRepository = database.getRepository(TrainingType.class);
            Cursor<TrainingType> trainingTypeCursor = trainingTypeRepository.find(ObjectFilters.ALL);
            if (trainingTypeCursor.size() == 0) {
                return true;
            }
            ObjectRepository<RunningPlan> runningPlanRepository = database.getRepository(RunningPlan.class);
            Cursor<RunningPlan> runningPlanCursor = runningPlanRepository.find(ObjectFilters.ALL);
            if (runningPlanCursor.size() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a flag to determine if the datastore is open.
     *
     * @return A flag to determine if the datastore is open.
     */
    public boolean isOpen() {
        if (database == null) {
            return false;
        }
        return !database.isClosed();

    }

    /**
     * Add a new or save an existing object.
     *
     * @param object to be saved
     * @throws SportsLibraryException if an error occurred while adding
     */
    public void save(@NotNull PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        if (isOpen()) {
            // save or update the object
            saveObject(object);
        } else {
            throw new SportsLibraryException("Database not available.");
        }
    }

    /**
     * Removes an existing object from the local datastore.
     *
     * @param object to be removed
     * @throws SportsLibraryException if an error occurred while removing
     */
    public void delete(@NotNull PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        if (isOpen()) {
            deleteObject(object);
        } else {
            throw new SportsLibraryException("Database not available.");
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
        if (isOpen()) {
            try {
                return persistenceManager.findById(withType, uuid);
            } catch (OnyxException exception) {
                logger.log(Logger.DEBUG, TAG, "Error while searching an object from type "
                        + withType + " and id " + uuid, exception);
            }
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
        if (isOpen()) {

        }
        return results;
    }

    /**
     * Delete all objects from the database.
     */
    public void clearAll() {
        if (database != null) {
           for (Class<?> type: Global.PERSISTENT_CLASSES_LIST) {
               database.getRepository(type).remove(ObjectFilters.ALL);
           }
        }
    }

    /**
     * Close the database.
     */
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    /*
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
    }*/

    private void saveObject(PersistentObject object) throws SportsLibraryException {
        try {
            ObjectRepository<PersistentObject> objectRepository = getObjectRepositoryForType(PersistentObject.class);
            if (objectRepository != null) {
                // insert or update?
                if (objectRepository.getById(object.getUUID()) == null) {
                    // insert
                    objectRepository.insert(object);
                } else {
                    // update
                    objectRepository.update(object);
                }
            }
        } catch (Exception exception) {
            logger.log(Logger.DEBUG, TAG, "Saving the object with id " + object.getUUID() + " failed.", exception);
            throw new SportsLibraryException(exception);
        }
    }

    private void deleteObject(PersistentObject object) throws SportsLibraryException {
        try {
            ObjectRepository<PersistentObject> objectRepository = getObjectRepositoryForType(PersistentObject.class);
            if (objectRepository != null) {
                // insert or update?
                if (objectRepository.getById(object.getUUID()) == null) {
                    // insert
                    objectRepository.insert(object);
                } else {
                    // update
                    objectRepository.update(object);
                }
            }
        } catch (Exception exception) {
            logger.log(Logger.DEBUG, TAG, "Saving the object with id " + object.getUUID() + " failed.", exception);
            throw new SportsLibraryException(exception);
        }
    }
}
