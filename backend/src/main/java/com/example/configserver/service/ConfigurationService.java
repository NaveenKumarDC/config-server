package com.example.configserver.service;

import com.example.configserver.dto.ConfigurationGroupDTO;
import com.example.configserver.dto.ConfigurationItemDTO;

import java.util.List;

public interface ConfigurationService {
    // Group operations
    List<ConfigurationGroupDTO> getAllGroups();
    ConfigurationGroupDTO getGroupById(Long id);
    ConfigurationGroupDTO getGroupByName(String name);
    ConfigurationGroupDTO createGroup(ConfigurationGroupDTO groupDTO);
    ConfigurationGroupDTO updateGroup(Long id, ConfigurationGroupDTO groupDTO);
    void deleteGroup(Long id);
    
    // Item operations
    List<ConfigurationItemDTO> getAllItems();
    List<ConfigurationItemDTO> getItemsByGroup(Long groupId);
    List<ConfigurationItemDTO> getItemsByGroupAndEnvironment(Long groupId, String environment);
    ConfigurationItemDTO getItemById(Long id);
    ConfigurationItemDTO createItem(ConfigurationItemDTO itemDTO);
    ConfigurationItemDTO updateItem(Long id, ConfigurationItemDTO itemDTO);
    void deleteItem(Long id);
} 