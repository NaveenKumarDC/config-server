package com.example.configserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.configserver.model.ConfigurationItem;

@Repository
public interface ConfigItemRepository extends JpaRepository<ConfigurationItem, Long> {
    
    /**
     * Find all configuration items for a specific group
     * 
     * @param groupId The ID of the group
     * @return List of configuration items for the group
     */
    List<ConfigurationItem> findByGroup_Id(Long groupId);
    
    /**
     * Find all configuration items for a specific group and environment
     * 
     * @param groupId The ID of the group
     * @param environment The environment name
     * @return List of configuration items for the group and environment
     */
    List<ConfigurationItem> findByGroup_IdAndEnvironment(Long groupId, String environment);
} 