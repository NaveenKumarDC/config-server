package com.example.configserver.service.impl;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigurationGroupRepository;
import com.example.configserver.service.ConfigurationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigGroupServiceImpl implements ConfigurationGroupService {

    private final ConfigurationGroupRepository groupRepository;

    @Override
    public List<ConfigurationGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Optional<ConfigurationGroup> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    public Optional<ConfigurationGroup> getGroupByName(String name) {
        return groupRepository.findByName(name);
    }

    @Override
    @Transactional
    public ConfigurationGroup createGroup(ConfigurationGroup group) {
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public Optional<ConfigurationGroup> updateGroup(Long id, ConfigurationGroup updatedGroup) {
        return groupRepository.findById(id)
                .map(existingGroup -> {
                    existingGroup.setName(updatedGroup.getName());
                    existingGroup.setDescription(updatedGroup.getDescription());
                    return groupRepository.save(existingGroup);
                });
    }

    @Override
    @Transactional
    public boolean deleteGroup(Long id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 