package com.example.configserver.controller;

import com.example.configserver.dto.ConfigurationItemDTO;
import com.example.configserver.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Configuration Items", description = "API for managing configuration items")
public class ConfigurationItemController {

    private final ConfigurationService configurationService;

    @GetMapping
    @Operation(summary = "Get all configuration items")
    public ResponseEntity<List<ConfigurationItemDTO>> getAllItems() {
        return ResponseEntity.ok(configurationService.getAllItems());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a configuration item by ID")
    public ResponseEntity<ConfigurationItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(configurationService.getItemById(id));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get all configuration items for a specific group")
    public ResponseEntity<List<ConfigurationItemDTO>> getItemsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(configurationService.getItemsByGroup(groupId));
    }

    @GetMapping("/group/{groupId}/environment/{environment}")
    @Operation(summary = "Get all configuration items for a specific group and environment")
    public ResponseEntity<List<ConfigurationItemDTO>> getItemsByGroupAndEnvironment(
            @PathVariable Long groupId,
            @PathVariable String environment) {
        return ResponseEntity.ok(configurationService.getItemsByGroupAndEnvironment(groupId, environment));
    }

    @PostMapping
    @Operation(summary = "Create a new configuration item")
    public ResponseEntity<ConfigurationItemDTO> createItem(@RequestBody ConfigurationItemDTO itemDTO) {
        return new ResponseEntity<>(configurationService.createItem(itemDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing configuration item")
    public ResponseEntity<ConfigurationItemDTO> updateItem(
            @PathVariable Long id,
            @RequestBody ConfigurationItemDTO itemDTO) {
        return ResponseEntity.ok(configurationService.updateItem(id, itemDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a configuration item")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        configurationService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
} 