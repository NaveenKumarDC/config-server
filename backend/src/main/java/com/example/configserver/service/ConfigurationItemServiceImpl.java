package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.repository.ConfigurationGroupRepository;
import com.example.configserver.repository.ConfigurationItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationItemServiceImpl implements ConfigurationItemService {

    private final ConfigurationItemRepository itemRepository;
    private final ConfigurationGroupRepository groupRepository;
    
    @Autowired
    public ConfigurationItemServiceImpl(ConfigurationItemRepository itemRepository, 
                                       ConfigurationGroupRepository groupRepository) {
        this.itemRepository = itemRepository;
        this.groupRepository = groupRepository;
    }

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
    public List<ConfigurationItem> getItemsByGroupAndEnvironment(Long groupId, String environment) {
        return itemRepository.findByGroup_IdAndEnvironment(groupId, environment);
    }

    @Override
    @Transactional
    public ConfigurationItem createItem(ConfigurationItem item) {
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public Optional<ConfigurationItem> updateItem(Long id, ConfigurationItem updatedItem) {
        return itemRepository.findById(id)
                .map(existingItem -> itemRepository.save(updatedItem));
    }

    @Override
    @Transactional
    public boolean deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 