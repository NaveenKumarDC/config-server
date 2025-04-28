package com.example.configserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.configserver.model.ConfigurationGroup;

@Repository
public interface ConfigGroupRepository extends JpaRepository<ConfigurationGroup, Long> {
    
    /**
     * Find a configuration group by name
     * 
     * @param name The name of the group
     * @return The configuration group with the given name
     */
    ConfigurationGroup findByName(String name);
} 