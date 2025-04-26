package com.example.configserver.repository;

import com.example.configserver.model.ConfigurationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationGroupRepository extends JpaRepository<ConfigurationGroup, Long> {
    Optional<ConfigurationGroup> findByName(String name);
    boolean existsByName(String name);
} 