package com.tasktracker.impl.validators;

import com.tasktracker.framework.validators.ResourceValidator;
import com.tasktracker.framework.validators.SchemaValidator;
import com.tasktracker.framework.validators.ValidationException;
import com.tasktracker.impl.models.Task;
import com.tasktracker.impl.models.TaskPriority;
import com.tasktracker.impl.models.TaskStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Validator for Task entities.
 * Performs entity-specific business rule validation after schema validation.
 */
@Component
public class TaskValidator implements ResourceValidator<Task> {
    
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    
    /**
     * Schema validator for type checking (framework-level validation)
     */
    private final SchemaValidator<Task> schemaValidator;
    
    /**
     * Constructor.
     */
    public TaskValidator() {
        this.schemaValidator = new SchemaValidator<>(Task.class);
    }
    
    /**
     * Validate a task for creation.
     * Performs schema validation first, then business rule validation.
     * For CREATE: all required fields must be present.
     *
     * @param task the task to validate
     * @throws ValidationException if validation fails
     */
    @Override
    public void validate(Task task) throws ValidationException {
        // Step 1: Schema validation (framework level - type checking)
        schemaValidator.validate(task);
        
        // Step 2: Business rule validation (entity specific)
        Map<String, String> errors = new HashMap<>();
        
        // Extract all fields using reflection
        Map<String, Object> fieldMap = taskToMap(task);
        
        // For CREATE: check required fields are present
        if (fieldMap.get("title") == null) {
            errors.put("title", "Title is required");
            throw new ValidationException(errors);
        }
        if (fieldMap.get("status") == null) {
            errors.put("status", "Status is required");
            throw new ValidationException(errors);
        }
        if (fieldMap.get("priority") == null) {
            errors.put("priority", "Priority is required");
            throw new ValidationException(errors);
        }
        
        // Validate all fields (business rules)
        for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
            validateField(entry.getKey(), entry.getValue(), errors);
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validate patch data for task updates.
     * Performs schema validation first (types), then business rule validation (values).
     * For PATCH: fields are optional, only validate if present.
     *
     * @param patchData map of fields to update
     * @throws ValidationException if validation fails
     */
    @Override
    public void validate(Map<String, Object> patchData) throws ValidationException {
        // Step 1: Schema validation (framework level - type checking)
        schemaValidator.validate(patchData);
        
        // Step 2: Business rule validation (entity specific)
        Map<String, String> errors = new HashMap<>();
        
        // For PATCH: validate each field (business rules only, no required checks)
        for (Map.Entry<String, Object> entry : patchData.entrySet()) {
            validateField(entry.getKey(), entry.getValue(), errors);
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Convert task to a map of field names to values using reflection.
     *
     * @param task the task to convert
     * @return map of field names to values
     */
    private Map<String, Object> taskToMap(Task task) {
        Map<String, Object> fieldMap = new HashMap<>();
        
        Field[] fields = Task.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(task);
                fieldMap.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                // Skip fields we can't access
            }
        }
        
        return fieldMap;
    }
    
    /**
     * Validate a single field's business rules.
     * Only performs business rule validation (length, format, etc).
     * Does NOT check if required - that's done separately in CREATE.
     *
     * @param fieldName the name of the field to validate
     * @param value the field value to validate
     * @param errors error map to add validation errors to
     */
    private void validateField(String fieldName, Object value, Map<String, String> errors) {
        switch (fieldName) {
            case "title":
                if (value != null && !value.toString().trim().isEmpty() && 
                    value.toString().length() > MAX_TITLE_LENGTH) {
                    errors.put("title", "Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
                }
                break;
                
            case "description":
                if (value != null && value.toString().length() > MAX_DESCRIPTION_LENGTH) {
                    errors.put("description", "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
                }
                break;
            default:
                // Unknown field - ignore (SchemaValidator already caught invalid fields)
                break;
        }
    }
}

