package com.example.configserver.service;

import com.example.configserver.dto.ConfigurationGroupDTO;
import com.example.configserver.dto.ConfigurationItemDTO;
import com.example.configserver.model.AuditLog;
import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.model.Environment;
import com.example.configserver.repository.AuditLogRepository;
import com.example.configserver.repository.ConfigurationGroupRepository;
import com.example.configserver.repository.ConfigurationItemRepository;
import com.example.configserver.service.ConfigurationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationGroupRepository groupRepository;
    private final ConfigurationItemRepository itemRepository;
    private final AuditLogRepository auditLogRepository;

    // Helper methods for conversion
    private ConfigurationGroupDTO mapToGroupDTO(ConfigurationGroup group) {
        return new ConfigurationGroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription()
        );
    }

    private ConfigurationItemDTO mapToItemDTO(ConfigurationItem item) {
        return new ConfigurationItemDTO(
                item.getId(),
                item.getKey(),
                item.getValue(),
                item.getDescription(),
                item.getEnvironment(),
                item.getGroupId(),
                item.getGroup() != null ? item.getGroup().getName() : null
        );
    }

    // Method to get the current user ID from the security context
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system"; // Default user for system operations
    }

    private AuditLog createAuditLog(String action, String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(LocalDateTime.now());
        log.setUserId(getCurrentUserId());
        return log;
    }

    @Override
    public ConfigurationItemDTO getItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::mapToItemDTO)
                .orElseThrow(() -> new EntityNotFoundException("Configuration item not found with id: " + id));
    }

    @Override
    @Transactional
    public ConfigurationItemDTO createItem(ConfigurationItemDTO itemDTO) {
        ConfigurationGroup group = groupRepository.findById(itemDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + itemDTO.getGroupId()));
        
        if (itemRepository.existsByKeyAndGroupAndEnvironment(itemDTO.getKey(), group, itemDTO.getEnvironment())) {
            throw new IllegalArgumentException(
                    "Configuration item with key " + itemDTO.getKey() + 
                    " already exists for group " + group.getName() + 
                    " in environment " + itemDTO.getEnvironment());
        }
        
        ConfigurationItem item = new ConfigurationItem();
        item.setKey(itemDTO.getKey());
        item.setValue(itemDTO.getValue());
        item.setDescription(itemDTO.getDescription());
        // Convert the String environment to the Environment enum
        item.setEnvironment(Environment.valueOf(itemDTO.getEnvironment()));
        item.setGroup(group);
        
        ConfigurationItem savedItem = itemRepository.save(item);
        
        createAuditLog("CREATE", "ConfigItem", savedItem.getId(),
                null,
                "key: " + savedItem.getKey() + 
                ", value: " + savedItem.getValue() + 
                ", env: " + savedItem.getEnvironment() + 
                ", groupId: " + savedItem.getGroup().getId());
        
        return mapToItemDTO(savedItem);
    }

    @Override
    @Transactional
    public ConfigurationItemDTO updateItem(Long id, ConfigurationItemDTO itemDTO) {
        ConfigurationItem item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configuration item not found with id: " + id));
        
        ConfigurationGroup group = groupRepository.findById(itemDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + itemDTO.getGroupId()));
        
        String oldKey = item.getKey();
        String oldValue = item.getValue();
        String oldEnvironment = item.getEnvironment();
        Long oldGroupId = item.getGroup().getId();
        
        // Check if key update conflicts with existing items
        if (!item.getKey().equals(itemDTO.getKey()) || 
            !item.getEnvironment().equals(itemDTO.getEnvironment()) ||
            !item.getGroup().getId().equals(itemDTO.getGroupId())) {
            
            if (itemRepository.existsByKeyAndGroupAndEnvironment(itemDTO.getKey(), group, itemDTO.getEnvironment())) {
                throw new IllegalArgumentException(
                        "Configuration item with key " + itemDTO.getKey() + 
                        " already exists for group " + group.getName() + 
                        " in environment " + itemDTO.getEnvironment());
            }
        }
        
        item.setKey(itemDTO.getKey());
        item.setValue(itemDTO.getValue());
        item.setDescription(itemDTO.getDescription());
        // Convert the String environment to the Environment enum
        item.setEnvironment(Environment.valueOf(itemDTO.getEnvironment()));
        item.setGroup(group);
        
        ConfigurationItem updatedItem = itemRepository.save(item);
        
        createAuditLog("UPDATE", "ConfigItem", updatedItem.getId(),
                "key: " + oldKey + 
                ", value: " + oldValue + 
                ", env: " + oldEnvironment + 
                ", groupId: " + oldGroupId,
                
                "key: " + updatedItem.getKey() + 
                ", value: " + updatedItem.getValue() + 
                ", env: " + updatedItem.getEnvironment() + 
                ", groupId: " + updatedItem.getGroup().getId());
        
        return mapToItemDTO(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        ConfigurationItem item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configuration item not found with id: " + id));
        
        itemRepository.delete(item);
        
        createAuditLog("DELETE", "ConfigItem", id,
                "key: " + item.getKey() + 
                ", value: " + item.getValue() + 
                ", env: " + item.getEnvironment() + 
                ", groupId: " + item.getGroup().getId(),
                null);
    }

    @Override
    public List<ConfigurationItemDTO> getItemsByGroup(Long groupId) {
        ConfigurationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        
        return itemRepository.findByGroup(group).stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationItemDTO> getItemsByGroupAndEnvironment(Long groupId, String environment) {
        ConfigurationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        
        return itemRepository.findByGroupAndEnvironment(group, environment).stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    // Group operations
    @Override
    public List<ConfigurationGroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConfigurationGroupDTO getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(this::mapToGroupDTO)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
    }

    @Override
    public ConfigurationGroupDTO getGroupByName(String name) {
        return groupRepository.findByName(name)
                .map(this::mapToGroupDTO)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with name: " + name));
    }

    @Override
    @Transactional
    public ConfigurationGroupDTO createGroup(ConfigurationGroupDTO groupDTO) {
        if (groupRepository.existsByName(groupDTO.getName())) {
            throw new IllegalArgumentException("Group with name " + groupDTO.getName() + " already exists");
        }

        ConfigurationGroup group = new ConfigurationGroup();
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        
        ConfigurationGroup savedGroup = groupRepository.save(group);
        
        createAuditLog("CREATE", "Group", savedGroup.getId(), null, savedGroup.getName());
        
        return mapToGroupDTO(savedGroup);
    }

    @Override
    @Transactional
    public ConfigurationGroupDTO updateGroup(Long id, ConfigurationGroupDTO groupDTO) {
        ConfigurationGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
        
        String oldName = group.getName();
        String oldDescription = group.getDescription();
        
        // Check if new name conflicts with existing groups
        if (!group.getName().equals(groupDTO.getName()) && groupRepository.existsByName(groupDTO.getName())) {
            throw new IllegalArgumentException("Group with name " + groupDTO.getName() + " already exists");
        }
        
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        
        ConfigurationGroup updatedGroup = groupRepository.save(group);
        
        createAuditLog("UPDATE", "Group", updatedGroup.getId(), 
                "name: " + oldName + ", description: " + oldDescription,
                "name: " + updatedGroup.getName() + ", description: " + updatedGroup.getDescription());
        
        return mapToGroupDTO(updatedGroup);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        ConfigurationGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
        
        groupRepository.delete(group);
        
        createAuditLog("DELETE", "Group", id, 
                "name: " + group.getName() + ", description: " + group.getDescription(),
                null);
    }
} 