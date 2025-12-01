package com.tasktracker.impl.storage;

import com.tasktracker.framework.storage.InMemoryStore;
import com.tasktracker.impl.models.Task;
import com.tasktracker.impl.models.TaskPriority;
import com.tasktracker.impl.models.TaskStatus;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Task-specific storage implementation that extends InMemoryStore.
 * Provides proper filtering for Task fields like status and priority.
 */
@Repository
public class TaskStore extends InMemoryStore<Task, String> {
    
    /**
     * Apply a partial update (patch) to a task using reflection.
     * Only the fields present in patchData will be updated.
     *
     * @param id the ID of the task to patch
     * @param patchData map of field names to new values
     * @return Optional containing the patched task, or empty if not found
     */
    @Override
    public Optional<Task> patch(String id, Map<String, Object> patchData) {
        Optional<Task> existingTask = get(id);
        if (existingTask.isEmpty()) {
            return Optional.empty();
        }
        
        Task task = existingTask.get();
        
        // Apply patch using reflection
        patchData.forEach((fieldName, value) -> {
            // Skip ID and timestamp fields - these shouldn't be patched
            if ("id".equals(fieldName) || "createdAt".equals(fieldName)) {
                return;
            }
            
            try {
                // Get the field from the class
                Field field = task.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Handle type conversions
                Object convertedValue = convertValue(value, field.getType());
                field.set(task, convertedValue);
                
            } catch (NoSuchFieldException e) {
                // Field doesn't exist - ignore silently (or log if needed)
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        });
        
        // Always update the timestamp
        task.setUpdatedAt(System.currentTimeMillis());
        
        // Save the updated task
        return update(id, task);
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
    
    /**
     * List all tasks with proper filtering support for Task-specific fields.
     * Supports filtering by status and priority.
     *
     * @param filters map of field names to filter values
     * @return list of tasks matching the filters
     */
    @Override
    public List<Task> listAll(Map<String, String> filters) {
        // Get all tasks from parent
        List<Task> allTasks = super.listAll(null);
        
        // If no filters, return all
        if (filters == null || filters.isEmpty()) {
            return allTasks;
        }
        
        // Apply Task-specific filters
        return allTasks.stream()
            .filter(task -> matchesTaskFilters(task, filters))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a task matches the provided filters.
     *
     * @param task the task to check
     * @param filters the filters to apply
     * @return true if the task matches all filters
     */
    private boolean matchesTaskFilters(Task task, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String fieldName = filter.getKey().toLowerCase();
            String filterValue = filter.getValue();
            
            switch (fieldName) {
                case "status":
                    try {
                        TaskStatus status = TaskStatus.valueOf(filterValue.toUpperCase());
                        if (task.getStatus() != status) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                    break;
                    
                case "priority":
                    try {
                        TaskPriority priority = TaskPriority.valueOf(filterValue.toUpperCase());
                        if (task.getPriority() != priority) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                    break;
                    
                case "title":
                    if (task.getTitle() != null && 
                        !task.getTitle().toLowerCase().contains(filterValue.toLowerCase())) {
                        return false;
                    }
                    break;
                    
                default:
                    // Ignore unknown filter fields
                    break;
            }
        }
        
        return true;
    }
}

