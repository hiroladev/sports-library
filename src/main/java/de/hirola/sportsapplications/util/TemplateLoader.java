package de.hirola.sportsapplications.util;

import com.fasterxml.jackson.databind.SerializationFeature;
import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.SportsLibraryApplication;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Create objects from template files (JSON).
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public class TemplateLoader {

    private final SportsLibrary sportsLibrary;
    private final SportsLibraryApplication application; // on Android load json from R.raw
    private final List<RunningPlanTemplate> runningPlanTemplatesImportList;
    private final List<RunningPlan> importedRunningPlans;
    private boolean isRunningOnAndroid;


    public TemplateLoader(@NotNull SportsLibrary sportsLibrary) {
        this.sportsLibrary = sportsLibrary;
        runningPlanTemplatesImportList = new ArrayList<>();
        importedRunningPlans = new ArrayList<>();
        application = null;
        try {
            isRunningOnAndroid = System.getProperty("java.vm.vendor").equals("The Android Project");
        } catch (SecurityException exception){
            isRunningOnAndroid = false;
        }
    }

    public TemplateLoader(@NotNull SportsLibrary sportsLibrary, @Nullable SportsLibraryApplication application) throws SportsLibraryException {
        this.sportsLibrary = sportsLibrary;
        this.application = application;
        runningPlanTemplatesImportList = new ArrayList<>();
        importedRunningPlans = new ArrayList<>();
        // determine if android or jvm
        // see https://developer.android.com/reference/java/lang/System#getProperties()
        try {
            isRunningOnAndroid = System.getProperty("java.vm.vendor").equals("The Android Project");
        } catch (SecurityException exception){
            isRunningOnAndroid = false;
        }
        if (isRunningOnAndroid && application == null) {
            throw new SportsLibraryException("The application must be not null.");
        }
    }

    /**
     * Loads objects of specific types from available templates (JSON) and adds them to the local data store.
     *
     * @throws SportsLibraryException if no templates were found or could not be loaded successfully
     */
    public void loadAllFromJSON() throws SportsLibraryException {
        addMovementTypesFromTemplate();
        addTrainingTypesFromTemplate();
        addRunningPlansFromTemplate();
    }

    /**
     * Loads objects of a specific type from templates (JSON) and adds them to the local data store.
     *
     * @param typeOf objects of this type should be loaded from templates
     * @throws SportsLibraryException if no templates were found or could not be loaded successfully
     */
    public void loadFromJSON(@NotNull Class<? extends PersistentObject> typeOf) throws SportsLibraryException {
        if (isRunningOnAndroid && application == null) {
            throw new SportsLibraryException("For using this method under Android, the application must not be null. " +
                    "Please initialize the template loader with the constructor (DataRepository, SportsLibraryApplication)");
        }
        if (typeOf.equals(MovementType.class)) {
            addMovementTypesFromTemplate();
            return;
        }
        if (typeOf.equals(TrainingType.class)) {
            addTrainingTypesFromTemplate();
            return;
        }
        if (typeOf.equals(RunningPlan.class)) {
            addRunningPlansFromTemplate();
            return;
        }
        throw new SportsLibraryException("Object type in parameter isn' valid.");
    }

    /**
     * Load a plan template to create (complex) plan objects from a json file.
     *
     * @param jsonInputStream to the template file
     * @return A template object to create a running plan
     * @throws SportsLibraryException if the template was not found or could not be loaded successfully
     * @see RunningPlanTemplate
     */
    public RunningPlanTemplate loadRunningPlanTemplateFromJSON(@NotNull InputStream jsonInputStream) throws SportsLibraryException {
        // aus JSON Objekte erstellen
        try {
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            // convert a JSON to a running plan templates
            // add to the local list of running plans
            return objectMapper.readValue(jsonInputStream, RunningPlanTemplate.class);
        } catch (IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while parsing json of running plan template: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }

    /**
     * Create a running plan from a plan template object.
     * @param template for the running plan
     * @return A running plan, create from a template object
     * @throws SportsLibraryException if the running plan could not create from template object
     * @see RunningPlan
     */
    public RunningPlan importRunningPlanFromTemplate(@NotNull RunningPlanTemplate template) throws SportsLibraryException {
        // set running plan at the end of existing plans
        List<? extends PersistentObject> runningPlans = sportsLibrary.findAll(RunningPlan.class);
        template.orderNumber = runningPlans.size() + 1;
        // add template to the import list
        runningPlanTemplatesImportList.add(template);
        // add template to local datastore
        addRunningPlansFromTemplate();
        if (importedRunningPlans.size() == 1) {
            return importedRunningPlans.get(0);
        }
        throw new SportsLibraryException("More than one template was imported.");
    }

    /**
     * Exports (saves?) a running plan as a template to a JSON file.
     *
     * @param runningPlan to export or add
     * @param jsonFile to export the running plan
     * @throws SportsLibraryException if the file could not be saved or an error occurred during export
     */
    public void exportRunningPlanToJSON(@NotNull RunningPlan runningPlan, @NotNull File jsonFile) throws SportsLibraryException {
        try {
            // check if the path exists and the file can be saved there
            if (jsonFile.isDirectory()) {
                throw new SportsLibraryException("The File object "
                        + jsonFile
                        + " is a directory, not a file.");
            }
            if (!jsonFile.exists()) {
                try {
                    Files.createFile(jsonFile.toPath());
                } catch (Exception exception) {
                    throw new SportsLibraryException("The file "
                            + jsonFile
                            + " could not created: "
                            + exception.getMessage());
                }
            }
            // create object mapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            // convert the running plan to JSON file
            //TODO: format the json, not only serialize
            String jsonFileContent = objectMapper.writeValueAsString(runningPlan);
            FileUtils.writeStringToFile(jsonFile, jsonFileContent, StandardCharsets.UTF_8);
        } catch (SecurityException | IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while exporting running plans: ".concat(exception.getMessage());
            throw new SportsLibraryException(errorMessage);
        }
    }

    // Laden von Trainingsarten aus Vorlagen (JSON) und Ablegen im Datenspeicher
    private void addTrainingTypesFromTemplate() throws SportsLibraryException {
        // aus JSON Objekte erstellen
        try {
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            TrainingType[] trainingTypes;
            if (isRunningOnAndroid) {
                // on Android get the JSON from R.json ...
                InputStream jsonInputStream = application.getTrainingTypeTemplates();
                // convert JSON array to list of movement types
                trainingTypes = objectMapper.readValue(jsonInputStream, TrainingType[].class);
            } else {
                // on JVM read JSON from jar resources
                // searching in folder resources
                // getting Resource as file object
                InputStream jsonInputStream = getClass().getResourceAsStream(Global.TRAINING_TYPES_JSON);
                if (jsonInputStream == null) {
                    throw new SportsLibraryException("Could not load the resource file for training types.");
                }
                // convert JSON array to list of available template files
                trainingTypes = objectMapper.readValue(jsonInputStream, TrainingType[].class);
            }
            if (trainingTypes.length == 0) {
                // TODO: Logging
                throw new SportsLibraryException("Could not load training types.");
            }
            // Trainingsarten speichern
            try {
                for (TrainingType trainingType : trainingTypes) {
                    sportsLibrary.add(trainingType);
                }
            } catch (SportsLibraryException exception) {
                // TODO: Logging
                String errorMessage = "Error occurred while saving an TrainingType object: ".concat(exception.getMessage());
                throw new SportsLibraryException(errorMessage);
            }
        } catch (IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while parsing json of running plan templates: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }

    // Laden von Bewegungsarten aus Vorlagen (JSON) und Ablegen im Datenspeicher
    private void addMovementTypesFromTemplate() throws SportsLibraryException {
        // aus JSON Objekte erstellen
        try {
            MovementType[] movementTypes;
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            if (isRunningOnAndroid) {
                // on Android get the JSON from R.json ...
                InputStream jsonInputStream = application.getMovementTypeTemplates();
                // convert JSON array to list of movement types
                movementTypes = objectMapper.readValue(jsonInputStream, MovementType[].class);
            } else {
                // on JVM read JSON from jar resources
                // searching in folder resources
                // getting Resource as file object
                InputStream jsonInputStream = getClass().getResourceAsStream(Global.MOVEMENT_TYPES_JSON);
                if (jsonInputStream == null) {
                    throw new SportsLibraryException("Could not load the resource file for movement types.");
                }
                // convert JSON array to list of movement types
                movementTypes = objectMapper.readValue(jsonInputStream, MovementType[].class);
            }
            // Bewegungsarten speichern
            try {
                for (MovementType movementType : movementTypes) {
                    sportsLibrary.add(movementType);
                }
            } catch (SportsLibraryException exception) {
                // TODO: Logging
                String errorMessage = "Error occurred while saving an MovementType: "
                        + exception.getMessage();
                throw new SportsLibraryException(errorMessage);
            }
        } catch (IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while parsing json of movement types: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }

    //  Laden von Laufplänen aus Vorlagen (JSON) und Ablegen im Datenspeicher
    // TODO: rollback on error
    private void addRunningPlansFromTemplate() throws SportsLibraryException {
        //  Liste der Bewegungsgarten - notwendig für das Anlegen von Laufplänen
        List<? extends PersistentObject> listOfObjects = sportsLibrary.findAll(MovementType.class);
        //  Bewegungsarten müssen bereits vorhanden sein, ansonsten können keine Laufpläne angelegt werden
        if (listOfObjects.size() == 0) {
            throw new SportsLibraryException("There are no movement types in local datastore. Try to import movement types first.");
        }
        List<MovementType> movementTypes = new ArrayList<>();
        for (PersistentObject object : listOfObjects) {
            try {
                movementTypes.add((MovementType) object);
            } catch (ClassCastException exception) {
                String errorMessage = " The list of movement types contains an object from type "
                        + object.getClass().getSimpleName();
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug(errorMessage, exception);
                }
            }
        }
        // Laden der Laufpläne aus JSON
        loadRunningPlanTemplates();
        //  Templates in Laufpläne umwandeln
        if (runningPlanTemplatesImportList.size() > 0) {
            for (RunningPlanTemplate runningPlanTemplate : runningPlanTemplatesImportList) {
                //  aus einer Vorlage einen Laufplan anlegen
                //  jeder Laufplan enthält für die Wochen und Tage jeweils einen
                //  Trainingsabschnitt "2:L;3:LG;2:L;3:LG;2:L;3:LG;2:L;3:LG;2:L;3:LG" (unit)
                ArrayList<RunningPlanEntry> runningPlanEntries = new ArrayList<>();
                for (RunningPlanTemplateUnit unit : runningPlanTemplate.trainingUnits) {
                    //  aus String-Array ["20", "ZG"] die einzelnen Elemente extrahieren
                    //  gerade = Zeit, ungerade = Art der Bewegung, 0,1,2,3
                    ArrayList<RunningUnit> runningUnits = new ArrayList<>();
                    //  Bewegungsart über Schlüssel suchen
                    MovementType movementType = null;
                    int duration = 0;
                    int index = 0;
                    Iterator<String> runningUnitStringsIterator = Arrays.stream(unit.units).iterator();
                    while (runningUnitStringsIterator.hasNext()) {
                        String runningUnitString = runningUnitStringsIterator.next();
                        if ((index % 2) == 0) {
                            //  gerade Zahl -> Dauer
                            try {
                                duration = Integer.parseInt(runningUnitString);
                            } catch (NumberFormatException exception) {
                                duration = 0;
                                if (sportsLibrary.isDebugMode()) {
                                    sportsLibrary.debug("Duration on running unit was not a number.");
                                }
                            }
                        } else {
                            //  über das Kürzel nach der Bewegungsart suchen
                            for (MovementType type : movementTypes) {
                                if (type.getKey().equalsIgnoreCase(runningUnitString)) {
                                    movementType = type;
                                }
                            }
                            //  Bewegungsart nicht gefunden?
                            if (movementType == null) {
                                // TODO: Logging, ausführlicher Fehler
                                // Eine notwendige Bewegungsart (Schlüssel) wurde im Datenspeicher nicht gefunden.
                                throw new SportsLibraryException("A required movement type (key) wasn't found in import.");
                            }
                            //  einen Trainingsabschnitt erstellen
                            RunningUnit runningUnit = new RunningUnit(duration, movementType);
                            //  den einzelnen Abschnitt (1:L) zur Trainingseinheit hinzufügen
                            runningUnits.add(runningUnit);
                        }
                        //  Schleifen-Index erhöhen
                        index += 1;
                    }
                    // ein Eintrag im Trainingsplan, also das Training eines Tages
                    RunningPlanEntry runningPlanEntry = new RunningPlanEntry(unit.day, unit.week, runningUnits);
                    //  den Trainingsplan-Eintrag zur Liste hinzufügen
                    runningPlanEntries.add(runningPlanEntry);
                }
                //  Laufplan anlegen
                RunningPlan runningPlan = new RunningPlan(
                        runningPlanTemplate.name,
                        runningPlanTemplate.remarks,
                        runningPlanTemplate.orderNumber,
                        runningPlanEntries,
                        runningPlanTemplate.isTemplate);
                try {
                    // add the running plan and all related objects
                    sportsLibrary.add(runningPlan);
                    // add to the imported list
                    importedRunningPlans.add(runningPlan);
                } catch (SportsLibraryException exception) {
                    String errorMessage = "Error occurred while saving a running plan.";
                    if (sportsLibrary.isDebugMode()) {
                        sportsLibrary.debug(errorMessage, exception);
                    }
                    throw new SportsLibraryException(errorMessage + ": " + exception.getMessage());
                }
            }
            // clear the list
            runningPlanTemplatesImportList.clear();
        } else {
            throw new SportsLibraryException("The list of running plan templates was empty. The import failed.");
        }
    }

    // loading plan templates to create (complex) plan objects
    // while first start of an app
    private void loadRunningPlanTemplates() throws SportsLibraryException {
        // aus JSON Objekte erstellen
        try {
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            if (isRunningOnAndroid) {
                // on Android get the JSON from R.json ...
                InputStream[] jsonInputStream = application.getRunningPlanTemplates();
                // convert JSON array to list of running plans templates
                Iterator<InputStream> inputStreamIterator = Arrays.stream(jsonInputStream).iterator();
                while (inputStreamIterator.hasNext()) {
                    RunningPlanTemplate runningPlanTemplate =
                            objectMapper.readValue(inputStreamIterator.next(), RunningPlanTemplate.class);
                    // set as template
                    runningPlanTemplate.isTemplate = true;
                    // add a template to the local list of running plans
                    runningPlanTemplatesImportList.add(runningPlanTemplate);
                }
            } else {
                // on JVM read JSON from jar resources
                // searching in folder resources
                // getting Resource as file object
                InputStream jsonInputStream = getClass().getResourceAsStream(Global.RUNNING_PLAN_TEMPLATE_INDEX_JSON);
                if (jsonInputStream == null) {
                    throw new SportsLibraryException("Could not load the resource file for running plan index.");
                }
                // convert JSON array to list of available template files
                List<RunningPlanTemplateFile> runningPlanTemplateFiles
                        = Arrays.asList(objectMapper.readValue(jsonInputStream, RunningPlanTemplateFile[].class));
                if (runningPlanTemplateFiles.isEmpty()) {
                    // TODO: Logging
                    throw new SportsLibraryException("There are no running plan templates in the index file.");
                }
                // import the templates determined via the index
                for (RunningPlanTemplateFile runningPlanTemplateFile : runningPlanTemplateFiles) {
                    // load the respective JSON file of the template
                    String jsonResourceFileName = Global.JSON_RESOURCES
                            + "/"
                            + runningPlanTemplateFile.fileName
                            + ".json";
                    InputStream inputStream = this.getClass().getResourceAsStream(jsonResourceFileName);
                    if (inputStream == null) {
                        throw new SportsLibraryException("Could not load the resource file for running plan template.");
                    }
                    // read a running plan template
                    // convert JSON file to a template object
                    RunningPlanTemplate runningPlanTemplate = objectMapper.readValue(inputStream, RunningPlanTemplate.class);
                    // set as template
                    runningPlanTemplate.isTemplate = true;
                    // add to list
                    runningPlanTemplatesImportList.add(runningPlanTemplate);
                }
            }
        } catch (IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while parsing json of running plan templates: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }
}