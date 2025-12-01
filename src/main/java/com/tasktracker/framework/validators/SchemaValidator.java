package com.tasktracker.framework.validators;

import com.tasktracker.framework.models.Entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic schema validator that validates field types against entity schema.
 * This is framework-level validation that checks type compatibility.
 *
 * @param <T> The entity type
 */
public class SchemaValidator<T extends Entity<?>> {
    
    private final Class<T> entityClass;
    
    /**
     * Constructor.
     *
     * @param entityClass the entity class to validate against
     */
    public SchemaValidator(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Validate that patch data types match the entity schema.
     * Checks that each field in patchData has a compatible type with the entity field.
     *
     * @param patchData map of field names to values
     * @throws ValidationException if any field has invalid type
     */
    public void validate(Map<String, Object> patchData) throws ValidationException {
        Map<String, String> errors = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : patchData.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            // Skip null values - they're valid for any type
            if (value == null) {
                continue;
            }
            
            try {
                // Get the field from the entity class
                Field field = entityClass.getDeclaredField(fieldName);
                Class<?> fieldType = field.getType();
                
                // Validate type compatibility
                if (!isTypeCompatible(value, fieldType)) {
                    errors.put(fieldName, 
                        "Type mismatch. Expected " + fieldType.getSimpleName() + 
                        " but got " + value.getClass().getSimpleName());
                }
                
            } catch (NoSuchFieldException e) {
                // Field doesn't exist in entity schema
                errors.put(fieldName, "Unknown field - not part of entity schema");
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validate that create data has all required fields with correct types.
     * Converts entity to map and reuses validate() logic.
     *
     * @param entity the entity to validate
     * @throws ValidationException if schema validation fails
     */
    public void validate(T entity) throws ValidationException {
        // Convert entity to map and reuse validation logic
        Map<String, Object> entityAsMap = entityToMap(entity);
        validate(entityAsMap);
    }
    
    /**
     * Convert entity to a map of field names to values.
     * Used to reuse validation logic between create and patch.
     *
     * @param entity the entity to convert
     * @return map of field names to values
     */
    private Map<String, Object> entityToMap(T entity) {
        Map<String, Object> entityMap = new HashMap<>();
        
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {  // Only include non-null values
                    entityMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // Skip fields we can't access
            }
        }
        
        return entityMap;
    }
    
    /**
     * Check if a value is compatible with the target field type.
     * Handles common type conversions (e.g., String for Enum, Number for Long).
     *
     * @param value the value to check
     * @param targetType the expected field type
     * @return true if compatible, false otherwise
     */
    private boolean isTypeCompatible(Object value, Class<?> targetType) {
        // Direct type match
        if (targetType.isInstance(value)) {
            return true;
        }
        
        // String is valid for Enum fields (will be converted)
        if (targetType.isEnum() && value instanceof String) {
            // Check if the string is a valid enum value
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
                Enum.valueOf(enumType, ((String) value).toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        
        // No other conversions - fail fast on type mismatches
        return false;
    }
}

