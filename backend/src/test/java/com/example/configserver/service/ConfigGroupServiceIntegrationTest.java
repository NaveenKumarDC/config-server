package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigurationGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConfigGroupServiceIntegrationTest {

    @Autowired
    private ConfigurationGroupService groupService;

    @Autowired
    private ConfigurationGroupRepository groupRepository;

    private ConfigurationGroup testGroup;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        groupRepository.deleteAll();

        // Create and persist test group
        testGroup = new ConfigurationGroup();
        testGroup.setName("test-group");
        testGroup.setDescription("Test Group Description");
        
        testGroup = groupRepository.save(testGroup);
    }

    @Test
    void getAllGroups_ShouldReturnAllGroups() {
        // Act
        List<ConfigurationGroup> result = groupService.getAllGroups();

        // Assert
        assertThat(result).hasSize(1);
        ConfigurationGroup group = result.get(0);
        assertThat(group.getId()).isNotNull();
        assertThat(group.getName()).isEqualTo("test-group");
    }

    @Test
    void getGroupById_WithExistingId_ShouldReturnGroup() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(testGroup.getId());

        // Assert
        assertThat(result).isPresent();
        ConfigurationGroup group = result.get();
        assertThat(group.getId()).isEqualTo(testGroup.getId());
        assertThat(group.getName()).isEqualTo("test-group");
    }

    @Test
    void getGroupById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getGroupByName_WithExistingName_ShouldReturnGroup() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("test-group");

        // Assert
        assertThat(result).isPresent();
        ConfigurationGroup group = result.get();
        assertThat(group.getName()).isEqualTo("test-group");
    }

    @Test
    void getGroupByName_WithNonExistingName_ShouldReturnEmpty() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("non-existent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createGroup_WithValidData_ShouldCreateAndReturnGroup() {
        // Arrange
        ConfigurationGroup groupToCreate = new ConfigurationGroup();
        groupToCreate.setName("new-group");
        groupToCreate.setDescription("New Group Description");

        // Act
        ConfigurationGroup result = groupService.createGroup(groupToCreate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("new-group");
        assertThat(result.getDescription()).isEqualTo("New Group Description");
        
        // Verify the group is in the database
        assertThat(groupRepository.findById(result.getId())).isPresent();
    }

    @Test
    void updateGroup_WithExistingIdAndValidData_ShouldUpdateAndReturnGroup() {
        // Arrange
        ConfigurationGroup groupToUpdate = new ConfigurationGroup();
        groupToUpdate.setName("updated-group");
        groupToUpdate.setDescription("Updated Group Description");

        // Act
        Optional<ConfigurationGroup> result = groupService.updateGroup(testGroup.getId(), groupToUpdate);

        // Assert
        assertThat(result).isPresent();
        ConfigurationGroup updatedGroup = result.get();
        assertThat(updatedGroup.getId()).isEqualTo(testGroup.getId());
        assertThat(updatedGroup.getName()).isEqualTo("updated-group");
        assertThat(updatedGroup.getDescription()).isEqualTo("Updated Group Description");
        
        // Verify the changes are in the database
        Optional<ConfigurationGroup> fromDb = groupRepository.findById(testGroup.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getName()).isEqualTo("updated-group");
    }

    @Test
    void updateGroup_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        ConfigurationGroup groupToUpdate = new ConfigurationGroup();
        groupToUpdate.setName("updated-group");
        groupToUpdate.setDescription("Updated Group Description");

        // Act
        Optional<ConfigurationGroup> result = groupService.updateGroup(999L, groupToUpdate);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deleteGroup_WithExistingId_ShouldReturnTrueAndRemoveGroup() {
        // Act
        boolean result = groupService.deleteGroup(testGroup.getId());

        // Assert
        assertThat(result).isTrue();
        assertThat(groupRepository.findById(testGroup.getId())).isEmpty();
    }

    @Test
    void deleteGroup_WithNonExistingId_ShouldReturnFalse() {
        // Act
        boolean result = groupService.deleteGroup(999L);

        // Assert
        assertThat(result).isFalse();
    }
} 