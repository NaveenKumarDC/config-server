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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationGroupController.class)
public class ConfigGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationGroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConfigurationGroup testGroup;
    private List<ConfigurationGroup> testGroups;

    @BeforeEach
    void setUp() {
        testGroup = new ConfigurationGroup();
        testGroup.setName("auth-service");
        testGroup.setDescription("Authentication service configurations");

        ConfigurationGroup testGroup2 = new ConfigurationGroup();
        testGroup2.setName("user-service");
        testGroup2.setDescription("User management service configurations");

        testGroups = Arrays.asList(testGroup, testGroup2);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllGroups_ShouldReturnAllGroups() throws Exception {
        when(groupService.getAllGroups()).thenReturn(testGroups);

        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("auth-service")))
                .andExpect(jsonPath("$[1].name", is("user-service")));

        verify(groupService, times(1)).getAllGroups();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupById_WhenGroupExists_ShouldReturnGroup() throws Exception {
        when(groupService.getGroupById(testGroup.getId())).thenReturn(Optional.of(testGroup));

        mockMvc.perform(get("/api/groups/{id}", testGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testGroup.getId())))
                .andExpect(jsonPath("$.name", is("auth-service")))
                .andExpect(jsonPath("$.description", is("Authentication service configurations")));

        verify(groupService, times(1)).getGroupById(testGroup.getId());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupById_WhenGroupDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(groupService.getGroupById(1l)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/groups/{id}", eq(1829378l)))
                .andExpect(status().isNotFound());

        verify(groupService, times(1)).getGroupById(eq(1829378l));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupByName_WhenGroupExists_ShouldReturnGroup() throws Exception {
        when(groupService.getGroupByName("auth-service")).thenReturn(Optional.of(testGroup));

        mockMvc.perform(get("/api/groups/name/{name}", "auth-service"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testGroup.getId())))
                .andExpect(jsonPath("$.name", is("auth-service")));

        verify(groupService, times(1)).getGroupByName("auth-service");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupByName_WhenGroupDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(groupService.getGroupByName("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/groups/name/{name}", "non-existent"))
                .andExpect(status().isNotFound());

        verify(groupService, times(1)).getGroupByName("non-existent");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_WithValidData_ShouldReturnCreatedGroup() throws Exception {
        when(groupService.createGroup(any(ConfigurationGroup.class))).thenReturn(testGroup);

        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("auth-service")));

        verify(groupService, times(1)).createGroup(any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void createGroup_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isForbidden());

        verify(groupService, never()).createGroup(any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WithValidData_ShouldReturnUpdatedGroup() throws Exception {
        ConfigurationGroup updatedGroup = new ConfigurationGroup();
        updatedGroup.setId(testGroup.getId());
        updatedGroup.setName("auth-service-updated");
        updatedGroup.setDescription("Updated authentication service configurations");

        when(groupService.updateGroup(eq(1829378l), any(ConfigurationGroup.class))).thenReturn(Optional.of(updatedGroup));

        mockMvc.perform(put("/api/groups/{id}", testGroup.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("auth-service-updated")))
                .andExpect(jsonPath("$.description", is("Updated authentication service configurations")));

        verify(groupService, times(1)).updateGroup(eq(testGroup.getId()), any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WhenGroupDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(groupService.updateGroup(eq(900912l), any(ConfigurationGroup.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/groups/{id}", eq(900912l))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isNotFound());

        verify(groupService, times(1)).updateGroup(eq(eq(900912l)), any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void updateGroup_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/groups/{id}", testGroup.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isForbidden());

        verify(groupService, never()).updateGroup(anyLong(), any(ConfigurationGroup.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WhenGroupExists_ShouldReturnNoContent() throws Exception {
        when(groupService.deleteGroup(testGroup.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/groups/{id}", testGroup.getId()))
                .andExpect(status().isNoContent());

        verify(groupService, times(1)).deleteGroup(testGroup.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WhenGroupDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(groupService.deleteGroup(910912l)).thenReturn(false);

        mockMvc.perform(delete("/api/groups/{id}", eq(91912l)))
                .andExpect(status().isNotFound());

        verify(groupService, times(1)).deleteGroup(eq(910912l));
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void deleteGroup_WithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/groups/{id}", testGroup.getId()))
                .andExpect(status().isForbidden());

        verify(groupService, never()).deleteGroup(anyLong());
    }

    @Test
    void accessEndpoint_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized());

        verify(groupService, never()).getAllGroups();
    }
} 