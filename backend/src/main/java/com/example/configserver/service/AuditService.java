package com.example.configserver.service;

import com.example.configserver.model.ConfigurationItem;

/**
 * Service for logging audit events related to configuration items
 */
public interface AuditService {
    
    /**
     * Logs the creation of a configuration item
     * 
     * @param item The item that was created
     * @param userId The ID of the user who created the item
     */
    void logItemCreation(ConfigurationItem item, String userId);
    
    /**
     * Logs an update to a configuration item
     * 
     * @param oldItem The item before the update
     * @param newItem The item after the update
     * @param userId The ID of the user who updated the item
     */
    void logItemUpdate(ConfigurationItem oldItem, ConfigurationItem newItem, String userId);
    
    /**
     * Logs the deletion of a configuration item
     * 
     * @param item The item that was deleted
     * @param userId The ID of the user who deleted the item
     */
    void logItemDeletion(ConfigurationItem item, String userId);
} 