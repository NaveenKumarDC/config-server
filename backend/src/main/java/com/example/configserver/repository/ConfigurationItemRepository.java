package com.example.configserver.repository;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItem, Long> {
    List<ConfigurationItem> findByGroup(ConfigurationGroup group);
    List<ConfigurationItem> findByGroupAndEnvironment(ConfigurationGroup group, String environment);
    Optional<ConfigurationItem> findByKeyAndGroupAndEnvironment(String key, ConfigurationGroup group, String environment);
    boolean existsByKeyAndGroupAndEnvironment(String key, ConfigurationGroup group, String environment);
} 