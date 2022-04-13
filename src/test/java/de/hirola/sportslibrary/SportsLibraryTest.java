package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.*;

import de.hirola.sportslibrary.model.UUID;
import de.hirola.sportslibrary.util.DateUtil;
import de.hirola.sportslibrary.util.LogManager;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

class SportsLibraryTest {

    SportsLibrary sportsLibrary;
    DataRepository dataRepository;

    @Test
    void testLibrary() {
        try {
            // empty app name
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");
            // test the import from the templates
            // exists 4 running plans in local datastore?
            List<? extends PersistentObject> runningPlans = dataRepository.findAll(RunningPlan.class);
            assertEquals(4, runningPlans.size());
            RunningPlan runningPlan = (RunningPlan) runningPlans.get(0);
            assertNotNull(runningPlan);
            assertTrue(runningPlan.isTemplate());
            // only 1 user must be exist
            List<? extends PersistentObject> users = dataRepository.findAll(User.class);
            assertEquals(users.size(), 1, "More than a user.");

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
            UUID runningPlanUUID = runningPlan.getUUID();
            User user = new User();
            user.setActiveRunningPlanUUID(runningPlan.getUUID());

            // create 2 location data with associated track
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);

            // create a track with locations
            Track track = new Track("Test-Track",null, locations);
            UUID trackUUID = track.getUUID();

            // create a training with track
            Training training = new Training("Training", null, null, trackUUID, null);

            // add all objects
            dataRepository.add(runningPlan);
            dataRepository.add(user);
            dataRepository.add(track);
            dataRepository.add(training);

            // running plan and user test
            PersistentObject object1 = dataRepository.findByUUID(User.class, user.getUUID());
            assertNotNull(object1, "Find no object with given UUID.");
            List<? extends PersistentObject> result = dataRepository.findAll(User.class);
            assertFalse(result.isEmpty(), "No results from datastore");
            UUID activeRunningPlanUUID = user.getActiveRunningPlanUUID();
            assert activeRunningPlanUUID != null;
            assertNotEquals("", activeRunningPlanUUID.toString(), "Active running plan from user must not be null.");
            assertEquals(runningPlanUUID, activeRunningPlanUUID, "Active running plan uuid is wrong");

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
                assertNotNull(((Training) object3).getTrackUUID(), "Track must not be null");
                assertEquals(trainingFromDB.getTrackUUID(), track.getUUID(), "Not the same track.");
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
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // test user
            User appUser1 = sportsLibrary.getAppUser();
            UUID appUser1UUID = appUser1.getUUID();
            appUser1.setMaxPulse(160);
            dataRepository.update(appUser1);
            User appUser2 = (User) dataRepository.findByUUID(User.class, appUser1UUID);
            assertNotNull(appUser2, "User not found in database.");
            assertEquals(appUser2.getUUID(), appUser1UUID, "Not the same object.");
            assertEquals(160, appUser2.getMaxPulse(), "Pulse not saved");

            List<? extends PersistentObject> runningPlans = dataRepository.findAll(RunningPlan.class);
            RunningPlan runningPlan1 = (RunningPlan) runningPlans.get(0);
            appUser2.setActiveRunningPlanUUID(runningPlan1.getUUID());
            dataRepository.update(appUser2);
            User appUser3 = (User) dataRepository.findByUUID(User.class, appUser1UUID);
            assertNotNull(appUser3, "User not found in database.");
            assertEquals(appUser3.getUUID(), appUser1UUID, "Not the same object.");
            UUID activeRunningPlanUUID = appUser3.getActiveRunningPlanUUID();
            assertNotEquals("", activeRunningPlanUUID, "Active running plan uuid must be not empty.");
            assertEquals(runningPlan1.getUUID(), activeRunningPlanUUID, "User's running plan not saved.");

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
            RunningPlan runningPlan2 = (RunningPlan) runningPlans.get(1);
            LocalDate newStartDate = DateUtil.getLocalDateFromNow().plusWeeks(2);
            runningPlan2.setStartDate(newStartDate);
            LocalDate correctedStartDate = runningPlan2.getStartDate();
            // the new start date must be monday in 2 weeks
            assertEquals(correctedStartDate.getDayOfWeek(), DayOfWeek.MONDAY,  "Date not corrected.");

            // only 1 app users must exist
            List<? extends PersistentObject> users = dataRepository.findAll(User.class);
            assertEquals(1, users.size());

            // test running plan
            PersistentObject object = runningPlans.get(2);
            if (object instanceof RunningPlan) {
                RunningPlan runningPlan3 = (RunningPlan) object;
                // test correct start date
                for (int i = 0; i < 7; i++) {
                    LocalDate startDate2 = LocalDate.now(ZoneId.systemDefault());
                    startDate2 = startDate2.plusDays(i);
                    runningPlan2.setStartDate(startDate2);
                    LocalDate actualStartDate = runningPlan3.getStartDate();
                    //assertEquals(DayOfWeek.MONDAY, actualStartDate.getDayOfWeek());
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
    void testTrackAndLocation() {
        // test track
        // start time: 1645726800000 (18:20)
        // stop time: 1645726860000 (18:21)
        Track track = new Track("Test", null, 1645726800000L, 1645726860000L, 140.0, null);
        assertEquals(1, track.getDuration(), "Duration should be 1 min.");
        assertEquals(8.4, track.getAverageSpeed(),  "AVG should be 8.4 min.");
    }

    @Test
    void testTrackAndLocationsCRUD() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            // create a track with locations
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            UUID location1UUID = locationData1.getUUID();
            UUID location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);
            Track track = new Track("Test-Track",null, locations);
            UUID trackUUID = track.getUUID();

            // add only the track
            dataRepository.add(track);

            // checks
            PersistentObject savedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved.");
            List<? extends PersistentObject> savedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from location.");
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
            UUID location1UUID = locationData1.getUUID();
            UUID location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);
            Track track = new Track("Test-Track",null, locations);
            UUID trackUUID = track.getUUID();
            dataRepository.add(track);

            // create a training with track
            Training training = new Training("Test-Training", null, null, trackUUID, null);
            UUID trainingUUID = training.getUUID();
            UUID trainingTypeUUID = training.getTrainingTypeUUID();
            // add only the training
            dataRepository.add(training);

            // checks
            PersistentObject savedTraining = dataRepository.findByUUID(Training.class, trainingUUID);
            assertNotNull(savedTraining, "Training was not saved.");
            PersistentObject savedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved.");
            List<? extends PersistentObject> savedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved.");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from locations.");
                }
            }

            // remove the training, type of training, track and locations should be NOT deleted
            dataRepository.delete(training);
            PersistentObject notDeletedTrack = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(notDeletedTrack, "Track was deleted.");
            List<? extends PersistentObject> notDeletedLocations = dataRepository.findAll(LocationData.class);
            assertEquals(2, notDeletedLocations.size(), "LocationData was deleted.");

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
            // add a new  movement type
            MovementType movementType2 = new MovementType("Y", "Yoga", "red", 0.0, 0.0);

            RunningUnit runningUnit1 = new RunningUnit(30, movementType1);
            RunningUnit runningUnit2 = new RunningUnit(5, movementType2);
            UUID runningUnit1UUID = runningUnit1.getUUID();
            UUID runningUnit2UUID = runningUnit2.getUUID();
            List<RunningUnit> runningUnits = new ArrayList<>(2);
            runningUnits.add(runningUnit1);
            runningUnits.add(runningUnit2);

            RunningPlanEntry runningPlanEntry = new RunningPlanEntry(1,1, runningUnits);
            UUID runningPlanEntryUUID = runningPlanEntry.getUUID();

            List<RunningPlanEntry> runningPlanEntries = new ArrayList<>(1);
            runningPlanEntries.add(runningPlanEntry);
            RunningPlan runningPlan = new RunningPlan("Test-Plan", null,1,runningPlanEntries, false);
            UUID runningPlanUUID = runningPlan.getUUID();

            // color is green by default
            MovementType movementType1beforeUpdated = (MovementType) dataRepository.findByUUID(MovementType.class, new UUID("L"));
            assertNotNull(movementType1beforeUpdated, "No movement type with key / uuid 'L'.");
            assertEquals(Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR, movementType1beforeUpdated.getColorKeyString(),
                    "Default color not " + Global.Defaults.DEFAULT_MOVEMENT_TYPE_COLOR + ".");

            // add only the runningPlan should throw an error because the movement typ with key 'L'
            dataRepository.add(runningPlan);

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
            PersistentObject savedMovementType2 = dataRepository.findByUUID(MovementType.class, new UUID("Y"));
            assertNotNull(savedMovementType2, "Movement type 2 was not saved.");

            // add running unit state
            runningPlan.completeUnit(runningUnit1);
            dataRepository.update(runningPlan);
            RunningPlan runningPlan1 = (RunningPlan) dataRepository.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNotNull(runningPlan1);
            RunningPlanEntry runningPlanEntry1 = runningPlan1.getEntries().get(0);
            assertNotNull(runningPlanEntry1);
            RunningUnit runningUnit = runningPlanEntry1.getRunningUnits().get(0);
            assertNotNull(runningUnit);
            assertEquals(runningUnit.getUUID(), runningUnit1UUID);
            assertTrue(runningUnit.isCompleted());

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
            PersistentObject movementType1PastDeleted = dataRepository.findByUUID(MovementType.class, new UUID("L"));
            assertNotNull(movementType1PastDeleted, "Movement type with key 'L' was deleted.");
            PersistentObject movementType2PastDeleted = dataRepository.findByUUID(MovementType.class, new UUID("Y"));
            assertNotNull(movementType2PastDeleted, "Movement type with key 'Y' was deleted.");

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testUser() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            RunningPlan runningPlan1 = (RunningPlan) dataRepository.findAll(RunningPlan.class).get(0);
            UUID runningPlan1UUID = runningPlan1.getUUID();
            assertNotNull(runningPlan1);
            User user1 = sportsLibrary.getAppUser();
            assertNotNull(user1);
            assertNull(user1.getActiveRunningPlanUUID()); // with empty database
            user1.setActiveRunningPlanUUID(runningPlan1UUID);
            dataRepository.update(user1);

            User user2 = sportsLibrary.getAppUser();
            assertNotNull(user2);
            UUID runningPlan2UUID = user2.getActiveRunningPlanUUID();
            assertNotNull(runningPlan2UUID, "Active running plan uuid must be not null.");
            assertEquals(runningPlan1UUID, runningPlan2UUID);

            RunningPlan runningPlan2 = (RunningPlan) dataRepository.findByUUID(RunningPlan.class, runningPlan2UUID);
            assertNotNull(runningPlan2);
            RunningUnit unit1 = runningPlan2.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit1);
            UUID unit1UUID = unit1.getUUID();

            runningPlan2.completeUnit(unit1);
            dataRepository.update(runningPlan2);

            RunningPlan runningPlan3 = (RunningPlan) dataRepository.findAll(RunningPlan.class).get(0);
            assertNotNull(runningPlan3);
            assertEquals(runningPlan1UUID, runningPlan3.getUUID());
            RunningUnit unit2 = runningPlan3.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit2);
            assertEquals(unit1UUID, unit2.getUUID());
            assertTrue(unit2.isCompleted());

            User user3 = sportsLibrary.getAppUser();
            assertNotNull(user3);
            UUID runningPlan4UUID = user3.getActiveRunningPlanUUID();
            assertNotNull(runningPlan4UUID, "Active running plan uuid must be not null.");
            RunningUnit unit4 = runningPlan3.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit4);
            assertEquals(unit1UUID, unit4.getUUID());
            assertTrue(unit4.isCompleted());

            // with existing data
            SportsLibrary sportsLibrary5 = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            DataRepository dataRepository5 = sportsLibrary5.getDataRepository();

            User user5 = sportsLibrary.getAppUser();
            assertNotNull(user5);
            UUID runningPlanUUID = user5.getActiveRunningPlanUUID();
            assertNotNull(runningPlanUUID);

            RunningPlan runningPlan5 = (RunningPlan) dataRepository5.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNotNull(runningPlan5);
            RunningUnit unit5 = runningPlan5.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit5);
            assertTrue(unit5.isCompleted());

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testTraining() {
        try {
            sportsLibrary = new SportsLibrary(Global.LIBRARY_PACKAGE_NAME, null);
            dataRepository = sportsLibrary.getDataRepository();

            List<? extends PersistentObject> trainingTypes = dataRepository.findAll(TrainingType.class);
            assertEquals(3, trainingTypes.size(), "The datastore contains no training types.");

            Track track = new Track("Test-Track", "A track for testing.", Instant.now().toEpochMilli());
            dataRepository.add(track);
            UUID trackUUID = track.getUUID();
            UUID trainingTypeUUID = sportsLibrary.getUuidForTrainingType(TrainingType.RUNNING);
            assertNotNull(trainingTypeUUID);
            PersistentObject trainingType = dataRepository.findByUUID(TrainingType.class, trainingTypeUUID);
            assertNotNull(trainingType);

            Training training = new Training("Test-Training", null, null, trainingTypeUUID, trackUUID);
            dataRepository.add(training);


        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
    }

    @Test
    void testLogging() {
        LogManager logManager = LogManager.getInstance(Global.LIBRARY_PACKAGE_NAME, true);
        assertTrue(logManager.isDebugMode());
        Logger.debug("Debug log entry.");
        List<LogManager.LogContent> logContentList = logManager.getLogContent();
        assertNotNull(logContentList, "Exception while getting the content of logfile.");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (LogManager.LogContent logContent : logContentList) {
            System.out.println(logContent.creationDate + " - " + logContent.contentString);
        }
    }

    @Test
    void testDateUtils() {
        LocalDate monday = DateUtil.getMondayOfActualWeek();
        assertEquals("MONDAY", monday.getDayOfWeek().toString());
    }
}