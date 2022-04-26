package de.hirola.sportsapplications.util;

import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager for app resources.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class ApplicationResources
{
	private static ApplicationResources instance = null;
	private static final String RESOURCE_NOT_FOUND = "[Resource cannot be found]";
	private ResourceBundle resourceBundle;

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
		try {
			// get the local to use from user prefs
			Preferences userPreferences = Preferences.userRoot().node(Global.UserPreferencesKeys.USER_ROOT_NODE);
			String localeString = userPreferences.get(Global.UserPreferencesKeys.USED_LOCALE,
					Global.DEFAULT_LOCALE.toLanguageTag());
			Locale locale;
			if (localeString.contains("_") && localeString.length() == 5) {
				// language and country
				String language = localeString.substring(0, 2).toLowerCase(Locale.ROOT);
				String country  = localeString.substring(2, 5).toUpperCase(Locale.ROOT);
				locale = new Locale(language, country);
			} else {
				// only the language
				locale = new Locale(localeString);
			}
			resourceBundle = ResourceBundle.getBundle(Global.ROOT_RESOURCE_BUNDLE_BASE_NAME, locale);
		} catch (MissingResourceException exception) {
			resourceBundle = ResourceBundle.getBundle(Global.ROOT_RESOURCE_BUNDLE_BASE_NAME);
		}
	}
}
