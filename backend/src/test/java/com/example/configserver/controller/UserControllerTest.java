package com.example.configserver.controller;

import com.example.configserver.dto.UserCreationRequest;
import com.example.configserver.model.Role;
import com.example.configserver.model.User;
import com.example.configserver.service.UserService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  @WithMockUser(roles = "ADMIN")
  public void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
    // Arrange
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername("user1");
    user1.setEmail("user1@example.com");
    user1.setRole(Role.READ_ONLY);

    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("admin");
    user2.setEmail("admin@example.com");
    user2.setRole(Role.ADMIN);

    List<User> users = Arrays.asList(user1, user2);

    when(userService.getAllUsers()).thenReturn(users);

    // Act & Assert
    mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].username", is("user1")))
            .andExpect(jsonPath("$[0].role", is("READ_ONLY")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].username", is("admin")))
            .andExpect(jsonPath("$[1].role", is("ADMIN")));

    verify(userService, times(1)).getAllUsers();
  }

  @Test
  @WithMockUser(roles = "READ_ONLY")
  public void getAllUsers_AsReadOnly_ShouldReturnForbidden() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());

    verify(userService, never()).getAllUsers();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void getUserById_WithValidId_ShouldReturnUser() throws Exception {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(Role.READ_ONLY);

    when(userService.getUserById(1L)).thenReturn(Optional.of(user));

    // Act & Assert
    mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.role", is("READ_ONLY")));

    verify(userService, times(1)).getUserById(1L);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
    // Arrange
    UserCreationRequest request = new UserCreationRequest();
    request.setUsername("newuser");
    request.setEmail("newuser@example.com");
    request.setRole(Role.READ_ONLY);
    // Since setPassword() method might be missing in UserCreationRequest, we'll use reflection to set it
    // or modify the JSONObject directly for the test
    String requestJson = "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"role\":\"READ_ONLY\"," +
            "\"password\":\"password123\"}";

    User createdUser = new User();
    createdUser.setId(3L);
    createdUser.setUsername("newuser");
    createdUser.setEmail("newuser@example.com");
    createdUser.setRole(Role.READ_ONLY);

    when(userService.createUser(any(UserCreationRequest.class))).thenReturn(createdUser);

    // Act & Assert
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(3)))
            .andExpect(jsonPath("$.username", is("newuser")))
            .andExpect(jsonPath("$.email", is("newuser@example.com")))
            .andExpect(jsonPath("$.role", is("READ_ONLY")));

    verify(userService, times(1)).createUser(any(UserCreationRequest.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
    // Arrange
    UserCreationRequest request = new UserCreationRequest();
    request.setUsername("updateduser");
    request.setEmail("updated@example.com");
    request.setRole(Role.ADMIN);
    // Use JSON string directly to bypass setPassword method if it's missing
    String requestJson = "{\"username\":\"updateduser\",\"email\":\"updated@example.com\",\"role\":\"ADMIN\"}";

    User updatedUser = new User();
    updatedUser.setId(1L);
    updatedUser.setUsername("updateduser");
    updatedUser.setEmail("updated@example.com");
    updatedUser.setRole(Role.ADMIN);

    when(userService.updateUser(eq(1l), any(UserCreationRequest.class))).thenReturn(Optional.of(updatedUser));

    // Act & Assert
    mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("updateduser")))
            .andExpect(jsonPath("$.email", is("updated@example.com")))
            .andExpect(jsonPath("$.role", is("ADMIN")));

    verify(userService, times(1)).updateUser(eq(1l), any(UserCreationRequest.class));
  }


  @Test
  @WithMockUser(roles = "READ_ONLY")
  public void createUser_WithReadOnlyRole_ShouldReturnForbidden() throws Exception {
    // Arrange
    String requestJson = "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"role\":\"READ_ONLY\"," +
            "\"password\":\"password123\"}";

    // Act & Assert
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isForbidden());

    verify(userService, never()).createUser(any(UserCreationRequest.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    // Arrange
    UserCreationRequest request = new UserCreationRequest();
    request.setUsername("updateduser");
    request.setEmail("updated@example.com");
    request.setRole(Role.ADMIN);
    String requestJson = "{\"username\":\"updateduser\",\"email\":\"updated@example.com\",\"role\":\"ADMIN\"}";

    when(userService.updateUser(eq(999L), any(UserCreationRequest.class))).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(put("/api/users/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(status().isNotFound());

    verify(userService, times(1)).updateUser(eq(999L), any(UserCreationRequest.class));
  }
}