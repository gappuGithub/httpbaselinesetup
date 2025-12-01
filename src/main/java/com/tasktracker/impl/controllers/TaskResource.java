package com.tasktracker.impl.controllers;

import com.tasktracker.framework.models.Collection;
import com.tasktracker.framework.validators.ValidationException;
import com.tasktracker.impl.models.Task;
import com.tasktracker.impl.storage.TaskStore;
import com.tasktracker.impl.validators.TaskValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST resource for Task entities.
 * Provides endpoints for CRUD operations on tasks.
 */
@RestController
@RequestMapping("/tasks")
public class TaskResource {
    
    private final TaskStore taskStore;
    private final TaskValidator taskValidator;
    
    /**
     * Constructor with dependency injection.
     *
     * @param taskStore the task storage client
     * @param taskValidator the task validator
     */
    @Autowired
    public TaskResource(TaskStore taskStore, TaskValidator taskValidator) {
        this.taskStore = taskStore;
        this.taskValidator = taskValidator;
    }
    
    /**
     * Create a new task or replace existing task if ID is provided.
     * POST /tasks
     *
     * @param task the task to create/replace
     * @return ResponseEntity with created task and 201 status, or 400 if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            // Validate the task
            taskValidator.validate(task);
            
            // If ID is provided, check if it exists
            if (task.getId() != null && !task.getId().isEmpty()) {
                if (taskStore.exists(task.getId())) {
                    // Replace existing task (upsert behavior)
                    Task updatedTask = taskStore.update(task.getId(), task).orElse(task);
                    return ResponseEntity.ok(updatedTask);
                }
                // ID provided but doesn't exist - create with this ID
            }
            
            // Create new task (ID will be generated if not provided)
            Task createdTask = taskStore.create(task);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
            
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed");
            errorResponse.put("details", e.getErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get a task by ID.
     * GET /tasks/{id}
     *
     * @param id the task ID
     * @return ResponseEntity with task and 200 status, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable String id) {
        Optional<Task> task = taskStore.get(id);
        
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Task not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * Batch get specific tasks by IDs.
     * GET /tasks?ids=id1,id2,id3
     *
     * @param ids list of task IDs (comma-separated or multiple params)
     * @return ResponseEntity with Collection of tasks
     */
    @GetMapping
    public ResponseEntity<Collection<String, Task>> batchGetTasks(@RequestParam("ids") List<String> ids) {
        Collection<String, Task> collection = taskStore.batchGet(ids);
        return ResponseEntity.ok(collection);
    }
    
    /**
     * Get all tasks with optional filtering.
     * GET /tasks/all
     * GET /tasks/all?status=TODO&priority=HIGH
     *
     * @param filters query parameters for filtering (e.g., status, priority)
     * @return ResponseEntity with list of tasks and count
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllTasks(@RequestParam Map<String, String> filters) {
        List<Task> tasks = taskStore.listAll(filters);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tasks", tasks);
        response.put("count", tasks.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Partially update a task.
     * PATCH /tasks/{id}
     *
     * @param id the task ID
     * @param patchData map of fields to update
     * @return ResponseEntity with updated task and 200 status, or 404/400 if error
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody Map<String, Object> patchData) {
        try {
            // Validate the patch data
            taskValidator.validate(patchData);
            
            // Apply the patch via storage layer
            Optional<Task> patchedTask = taskStore.patch(id, patchData);
            
            if (patchedTask.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Task not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            return ResponseEntity.ok(patchedTask.get());
            
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed");
            errorResponse.put("details", e.getErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Delete a task.
     * DELETE /tasks/{id}
     *
     * @param id the task ID
     * @return ResponseEntity with 204 status if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        boolean deleted = taskStore.delete(id);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Task not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint.
     * GET /tasks/health
     *
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Task Tracker API");
        return ResponseEntity.ok(response);
    }
}

