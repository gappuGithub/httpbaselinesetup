package com.tasktracker.impl.models;

import com.tasktracker.framework.models.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Task entity representing a team task.
 * Implements the generic Entity interface with String as the ID type.
 */
public class Task implements Entity<String> {
    
    private String id;
    
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Status is required")
    private TaskStatus status;
    
    @NotNull(message = "Priority is required")
    private TaskPriority priority;
    
    private Long createdAt;
    
    private Long updatedAt;
    
    /**
     * Default constructor.
     */
    public Task() {
    }
    
    /**
     * Constructor with all fields.
     */
    public Task(String id, String title, String description, TaskStatus status, 
                TaskPriority priority, Long createdAt, Long updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public TaskPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
    
    @Override
    public Long getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public Long getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Update this task from a patch data map using reflection.
     * Only updates fields that are present in the patch data.
     *
     * @param patchData map of field names to new values
     */
    @Override
    public void updateFromPatch(Map<String, Object> patchData) {
        patchData.forEach((fieldName, value) -> {
            // Skip ID and timestamp fields - these shouldn't be patched
            if ("id".equals(fieldName) || "createdAt".equals(fieldName)) {
                return;
            }
            
            try {
                // Get the field from the class
                java.lang.reflect.Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Handle type conversions
                Object convertedValue = convertValue(value, field.getType());
                field.set(this, convertedValue);
                
            } catch (NoSuchFieldException e) {
                // Field doesn't exist - ignore silently (or log if needed)
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        });
        
        // Always update the timestamp
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * Convert patch value to the appropriate type.
     * Assumes schema validation has already been performed.
     * Only handles necessary type conversions (e.g., String → Enum).
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        // If types already match, return as-is
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // String → Enum conversion (only conversion needed for JSON)
        if (targetType.isEnum() && value instanceof String) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumType, ((String) value).toUpperCase());
        }
        
        // No other conversions - Jackson handles everything else
        // If we reach here, schema validation should have caught it
        throw new IllegalArgumentException(
            "Unexpected type mismatch: " + targetType.getSimpleName() + 
            " vs " + value.getClass().getSimpleName());
    }
}

