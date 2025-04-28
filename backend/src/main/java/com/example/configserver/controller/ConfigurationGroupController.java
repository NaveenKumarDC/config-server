package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.service.ConfigurationGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Configuration Groups", description = "API for managing configuration groups")
public class ConfigurationGroupController {

    private final ConfigurationGroupService groupService;

    @GetMapping
    @Operation(summary = "Get all configuration groups")
    public ResponseEntity<List<ConfigurationGroup>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a configuration group by ID")
    public ResponseEntity<ConfigurationGroup> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get a configuration group by name")
    public ResponseEntity<ConfigurationGroup> getGroupByName(@PathVariable String name) {
        return groupService.getGroupByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new configuration group")
    public ResponseEntity<ConfigurationGroup> createGroup(@RequestBody ConfigurationGroup group) {
        ConfigurationGroup createdGroup = groupService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing configuration group")
    public ResponseEntity<ConfigurationGroup> updateGroup(@PathVariable Long id, @RequestBody ConfigurationGroup group) {
        return groupService.updateGroup(id, group)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a configuration group")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        if (groupService.deleteGroup(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 