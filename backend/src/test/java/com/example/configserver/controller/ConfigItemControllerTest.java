package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationItem;
import com.example.configserver.service.ConfigItemService;
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

@WebMvcTest(ConfigItemController.class)
public class ConfigItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConfigurationItem testItem;
    private List<ConfigurationItem> testItems;
    private Long groupId;

    @BeforeEach
    void setUp() {
        groupId = 1L;

        testItem = new ConfigurationItem();
        testItem.setId(1L);
        testItem.setKey("auth.timeout");
        testItem.setValue("30");
        testItem.setDescription("Authentication timeout in seconds");
        testItem.setGroupId(groupId);
        testItem.setEnvironment("DEV");

        ConfigurationItem testItem2 = new ConfigurationItem();
        testItem2.setId(2L);
        testItem2.setKey("auth.retries");
        testItem2.setValue("3");
        testItem2.setDescription("Authentication retry count");
        testItem2.setGroupId(groupId);
        testItem2.setEnvironment("PROD");

        testItems = Arrays.asList(testItem, testItem2);
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser")
    void getAllItems_ShouldReturnAllItems() throws Exception {
        when(itemService.getAllItems()).thenReturn(testItems);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].key", is("auth.timeout")))
                .andExpect(jsonPath("$[1].key", is("auth.retries")));

        verify(itemService, times(1)).getAllItems();
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser")
    void getItemById_WhenItemExists_ShouldReturnItem() throws Exception {
        when(itemService.getItemById(testItem.getId())).thenReturn(Optional.of(testItem));

        mockMvc.perform(get("/api/items/{id}", testItem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testItem.getId().intValue())))
                .andExpect(jsonPath("$.key", is("auth.timeout")))
                .andExpect(jsonPath("$.value", is("30")))
                .andExpect(jsonPath("$.environment", is("DEV")));

        verify(itemService, times(1)).getItemById(testItem.getId());
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser")
    void getItemById_WhenItemDoesNotExist_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(itemService.getItemById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemById(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser")
    void getItemsByGroup_ShouldReturnItemsForGroup() throws Exception {
        when(itemService.getItemsByGroup(groupId)).thenReturn(testItems);

        mockMvc.perform(get("/api/items/byGroup/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].groupId", is(groupId.intValue())))
                .andExpect(jsonPath("$[1].groupId", is(groupId.intValue())));

        verify(itemService, times(1)).getItemsByGroup(groupId);
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser")
    void getItemsByGroupAndEnvironment_ShouldReturnFilteredItems() throws Exception {
        when(itemService.getItemsByGroupAndEnvironment(groupId, "DEV"))
                .thenReturn(Optional.of(Arrays.asList(testItem)));

        mockMvc.perform(get("/api/items/byGroupAndEnv/{groupId}/{env}", groupId, "DEV"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].key", is("auth.timeout")))
                .andExpect(jsonPath("$[0].environment", is("DEV")));

        verify(itemService, times(1)).getItemsByGroupAndEnvironment(groupId, "DEV");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void createItem_WithValidData_ShouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(ConfigurationItem.class), eq("admin"))).thenReturn(Optional.ofNullable(testItem));

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key", is("auth.timeout")))
                .andExpect(jsonPath("$.value", is("30")));

        verify(itemService, times(1)).createItem(any(ConfigurationItem.class), eq("admin"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser") // Non-admin user
    void createItem_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isForbidden());

        verify(itemService, never()).createItem(any(ConfigurationItem.class), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void updateItem_WithValidData_ShouldReturnUpdatedItem() throws Exception {
        ConfigurationItem updatedItem = new ConfigurationItem();
        updatedItem.setId(testItem.getId());
        updatedItem.setKey("auth.timeout");
        updatedItem.setValue("60"); // Updated value
        updatedItem.setDescription("Updated authentication timeout");
        updatedItem.setGroupId(groupId);
        updatedItem.setEnvironment("DEV");

        when(itemService.updateItem(eq(testItem.getId()), any(ConfigurationItem.class), eq("admin")))
                .thenReturn(Optional.of(updatedItem));

        mockMvc.perform(put("/api/items/{id}", testItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value", is("60")))
                .andExpect(jsonPath("$.description", is("Updated authentication timeout")));

        verify(itemService, times(1)).updateItem(eq(testItem.getId()), any(ConfigurationItem.class), eq("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void updateItem_WhenItemDoesNotExist_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(itemService.updateItem(eq(nonExistentId), any(ConfigurationItem.class), eq("admin")))
                .thenReturn(null);

        mockMvc.perform(put("/api/items/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(eq(nonExistentId), any(ConfigurationItem.class), eq("admin"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser") // Non-admin user
    void updateItem_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/items/{id}", testItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isForbidden());

        verify(itemService, never()).updateItem(any(Long.class), any(ConfigurationItem.class), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void deleteItem_WhenItemExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(itemService).deleteItem(testItem.getId(), "admin");

        mockMvc.perform(delete("/api/items/{id}", testItem.getId()))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).deleteItem(testItem.getId(), "admin");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void deleteItem_WhenItemDoesNotExist_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 999L;
        doThrow(new RuntimeException("Item not found")).when(itemService).deleteItem(nonExistentId, "admin");

        mockMvc.perform(delete("/api/items/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).deleteItem(nonExistentId, "admin");
    }

    @Test
    @WithMockUser(roles = "USER", username = "testuser") // Non-admin user
    void deleteItem_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", testItem.getId()))
                .andExpect(status().isForbidden());

        verify(itemService, never()).deleteItem(any(Long.class), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin")
    void createItem_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create an invalid item with no key
        ConfigurationItem invalidItem = new ConfigurationItem();
        invalidItem.setValue("some-value");
        invalidItem.setGroupId(groupId);

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(any(ConfigurationItem.class), anyString());
    }

    @Test
    void accessItemEndpoint_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());

        verify(itemService, never()).getAllItems();
    }
}