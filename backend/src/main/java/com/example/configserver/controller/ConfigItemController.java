package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.service.ConfigItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/config-items")
public class ConfigItemController {

  private final ConfigItemService itemService;

  @Autowired
  public ConfigItemController(ConfigItemService itemService) {
    this.itemService = itemService;
  }

  @GetMapping
  public ResponseEntity<List<ConfigurationItem>> getAllItems() {
    return ResponseEntity.ok(itemService.getAllItems());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ConfigurationItem> getItemById(@PathVariable Long id) {
    return itemService.getItemById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/group/{groupId}")
  public ResponseEntity<List<ConfigurationItem>> getItemsByGroup(@PathVariable Long groupId) {
    return ResponseEntity.ok(itemService.getItemsByGroup(groupId));
  }

  @GetMapping("/group/{groupId}/environment/{environment}")
  public ResponseEntity<List<ConfigurationItem>> getItemsByGroupAndEnvironment(
          @PathVariable Long groupId, @PathVariable String environment) {
    return ResponseEntity.ok(itemService.getItemsByGroupAndEnvironment(groupId, environment).get());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ConfigurationItem> createItem(
          @RequestBody ConfigurationItem item,
          @AuthenticationPrincipal UserDetails userDetails) {
    String userId = userDetails.getUsername();
    Optional<ConfigurationItem> createdItem = itemService.createItem(item, userId);
    if (createdItem.isPresent()) {
      return ResponseEntity.status(HttpStatus.CREATED).body(createdItem.get());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ConfigurationItem> updateItem(
          @PathVariable Long id,
          @RequestBody ConfigurationItem item,
          @AuthenticationPrincipal UserDetails userDetails) {
    String userId = userDetails.getUsername();
    Optional<ConfigurationItem> updatedItem = itemService.updateItem(id, item, userId);
    if (updatedItem.isPresent() && updatedItem.get() != null) {
      return ResponseEntity.ok(updatedItem.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteItem(
          @PathVariable Long id,
          @AuthenticationPrincipal UserDetails userDetails) {
    String userId = userDetails.getUsername();
    try {
      itemService.deleteItem(id, userId);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
} 