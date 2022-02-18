package de.hirola.sportslibrary.database;

import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.util.Logger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    private final int INSERT_ACTION = 0;
    private final int UPDATE_ACTION = 1;
    private final int REMOVE_ACTION = 2;

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
            if (findByUUID(object.getClass(), object.getUUID()) == null) {
                // insert
                doActionWithObject(INSERT_ACTION, object);
            } else {
                doActionWithObject(UPDATE_ACTION, object);
            }
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
            if (findByUUID(object.getClass(), object.getUUID()) != null) {
                // remove
                doActionWithObject(REMOVE_ACTION, object);
            } else {
                throw new SportsLibraryException("The object was not found in database. Can not delete it.");
            }
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
            ObjectRepository<? extends PersistentObject> repository = database.getRepository(withType);
            Cursor<? extends PersistentObject> cursor;
            if (withType.getSimpleName().equals("MovementType")) {
                // movement type has a unique key
                cursor = repository.find(ObjectFilters.eq("key", uuid));
            } else if (withType.getSimpleName().equals("TrainingType")) {
                // training type has a unique name
                cursor = repository.find(ObjectFilters.eq("name", uuid));
            } else {
                cursor = repository.find(ObjectFilters.eq("uuid", uuid));
            }
            if (cursor.size() == 1 ) {
                return cursor.firstOrDefault();
            }
            if (cursor.size() > 1) {
                // very bad
                logger.log(Logger.DEBUG, TAG, "findByUUID has more than one result", null);
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
            ObjectRepository<? extends PersistentObject> repository = database.getRepository(fromType);
            Cursor<? extends PersistentObject> cursor = repository.find();
            return cursor.toList();
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

    private void doActionWithObject(int action, PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        try {
            if (object instanceof User) {
                ObjectRepository<User> objectRepository = database.getRepository(User.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((User) object); return;
                    case UPDATE_ACTION: objectRepository.update((User) object); return;
                    case REMOVE_ACTION: objectRepository.remove((User) object); return;
                }

            }
            if (object instanceof LocationData) {
                ObjectRepository<LocationData> objectRepository = database.getRepository(LocationData.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((LocationData) object); return;
                    case UPDATE_ACTION: objectRepository.update((LocationData) object); return;
                    case REMOVE_ACTION: objectRepository.remove((LocationData) object); return;
                }
            }
            if (object instanceof Track) {
                switch (action) {
                    case INSERT_ACTION: doActionWithTrack(INSERT_ACTION, (Track) object); return;
                    case UPDATE_ACTION: doActionWithTrack(UPDATE_ACTION, (Track) object); return;
                    case REMOVE_ACTION: doActionWithTrack(REMOVE_ACTION, (Track) object); return;
                }
            }
            if (object instanceof TrainingType) {
                ObjectRepository<TrainingType> objectRepository = database.getRepository(TrainingType.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((TrainingType) object); return;
                    case UPDATE_ACTION: objectRepository.update((TrainingType) object); return;
                    case REMOVE_ACTION: objectRepository.remove((TrainingType) object); return;
                }
            }
            if (object instanceof Training) {
                switch (action) {
                    case INSERT_ACTION: doActionWithTraining(INSERT_ACTION, (Training) object); return;
                    case UPDATE_ACTION: doActionWithTraining(UPDATE_ACTION, (Training) object); return;
                    case REMOVE_ACTION: doActionWithTraining(REMOVE_ACTION, (Training) object); return;
                }
            }
            if (object instanceof MovementType) {
                ObjectRepository<MovementType> objectRepository = database.getRepository(MovementType.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((MovementType) object); return;
                    case UPDATE_ACTION: objectRepository.update((MovementType) object); return;
                    case REMOVE_ACTION: objectRepository.remove((MovementType) object); return;
                }
            }
            if (object instanceof RunningUnit) {
                ObjectRepository<RunningUnit> objectRepository = database.getRepository(RunningUnit.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((RunningUnit) object); return;
                    case UPDATE_ACTION: objectRepository.update((RunningUnit) object); return;
                    case REMOVE_ACTION: objectRepository.remove((RunningUnit) object); return;
                }
            }
            if (object instanceof RunningPlanEntry) {
                ObjectRepository<RunningPlanEntry> objectRepository = database.getRepository(RunningPlanEntry.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((RunningPlanEntry) object); return;
                    case UPDATE_ACTION: objectRepository.update((RunningPlanEntry) object); return;
                    case REMOVE_ACTION: objectRepository.remove((RunningPlanEntry) object); return;
                }
            }
            if (object instanceof RunningPlan) {
                switch (action) {
                    case INSERT_ACTION: doActionWithRunningPlan(INSERT_ACTION, (RunningPlan) object); return;
                    case UPDATE_ACTION: doActionWithRunningPlan(UPDATE_ACTION, (RunningPlan) object); return;
                    case REMOVE_ACTION: doActionWithRunningPlan(REMOVE_ACTION, (RunningPlan) object); return;
                }
            }
            throw new SportsLibraryException("Unsupported type of object.");
        } catch (Exception exception) {
            String errorMessage = "Saving the object from type "
                    + object.getClass().getSimpleName()
                    +" and with id " + object.getUUID() + " failed.";
            logger.log(Logger.DEBUG, TAG, errorMessage, exception);
            throw new SportsLibraryException(exception);
        }
    }

    // handle a track with embedded locations
    private void doActionWithTrack(int action, @NotNull Track track) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<Track> trackRepository = database.getRepository(Track.class);
        ObjectRepository<LocationData> locationsRepository = database.getRepository(LocationData.class);
        List<LocationData> locations = track.getLocations();
        switch (action) {
            case INSERT_ACTION:
                // save locations
                for(LocationData locationData: locations) {
                    if (findByUUID(Track.class, locationData.getUUID()) == null) {
                        // insert
                        locationsRepository.insert(locationData);
                    } else {
                        // update
                        locationsRepository.update(locationData);
                    }
                }
                // save the track
                trackRepository.insert(track);
                return;

            case UPDATE_ACTION:
                // update locations
                for(LocationData locationData: locations) {
                    if (findByUUID(Track.class, locationData.getUUID()) == null) {
                        // insert a new location in the list
                        locationsRepository.insert(locationData);
                    } else {
                        // update
                        locationsRepository.update(locationData);
                    }
                }
                // update the track
                trackRepository.update(track);
                return;

            case REMOVE_ACTION:
                // remove locations
                for(LocationData locationData: locations) {
                    if (findByUUID(LocationData.class, locationData.getUUID()) != null) {
                        // remove the location
                        locationsRepository.remove(locationData);
                    }
                }
                // remove the track
                trackRepository.remove(track);
        }
    }

    // handle a training with embedded training type and track (and the locations of the track)
    // training type must exist in database
    private void doActionWithTraining(int action, @NotNull Training training) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<Training> trainingRepository = database.getRepository(Training.class);
        ObjectRepository<TrainingType> trainingTypeRepository = database.getRepository(TrainingType.class);
        TrainingType trainingType = training.getTrainingType();
        Track track = training.getTrack();
        switch (action) {
            case INSERT_ACTION:
                // add a new training type
                if (findByUUID(TrainingType.class, trainingType.getUUID()) == null) {
                    trainingTypeRepository.insert(trainingType);
                }
                // add a new track
                if (track != null) {
                    if (findByUUID(Track.class, track.getUUID()) == null) {
                        // add the track with locations
                        doActionWithTrack(INSERT_ACTION, track);
                    }
                }
                // save the training
                trainingRepository.insert(training);
                return;

            case UPDATE_ACTION:
                // add a new training type
                if (findByUUID(TrainingType.class, trainingType.getUUID()) == null) {
                    trainingTypeRepository.insert(trainingType);
                }
                // add a new track
                if (track != null) {
                    if (findByUUID(Track.class, track.getUUID()) == null) {
                        // add the track with locations
                        doActionWithTrack(INSERT_ACTION, track);
                    }
                }
                // save the training
                trainingRepository.update(training);
                return;

            case REMOVE_ACTION:
                // training type and track will be not remove
                // they may have references to other objects
                // remove the training
                trainingRepository.remove(training);
        }
    }

    // handle a running plan with embedded entries and units
    // movement type must exist in database
    private void doActionWithRunningPlan(int action, @NotNull RunningPlan runningPlan) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<RunningPlan> runningPlanRepository = database.getRepository(RunningPlan.class);
        ObjectRepository<RunningPlanEntry> runningPlanEntryRepository = database.getRepository(RunningPlanEntry.class);
        ObjectRepository<RunningUnit> runningUnitRepository = database.getRepository(RunningUnit.class);
        ObjectRepository<MovementType> movementTypeRepository = database.getRepository(MovementType.class);
        List<RunningPlanEntry> entries = runningPlan.getEntries();
        switch (action) {
            case INSERT_ACTION:
                // save running plan entries
                for(RunningPlanEntry entry: entries) {
                    if (findByUUID(RunningPlanEntry.class, entry.getUUID()) == null) {
                        // insert the units
                        List<RunningUnit> units = entry.getRunningUnits();
                        for(RunningUnit unit: units) {
                            if (findByUUID(RunningUnit.class, unit.getUUID()) == null) {
                                // insert the unit
                                runningUnitRepository.insert(unit);
                            } else {
                                // update the unit
                                runningUnitRepository.update(unit);
                            }
                            // insert a new movement type
                            MovementType movementType = unit.getMovementType();
                            if (findByUUID(MovementType.class, movementType.getUUID()) == null) {
                                movementTypeRepository.insert(movementType);
                            }
                        }
                        // insert the entry
                        runningPlanEntryRepository.insert(entry);
                    } else {
                        // update the entry
                        runningPlanEntryRepository.update(entry);
                    }
                }
                // save the running plan
                runningPlanRepository.insert(runningPlan);
                return;

            case UPDATE_ACTION:
                // save or update running plan entries
                for(RunningPlanEntry entry: entries) {
                    if (findByUUID(RunningPlanEntry.class, entry.getUUID()) == null) {
                        // insert the units
                        List<RunningUnit> units = entry.getRunningUnits();
                        for(RunningUnit unit: units) {
                            if (findByUUID(RunningUnit.class, unit.getUUID()) == null) {
                                // insert the unit
                                runningUnitRepository.insert(unit);
                            } else {
                                // update the unit
                                runningUnitRepository.update(unit);
                            }
                            // insert a new movement type
                            MovementType movementType = unit.getMovementType();
                            if (findByUUID(MovementType.class, movementType.getUUID()) == null) {
                                movementTypeRepository.insert(movementType);
                            }
                        }
                        // insert the entry
                        runningPlanEntryRepository.insert(entry);
                    } else {
                        // update the entry
                        runningPlanEntryRepository.update(entry);
                    }
                }
                // update the running plan
                runningPlanRepository.update(runningPlan);
                return;

            case REMOVE_ACTION:
                // save or update running plan entries
                for(RunningPlanEntry entry: entries) {
                    if (findByUUID(RunningPlanEntry.class, entry.getUUID()) != null) {
                        // insert the units
                        List<RunningUnit> units = entry.getRunningUnits();
                        for(RunningUnit unit: units) {
                            if (findByUUID(RunningUnit.class, unit.getUUID()) != null) {
                                // remove the unit
                                // movement type are not deleted
                                runningUnitRepository.remove(unit);
                            }
                        }
                        // remove the entry
                        runningPlanEntryRepository.remove(entry);
                    }
                }
                // remove the running plan
                runningPlanRepository.remove(runningPlan);
        }
    }
}
