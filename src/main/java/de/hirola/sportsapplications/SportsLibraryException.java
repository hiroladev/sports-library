package de.hirola.sportsapplications;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Bundling of all exceptions included libraries.
 *
 * @author Michael Schmidt (Hirola)
 * @since v.0.1
 */
public class SportsLibraryException extends Exception {

    private final String message;

    /**
     * Creates an empty exception object.
     */
    public SportsLibraryException() {
        super();
        message = "Unknown exception message.";
    }

    /**
     * Creates an exception object with a given exception
     * .
     * @param exception for the new object
     */
    public SportsLibraryException(Exception exception) {
        super(exception);
        message = exception.getMessage();
    }

    /**
     * Creates an exception object with a given error message.
     * @param message of the exception
     */
    public SportsLibraryException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
