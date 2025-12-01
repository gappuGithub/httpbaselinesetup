package com.tasktracker.framework.validators;

import com.tasktracker.framework.models.Entity;

import java.util.Map;

/**
 * Generic validator interface for entity validation.
 *
 * @param <T> The entity type to validate
 */
public interface ResourceValidator<T extends Entity<?>> {
    
    /**
     * Validate an entity for creation (overload 1).
     * Should check all required fields and constraints.
     *
     * @param entity the entity to validate
     * @throws ValidationException if validation fails
     */
    void validate(T entity) throws ValidationException;
    
    /**
     * Validate patch data for partial updates (overload 2).
     * Should check that provided fields are valid, but not require all fields.
     *
     * @param patchData map of fields to update
     * @throws ValidationException if validation fails
     */
    void validate(Map<String, Object> patchData) throws ValidationException;
}

