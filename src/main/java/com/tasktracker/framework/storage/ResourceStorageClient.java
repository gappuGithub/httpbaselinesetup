package com.tasktracker.framework.storage;

import com.tasktracker.framework.models.Collection;
import com.tasktracker.framework.models.Entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic storage interface for entity CRUD operations.
 * Provides a contract for storage implementations to manage entities of any type.
 *
 * @param <T> The entity type, must extend Entity
 * @param <ID> The type of the entity identifier
 */
public interface ResourceStorageClient<T extends Entity<ID>, ID> {
    
    /**
     * Create a new entity with a server-generated ID.
     *
     * @param entity the entity to create (ID will be generated)
     * @return the created entity with assigned ID and timestamps
     */
    T create(T entity);
    
    /**
     * Retrieve an entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<T> get(ID id);
    
    /**
     * Retrieve multiple entities by their IDs in a batch operation.
     * Returns a Collection with both successfully retrieved entities and errors.
     *
     * @param ids list of entity IDs to retrieve
     * @return Collection containing results (found entities) and errors (not found IDs)
     */
    Collection<ID, T> batchGet(List<ID> ids);
    
    /**
     * List all entities, optionally filtered by criteria.
     *
     * @param filters map of field names to filter values (e.g., "status" -> "TODO")
     * @return list of entities matching the filters
     */
    List<T> listAll(Map<String, String> filters);
    
    /**
     * Update an existing entity.
     *
     * @param id the entity ID
     * @param entity the updated entity
     * @return Optional containing the updated entity if found, empty otherwise
     */
    Optional<T> update(ID id, T entity);
    
    /**
     * Delete an entity by its ID.
     *
     * @param id the entity ID
     * @return true if the entity was deleted, false if not found
     */
    boolean delete(ID id);
    
    /**
     * Check if an entity exists by its ID.
     *
     * @param id the entity ID
     * @return true if the entity exists, false otherwise
     */
    boolean exists(ID id);
}

