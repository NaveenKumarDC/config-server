package com.example.configserver.dto;

import com.example.configserver.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String token;
    private String username;
    private Role role;
    private boolean success;
    private String message;
    
    public boolean isSuccess() {
        return success;
    }
} 