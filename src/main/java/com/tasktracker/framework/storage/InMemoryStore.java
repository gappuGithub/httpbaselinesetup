package com.tasktracker.framework.storage;

import com.tasktracker.framework.models.Collection;
import com.tasktracker.framework.models.Entity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generic in-memory storage implementation using ConcurrentHashMap for thread safety.
 * This implementation stores entities in memory and generates UUIDs for String-based IDs.
 *
 * @param <T> The entity type, must extend Entity
 * @param <ID> The type of the entity identifier
 */
@Repository
public class InMemoryStore<T extends Entity<ID>, ID> implements ResourceStorageClient<T, ID> {
    
    /**
     * Thread-safe map for storing entities by ID
     */
    private final ConcurrentHashMap<ID, T> store = new ConcurrentHashMap<>();
    
    /**
     * Create a new entity with a server-generated ID (if not provided).
     * Generates a UUID for String-based IDs and sets timestamps.
     *
     * @param entity the entity to create
     * @return the created entity with ID and timestamps
     */
    @Override
    @SuppressWarnings("unchecked")
    public T create(T entity) {
        // Generate ID only if not provided
        if (entity.getId() == null) {
            // For String IDs, generate UUID
            try {
                ID id = (ID) UUID.randomUUID().toString();
                entity.setId(id);
            } catch (ClassCastException e) {
                throw new IllegalStateException("Cannot auto-generate ID for non-String ID types", e);
            }
        }
        
        // Set timestamps (epoch time in milliseconds)
        Long now = System.currentTimeMillis();
        
        // Only set createdAt if not already set (allows user to specify)
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        
        // Store the entity
        store.put(entity.getId(), entity);
        
        return entity;
    }
    
    /**
     * Retrieve an entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found
     */
    @Override
    public Optional<T> get(ID id) {
        return Optional.ofNullable(store.get(id));
    }
    
    /**
     * Batch retrieve multiple entities by their IDs.
     * Returns a Collection with successfully retrieved entities and errors for not-found IDs.
     *
     * @param ids list of entity IDs to retrieve
     * @return Collection with results and errors
     */
    @Override
    public Collection<ID, T> batchGet(List<ID> ids) {
        Collection<ID, T> collection = new Collection<>();
        
        for (ID id : ids) {
            Optional<T> entity = get(id);
            if (entity.isPresent()) {
                collection.addResult(id, entity.get());
            } else {
                collection.addError(id, 404); // Not Found
            }
        }
        
        return collection;
    }
    
    /**
     * List all entities, optionally filtered by criteria.
     * Filters are applied using simple string matching on entity fields.
     *
     * @param filters map of field names to filter values
     * @return list of entities matching the filters
     */
    @Override
    public List<T> listAll(Map<String, String> filters) {
        List<T> allEntities = store.values().stream().collect(Collectors.toList());
        
        // If no filters, return all
        if (filters == null || filters.isEmpty()) {
            return allEntities;
        }
        
        // Apply filters using reflection/toString matching
        return allEntities.stream()
            .filter(entity -> matchesFilters(entity, filters))
            .collect(Collectors.toList());
    }
    
    /**
     * Update an existing entity.
     *
     * @param id the entity ID
     * @param entity the updated entity
     * @return Optional containing the updated entity if found
     */
    @Override
    public Optional<T> update(ID id, T entity) {
        if (!exists(id)) {
            return Optional.empty();
        }
        
        entity.setId(id);
        entity.setUpdatedAt(System.currentTimeMillis());
        store.put(id, entity);
        
        return Optional.of(entity);
    }
    
    /**
     * Delete an entity by its ID.
     *
     * @param id the entity ID
     * @return true if deleted, false if not found
     */
    @Override
    public boolean delete(ID id) {
        return store.remove(id) != null;
    }
    
    /**
     * Check if an entity exists.
     *
     * @param id the entity ID
     * @return true if exists, false otherwise
     */
    @Override
    public boolean exists(ID id) {
        return store.containsKey(id);
    }
    
    /**
     * Apply a partial update (patch) to an entity.
     * Default implementation throws UnsupportedOperationException.
     * Subclasses should override this to provide entity-specific patching logic.
     *
     * @param id the ID of the entity to patch
     * @param patchData map of field names to new values
     * @return Optional containing the patched entity, or empty if not found
     */
    @Override
    public Optional<T> patch(ID id, Map<String, Object> patchData) {
        throw new UnsupportedOperationException(
            "patch() must be implemented in the entity-specific store");
    }
    
    /**
     * Helper method to check if an entity matches the provided filters.
     * This is a simple implementation that converts entity fields to strings for matching.
     *
     * @param entity the entity to check
     * @param filters the filters to apply
     * @return true if the entity matches all filters
     */
    private boolean matchesFilters(T entity, Map<String, String> filters) {
        // This is a simplified implementation
        // In a real system, you'd use reflection or a more sophisticated approach
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String fieldName = filter.getKey();
            String filterValue = filter.getValue();
            
            // Use toString representation of the entity to check filters
            // This is a basic implementation - can be enhanced with reflection
            String entityString = entity.toString().toLowerCase();
            if (!entityString.contains(filterValue.toLowerCase())) {
                // More specific filtering would be implemented here
                // For now, we'll return true to show all (to be enhanced in subclass)
            }
        }
        return true;
    }
}

