package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.util.DateUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SportsLibraryTest {

    SportsLibrary sportsLibrary;
    DataRepository dataRepository;

    @Test
    void testLibrary() {
        try {
            // empty app name
            sportsLibrary = new SportsLibrary("", null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    void testRelations() {
        try {

            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // user has an active running plan
            RunningPlan runningPlan = new RunningPlan();
            User user = new User();
            user.setActiveRunningPlan(runningPlan);

            // create 2 location data with associated track
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);

            // create a track with locations
            Track track = new Track("Test-Track",null, locations);

            // create a training with track
            Training training = new Training("Training", null, null, track, null);

            // save all objects
            dataRepository.save(runningPlan);
            dataRepository.save(user);
            dataRepository.save(track);
            dataRepository.save(training);
            String runningPlanUUID = runningPlan.getUUID();

            // running plan and user test
            PersistentObject object1 = dataRepository.findByUUID(User.class, user.getUUID());
            assertNotNull(object1, "Find no object with given UUID.");
            List<? extends PersistentObject> result = dataRepository.findAll(User.class);
            assertFalse(result.isEmpty(), "No results from datastore");
            RunningPlan activePlan = user.getActiveRunningPlan();
            assertNotNull(activePlan, "Active running plan from user must not be null.");
            assertEquals(runningPlanUUID, activePlan.getUUID(), "Active running plan uuid is wrong");

            // track and locations test
            PersistentObject object2 = dataRepository.findByUUID(Track.class, track.getUUID());
            assertNotNull(object2, "Find no object with given UUID.");
            if (object2 instanceof Track) {
                Track trackFromDB = (Track) object2;
                assertFalse(trackFromDB.getLocations().isEmpty(), "List must not bee empty");
            } else {
                fail("Wrong type of object.");
            }

            // training
            PersistentObject object3 = dataRepository.findByUUID(Training.class, training.getUUID());
            assertNotNull(object3, "Find no object with given UUID.");
            if (object3 instanceof Training) {
                Training trainingFromDB = (Training) object3;
                assertNotNull(object3, "Training must not be null");
                assertNotNull(((Training) object3).getTrack(), "Track must not be null");
                assertEquals(trainingFromDB.getTrack().getUUID(), track.getUUID(), "Not the same track.");
            } else {
                fail("Wrong type of object.");
            }

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testObjects() {
        try {
            sportsLibrary = new SportsLibrary("de.hirola.sportslibrary", null);
            dataRepository = sportsLibrary.getDataRepository();

            // test the import from the templates
            // exists 3 running plans in local datastore?
            List<? extends PersistentObject> runningPlans = dataRepository.findAll(RunningPlan.class);
            assertEquals(3,runningPlans.size());

            // test the compare from running plan entry
            RunningPlanEntry runningPlanEntry1 = new RunningPlanEntry(1,1, new ArrayList<>());
            RunningPlanEntry runningPlanEntry2 = new RunningPlanEntry(2,1, new ArrayList<>());
            RunningPlanEntry runningPlanEntry3 = new RunningPlanEntry(3,2, new ArrayList<>());
            RunningPlanEntry runningPlanEntry4 = new RunningPlanEntry(7,2, new ArrayList<>());
            List<RunningPlanEntry> entries1 = new ArrayList<>(4);
            entries1.add(runningPlanEntry3);
            entries1.add(runningPlanEntry2);
            entries1.add(runningPlanEntry1);
            entries1.add(runningPlanEntry4);
            // sort
            Collections.sort(entries1);
            assertEquals(runningPlanEntry1, entries1.get(0), "Entries not sorted by week and day.");
            assertEquals(runningPlanEntry2, entries1.get(1), "Entries not sorted by week and day.");
            assertEquals(runningPlanEntry3, entries1.get(2), "Entries not sorted by week and day.");

            // test the correct start date
            RunningPlan runningPlan1 = (RunningPlan) runningPlans.get(0);
            LocalDate newStartDate = DateUtil.getLocalDateFromNow().plusWeeks(2);
            runningPlan1.setStartDate(newStartDate);
            LocalDate correctedStartDate = runningPlan1.getStartDate();
            // the new start date must be monday in 2 weeks
            assertEquals(correctedStartDate.getDayOfWeek(), DayOfWeek.MONDAY,  "Date not corrected.");

            // only 1 app users must exist
            List<? extends PersistentObject> users = dataRepository.findAll(User.class);
            assertEquals(1, users.size());

            // test running plan
            PersistentObject object = runningPlans.get(1);
            if (object instanceof RunningPlan) {
                RunningPlan runningPlan2= (RunningPlan) object;
                // test correct start date
                for (int i = 0; i < 7; i++) {
                    LocalDate startDate2 = LocalDate.now(ZoneId.systemDefault());
                    startDate2 = startDate2.plusDays(i);
                    runningPlan2.setStartDate(startDate2);
                    LocalDate actualStartDate = runningPlan2.getStartDate();
                    assertEquals(DayOfWeek.MONDAY, actualStartDate.getDayOfWeek());
                }
                // test if duration correction
                List<RunningPlanEntry> entries2 = runningPlan2.getEntries();
                if (!entries2.isEmpty()) {
                    RunningPlanEntry entry = entries2.get(0);
                    List<RunningUnit> units = entry.getRunningUnits();
                    if (!units.isEmpty()) {
                        RunningUnit unit = units.get(0);
                        unit.setCompleted(true);
                        int sumOfUnitDurations = 0;
                        for (RunningUnit runningUnit : units) {
                            sumOfUnitDurations += runningUnit.getDuration();
                        }
                        assertEquals(entry.getDuration(), sumOfUnitDurations);
                    }
                } else {
                    fail("RunningPlan has no entries.");
                }

                // test the duration values
                // 1. running plan entry = sum of duration from the units
                int calculatedDuration = 0;
                for (RunningPlanEntry runningPlanEntry : entries2) {
                    List<RunningUnit> runningUnits = runningPlanEntry.getRunningUnits();
                    for (RunningUnit runningUnit1 : runningUnits) {
                        calculatedDuration += runningUnit1.getDuration();
                    }
                }
                assertEquals(runningPlan2.getDuration(), calculatedDuration, "RunningPlan duration is wrong.");

                // complete of running plan
                for (RunningPlanEntry entry1 : entries2) {
                    for (RunningUnit runningUnit : entry1.getRunningUnits()) {
                        runningUnit.setCompleted(true);
                    }
                    assertTrue(entry1.isCompleted(), "Entry must be completed.");
                }
                assertTrue(runningPlan2.isCompleted(), "Plan must be completed.");

            } else {
                fail("Object not from type RunningPlan.");
            }

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testTrackAndLocationsCRUD() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // create a track with locations
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            String location1UUID = locationData1.getUUID();
            String location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);
            Track track = new Track("Test-Track",null, locations);
            String trackUUID = track.getUUID();

            // save only the track
            dataRepository.save(track);

            // checks
            PersistentObject savedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved");
            List<? extends PersistentObject> savedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from locations");
                }

            }

            // remove the track, locations should be deleted
            dataRepository.delete(track);
            PersistentObject deletedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNull(deletedTrack, "Track was not deleted");
            List<? extends PersistentObject> deletedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(0, deletedLocations.size(), "LocationData was not deleted.");



        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testTrackAndTrainingTypeAndTrainingCRUD() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // create a track with locations
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            String location1UUID = locationData1.getUUID();
            String location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);
            Track track = new Track("Test-Track",null, locations);
            String trackUUID = track.getUUID();

            // create a training with track
            Training training = new Training("Test-Training", null, null, track, null);
            String trainingUUID = training.getUUID();
            String trainingTypeUUID = training.getTrainingType().getUUID();
            // save only the training
            dataRepository.save(training);

            // checks
            PersistentObject savedTraining = dataRepository.findByUUID(Training.class, trainingUUID);
            assertNotNull(savedTraining, "Training was not saved");
            PersistentObject savedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved");
            List<? extends PersistentObject> savedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from locations");
                }
            }

            // remove the training, type of training, track and locations should be NOT deleted
            dataRepository.delete(training);
            PersistentObject notDeletedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(notDeletedTrack, "Track was deleted");
            List<? extends PersistentObject> notDeletedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, notDeletedLocations.size(), "LocationData was deleted.");
            PersistentObject notDeletedTrainingType = dataRepository.findByUUID(TrainingType.class, trainingTypeUUID);
            assertNull(notDeletedTrainingType, "TrainingType was deleted");

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testRunningPlanCRUD() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // create a running plan
            // this movement type (with the key 'L') already exists!
            // saving th running plan updates an existing object
            MovementType movementType1 = new MovementType("L", "Running", "red", 5, 5);
            // a new  movement type
            MovementType movementType2 = new MovementType("Y", "Yoga", "red", 0.0, 0.0);

            RunningUnit runningUnit1 = new RunningUnit(30, movementType1);
            RunningUnit runningUnit2 = new RunningUnit(5, movementType2);
            String runningUnit1UUID = runningUnit1.getUUID();
            String runningUnit2UUID = runningUnit2.getUUID();
            List<RunningUnit> runningUnits = new ArrayList<>(2);
            runningUnits.add(runningUnit1);
            runningUnits.add(runningUnit2);

            RunningPlanEntry runningPlanEntry = new RunningPlanEntry(1,1, runningUnits);
            String runningPlanEntryUUID = runningPlanEntry.getUUID();

            List<RunningPlanEntry> runningPlanEntries = new ArrayList<>(1);
            runningPlanEntries.add(runningPlanEntry);
            RunningPlan runningPlan = new RunningPlan("Test-Plan", null,1,runningPlanEntries, false);
            String runningPlanUUID = runningPlan.getUUID();

            // color is green by default
            MovementType movementType1beforeUpdated = (MovementType) dataRepository.findByUUID(MovementType.class, "L");
            assertNotNull(movementType1beforeUpdated, "No movement type with key / uuid 'L'.");
            assertEquals(Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR, movementType1beforeUpdated.getColorKeyString(),
                    "Default color not " + Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR + ".");

            // save only the runningPlan should throw an error because the movement typ with key 'L'
            dataRepository.save(runningPlan);

            // checks
            PersistentObject savedRunningPlan = dataRepository.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNotNull(savedRunningPlan, "RunningPlan was not saved.");
            PersistentObject savedRunningPlanEntry = dataRepository.findByUUID(RunningPlanEntry.class, runningPlanEntryUUID);
            assertNotNull(savedRunningPlanEntry, "RunningPlanEntry was not saved.");
            PersistentObject savedRunningUnit1 = dataRepository.findByUUID(RunningUnit.class, runningUnit1UUID);
            assertNotNull(savedRunningUnit1, "RunningUnit 1 was not saved.");
            PersistentObject savedRunningUnit2 = dataRepository.findByUUID(RunningUnit.class, runningUnit2UUID);
            assertNotNull(savedRunningUnit2, "RunningUnit 2 was not saved.");
            // movement type with key 'L' has now a new color
            assertEquals(Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR, movementType1beforeUpdated.getColorKeyString(),
                    "Default color not " + Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR + ".");
            PersistentObject savedMovementType2 = dataRepository.findByUUID(MovementType.class, "Y");
            assertNotNull(savedMovementType2, "Movement type 2 was not saved.");


            // remove the plan, entry and units should be deleted but the movement types not
            dataRepository.delete(runningPlan);

            PersistentObject deletedRunningPlan = dataRepository.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNull(deletedRunningPlan, "Running plan was not deleted.");
            PersistentObject deletedRunningPlanEntry = dataRepository.findByUUID(RunningPlanEntry.class, runningPlanEntryUUID);
            assertNull(deletedRunningPlanEntry, "Entry was not deleted.");
            PersistentObject deletedRunningPlanUnit1 = dataRepository.findByUUID(RunningUnit.class, runningPlanUUID);
            assertNull(deletedRunningPlanUnit1, "Unit 1 was not deleted.");
            PersistentObject deletedRunningPlanUnit2 = dataRepository.findByUUID(RunningUnit.class, runningPlanUUID);
            assertNull(deletedRunningPlanUnit2, "Unit 2 was not deleted.");
            PersistentObject movementType1PastDeleted = dataRepository.findByUUID(MovementType.class, "L");
            assertNotNull(movementType1PastDeleted, "Movement type with key 'L' was deleted.");
            PersistentObject movementType2PastDeleted = dataRepository.findByUUID(MovementType.class, "Y");
            assertNotNull(movementType2PastDeleted, "Movement type with key 'Y' was deleted.");


        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

}