package com.example.configserver.service;

import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.repository.ConfigItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigItemServiceTest {

    @Mock
    private ConfigItemRepository itemRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ConfigItemServiceImpl itemService;

    private ConfigurationItem testItem;
    private Long testItemId;
    private Long groupId;

    @BeforeEach
    void setUp() {
        testItemId = 1L;
        groupId = 1L;

        testItem = new ConfigurationItem();
        testItem.setId(testItemId);
        testItem.setKey("app.timeout");
        testItem.setValue("30");
        testItem.setDescription("Application timeout in seconds");
        testItem.setGroupId(groupId);
        testItem.setEnvironment("DEV");
    }

    @Test
    void getAllItems_ReturnsAllItems() {
        // Arrange
        ConfigurationItem item2 = new ConfigurationItem();
        item2.setId(2L);
        item2.setKey("app.retries");
        item2.setValue("3");
        item2.setGroupId(groupId);
        item2.setEnvironment("PROD");

        List<ConfigurationItem> expectedItems = Arrays.asList(testItem, item2);
        when(itemRepository.findAll()).thenReturn(expectedItems);

        // Act
        List<ConfigurationItem> actualItems = itemService.getAllItems();

        // Assert
        assertThat(actualItems).isEqualTo(expectedItems);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        // Arrange
        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(testItemId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testItem);
        verify(itemRepository, times(1)).findById(testItemId);
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ReturnsEmpty() {
        // Arrange
        Long nonExistentId = 999L;
        when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationItem> result = itemService.getItemById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
        verify(itemRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void getItemsByGroupShouldReturnItems() {
        // Arrange
        Long groupId = 1L;
        List<ConfigurationItem> expectedItems = new ArrayList<>();
        expectedItems.add(new ConfigurationItem());
        when(itemRepository.findByGroup_Id(groupId)).thenReturn(expectedItems);
        
        // Act
        List<ConfigurationItem> actualItems = itemService.getItemsByGroup(groupId);
        
        // Assert
        assertEquals(expectedItems, actualItems);
        verify(itemRepository, times(1)).findByGroup_Id(groupId);
    }

    @Test
    void getItemsByGroupAndEnvironmentShouldReturnItems() {
        // Arrange
        Long groupId = 1L;
        String environment = "dev";
        List<ConfigurationItem> expectedItems = new ArrayList<>();
        expectedItems.add(new ConfigurationItem());
        when(itemRepository.findByGroup_IdAndEnvironment(groupId, environment)).thenReturn(expectedItems);
        
        // Act
        Optional<List<ConfigurationItem>> actualItems = itemService.getItemsByGroupAndEnvironment(groupId, environment);
        
        // Assert
        assertTrue(actualItems.isPresent());
        assertEquals(expectedItems, actualItems.get());
        verify(itemRepository, times(1)).findByGroup_IdAndEnvironment(groupId, environment);
    }

    @Test
    void createItem_SavesAndReturnsItem() {
        // Arrange
        ConfigurationItem itemToCreate = new ConfigurationItem();
        itemToCreate.setKey("new.setting");
        itemToCreate.setValue("value");
        itemToCreate.setGroupId(groupId);
        itemToCreate.setEnvironment("DEV");
        String userId = "testUser";

        when(itemRepository.save(any(ConfigurationItem.class))).thenAnswer(invocation -> {
            ConfigurationItem savedItem = invocation.getArgument(0);
            savedItem.setId(3L); // Simulate ID generation
            return savedItem;
        });

        // Act
        Optional<ConfigurationItem> result = itemService.createItem(itemToCreate, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getKey()).isEqualTo("new.setting");
        verify(itemRepository, times(1)).save(any(ConfigurationItem.class));
        verify(auditService, times(1)).logItemCreation(any(ConfigurationItem.class), eq(userId));
    }

    @Test
    void updateItem_WhenItemExists_UpdatesAndReturnsItem() {
        // Arrange
        Long existingId = testItemId;
        ConfigurationItem updatedItem = new ConfigurationItem();
        updatedItem.setKey("app.timeout");
        updatedItem.setValue("60"); // Changed value
        updatedItem.setDescription("Updated timeout");
        updatedItem.setGroupId(groupId);
        updatedItem.setEnvironment("DEV");
        String userId = "testUser";

        when(itemRepository.findById(existingId)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(ConfigurationItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(existingId, updatedItem, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get().getId()).isEqualTo(existingId);
        assertThat(result.get().getValue()).isEqualTo("60");
        assertThat(result.get().getDescription()).isEqualTo("Updated timeout");
        verify(itemRepository, times(1)).findById(existingId);
        verify(itemRepository, times(1)).save(any(ConfigurationItem.class));
        verify(auditService, times(1)).logItemUpdate(any(ConfigurationItem.class), any(ConfigurationItem.class), eq(userId));
    }

    @Test
    void updateItem_WhenItemDoesNotExist_ReturnsNull() {
        // Arrange
        Long nonExistentId = 999L;
        ConfigurationItem updatedItem = new ConfigurationItem();
        updatedItem.setValue("new value");
        String userId = "testUser";

        when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<ConfigurationItem> result = itemService.updateItem(nonExistentId, updatedItem, userId);

        // Assert
        assertThat(result).isNull();
        verify(itemRepository, times(1)).findById(nonExistentId);
        verify(itemRepository, never()).save(any(ConfigurationItem.class));
        verify(auditService, never()).logItemUpdate(any(), any(), anyString());
    }

    @Test
    void deleteItem_WhenItemExists_DeletesSuccessfully() {
        // Arrange
        Long existingId = testItemId;
        String userId = "testUser";
        when(itemRepository.findById(existingId)).thenReturn(Optional.of(testItem));
        doNothing().when(itemRepository).delete(any(ConfigurationItem.class));

        // Act
        itemService.deleteItem(existingId, userId);

        // Assert
        verify(itemRepository, times(1)).findById(existingId);
        verify(itemRepository, times(1)).delete(testItem);
        verify(auditService, times(1)).logItemDeletion(any(ConfigurationItem.class), eq(userId));
    }

    @Test
    void deleteItem_WhenItemDoesNotExist_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        String userId = "testUser";
        when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.deleteItem(nonExistentId, userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not exist");
            
        verify(itemRepository, times(1)).findById(nonExistentId);
        verify(itemRepository, never()).delete(any(ConfigurationItem.class));
        verify(auditService, never()).logItemDeletion(any(), anyString());
    }
} 