package com.example.configserver.service;

import com.example.configserver.model.ConfigurationItem;

import java.util.List;
import java.util.Optional;

public interface ConfigurationItemService {
    List<ConfigurationItem> getAllItems();
    Optional<ConfigurationItem> getItemById(Long id);
    List<ConfigurationItem> getItemsByGroup(Long groupId);
    List<ConfigurationItem> getItemsByGroupAndEnvironment(Long groupId, String environment);
    ConfigurationItem createItem(ConfigurationItem item);
    Optional<ConfigurationItem> updateItem(Long id, ConfigurationItem item);
    boolean deleteItem(Long id);
} 