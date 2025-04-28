package com.example.configserver.controller;

import com.example.configserver.dto.PasswordResetRequest;
import com.example.configserver.dto.UserCreationRequest;
import com.example.configserver.model.User;
import com.example.configserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for managing users")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new user and send a welcome email")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserCreationRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "message", "User created successfully. A welcome email has been sent with password setup instructions."
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update an existing user (Admin only)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreationRequest request) {
        log.debug("Updating user with ID: {}", id);
        Optional<User> updatedUser = userService.updateUser(id, request);
        
        if (updatedUser.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "username", updatedUser.get().getUsername(),
                "email", updatedUser.get().getEmail(),
                "message", "User updated successfully"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Generate a password reset token and send it via email")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(Map.of("message", "Password reset email sent successfully"));
        } catch (Exception e) {
            // Don't expose whether the email exists or not for security reasons
            return ResponseEntity.ok(Map.of("message", "If the email exists, a password reset link will be sent"));
        }
    }
    
    @GetMapping("/validate-token")
    @Operation(summary = "Validate a password reset token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        boolean isValid = userService.validatePasswordResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            userService.resetPassword(token, request);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a user (Admin only)")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.debug("Deleting user with ID: {}", id);
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 