package de.hirola.sportsapplications;

import java.io.InputStream;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * On Android you must bei implements this interface to load templates.
 * The resources can't be imported from jar.
 * It's create an Exception with "java.nio.file.FileSystemNotFoundException: Provider "jar" not installed".
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public interface SportsLibraryApplication {
    InputStream getMovementTypeTemplates();
    InputStream getTrainingTypeTemplates();
    InputStream[] getRunningPlanTemplates();
}
