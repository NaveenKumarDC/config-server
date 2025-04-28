package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.model.Environment;
import com.example.configserver.service.ConfigurationItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationItemController.class)
public class ConfigurationItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigurationItemService itemService;

    private ConfigurationGroup testGroup;
    private ConfigurationItem testItem;
    private ConfigurationItem testItemDTO;

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
        testItem.setEnvironment(Environment.DEV);

        // Setup test item DTO
        testItemDTO = new ConfigurationItem();
        testItemDTO.setId(1L);
        testItemDTO.setKey("test.key");
        testItemDTO.setValue("test-value");
        testItemDTO.setDescription("Test Item Description");
        testItemDTO.setGroupId(1L);
        testItemDTO.setEnvironment("DEV");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllItems_ShouldReturnAllItems() throws Exception {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        when(itemService.getAllItems()).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].key", is("test.key")))
                .andExpect(jsonPath("$[0].value", is("test-value")))
                .andExpect(jsonPath("$[0].group.id", is(1)))
                .andExpect(jsonPath("$[0].environment", is("DEV")));

        verify(itemService).getAllItems();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getItemById_WithExistingId_ShouldReturnItem() throws Exception {
        // Arrange
        when(itemService.getItemById(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("test.key")))
                .andExpect(jsonPath("$.value", is("test-value")))
                .andExpect(jsonPath("$.group.id", is(1)))
                .andExpect(jsonPath("$.environment", is("DEV")));

        verify(itemService).getItemById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getItemById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.getItemById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());

        verify(itemService).getItemById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getItemsByGroup_WithExistingGroupId_ShouldReturnItems() throws Exception {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        when(itemService.getItemsByGroup(1L)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/items/group/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].key", is("test.key")))
                .andExpect(jsonPath("$[0].group.id", is(1)));

        verify(itemService).getItemsByGroup(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getItemsByGroupAndEnvironment_WithExistingParams_ShouldReturnItems() throws Exception {
        // Arrange
        List<ConfigurationItem> items = Arrays.asList(testItem);
        when(itemService.getItemsByGroupAndEnvironment(1L, String.valueOf(Environment.DEV))).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/items/group/1/environment/DEV"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].key", is("test.key")))
                .andExpect(jsonPath("$[0].group.id", is(1)))
                .andExpect(jsonPath("$[0].environment", is("DEV")));

        verify(itemService).getItemsByGroupAndEnvironment(1L, String.valueOf(Environment.DEV));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createItem_WithValidData_ShouldReturnCreatedItem() throws Exception {
        // Arrange
        when(itemService.createItem(any(ConfigurationItem.class))).thenReturn(testItem);

        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("test.key")))
                .andExpect(jsonPath("$.value", is("test-value")))
                .andExpect(jsonPath("$.group.id", is(1)))
                .andExpect(jsonPath("$.environment", is("DEV")));

        verify(itemService).createItem(any(ConfigurationItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateItem_WithExistingIdAndValidData_ShouldReturnUpdatedItem() throws Exception {
        // Arrange
        when(itemService.updateItem(eq(1L), any(ConfigurationItem.class))).thenReturn(Optional.of(testItem));

        // Act & Assert
        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("test.key")))
                .andExpect(jsonPath("$.value", is("test-value")))
                .andExpect(jsonPath("$.group.id", is(1)))
                .andExpect(jsonPath("$.environment", is("DEV")));

        verify(itemService).updateItem(eq(1L), any(ConfigurationItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.updateItem(eq(999L), any(ConfigurationItem.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/items/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDTO)))
                .andExpect(status().isNotFound());

        verify(itemService).updateItem(eq(999L), any(ConfigurationItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItem_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(itemService.deleteItem(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.deleteItem(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/items/999"))
                .andExpect(status().isNotFound());

        verify(itemService).deleteItem(999L);
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void createItem_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDTO)))
                .andExpect(status().isForbidden());

        verify(itemService, never()).createItem(any(ConfigurationItem.class));
    }
} 