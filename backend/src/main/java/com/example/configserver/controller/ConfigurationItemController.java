package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.service.ConfigurationItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Configuration Items", description = "API for managing configuration items")
public class ConfigurationItemController {

    private final ConfigurationItemService itemService;

    @Autowired
    private ConfigurationItemController(ConfigurationItemService configurationItemService) {
        itemService = configurationItemService;
    }

    @GetMapping
    @Operation(summary = "Get all configuration items")
    public ResponseEntity<List<ConfigurationItem>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a configuration item by ID")
    public ResponseEntity<ConfigurationItem> getItemById(@PathVariable Long id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get all configuration items for a specific group")
    public ResponseEntity<List<ConfigurationItem>> getItemsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(itemService.getItemsByGroup(groupId));
    }

    @GetMapping("/group/{groupId}/environment/{environment}")
    @Operation(summary = "Get all configuration items for a specific group and environment")
    public ResponseEntity<List<ConfigurationItem>> getItemsByGroupAndEnvironment(
            @PathVariable Long groupId,
            @PathVariable String environment) {
        return ResponseEntity.ok(itemService.getItemsByGroupAndEnvironment(groupId, environment));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new configuration item")
    public ResponseEntity<ConfigurationItem> createItem(@RequestBody ConfigurationItem item) {
        ConfigurationItem createdItem = itemService.createItem(item);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing configuration item")
    public ResponseEntity<ConfigurationItem> updateItem(
            @PathVariable Long id,
            @RequestBody ConfigurationItem item) {
        return itemService.updateItem(id, item)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a configuration item")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.deleteItem(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 