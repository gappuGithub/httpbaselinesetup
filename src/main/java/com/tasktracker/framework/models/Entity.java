package com.tasktracker.framework.models;

/**
 * Generic base class for all entities in the system.
 * Provides common fields (id, createdAt, updatedAt) that all entities inherit.
 * Subclasses only need to define their entity-specific fields.
 *
 * @param <ID> The type of the entity identifier (e.g., String, Long, UUID)
 */
public abstract class Entity<ID> {
    
    /**
     * Unique identifier for this entity
     */
    protected ID id;
    
    /**
     * Timestamp when this entity was created (epoch time in milliseconds)
     */
    protected Long createdAt;
    
    /**
     * Timestamp when this entity was last updated (epoch time in milliseconds)
     */
    protected Long updatedAt;
    
    /**
     * Default constructor.
     */
    public Entity() {
    }
    
    /**
     * Constructor with all common fields.
     * 
     * @param id the entity identifier
     * @param createdAt creation timestamp
     * @param updatedAt last update timestamp
     */
    public Entity(ID id, Long createdAt, Long updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Get the unique identifier of this entity.
     * @return the entity ID
     */
    public ID getId() {
        return id;
    }
    
    /**
     * Set the unique identifier of this entity.
     * @param id the entity ID
     */
    public void setId(ID id) {
        this.id = id;
    }
    
    /**
     * Get the timestamp when this entity was created (epoch time in milliseconds).
     * @return the creation timestamp in epoch milliseconds
     */
    public Long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set the timestamp when this entity was created (epoch time in milliseconds).
     * @param createdAt the creation timestamp in epoch milliseconds
     */
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get the timestamp when this entity was last updated (epoch time in milliseconds).
     * @return the last update timestamp in epoch milliseconds
     */
    public Long getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set the timestamp when this entity was last updated (epoch time in milliseconds).
     * @param updatedAt the last update timestamp in epoch milliseconds
     */
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

