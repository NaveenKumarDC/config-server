package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigurationGroupRepository;
import com.example.configserver.service.impl.ConfigGroupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigGroupServiceTest {

    @Mock
    private ConfigurationGroupRepository groupRepository;

    @InjectMocks
    private ConfigGroupServiceImpl groupService;

    @Test
    void getAllGroups_ShouldReturnAllGroups() {
        // Arrange
        ConfigurationGroup group = mock(ConfigurationGroup.class);
        List<ConfigurationGroup> groups = Arrays.asList(group);
        when(groupRepository.findAll()).thenReturn(groups);

        // Act
        List<ConfigurationGroup> result = groupService.getAllGroups();

        // Assert
        assertEquals(1, result.size());
        assertSame(group, result.get(0));
        verify(groupRepository).findAll();
    }

    @Test
    void getGroupById_WithExistingId_ShouldReturnGroup() {
        // Arrange
        ConfigurationGroup group = mock(ConfigurationGroup.class);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertSame(group, result.get());
        verify(groupRepository).findById(1L);
    }

    @Test
    void getGroupById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(999L);

        // Assert
        assertTrue(result.isEmpty());
        verify(groupRepository).findById(999L);
    }

    @Test
    void getGroupByName_WithExistingName_ShouldReturnGroup() {
        // Arrange
        ConfigurationGroup group = mock(ConfigurationGroup.class);
        when(groupRepository.findByName("test-group")).thenReturn(Optional.of(group));

        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("test-group");

        // Assert
        assertTrue(result.isPresent());
        assertSame(group, result.get());
        verify(groupRepository).findByName("test-group");
    }

    @Test
    void getGroupByName_WithNonExistingName_ShouldReturnEmpty() {
        // Arrange
        when(groupRepository.findByName("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("non-existent");

        // Assert
        assertTrue(result.isEmpty());
        verify(groupRepository).findByName("non-existent");
    }

    @Test
    void createGroup_WithValidData_ShouldCreateAndReturnGroup() {
        // Arrange
        ConfigurationGroup groupToCreate = mock(ConfigurationGroup.class);
        ConfigurationGroup savedGroup = mock(ConfigurationGroup.class);
        when(groupRepository.save(any(ConfigurationGroup.class))).thenReturn(savedGroup);

        // Act
        ConfigurationGroup result = groupService.createGroup(groupToCreate);

        // Assert
        assertNotNull(result);
        assertSame(savedGroup, result);
        verify(groupRepository).save(groupToCreate);
    }

    @Test
    void updateGroup_WithExistingIdAndValidData_ShouldUpdateAndReturnGroup() {
        // Arrange
        ConfigurationGroup existingGroup = mock(ConfigurationGroup.class);
        ConfigurationGroup groupToUpdate = mock(ConfigurationGroup.class);
        ConfigurationGroup updatedGroup = mock(ConfigurationGroup.class);
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupToUpdate.getName()).thenReturn("updated-group");
        when(groupToUpdate.getDescription()).thenReturn("Updated Description");
        when(groupRepository.save(existingGroup)).thenReturn(updatedGroup);

        // Act
        Optional<ConfigurationGroup> result = groupService.updateGroup(1L, groupToUpdate);

        // Assert
        assertTrue(result.isPresent());
        assertSame(updatedGroup, result.get());
        verify(groupRepository).findById(1L);
        verify(existingGroup).setName("updated-group");
        verify(existingGroup).setDescription("Updated Description");
        verify(groupRepository).save(existingGroup);
    }

    @Test
    void updateGroup_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        ConfigurationGroup groupToUpdate = mock(ConfigurationGroup.class);
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationGroup> result = groupService.updateGroup(999L, groupToUpdate);

        // Assert
        assertTrue(result.isEmpty());
        verify(groupRepository).findById(999L);
        verify(groupRepository, never()).save(any(ConfigurationGroup.class));
    }

    @Test
    void deleteGroup_WithExistingId_ShouldReturnTrue() {
        // Arrange
        when(groupRepository.existsById(1L)).thenReturn(true);
        doNothing().when(groupRepository).deleteById(1L);

        // Act
        boolean result = groupService.deleteGroup(1L);

        // Assert
        assertTrue(result);
        verify(groupRepository).existsById(1L);
        verify(groupRepository).deleteById(1L);
    }

    @Test
    void deleteGroup_WithNonExistingId_ShouldReturnFalse() {
        // Arrange
        when(groupRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = groupService.deleteGroup(999L);

        // Assert
        assertFalse(result);
        verify(groupRepository).existsById(999L);
        verify(groupRepository, never()).deleteById(anyLong());
    }
} 