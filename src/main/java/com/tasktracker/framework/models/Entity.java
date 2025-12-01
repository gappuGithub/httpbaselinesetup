package com.tasktracker.framework.models;

import java.util.Map;

/**
 * Generic base interface for all entities in the system.
 * Provides common fields and methods that all entities must implement.
 *
 * @param <ID> The type of the entity identifier (e.g., String, Long, UUID)
 */
public interface Entity<ID> {
    
    /**
     * Get the unique identifier of this entity.
     * @return the entity ID
     */
    ID getId();
    
    /**
     * Set the unique identifier of this entity.
     * @param id the entity ID
     */
    void setId(ID id);
    
    /**
     * Get the timestamp when this entity was created (epoch time in milliseconds).
     * @return the creation timestamp in epoch milliseconds
     */
    Long getCreatedAt();
    
    /**
     * Set the timestamp when this entity was created (epoch time in milliseconds).
     * @param createdAt the creation timestamp in epoch milliseconds
     */
    void setCreatedAt(Long createdAt);
    
    /**
     * Get the timestamp when this entity was last updated (epoch time in milliseconds).
     * @return the last update timestamp in epoch milliseconds
     */
    Long getUpdatedAt();
    
    /**
     * Set the timestamp when this entity was last updated (epoch time in milliseconds).
     * @param updatedAt the last update timestamp in epoch milliseconds
     */
    void setUpdatedAt(Long updatedAt);
    
    /**
     * Apply a partial update to this entity from a patch data map.
     * Only the fields present in the patchData should be updated.
     *
     * @param patchData map of field names to new values
     */
    void updateFromPatch(Map<String, Object> patchData);
}

