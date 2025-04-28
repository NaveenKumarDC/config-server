package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigurationGroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = {ConfigGroupServiceContainerTest.Initializer.class})
class ConfigGroupServiceContainerTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(@NonNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private ConfigurationGroupService groupService;

    @Autowired
    private ConfigurationGroupRepository groupRepository;

    private ConfigurationGroup testGroup;

    @BeforeEach
    void setUp() {
        // Create a test group
        testGroup = new ConfigurationGroup();
        // Use setter methods instead of direct field assignment
        testGroup.setId(1L);
        testGroup.setName("test-group");
        testGroup.setDescription("Test Group Description");
        
        groupRepository.save(testGroup);
    }

    @AfterEach
    void tearDown() {
        groupRepository.deleteAll();
    }

    @Test
    void getAllGroups_ShouldReturnAllGroups() {
        // Act
        List<ConfigurationGroup> result = groupService.getAllGroups();

        // Assert
        assertEquals(1, result.size());
        assertEquals("test-group", result.get(0).getName());
    }

    @Test
    void getGroupById_WithExistingId_ShouldReturnGroup() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(testGroup.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test-group", result.get().getName());
    }

    @Test
    void getGroupById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getGroupByName_WithExistingName_ShouldReturnGroup() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("test-group");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test-group", result.get().getName());
    }

    @Test
    void getGroupByName_WithNonExistingName_ShouldReturnEmpty() {
        // Act
        Optional<ConfigurationGroup> result = groupService.getGroupByName("non-existent");

        // Assert
        assertTrue(result.isEmpty());
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
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("new-group", result.getName());
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
        assertTrue(result.isPresent());
        assertEquals("updated-group", result.get().getName());
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
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteGroup_WithExistingId_ShouldReturnTrueAndRemoveGroup() {
        // Act
        boolean result = groupService.deleteGroup(testGroup.getId());

        // Assert
        assertTrue(result);
        assertTrue(groupRepository.findById(testGroup.getId()).isEmpty());
    }

    @Test
    void deleteGroup_WithNonExistingId_ShouldReturnFalse() {
        // Act
        boolean result = groupService.deleteGroup(999L);

        // Assert
        assertFalse(result);
    }
} 