package com.example.configserver.service;

import com.example.configserver.model.ConfigurationItem;

import java.util.List;
import java.util.Optional;

public interface ConfigItemService {
    
    /**
     * Retrieves all configuration items
     */
    List<ConfigurationItem> getAllItems();
    
    /**
     * Retrieves a configuration item by ID
     */
    Optional<ConfigurationItem> getItemById(Long id);
    
    /**
     * Retrieves all configuration items for a specific group
     */
    List<ConfigurationItem> getItemsByGroup(Long groupId);
    
    /**
     * Retrieves all configuration items for a specific group and environment
     */
    Optional<List<ConfigurationItem>> getItemsByGroupAndEnvironment(Long groupId, String environment);
    
    /**
     * Creates a new configuration item
     */
    Optional<ConfigurationItem>  createItem(ConfigurationItem item, String userId);
    
    /**
     * Updates an existing configuration item
     */
    Optional<ConfigurationItem> updateItem(Long id, ConfigurationItem item, String userId);
    
    /**
     * Deletes a configuration item
     */
    void deleteItem(Long id, String userId);
} 