package de.hirola.sportslibrary.util;

import de.hirola.sportslibrary.SportsLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager for app resources.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public final class ApplicationResources
{
	private static ApplicationResources instance = null;
	private static final String RESOURCE_NOT_FOUND = "[Resource cannot be found]";
	private final ResourceBundle resourceBundle;

	public static ApplicationResources getInstance() {
		if (instance == null) {
			instance = new ApplicationResources();
		}
		return instance;
	}

	/**
	 * Returns a resource string for the key. If the key cannot be found or is not a string,
	 * returns a string that indicates an error.
	 * 
	 * @param forKey for the string
	 * @return The string for the given key or a default error string.
	 */
	public String getString(@NotNull String forKey) {
		try {
			return resourceBundle.getString(forKey);
		} catch( MissingResourceException | ClassCastException exception ) {
			return RESOURCE_NOT_FOUND;
		}
	}
	
	/**
	 * Check if exist a string for a key.
	 *
	 * @param key for string
	 * @return True if the key is found in the resources.
	 */
	public boolean containsKey(@NotNull String key) {
		return resourceBundle.containsKey(key);
	}

	private ApplicationResources() {
		resourceBundle = ResourceBundle.getBundle(SportsLibrary.class.getSimpleName());
	}
}
