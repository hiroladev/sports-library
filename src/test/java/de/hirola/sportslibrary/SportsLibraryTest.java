package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class SportsLibraryTest {

    SportsLibrary sportsLibrary;
    DataRepository dataRepository;
    List<? extends PersistentObject>  runningPlans;

    @Test
    void checkRelations() {
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
            DataRepository repo = new DataRepository(null);
            repo.save(runningPlan);
            repo.save(user);
            repo.save(track);
            repo.save(training);
            String runningPlanUUID = runningPlan.getUUID();

            // running plan and user test
            PersistentObject object1 = repo.findByUUID(User.class, user.getUUID());
            assertNotNull(object1, "Find no object with given UUID.");
            List<? extends PersistentObject> result = repo.findAll(User.class);
            assertFalse(result.isEmpty(), "No results from datastore");
            RunningPlan activePlan = user.getActiveRunningPlan();
            assertNotNull(activePlan, "Active running plan from user must not be null.");
            assertEquals(runningPlanUUID, activePlan.getUUID(), "Active running plan uuid is wrong");

            // track adn locations test
            PersistentObject object2 = repo.findByUUID(Track.class, track.getUUID());
            assertNotNull(object2, "Find no object with given UUID.");
            if (object2 instanceof Track) {
                Track trackFromDB = (Track) object2;
                assertFalse(trackFromDB.getLocations().isEmpty(), "List must not bee empty");
            } else {
                fail("Wrong type of object.");
            }

            // training
            PersistentObject object3 = repo.findByUUID(Training.class, training.getUUID());
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
    }

    //@Test
    void initializeSportsLibrary() {
        try {
            sportsLibrary = new SportsLibrary("SportsLibraryTest", null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");

            // test the import from the templates
            // exists 3 running plans in local datastore?
            runningPlans = dataRepository.findAll(RunningPlan.class);
            assertEquals(3,runningPlans.size());

            // only 1 app users must exist
            List<? extends PersistentObject> users = dataRepository.findAll(User.class);
            assertEquals(1, users.size());
            // running plan start date, trainings starts on mondays
            PersistentObject object = runningPlans.get(0);
            if (object instanceof RunningPlan) {
                RunningPlan runningPlan = (RunningPlan) object;
                for (int i = 0; i < 7; i++) {
                    LocalDate startDate = LocalDate.now(ZoneId.systemDefault());
                    startDate = startDate.plusDays(i);
                    runningPlan.setStartDate(java.sql.Date.valueOf(startDate));
                    LocalDate actualStartDate = Instant.ofEpochMilli(runningPlan.getStartDate().getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    assertEquals(DayOfWeek.MONDAY, actualStartDate.getDayOfWeek());
                }
                // set start date on active running plan
                List<RunningPlanEntry> entries = runningPlan.getEntries();
                if (!entries.isEmpty()) {
                    RunningPlanEntry entry = entries.get(0);
                    List<RunningUnit> units = entry.getRunningUnits();
                    if (!units.isEmpty()) {
                        RunningUnit unit = units.get(0);
                        unit.setCompleted(true);
                        assertTrue(runningPlan.isActive());
                        Date startDate = runningPlan.getStartDate();
                        LocalDate newStartDate = LocalDate.now().plusWeeks(2);
                        runningPlan.setStartDate(java.sql.Date.valueOf(newStartDate));
                        // no changes for the start date on active running plan
                        assertEquals(startDate, runningPlan.getStartDate());
                        // get the duration of running plan units
                        int sumOfUnitDurations = 0;
                        for (RunningUnit runningUnit : units) {
                            sumOfUnitDurations += runningUnit.getDuration();
                        }
                        assertEquals(entry.getDuration(), sumOfUnitDurations);
                    }
                } else {
                    fail("RunningPlan has no entries.");
                }

            } else {
                fail("Object not from type RunningPlan.");
            }


        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }
    }

    //@Test
    void testSportsLibrary() {
        try {
            sportsLibrary = new SportsLibrary("SportsLibraryTest", null);
            assertNotNull(sportsLibrary, "Library not initialize.");
            dataRepository = sportsLibrary.getDataRepository();
            assertNotNull(dataRepository, "DataRepository not initialize.");

            // test the import from the templates
            // exists 3 running plans in local datastore?
            RunningPlan runningPlan = (RunningPlan) dataRepository.findAll(RunningPlan.class).get(0);
            List<RunningPlanEntry> runningPlanEntries = runningPlan.getEntries();
            RunningUnit runningUnit = runningPlanEntries.get(0).getRunningUnits().get(0);
            assertTrue(runningUnit.getUUID().isEmpty(),"RunningUnit is empty.");

            // test the duration values
            // 1. running plan entry = sum of duration from the units
            int calculatedDuration = 0;
            for (RunningPlanEntry runningPlanEntry : runningPlanEntries) {
                List<RunningUnit> runningUnits = runningPlanEntry.getRunningUnits();
                for (RunningUnit runningUnit1 : runningUnits) {
                    calculatedDuration += runningUnit1.getDuration();
                }
            }
            assertEquals(runningPlan.getDuration(), calculatedDuration, "RunningPlan duration is wrong.");

        } catch (SportsLibraryException exception) {
            fail(exception.getMessage());
        }
    }

}