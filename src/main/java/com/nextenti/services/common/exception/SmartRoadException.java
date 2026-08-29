package com.nextenti.services.common.exception;

import java.util.Arrays;
import java.util.List;

/**
 * The main class for the SmartRoadEntity Exception
 *
 * @author
 * @version 1.0
 */
public class SmartRoadException extends Exception {
    private static final long serialVersionUID = 1L;
    private final ApplicationLayer applicationLayer;
    private final ErrorCodeMapping errorCode;
    private final List<String> arguments;

    /**
     * Constructs a SmartRoadException with application layer, error code, message, and arguments.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     * @param arguments the message arguments for formatting
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, String... arguments) {
        super(message);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = Arrays.asList(arguments);
    }

    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, Object... arguments) {
        super(message);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = Arrays.stream(arguments).map(String::valueOf).toList();
    }

    /**
     * Constructs a SmartRoadException with application layer, error code, message, and arguments list.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     * @param arguments the message arguments list for formatting
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, List<String> arguments) {
        super(message);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = arguments;

    }

    /**
     * Constructs a SmartRoadException with application layer, error code, and message.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message) {
        super(message);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = null;
    }

    /**
     * Constructs a SmartRoadException with application layer, error code, message, and cause.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     * @param cause the underlying cause of the exception
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, Throwable cause) {
        super(message, cause);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = null;
    }

    /**
     * Constructs a SmartRoadException with application layer, error code, message, cause, and arguments.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     * @param cause the underlying cause of the exception
     * @param arguments the message arguments for formatting
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, Throwable cause,
                               String... arguments) {
        super(message, cause);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = Arrays.asList(arguments);
    }

    /**
     * Constructs a SmartRoadException with application layer, error code, message, cause, and arguments list.
     *
     * @param applicationLayer the application layer where the exception occurred
     * @param errorCode the error code mapping
     * @param message the error message
     * @param cause the underlying cause of the exception
     * @param arguments the message arguments list for formatting
     */
    public SmartRoadException(ApplicationLayer applicationLayer,
                               ErrorCodeMapping errorCode, String message, Throwable cause,
                               List<String> arguments) {
        super(message, cause);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    /**
     * Gets the application layer where the exception occurred.
     *
     * @return the application layer
     */
    public ApplicationLayer getApplicationLayer() {

        return applicationLayer;
    }

    /**
     * Gets the error code mapping.
     *
     * @return the error code mapping
     */
    public ErrorCodeMapping getErrorCode() {

        return errorCode;
    }

    /**
     * Gets the message arguments for formatting.
     *
     * @return the list of arguments, or null if none
     */
    public List<String> getArguments() {

        return arguments;
    }

    /**
     * Gets the message key for internationalization.
     *
     * @return the message key
     */
    public String getMessageKey() {
        return getMessage();
    }
}
