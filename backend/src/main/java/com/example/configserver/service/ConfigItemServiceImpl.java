package com.example.configserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigItemRepository;
import com.example.configserver.repository.ConfigGroupRepository;

@Service
@Transactional
public class ConfigItemServiceImpl implements ConfigItemService {

    @Autowired
    private ConfigItemRepository itemRepository;
    
    @Autowired
    private ConfigGroupRepository groupRepository;
    
    @Autowired
    private AuditService auditService;

    @Override
    public List<ConfigurationItem> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Optional<ConfigurationItem> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<ConfigurationItem> getItemsByGroup(Long groupId) {
        return itemRepository.findByGroup_Id(groupId);
    }

    @Override
    public Optional<List<ConfigurationItem>> getItemsByGroupAndEnvironment(Long groupId, String environment) {
        return Optional.ofNullable(itemRepository.findByGroup_IdAndEnvironment(groupId, environment));
    }

    @Override
    public Optional<ConfigurationItem> createItem(ConfigurationItem item, String userId) {
        // Set the group reference from the provided groupId
        if (item.getGroupId() != null) {
            groupRepository.findById(item.getGroupId())
                .ifPresent(item::setGroup);
        }
        
        if (item.getGroup() == null) {
            throw new IllegalArgumentException("Group not found for id: " + item.getGroupId());
        }
        
        ConfigurationItem savedItem = itemRepository.save(item);
        auditService.logItemCreation(savedItem, userId);
        return Optional.ofNullable(savedItem);
    }

    @Override
    public Optional<ConfigurationItem> updateItem(Long id, ConfigurationItem updatedItem, String userId) {
        Optional<ConfigurationItem> existingItemOpt = itemRepository.findById(id);
        
        if (!existingItemOpt.isPresent()) {
            return Optional.empty();
        }
        
        ConfigurationItem existingItem = existingItemOpt.get();
        ConfigurationItem itemBeforeUpdate = new ConfigurationItem();
        // Copy the existing item before updating for audit purposes
        copyItem(existingItem, itemBeforeUpdate);
        
        // Update fields
        existingItem.setKey(updatedItem.getKey());
        existingItem.setValue(updatedItem.getValue());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setEnvironment(updatedItem.getEnvironment());
        
        // Handle group update if groupId changed
        if (updatedItem.getGroupId() != null && 
            (existingItem.getGroup() == null || !updatedItem.getGroupId().equals(existingItem.getGroup().getId()))) {
            Optional<ConfigurationGroup> groupOpt = groupRepository.findById(updatedItem.getGroupId());
            if (groupOpt.isPresent()) {
                existingItem.setGroup(groupOpt.get());
            } else {
                return Optional.empty(); // Group not found
            }
        }
        
        ConfigurationItem saved = itemRepository.save(existingItem);
        auditService.logItemUpdate(itemBeforeUpdate, saved, userId);
        
        return Optional.of(saved);
    }

    @Override
    public void deleteItem(Long id, String userId) {
        Optional<ConfigurationItem> itemOpt = itemRepository.findById(id);
        
        if (!itemOpt.isPresent()) {
            System.out.println("Item with id " + id + " does not exist for deletion");
            throw new IllegalArgumentException("Item with id " + id + " does not exist");
        }
        
        ConfigurationItem item = itemOpt.get();
        try {
            System.out.println("Deleting item with id " + id + " by user " + userId);
            itemRepository.delete(item);
            auditService.logItemDeletion(item, userId);
            System.out.println("Successfully deleted item with id " + id);
        } catch (Exception e) {
            System.err.println("Error deleting item with id " + id + ": " + e.getMessage());
            throw e;
        }
    }
    
    private void copyItem(ConfigurationItem source, ConfigurationItem target) {
        target.setId(source.getId());
        target.setKey(source.getKey());
        target.setValue(source.getValue());
        target.setDescription(source.getDescription());
        target.setEnvironment(source.getEnvironment());
        target.setGroupId(source.getGroupId());
    }
} 