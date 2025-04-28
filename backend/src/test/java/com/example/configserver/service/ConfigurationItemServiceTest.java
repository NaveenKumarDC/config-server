package com.example.configserver.service;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.model.Environment;
import com.example.configserver.repository.ConfigurationGroupRepository;
import com.example.configserver.repository.ConfigurationItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigurationItemServiceTest {

    @Mock
    private ConfigurationItemRepository itemRepository;

    @Mock
    private ConfigurationGroupRepository groupRepository;

    @InjectMocks
    private ConfigurationItemServiceImpl itemService;

    private ConfigurationGroup testGroup;
    private ConfigurationItem testItem;
    private ConfigurationItem itemToCreate;
    private ConfigurationItem itemToUpdate;

    @BeforeEach
    void setUp() {
        // Setup test group
        testGroup = new ConfigurationGroup();
        testGroup.setId(1L);
        testGroup.setName("test-group");
        testGroup.setDescription("Test Group Description");

        // Setup test item
        testItem = new ConfigurationItem();
        testItem.setId(1L);
        testItem.setKey("test.key");
        testItem.setValue("test-value");
        testItem.setDescription("Test Item Description");
        testItem.setGroup(testGroup);
        testItem.setEnvironment(Environment.DEV.name());
        
        // Setup item to create (without group set, will be set by service)
        itemToCreate = new ConfigurationItem();
        itemToCreate.setKey("test.key");
        itemToCreate.setValue("test-value");
        itemToCreate.setDescription("Test Item Description");
        itemToCreate.setEnvironment(Environment.DEV.name());
        // We'll set the GroupId in the test to simulate what would happen when the DTO is converted
        
        // Setup item to update
        itemToUpdate = new ConfigurationItem();
        itemToUpdate.setKey("updated.key");
        itemToUpdate.setValue("updated-value");
        itemToUpdate.setDescription("Updated description");
        itemToUpdate.setEnvironment(Environment.DEV.name());
        // GroupId will be set in the test
    }

    @Test
    void getAllItems_ShouldReturnAllItems() {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        when(itemRepository.findAll()).thenReturn(items);

        // Act
        List<ConfigurationItem> result = itemService.getAllItems();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("test.key");
        verify(itemRepository).findAll();
    }

    @Test
    void getItemById_WithExistingId_ShouldReturnItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isEqualTo("test.key");
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(itemRepository).findById(999L);
    }

    @Test
    void getItemsByGroup_ShouldReturnItemsForGroup() {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        when(itemRepository.findByGroup_Id(1L)).thenReturn(items);

        // Act
        List<ConfigurationItem> result = itemService.getItemsByGroup(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("test.key");
        verify(itemRepository).findByGroup_Id(1L);
    }

    @Test
    void getItemsByGroupAndEnvironment_ShouldReturnFilteredItems() {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        String envName = Environment.DEV.name();
        when(itemRepository.findByGroup_IdAndEnvironment(eq(1L), eq(envName))).thenReturn(items);

        // Act
        List<ConfigurationItem> result = itemService.getItemsByGroupAndEnvironment(1L, envName);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("test.key");
        assertThat(result.get(0).getEnvironment()).isEqualTo(Environment.DEV.name());
        verify(itemRepository).findByGroup_IdAndEnvironment(eq(1L), eq(envName));
    }

    @Test
    void createItem_WithValidData_ShouldCreateAndReturnItem() {
        // Arrange
        // Set the groupId property which would normally be set from the DTO
        when(itemToCreate.getGroupId()).thenReturn(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(itemRepository.save(any(ConfigurationItem.class))).thenReturn(testItem);

        // Act
        ConfigurationItem result = itemService.createItem(itemToCreate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("test.key");
        assertThat(result.getValue()).isEqualTo("test-value");
        assertThat(result.getGroup().getName()).isEqualTo("test-group");
        assertThat(result.getEnvironment()).isEqualTo(Environment.DEV.name());
        verify(groupRepository).findById(1L);
        verify(itemRepository).save(any(ConfigurationItem.class));
    }

    @Test
    void updateItem_WithExistingIdAndValidData_ShouldUpdateAndReturnItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemToUpdate.getGroupId()).thenReturn(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(itemRepository.save(any(ConfigurationItem.class))).thenReturn(testItem);

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(1L, itemToUpdate);

        // Assert
        assertThat(result).isPresent();
        verify(itemRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(itemRepository).save(any(ConfigurationItem.class));
    }

    @Test
    void updateItem_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(999L, itemToUpdate);

        // Assert
        assertThat(result).isEmpty();
        verify(itemRepository).findById(999L);
        verify(itemRepository, never()).save(any(ConfigurationItem.class));
    }

    @Test
    void deleteItem_WithExistingId_ShouldReturnTrue() {
        // Arrange
        when(itemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(1L);

        // Act
        boolean result = itemService.deleteItem(1L);

        // Assert
        assertThat(result).isTrue();
        verify(itemRepository).existsById(1L);
        verify(itemRepository).deleteById(1L);
    }

    @Test
    void deleteItem_WithNonExistingId_ShouldReturnFalse() {
        // Arrange
        when(itemRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = itemService.deleteItem(999L);

        // Assert
        assertThat(result).isFalse();
        verify(itemRepository).existsById(999L);
        verify(itemRepository, never()).deleteById(999L);
    }
} 