package com.tasktracker.impl.storage;

import com.tasktracker.framework.storage.InMemoryStore;
import com.tasktracker.impl.models.Task;
import com.tasktracker.impl.models.TaskPriority;
import com.tasktracker.impl.models.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Task-specific storage implementation that extends InMemoryStore.
 * Provides proper filtering for Task fields like status and priority.
 */
@Repository
public class TaskStore extends InMemoryStore<Task, String> {
    
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

