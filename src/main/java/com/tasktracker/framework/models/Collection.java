package com.tasktracker.framework.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic collection wrapper for batch get operations.
 * Contains both successfully retrieved entities and errors for failed retrievals.
 *
 * @param <K> The type of the entity identifier/key
 * @param <T> The type of the entity
 */
public class Collection<K, T> {
    
    /**
     * Map of successfully retrieved entities (ID -> Entity)
     */
    private Map<K, T> results;
    
    /**
     * Map of failed retrievals (ID -> HTTP error code)
     */
    private Map<K, Integer> errors;
    
    /**
     * Default constructor initializing empty maps.
     */
    public Collection() {
        this.results = new HashMap<>();
        this.errors = new HashMap<>();
    }
    
    /**
     * Constructor with initial maps.
     *
     * @param results map of successfully retrieved entities
     * @param errors map of failed retrievals with error codes
     */
    public Collection(Map<K, T> results, Map<K, Integer> errors) {
        this.results = results != null ? results : new HashMap<>();
        this.errors = errors != null ? errors : new HashMap<>();
    }
    
    /**
     * Get the map of successfully retrieved entities.
     * @return map of ID to entity
     */
    public Map<K, T> getResults() {
        return results;
    }
    
    /**
     * Set the map of successfully retrieved entities.
     * @param results map of ID to entity
     */
    public void setResults(Map<K, T> results) {
        this.results = results;
    }
    
    /**
     * Get the map of failed retrievals with error codes.
     * @return map of ID to HTTP error code
     */
    public Map<K, Integer> getErrors() {
        return errors;
    }
    
    /**
     * Set the map of failed retrievals with error codes.
     * @param errors map of ID to HTTP error code
     */
    public void setErrors(Map<K, Integer> errors) {
        this.errors = errors;
    }
    
    /**
     * Add a successful result to the collection.
     *
     * @param key the entity ID
     * @param entity the entity
     */
    public void addResult(K key, T entity) {
        this.results.put(key, entity);
    }
    
    /**
     * Add an error to the collection.
     *
     * @param key the entity ID that failed
     * @param errorCode the HTTP error code
     */
    public void addError(K key, Integer errorCode) {
        this.errors.put(key, errorCode);
    }
}

