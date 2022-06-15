package de.hirola.sportsapplications.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.SportsLibraryApplication;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

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
 * @since v0.1
 */
public class TemplateLoader {

    private final SportsLibrary sportsLibrary;
    private final SportsLibraryApplication application; // on Android load json from R.raw
    private final List<RunningPlanWrapper> runningPlanTemplatesImportList;
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

    public TemplateLoader(@NotNull SportsLibrary sportsLibrary, @Null SportsLibraryApplication application) throws SportsLibraryException {
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
     * Checks whether it is a valid JSON file.
     * Does not check whether a running plan template is included.
     *
     * @param inputStream with the template to be checked
     * @return <b>True</b> if a valid template.
     */
    public boolean isValidTemplate(@NotNull InputStream inputStream) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
            objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            InputStream copyOfInputStream = IOUtils.toBufferedInputStream(inputStream);
            objectMapper.readTree(copyOfInputStream);
            return true;
        } catch (IOException var4) {
            if (this.sportsLibrary.isDebugMode()) {
                this.sportsLibrary.debug(var4, "Not a valid json format.");
            }

            return false;
        }
    }

    /**
     * Checks whether it is a valid JSON file.
     * Does not check whether a running plan template is included.
     *
     * @param jsonFile with the template to be checked
     * @return <b>True</b> if a valid template.
     */
    public boolean isValidTemplate(@NotNull File jsonFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
            objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            objectMapper.readTree(jsonFile);
            return true;
        } catch (IOException var3) {
            if (this.sportsLibrary.isDebugMode()) {
                this.sportsLibrary.debug(var3, "Not a valid json format.");
            }

            return false;
        }
    }

    /**
     * Loads objects of specific types from available templates (JSON) and adds them to the local data store.
     *
     * @throws SportsLibraryException if no templates were found or could not be loaded successfully
     */
    public void addAllFromJSON() throws SportsLibraryException {
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
    public void addFromJSON(@NotNull Class<? extends PersistentObject> typeOf) throws SportsLibraryException {
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
     * Load a plan template from a json file and create a running plan object .
     *
     * @param jsonFile with the template
     * @return A template object to create a running plan.
     * @throws SportsLibraryException if the template was not found or could not be loaded successfully
     * @see RunningPlanWrapper
     */
    public RunningPlan loadRunningPlanFromJSON(@NotNull File jsonFile) throws SportsLibraryException {
        try {
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            // unrecognized field "xxx" (class xxx), not marked as ignorable
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // convert a JSON to a running plan template
            RunningPlanWrapper template = objectMapper.readValue(jsonFile, RunningPlanWrapper.class);
            return createRunningPlanFromTemplate(template);
        } catch (IOException exception) {
            String errorMessage = "Error occurred while parsing json of running plan template: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }

    /**
     * Load a plan template to create (complex) plan objects from an input stream.
     *
     * @param jsonInputStream to the template file
     * @return A template object to create a running plan.
     * @throws SportsLibraryException if the template was not found or could not be loaded successfully
     * @see RunningPlanWrapper
     */
    public RunningPlan loadRunningPlanFromJSON(@NotNull InputStream jsonInputStream) throws SportsLibraryException {
        try {
            // create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            // unrecognized field "xxx" (class xxx), not marked as ignorable
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // convert a JSON to a running plan
            RunningPlanWrapper template = objectMapper.readValue(jsonInputStream, RunningPlanWrapper.class);
            return createRunningPlanFromTemplate(template);
        } catch (IOException exception) {
            String errorMessage = "Error occurred while parsing json of running plan template: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
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
            // write in correct format to the json file
            FileUtils.writeStringToFile(jsonFile, buildJSONStringFromRunningPlan(runningPlan), StandardCharsets.UTF_8);
        } catch (SecurityException | IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while exporting running plans: ".concat(exception.getMessage());
            throw new SportsLibraryException(errorMessage);
        }
    }

    // convert a template to a running plan and add it to the data store
    private void importRunningPlanFromTemplate(@NotNull RunningPlanWrapper template) throws SportsLibraryException {
        // set running plan at the end of existing plans
        List<? extends PersistentObject> runningPlans = sportsLibrary.findAll(RunningPlan.class);
        template.orderNumber = runningPlans.size() + 1;
        // add template to the import list
        runningPlanTemplatesImportList.add(template);
        // add template to local datastore
        addRunningPlansFromTemplate();
        if (importedRunningPlans.size() > 1) {
            throw new SportsLibraryException("More than one template was imported.");
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

    // create running plans from template and add these to the data store
    private void addRunningPlansFromTemplate() throws SportsLibraryException {
        // load the templates from json
        loadRunningPlanTemplates();
        //  Templates in Laufpläne umwandeln
        if (runningPlanTemplatesImportList.size() > 0) {
            for (RunningPlanWrapper template : runningPlanTemplatesImportList) {
                RunningPlan runningPlan = createRunningPlanFromTemplate(template);
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
                    RunningPlanWrapper runningPlanWrapper =
                            objectMapper.readValue(inputStreamIterator.next(), RunningPlanWrapper.class);
                    // set as template
                    runningPlanWrapper.isTemplate = true;
                    // add a template to the local list of running plans
                    runningPlanTemplatesImportList.add(runningPlanWrapper);
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
                    RunningPlanWrapper runningPlanWrapper = objectMapper.readValue(inputStream, RunningPlanWrapper.class);
                    // set as template
                    runningPlanWrapper.isTemplate = true;
                    // add to list
                    runningPlanTemplatesImportList.add(runningPlanWrapper);
                }
            }
        } catch (IOException exception) {
            // TODO: Logging
            String errorMessage = "Error occurred while parsing json of running plan templates: "
                    + exception;
            throw new SportsLibraryException(errorMessage);
        }
    }

    // convert a template to a running plan
    private RunningPlan createRunningPlanFromTemplate(@NotNull RunningPlanWrapper template) throws SportsLibraryException {
        // list of movement types - necessary for creating route plans
        List<? extends PersistentObject> listOfObjects = sportsLibrary.findAll(MovementType.class);
        // Movement types must already exist, otherwise no schedules can be created
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
        //  Templates in Laufpläne umwandeln
        ArrayList<RunningPlanEntry> runningPlanEntries = new ArrayList<>();
        for (RunningPlanEntryWrapper unit : template.runningEntries) {
            //  aus String-Array ["20", "ZG"] die einzelnen Elemente extrahieren
            //  gerade = Zeit, ungerade = Art der Bewegung, 0,1,2,3
            ArrayList<RunningUnit> runningUnits = new ArrayList<>();
            //  Bewegungsart über Schlüssel suchen
            MovementType movementType = null;
            int duration = 0;
            int index = 0;
            Iterator<String> runningUnitStringsIterator = Arrays.stream(unit.runningUnits).iterator();
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
        return new RunningPlan(
                template.name,
                template.remarks,
                template.orderNumber,
                runningPlanEntries,
                template.isTemplate);
    }

    private String buildJSONStringFromRunningPlan(@NotNull RunningPlan runningPlan) {
        StringBuilder stringBuilder = new StringBuilder();
        // running plan meta data
        stringBuilder.append("{\n");
        stringBuilder.append("\"name\": \"");
        stringBuilder.append(runningPlan.getName());
        stringBuilder.append("\",\n");
        stringBuilder.append("\"orderNumber\": \"");
        stringBuilder.append(runningPlan.getOrderNumber());
        stringBuilder.append("\",\n");
        stringBuilder.append("\"remarks\": \"");
        stringBuilder.append(runningPlan.getRemarks());
        stringBuilder.append("\",\n");
        stringBuilder.append("\"runningEntries\": [\n");
        List<RunningPlanEntry> runningPlanEntries = runningPlan.getEntries();
        int entryCount = 0;
        for (RunningPlanEntry entry: runningPlan.getEntries()) {
            entryCount++;
            // running plan entries
            stringBuilder.append("{\n");
            stringBuilder.append("\"week\": \"");
            stringBuilder.append(entry.getWeek());
            stringBuilder.append("\",\n");
            stringBuilder.append("\"day\": \"");
            stringBuilder.append(entry.getDay());
            stringBuilder.append("\",\n");
            stringBuilder.append("\"runningUnits\": [ ");
            // running units
            List<RunningUnit> runningUnits = entry.getRunningUnits();
            int unitCount = 0;
            for (RunningUnit runningUnit: runningUnits) {
                unitCount++;
                stringBuilder.append("\"");
                stringBuilder.append(runningUnit.getDuration());
                stringBuilder.append("\", ");
                MovementType movementType = runningUnit.getMovementType();
                stringBuilder.append("\"");
                stringBuilder.append(movementType.getKey());
                stringBuilder.append("\"");
                if (unitCount < runningUnits.size()) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(" ]\n}");
            if (entryCount < runningPlanEntries.size()) {
                stringBuilder.append(",\n");
            }
        }
        stringBuilder.append("]\n}");
        return stringBuilder.toString();
    }
    private boolean isValidJSON(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
            objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException var4) {
            if (this.sportsLibrary.isDebugMode()) {
                String errorMessage = "The string " + json + " is not a valid format.";
                this.sportsLibrary.debug(var4, errorMessage);
            }

            return false;
        }
    }

}
