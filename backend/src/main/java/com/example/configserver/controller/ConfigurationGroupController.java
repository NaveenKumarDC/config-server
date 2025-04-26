package com.example.configserver.controller;

import com.example.configserver.dto.ConfigurationGroupDTO;
import com.example.configserver.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Configuration Groups", description = "API for managing configuration groups")
public class ConfigurationGroupController {

    private final ConfigurationService configurationService;

    @GetMapping
    @Operation(summary = "Get all configuration groups")
    public ResponseEntity<List<ConfigurationGroupDTO>> getAllGroups() {
        return ResponseEntity.ok(configurationService.getAllGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a configuration group by ID")
    public ResponseEntity<ConfigurationGroupDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(configurationService.getGroupById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get a configuration group by name")
    public ResponseEntity<ConfigurationGroupDTO> getGroupByName(@PathVariable String name) {
        return ResponseEntity.ok(configurationService.getGroupByName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new configuration group")
    public ResponseEntity<ConfigurationGroupDTO> createGroup(@RequestBody ConfigurationGroupDTO groupDTO) {
        return new ResponseEntity<>(configurationService.createGroup(groupDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing configuration group")
    public ResponseEntity<ConfigurationGroupDTO> updateGroup(
            @PathVariable Long id, 
            @RequestBody ConfigurationGroupDTO groupDTO) {
        return ResponseEntity.ok(configurationService.updateGroup(id, groupDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a configuration group")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        configurationService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
} 