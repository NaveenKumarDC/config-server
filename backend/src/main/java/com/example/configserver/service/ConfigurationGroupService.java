package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;

import java.util.List;
import java.util.Optional;

public interface ConfigurationGroupService {
    List<ConfigurationGroup> getAllGroups();
    Optional<ConfigurationGroup> getGroupById(Long id);
    Optional<ConfigurationGroup> getGroupByName(String name);
    ConfigurationGroup createGroup(ConfigurationGroup group);
    Optional<ConfigurationGroup> updateGroup(Long id, ConfigurationGroup group);
    boolean deleteGroup(Long id);
} 