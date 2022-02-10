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
import java.util.HashSet;
import java.util.List;

class SportsLibraryTest {

    SportsLibrary sportsLibrary;
    DataRepository dataRepository;
    List<? extends PersistentObject>  runningPlans;

    @Test
    void testRelations() {
        // user has an active running plan
        RunningPlan runningPlan = new RunningPlan();
        User user = new User();
        user.setActiveRunningPlan(runningPlan);

        // 2 location data
        LocationData locationData1 = new LocationData();
        LocationData locationData2 = new LocationData();
        // add to a list
        List<LocationData> locations = new ArrayList<>(2);
        locations.add(locationData1);
        locations.add(locationData2);
        // create a track with locations
        Track track = new Track("Test-Track",null, null,locations);

        // create a training with track
        Training training = new Training("Training", null, null, track, null);

        try {
            // save
            dataRepository= new DataRepository(null);
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
        dataRepository.close();
    }

    @Test
    void testObjects() {
        try {
            sportsLibrary = new SportsLibrary("SportsLibraryTest", null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");

            // test the import from the templates
            // exists 3 running plans in local datastore?
            runningPlans = dataRepository.findAll(RunningPlan.class);
            assertEquals(3,runningPlans.size());

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
                List<RunningPlanEntry> entries = runningPlan2.getEntries();
                if (!entries.isEmpty()) {
                    RunningPlanEntry entry = entries.get(0);
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
                for (RunningPlanEntry runningPlanEntry : entries) {
                    List<RunningUnit> runningUnits = runningPlanEntry.getRunningUnits();
                    for (RunningUnit runningUnit1 : runningUnits) {
                        calculatedDuration += runningUnit1.getDuration();
                    }
                }
                assertEquals(runningPlan2.getDuration(), calculatedDuration, "RunningPlan duration is wrong.");

                // complete of running plan
                for (RunningPlanEntry entry1 : entries) {
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
        dataRepository.close();
    }

    @Test
    void testCRUD() {
        try {
            sportsLibrary = new SportsLibrary("SportsLibraryTest", null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");

            /* failure - need to checked
            // delete an object with relations
            LocationData locationData1 = new LocationData();
            LocationData locationData2 = new LocationData();
            List<LocationData> locations = new ArrayList<>(2);
            locations.add(locationData1);
            locations.add(locationData2);

            // add a track with locations
            Track track = new Track("Test-Track", null,null,locations);
            String trackUUID = track.getUUID();
            PersistentObject object1 = dataRepository.findByUUID(Track.class, trackUUID);
            assertNotNull(object1, "Track was not saved");
            List<? extends PersistentObject> objects1 = dataRepository.findAll(LocationData.class);
            assertEquals(2, objects1.size(), "LocationData not saved");
            // remove the track, locations should be deleted
            dataRepository.delete(track);
            PersistentObject object2 = dataRepository.findByUUID(Track.class, trackUUID);
            assertNull(object2, "Track was not deleted");
            List<? extends PersistentObject> objects2 = dataRepository.findAll(LocationData.class);
            assertEquals(0, objects1.size(), "LocationData was not deleted.");

            RunningPlan runningPlan = (RunningPlan) dataRepository.findAll(RunningPlan.class).get(0);
            RunningPlanEntry entry = runningPlan.getEntries().get(0);
            assertNotNull(entry);
            String runningPlanEntryUUID = entry.getUUID();
            RunningUnit unit = entry.getRunningUnits().get(0);
            assertNotNull(unit);
            String unitUUID = unit.getUUID();
            MovementType movementType = unit.getMovementType();
            assertNotNull(movementType);
            String movementTypeUUID = movementType.getUUID();

            String runningPlanUUID = runningPlan.getUUID();
            dataRepository.delete(runningPlan);
            PersistentObject object1 = dataRepository.findByUUID(RunningPlan.class, runningPlanUUID);
            assertNull(object1, "Plan not deleted.");
            // entry must be deleted, cascade policy is ALL
            PersistentObject object2 = dataRepository.findByUUID(RunningPlanEntry.class, runningPlanEntryUUID);
            assertNull(object2,"Entry not deleted.");
            // unit must be deleted, cascade policy is ALL
            PersistentObject object3 = dataRepository.findByUUID(RunningUnit.class, unitUUID);
            assertNull(object3, "Unit not deleted.");
            // movement type must be not deleted
            PersistentObject object4 = dataRepository.findByUUID(MovementType.class, movementTypeUUID);
            assertNotNull(object4, "MovementType deleted."); */

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }

        // delete all objects
        dataRepository.clearAll();
        dataRepository.close();
    }

}