package com.example.configserver.controller;

import com.example.configserver.dto.LoginRequest;
import com.example.configserver.dto.LoginResponse;
import com.example.configserver.model.Role;
import com.example.configserver.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin", "password");

        // Create a mock response with proper constructor or factory method if setId is not available
        LoginResponse loginResponse = createLoginResponse(1L, "admin", Role.ADMIN, "jwt-token-here");

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("invalid", "invalid");

        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void login_WithEmptyCredentials_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        // Empty username and password

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }
    
    // Helper method to create a LoginResponse based on the updated class structure
    private LoginResponse createLoginResponse(Long id, String username, Role role, String token) {
        // If LoginResponse has a constructor with these parameters, use it directly
        // Otherwise we'll use reflection or another approach that works with the actual implementation
        return new LoginResponse(id, token,  username, role, true, null);
    }
}