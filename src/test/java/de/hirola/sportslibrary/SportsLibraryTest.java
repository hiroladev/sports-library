package de.hirola.sportslibrary;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.model.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class SportsLibraryTest {

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

}