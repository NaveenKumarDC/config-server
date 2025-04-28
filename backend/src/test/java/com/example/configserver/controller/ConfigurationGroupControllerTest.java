package com.example.configserver.controller;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.service.ConfigurationGroupService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationGroupController.class)
public class ConfigurationGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigurationGroupService groupService;

    private ConfigurationGroup testGroup;
    private ConfigurationGroup testGroupDTO;

    @BeforeEach
    void setUp() {
        // Setup test group
        testGroup = new ConfigurationGroup();
        testGroup.setId(1L);
        testGroup.setName("test-group");
        testGroup.setDescription("Test Group Description");

        // Setup test group DTO
        testGroupDTO = new ConfigurationGroup();
        testGroupDTO.setId(1L);
        testGroupDTO.setName("test-group");
        testGroupDTO.setDescription("Test Group Description");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllGroups_ShouldReturnAllGroups() throws Exception {
        // Arrange
        List<ConfigurationGroup> groups = Arrays.asList(testGroup);
        when(groupService.getAllGroups()).thenReturn(groups);

        // Act & Assert
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("test-group")))
                .andExpect(jsonPath("$[0].description", is("Test Group Description")));

        verify(groupService).getAllGroups();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getGroupById_WithExistingId_ShouldReturnGroup() throws Exception {
        // Arrange
        when(groupService.getGroupById(1L)).thenReturn(Optional.of(testGroup));

        // Act & Assert
        mockMvc.perform(get("/api/groups/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test-group")))
                .andExpect(jsonPath("$.description", is("Test Group Description")));

        verify(groupService).getGroupById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getGroupById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(groupService.getGroupById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/groups/999"))
                .andExpect(status().isNotFound());

        verify(groupService).getGroupById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getGroupByName_WithExistingName_ShouldReturnGroup() throws Exception {
        // Arrange
        when(groupService.getGroupByName("test-group")).thenReturn(Optional.of(testGroup));

        // Act & Assert
        mockMvc.perform(get("/api/groups/name/test-group"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test-group")))
                .andExpect(jsonPath("$.description", is("Test Group Description")));

        verify(groupService).getGroupByName("test-group");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getGroupByName_WithNonExistingName_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(groupService.getGroupByName("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/groups/name/non-existent"))
                .andExpect(status().isNotFound());

        verify(groupService).getGroupByName("non-existent");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_WithValidData_ShouldReturnCreatedGroup() throws Exception {
        // Arrange
        when(groupService.createGroup(any(ConfigurationGroup.class))).thenReturn(testGroup);

        // Act & Assert
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroupDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test-group")))
                .andExpect(jsonPath("$.description", is("Test Group Description")));

        verify(groupService).createGroup(any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WithExistingIdAndValidData_ShouldReturnUpdatedGroup() throws Exception {
        // Arrange
        when(groupService.updateGroup(eq(1L), any(ConfigurationGroup.class))).thenReturn(Optional.of(testGroup));

        // Act & Assert
        mockMvc.perform(put("/api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroupDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test-group")))
                .andExpect(jsonPath("$.description", is("Test Group Description")));

        verify(groupService).updateGroup(eq(1L), any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(groupService.updateGroup(eq(999L), any(ConfigurationGroup.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/groups/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroupDTO)))
                .andExpect(status().isNotFound());

        verify(groupService).updateGroup(eq(999L), any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(groupService.deleteGroup(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/groups/1"))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(groupService.deleteGroup(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/groups/999"))
                .andExpect(status().isNotFound());

        verify(groupService).deleteGroup(999L);
    }

    @Test
    @WithMockUser(roles = "USER")  // Non-admin user
    void createGroup_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroupDTO)))
                .andExpect(status().isForbidden());

        verify(groupService, never()).createGroup(any(ConfigurationGroup.class));
    }
} 