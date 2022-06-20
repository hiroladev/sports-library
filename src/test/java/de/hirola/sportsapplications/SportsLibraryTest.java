package de.hirola.sportsapplications;

import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.model.*;

import de.hirola.sportsapplications.model.UUID;
import de.hirola.sportsapplications.util.*;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

class SportsLibraryTest {

    @Test
    void testLibrary() {
        SportsLibrary sportsLibrary = null;
        try {
            // empty app name
            sportsLibrary = SportsLibrary.getInstance(true, new Locale("de"), null, null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            // test the import from the templates
            // exists 4 running plans in local datastore?
            List<? extends PersistentObject> runningPlans = sportsLibrary.findAll(RunningPlan.class);
            assertEquals(4, runningPlans.size());
            RunningPlan runningPlan = (RunningPlan) runningPlans.get(0);
            assertNotNull(runningPlan);
            assertTrue(runningPlan.isTemplate());
            // only one user must be existed
            List<? extends PersistentObject> users = sportsLibrary.findAll(User.class);
            assertEquals(users.size(), 1, "More than a user.");
            // movement types from json
            List<MovementType> movementTypes = sportsLibrary.getMovementTypes();
            assertEquals(14, movementTypes.size());
            Optional<MovementType> optional = movementTypes
                    .stream()
                    .filter(movementType -> movementType.getKey().equals("FL"))
                    .findFirst();
            if (optional.isPresent()) {
                MovementType movementType = optional.get();
                assertEquals(5.3, movementType.getPace(), "Pace from json import must be 5.3.");
                assertEquals("blue", movementType.getColorKeyString(),"Color from json import must be blue.");
                assertEquals("Flottes Laufen", movementType.getName());
            }
        } catch (InstantiationException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testRelations() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // user has an active running plan
            RunningPlan runningPlan = new RunningPlan();
            UUID runningPlanUUID = runningPlan.getUUID();
            User user = new User();
            user.setActiveRunningPlanUUID(runningPlan.getUUID());

            // create 2 location data with associated track
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();

            // add to a list
            List<LocationData> locationData = new ArrayList<>(2);
            locationData.add(locationData1);
            locationData.add(locationData2);

            // create a track with locationData
            Track track = new Track("Test-Track",null, locationData);
            UUID trackUUID = track.getUUID();

            // create a training with track
            Training training = new Training("Training", null, null, null , trackUUID);

            // add all objects
            sportsLibrary.add(runningPlan);
            sportsLibrary.add(user);
            sportsLibrary.add(track);
            sportsLibrary.add(training);

            // running plan and user test
            PersistentObject object1 = sportsLibrary.findByUUID(User.class, user.getUUID());
            assertNotNull(object1, "Find no object with given UUID.");
            List<? extends PersistentObject> result = sportsLibrary.findAll(User.class);
            assertFalse(result.isEmpty(), "No results from datastore");
            Optional<UUID> activeRunningPlanUUID = user.getActiveRunningPlanUUID();
            assert activeRunningPlanUUID.isPresent();
            assertNotEquals("", activeRunningPlanUUID.toString(), "Active running plan from user must not be null.");
            assertEquals(runningPlanUUID, activeRunningPlanUUID.get(), "Active running plan uuid is wrong");

            // track and locationData test
            PersistentObject object2 = sportsLibrary.findByUUID(Track.class, track.getUUID());
            assertNotNull(object2, "Find no object with given UUID.");
            if (object2 instanceof Track) {
                Track trackFromDB = (Track) object2;
                assertFalse(trackFromDB.getLocations().isEmpty(), "List must not bee empty");
            } else {
                fail("Wrong type of object.");
            }

            // training
            PersistentObject object3 = sportsLibrary.findByUUID(Training.class, training.getUUID());
            assertNotNull(object3, "Find no object with given UUID.");
            if (object3 instanceof Training) {
                Training trainingFromDB = (Training) object3;
                assertNotNull(object3, "Training must not be null");
                assertNotNull(((Training) object3).getTrackUUID(), "Track must not be null");
                assertTrue(trainingFromDB.getTrackUUID().isPresent());
                assertEquals(trainingFromDB.getTrackUUID().get(), track.getUUID(), "Not the same track.");
            } else {
                fail("Wrong type of object.");
            }

        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testObjects() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);
            // test user
            User appUser1 = sportsLibrary.getAppUser();
            UUID appUser1UUID = appUser1.getUUID();
            appUser1.setMaxPulse(160);
            sportsLibrary.update(appUser1);
            User appUser2 = (User) sportsLibrary.findByUUID(User.class, appUser1UUID);
            assertNotNull(appUser2, "User not found in database.");
            assertEquals(appUser2.getUUID(), appUser1UUID, "Not the same object.");
            assertEquals(160, appUser2.getMaxPulse(), "Pulse not saved");

            List<? extends PersistentObject> runningPlans = sportsLibrary.findAll(RunningPlan.class);
            RunningPlan runningPlan1 = (RunningPlan) runningPlans.get(0);
            appUser2.setActiveRunningPlanUUID(runningPlan1.getUUID());
            sportsLibrary.update(appUser2);
            User appUser3 = (User) sportsLibrary.findByUUID(User.class, appUser1UUID);
            assertNotNull(appUser3, "User not found in database.");
            assertEquals(appUser3.getUUID(), appUser1UUID, "Not the same object.");
            Optional<UUID> activeRunningPlanUUID = appUser3.getActiveRunningPlanUUID();
            assertTrue(activeRunningPlanUUID.isPresent(), "Active running plan uuid must be not empty.");
            assertEquals(runningPlan1.getUUID(), activeRunningPlanUUID.get(), "User's running plan not saved.");

            // test the compare from running plan entry
            RunningPlanEntry runningPlanEntry1 = new RunningPlanEntry(1,1, new ArrayList<>());
            RunningPlanEntry runningPlanEntry2 = new RunningPlanEntry(1,2, new ArrayList<>());
            RunningPlanEntry runningPlanEntry3 = new RunningPlanEntry(2,3, new ArrayList<>());
            RunningPlanEntry runningPlanEntry4 = new RunningPlanEntry(2,7, new ArrayList<>());
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
            List<? extends PersistentObject> users = sportsLibrary.findAll(User.class);
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

        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
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
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // create a track with locationData
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            UUID location1UUID = locationData1.getUUID();
            UUID location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locationData = new ArrayList<>(2);
            locationData.add(locationData1);
            locationData.add(locationData2);
            Track track = new Track("Test-Track",null, locationData);
            UUID trackUUID = track.getUUID();

            // add only the track
            sportsLibrary.add(track);

            // checks
            PersistentObject savedTrack = sportsLibrary.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved.");
            List<? extends PersistentObject> savedLocations = sportsLibrary.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from location.");
                }

            }

            // remove the track, locationData should be deleted
            sportsLibrary.delete(track);
            PersistentObject deletedTrack = sportsLibrary.findByUUID(Track.class, trackUUID);
            assertNull(deletedTrack, "Track was not deleted");
            List<? extends PersistentObject> deletedLocations = sportsLibrary.findAll(LocationData.class);
            assertEquals(0, deletedLocations.size(), "LocationData was not deleted.");



        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testTrackAndTrainingTypeAndTrainingCRUD() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // create a track with locationData
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            UUID location1UUID = locationData1.getUUID();
            UUID location2UUID = locationData2.getUUID();

            // add to a list
            List<LocationData> locationData = new ArrayList<>(2);
            locationData.add(locationData1);
            locationData.add(locationData2);
            Track track = new Track("Test-Track",null, locationData);
            UUID trackUUID = track.getUUID();
            sportsLibrary.add(track);

            // create a training with track
            Training training = new Training("Test-Training", null, null, null, trackUUID);
            UUID trainingUUID = training.getUUID();
            Optional<UUID> trainingTypeUUID = training.getTrainingTypeUUID();
            // add only the training
            sportsLibrary.add(training);

            // checks
            PersistentObject savedTraining = sportsLibrary.findByUUID(Training.class, trainingUUID);
            assertNotNull(savedTraining, "Training was not saved.");
            PersistentObject savedTrack = sportsLibrary.findByUUID(Track.class, trackUUID);
            assertNotNull(savedTrack, "Track was not saved.");
            List<? extends PersistentObject> savedLocations = sportsLibrary.findAll(LocationData.class);
            assertEquals(2, savedLocations.size(), "LocationData not saved.");
            for (PersistentObject p : savedLocations) {
                if (!p.getUUID().equals(location1UUID) && !p.getUUID().equals(location2UUID)) {
                    fail("Different UUID from locationData.");
                }
            }

            // remove the training, type of training, track and locationData should be NOT deleted
            sportsLibrary.delete(training);
            PersistentObject notDeletedTrack = sportsLibrary.findByUUID(Track.class, trackUUID);
            assertNotNull(notDeletedTrack, "Track was deleted.");
            List<? extends PersistentObject> notDeletedLocations = sportsLibrary.findAll(LocationData.class);
            assertEquals(2, notDeletedLocations.size(), "LocationData was deleted.");

        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testRunningPlanCRUD() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // create a running plan
            // this movement type (with the key 'L') already exists!
            // saving th running plan updates an existing object
            MovementType movementType1 = new MovementType("L", null, 5, 5);
            assertEquals("Running", movementType1.getName(), "No name for the key was found.");
            // add a new  movement type
            MovementType movementType2 = new MovementType("Y", "red", 0.0, 0.0);
            assertEquals("Y", movementType2.getName());

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

            // add the running plan to local datastore - new movement type should be added
            sportsLibrary.add(runningPlan);

            List<MovementType> movementTypes = sportsLibrary.getMovementTypes()
                    .stream()
                    .filter(movementType -> movementType.getKey().compareToIgnoreCase("L") == 0)
                    .collect(Collectors.toList());
            assertEquals(1,movementTypes.size(), "More than one movement type with key 'L'.");

            MovementType movementType1beforeUpdated = (MovementType) sportsLibrary.findByUUID(MovementType.class, new UUID("Y"));
            assertNotNull(movementType1beforeUpdated, "No movement type with key / uuid 'Y'.");
            assertEquals("red", movementType1beforeUpdated.getColorKeyString(),
                    "The color must be red.");

            // checks
            PersistentObject savedRunningPlan = sportsLibrary.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNotNull(savedRunningPlan, "RunningPlan was not saved.");
            PersistentObject savedRunningPlanEntry = sportsLibrary.findByUUID(RunningPlanEntry.class, runningPlanEntryUUID);
            assertNotNull(savedRunningPlanEntry, "RunningPlanEntry was not saved.");
            PersistentObject savedRunningUnit1 = sportsLibrary.findByUUID(RunningUnit.class, runningUnit1UUID);
            assertNotNull(savedRunningUnit1, "RunningUnit 1 was not saved.");
            PersistentObject savedRunningUnit2 = sportsLibrary.findByUUID(RunningUnit.class, runningUnit2UUID);
            assertNotNull(savedRunningUnit2, "RunningUnit 2 was not saved.");
            PersistentObject savedMovementType2 = sportsLibrary.findByUUID(MovementType.class, new UUID("Y"));
            assertNotNull(savedMovementType2, "Movement type 2 was not saved.");

            // add running unit state
            runningPlan.completeUnit(runningUnit1);
            sportsLibrary.update(runningPlan);
            RunningPlan runningPlan1 = (RunningPlan) sportsLibrary.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNotNull(runningPlan1);
            RunningPlanEntry runningPlanEntry1 = runningPlan1.getEntries().get(0);
            assertNotNull(runningPlanEntry1);
            RunningUnit runningUnit = runningPlanEntry1.getRunningUnits().get(0);
            assertNotNull(runningUnit);
            assertEquals(runningUnit.getUUID(), runningUnit1UUID);
            assertTrue(runningUnit.isCompleted());

            // remove the plan, entry and units should be deleted but the movement types not
            sportsLibrary.delete(runningPlan);

            PersistentObject deletedRunningPlan = sportsLibrary.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNull(deletedRunningPlan, "Running plan was not deleted.");
            PersistentObject deletedRunningPlanEntry = sportsLibrary.findByUUID(RunningPlanEntry.class, runningPlanEntryUUID);
            assertNull(deletedRunningPlanEntry, "Entry was not deleted.");
            PersistentObject deletedRunningPlanUnit1 = sportsLibrary.findByUUID(RunningUnit.class, runningPlanUUID);
            assertNull(deletedRunningPlanUnit1, "Unit 1 was not deleted.");
            PersistentObject deletedRunningPlanUnit2 = sportsLibrary.findByUUID(RunningUnit.class, runningPlanUUID);
            assertNull(deletedRunningPlanUnit2, "Unit 2 was not deleted.");
            PersistentObject movementType1PastDeleted = sportsLibrary.findByUUID(MovementType.class, new UUID("L"));
            assertNotNull(movementType1PastDeleted, "Movement type with key 'L' was deleted.");
            PersistentObject movementType2PastDeleted = sportsLibrary.findByUUID(MovementType.class, new UUID("Y"));
            assertNotNull(movementType2PastDeleted, "Movement type with key 'Y' was deleted.");

        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testUser() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            RunningPlan runningPlan1 = (RunningPlan) sportsLibrary.findAll(RunningPlan.class).get(0);
            UUID runningPlan1UUID = runningPlan1.getUUID();
            assertNotNull(runningPlan1);
            User user1 = sportsLibrary.getAppUser();
            assertNotNull(user1);
            user1.setActiveRunningPlanUUID(runningPlan1UUID);
            sportsLibrary.update(user1);

            User user2 = sportsLibrary.getAppUser();
            assertNotNull(user2);
            Optional<UUID> runningPlan2UUID = user2.getActiveRunningPlanUUID();
            assertNotNull(runningPlan2UUID, "Active running plan uuid must be not null.");
            assertTrue(runningPlan2UUID.isPresent());
            assertEquals(runningPlan1UUID, runningPlan2UUID.get());

            RunningPlan runningPlan2 = (RunningPlan) sportsLibrary.findByUUID(RunningPlan.class, runningPlan2UUID.get());
            assertNotNull(runningPlan2);
            RunningUnit unit1 = runningPlan2.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit1);
            UUID unit1UUID = unit1.getUUID();

            runningPlan2.completeUnit(unit1);
            sportsLibrary.update(runningPlan2);

            RunningPlan runningPlan3 = (RunningPlan) sportsLibrary.findAll(RunningPlan.class).get(0);
            assertNotNull(runningPlan3);
            assertEquals(runningPlan1UUID, runningPlan3.getUUID());
            RunningUnit unit2 = runningPlan3.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit2);
            assertEquals(unit1UUID, unit2.getUUID());
            assertTrue(unit2.isCompleted());

            User user3 = sportsLibrary.getAppUser();
            assertNotNull(user3);
            Optional<UUID> runningPlan4UUID = user3.getActiveRunningPlanUUID();
            assertNotNull(runningPlan4UUID, "Active running plan uuid must be not null.");
            RunningUnit unit4 = runningPlan3.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit4);
            assertEquals(unit1UUID, unit4.getUUID());
            assertTrue(unit4.isCompleted());

            // with existing data
            SportsLibrary sportsLibrary5 = SportsLibrary.getInstance(true,null,null, null);

            User user5 = sportsLibrary.getAppUser();
            assertNotNull(user5);
            Optional<UUID> runningPlanUUID = user5.getActiveRunningPlanUUID();
            assertTrue(runningPlanUUID.isPresent());
            RunningPlan runningPlan5 = (RunningPlan) sportsLibrary5.findByUUID(RunningPlan.class, runningPlanUUID.get());
            assertNotNull(runningPlan5);
            RunningUnit unit5 = runningPlan5.getEntries().get(0).getRunningUnits().get(0);
            assertNotNull(unit5);
            assertTrue(unit5.isCompleted());

        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testTraining() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            List<? extends PersistentObject> trainingTypes = sportsLibrary.findAll(TrainingType.class);
            assertEquals(3, trainingTypes.size(), "The datastore contains no training types.");

            Track track = new Track("Test-Track", "A track for testing.", Instant.now().toEpochMilli());
            sportsLibrary.add(track);
            UUID trackUUID = track.getUUID();
            UUID trainingTypeUUID = sportsLibrary.getUuidForTrainingType(TrainingType.RUNNING);
            assertNotNull(trainingTypeUUID);
            PersistentObject trainingType = sportsLibrary.findByUUID(TrainingType.class, trainingTypeUUID);
            assertNotNull(trainingType);

            Training training = new Training("Test-Training", null, null, trainingTypeUUID, trackUUID);
            sportsLibrary.add(training);


        } catch (InstantiationException |SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testLogging() {
        SportsLibrary sportsLibrary = null;
        try {
            File loggingDirectory = SportsLibrary.initializeAppDirectory(Global.LIBRARY_PACKAGE_NAME);
            sportsLibrary = SportsLibrary.getInstance(true, null, loggingDirectory, null);
            assertTrue(sportsLibrary.isDebugMode());
            sportsLibrary.debug("Test log");
            List<LogContent> logContentList = sportsLibrary.getLogContent();
            assertNotNull(logContentList, "Exception while getting the content of logfile.");
            for (LogContent logContent : logContentList) {
                System.out.println(logContent.creationDate + " - " + logContent.contentString);
            }
        } catch (InstantiationException | SportsLibraryException exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testDateUtils() {
        LocalDate monday = DateUtil.getMondayOfActualWeek();
        assertEquals("MONDAY", monday.getDayOfWeek().toString());
    }

    @Test
    void testJSONExport() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            RunningPlan runningPlan = (RunningPlan) sportsLibrary.findAll(RunningPlan.class).get(3);
            assertNotNull(runningPlan);
            String pathName = System.getProperty("user.home")
                    + FileSystems.getDefault().getSeparator()
                    + "RunningPlan-Test.json";
            File jsonFile = new File(pathName);
            TemplateLoader templateLoader = new TemplateLoader(sportsLibrary);
            templateLoader.exportRunningPlanToJSON(runningPlan, jsonFile);

        } catch (Exception exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testJSONImport() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            String pathName = System.getProperty("user.home")
                    + FileSystems.getDefault().getSeparator()
                    + "RunningPlan-Test.json";
            File jsonFile = new File(pathName);
            TemplateLoader templateLoader = new TemplateLoader(sportsLibrary);
            RunningPlan runningPlan = templateLoader.loadRunningPlanFromJSON(jsonFile);
            assertNotNull(runningPlan);
            assertFalse(runningPlan.getEntries().isEmpty());

        } catch (Exception exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testGPXImport() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // import the gpx
            String pathName = System.getProperty("user.home")
                    + FileSystems.getDefault().getSeparator()
                    + "Test.gpx";
            File gpxFile = new File(pathName);
            GPXManager.importGPX(sportsLibrary, gpxFile);
            // get the track from library
            List<? extends PersistentObject> tracks = sportsLibrary.findAll(Track.class);
            assertEquals(1, tracks.size(), "Track was not added to the datastore.");

        } catch (Exception exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }

    @Test
    void testGPXExport() {
        // time stamp in ISO 8601 format
        String timeStamp1InISO8601Format = "2018-08-08T10:02:44Z";
        String timeStamp2InISO8601Format = "2018-08-08T10:03:17Z";
        // create a simple track object with 2 track points
        List<LocationData> locationData = new ArrayList<>();
        LocationData locationData1 = new LocationData(50.967010525102694, 14.251019814996281);
        locationData1.setElevation(293.6364548339844);
        locationData1.setTimeStamp(Instant.parse(timeStamp1InISO8601Format).toEpochMilli());
        locationData.add(locationData1);
        LocationData locationData2 = new LocationData(50.966995525102696, 14.25085681499628);
        locationData2.setElevation(287.6724548339844);
        locationData2.setTimeStamp(Instant.parse(timeStamp2InISO8601Format).toEpochMilli());
        locationData.add(locationData2);
        Track track = new Track();
        track.setName("Track 1");
        track.setDescription("Sample track export");
        track.setLocations(locationData);

        // export as GPX
        String pathName = System.getProperty("user.home")
                + FileSystems.getDefault().getSeparator()
                + "Export.gpx";
        File gpxFile = new File(pathName);
        try {
            GPXManager.exportGPX(track, gpxFile);
        } catch (IOException exception) {
            fail(exception.getMessage());
        }
        // read the exported file
        try {
            Optional<io.jenetics.jpx.Track> optionalTrack = GPX.read(gpxFile.getPath()).tracks().findFirst();
            if (optionalTrack.isPresent()) {
                Optional<TrackSegment> optionalTrackSegment = optionalTrack.get().segments().findFirst();
                if (optionalTrackSegment.isPresent()) {
                    List<WayPoint> wayPoints = optionalTrackSegment.get().getPoints();
                    assertEquals(2, wayPoints.size(), "The track should contain two waypoints.");
                    WayPoint wayPoint = wayPoints.get(0);
                   Optional<ZonedDateTime> optionalTimestamp = wayPoint.getTime();
                   if (optionalTimestamp.isPresent()) {
                       assertEquals(timeStamp1InISO8601Format, optionalTimestamp.get().toString(), "Wrong time stamp.");
                   } else {
                       fail("The waypoint contains no time stamp.");
                   }
                } else {
                    fail("The track contains no segments.");
                }
            } else {
                fail("The export file contains no tracks.");
            }
        } catch (IOException exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    void testICALImport() {
        SportsLibrary sportsLibrary = null;
        try {
            sportsLibrary = SportsLibrary.getInstance(true, null, null, null);

            // import the gpx
            String pathName = System.getProperty("user.home")
                    + FileSystems.getDefault().getSeparator()
                    + "flex.ics";
            File iCALFile = new File(pathName);
            ICALManager.importICAL(sportsLibrary, iCALFile);
            List<RunningPlan> runningPlans = sportsLibrary.getRunningPlans();
            assertEquals(5, runningPlans.size(), "Running plan from ical was not imported.");
            RunningPlan importedRunningPlan = runningPlans.get(4);
            System.out.println(importedRunningPlan.getName());
            List<RunningPlanEntry> entries = importedRunningPlan.getEntries();
            for (RunningPlanEntry entry: entries) {
                System.out.println(entry.getDuration() + " " + entry.getDistance());
                if (entry.getRunningUnits().size() > 0) {
                    System.out.println(" " + entry.getRunningUnits().get(0).getPace());
                    System.out.println(" " + entry.getRunningUnits().get(0).getLowerPulseLimit()
                            + " : " + entry.getRunningUnits().get(0).getUpperPulseLimit());
                }
            }
        } catch (Exception exception) {
            fail(exception.getMessage());
        } finally {
            // delete all objects
            if (sportsLibrary != null) {
                sportsLibrary.clearAll();
            }
        }
    }
}