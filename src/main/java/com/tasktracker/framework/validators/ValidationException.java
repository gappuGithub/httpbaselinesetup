package com.tasktracker.framework.validators;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when resource validation fails.
 * Contains a map of field names to error messages.
 */
public class ValidationException extends Exception {
    
    private final Map<String, String> errors;
    
    /**
     * Constructor with a single error message.
     *
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
        this.errors.put("error", message);
    }
    
    /**
     * Constructor with a map of field-specific errors.
     *
     * @param errors map of field names to error messages
     */
    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors != null ? errors : new HashMap<>();
    }
    
    /**
     * Get the validation errors.
     *
     * @return map of field names to error messages
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}

