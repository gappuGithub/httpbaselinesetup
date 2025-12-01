package com.tasktracker.impl.models;

import com.tasktracker.framework.models.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Task entity representing a team task.
 * Extends the generic Entity class which provides id, createdAt, and updatedAt fields.
 */
public class Task extends Entity<String> {
    
    // Task-specific fields (id, createdAt, updatedAt inherited from Entity)
    
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Status is required")
    private TaskStatus status;
    
    @NotNull(message = "Priority is required")
    private TaskPriority priority;
    
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
        super(id, createdAt, updatedAt);
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
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
}

