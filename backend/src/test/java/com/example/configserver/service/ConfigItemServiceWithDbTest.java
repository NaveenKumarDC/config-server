package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.repository.ConfigGroupRepository;
import com.example.configserver.repository.ConfigItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ConfigItemServiceWithDbTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ConfigItemService itemService;

    @Autowired
    private ConfigItemRepository itemRepository;

    @Autowired
    private ConfigGroupRepository groupRepository;

    private ConfigurationGroup testGroup;
    private ConfigurationItem testItem;
    private final String testUserId = "test-user";

    @BeforeEach
    void setUp() {
        // Clean up the repositories
        itemRepository.deleteAll();
        groupRepository.deleteAll();

        // Create a test group
        testGroup = new ConfigurationGroup();
        testGroup.setName("test-group");
        testGroup.setDescription("Test group for service tests");
        testGroup = groupRepository.save(testGroup);

        // Create a test item
        testItem = new ConfigurationItem();
        testItem.setKey("test.key");
        testItem.setValue("test-value");
        testItem.setDescription("Test item for service tests");
        testItem.setGroupId(testGroup.getId());
        testItem.setEnvironment("DEV");
        testItem = itemRepository.save(testItem);
    }

    @Test
    void getAllItems_ReturnsAllItems() {
        // Arrange - additional item
        ConfigurationItem item2 = new ConfigurationItem();
        item2.setKey("test.key2");
        item2.setValue("test-value2");
        item2.setGroupId(testGroup.getId());
        item2.setEnvironment("PROD");
        itemRepository.save(item2);

        // Act
        List<ConfigurationItem> items = itemService.getAllItems();

        // Assert
        assertThat(items).hasSize(2);
        assertThat(items).extracting(ConfigurationItem::getKey)
                .containsExactlyInAnyOrder("test.key", "test.key2");
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(testItem.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isEqualTo("test.key");
        assertThat(result.get().getValue()).isEqualTo("test-value");
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ReturnsEmpty() {
        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(999L); // Changed from String to Long

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getItemsByGroup_ReturnsItemsForGroup() {
        // Arrange - create another group and item
        ConfigurationGroup anotherGroup = new ConfigurationGroup();
        anotherGroup.setName("another-group");
        anotherGroup = groupRepository.save(anotherGroup);

        ConfigurationItem itemInAnotherGroup = new ConfigurationItem();
        itemInAnotherGroup.setKey("another.key");
        itemInAnotherGroup.setValue("another-value");
        itemInAnotherGroup.setGroupId(anotherGroup.getId());
        itemInAnotherGroup.setEnvironment("DEV");
        itemRepository.save(itemInAnotherGroup);

        // Act
        List<ConfigurationItem> items = itemService.getItemsByGroup(testGroup.getId());

        // Assert
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getKey()).isEqualTo("test.key");
        assertThat(items.get(0).getGroupId()).isEqualTo(testGroup.getId());
    }

    @Test
    void getItemsByGroupAndEnvironment_ReturnsFilteredItems() {
        // Arrange - additional item in same group but different environment
        ConfigurationItem prodItem = new ConfigurationItem();
        prodItem.setKey("test.key.prod");
        prodItem.setValue("prod-value");
        prodItem.setGroupId(testGroup.getId());
        prodItem.setEnvironment("PROD");
        itemRepository.save(prodItem);

        // Act
        Optional<List<ConfigurationItem>> devItems = itemService.getItemsByGroupAndEnvironment(testGroup.getId(), "DEV");
        Optional<List<ConfigurationItem>> prodItems = itemService.getItemsByGroupAndEnvironment(testGroup.getId(), "PROD");

        // Assert
        assertThat(devItems.get()).hasSize(1);
        assertThat(devItems.get().get(0).getKey()).isEqualTo("test.key");
        assertThat(devItems.get().get(0).getEnvironment()).isEqualTo("DEV");

        assertThat(prodItems.get()).hasSize(1);
        assertThat(prodItems.get().get(0).getKey()).isEqualTo("test.key.prod");
        assertThat(prodItems.get().get(0).getEnvironment()).isEqualTo("PROD");
    }

    @Test
    void createItem_SavesAndReturnsItem() {
        // Arrange
        ConfigurationItem newItem = new ConfigurationItem();
        newItem.setKey("new.key");
        newItem.setValue("new-value");
        newItem.setDescription("New item");
        newItem.setGroupId(testGroup.getId());
        newItem.setEnvironment("STAGE");

        // Act
        Optional<ConfigurationItem> createdItem = itemService.createItem(newItem, testUserId);

        // Assert
        assertThat(createdItem.get().getId()).isNotNull();
        assertThat(createdItem.get().getKey()).isEqualTo("new.key");
        assertThat(createdItem.get().getValue()).isEqualTo("new-value");
        assertThat(createdItem.get().getEnvironment()).isEqualTo("STAGE");

        // Verify it was saved in the database
        Optional<ConfigurationItem> fromDb = itemRepository.findById(createdItem.get().getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getKey()).isEqualTo("new.key");
    }

    @Test
    void updateItem_WhenItemExists_UpdatesAndReturnsItem() {
        // Arrange
        ConfigurationItem updatedItem = new ConfigurationItem();
        updatedItem.setKey("updated.key");
        updatedItem.setValue("updated-value");
        updatedItem.setDescription("Updated description");
        updatedItem.setGroupId(testGroup.getId());
        updatedItem.setEnvironment("DEV");

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(testItem.getId(), updatedItem, testUserId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testItem.getId());
        assertThat(result.get().getKey()).isEqualTo("updated.key");
        assertThat(result.get().getValue()).isEqualTo("updated-value");
        assertThat(result.get().getDescription()).isEqualTo("Updated description");

        // Verify it was updated in the database
        Optional<ConfigurationItem> fromDb = itemRepository.findById(testItem.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getKey()).isEqualTo("updated.key");
        assertThat(fromDb.get().getValue()).isEqualTo("updated-value");
    }

    @Test
    void updateItem_WhenItemDoesNotExist_ReturnsNull() {
        // Arrange
        ConfigurationItem updatedItem = new ConfigurationItem();
        updatedItem.setKey("nonexistent.key");
        updatedItem.setValue("updated-value");

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(999L, updatedItem, testUserId); // Changed from String to Long

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deleteItem_WhenItemExists_DeletesItem() {
        // Act
        itemService.deleteItem(testItem.getId(), testUserId);

        // Assert
        assertThat(itemRepository.existsById(testItem.getId())).isFalse();
    }

    @Test
    void deleteItem_WhenItemDoesNotExist_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> 
            itemService.deleteItem(999L, testUserId) // Changed from String to Long
        ).isInstanceOf(Exception.class);
    }
}