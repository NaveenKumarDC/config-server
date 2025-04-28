package com.example.configserver.service;

import com.example.configserver.dto.LoginRequest;
import com.example.configserver.dto.LoginResponse;
import com.example.configserver.model.Role;
import com.example.configserver.model.User;
import com.example.configserver.repository.UserRepository;
import com.example.configserver.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow();
            
            // Update last login timestamp
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            var jwtToken = jwtService.generateToken(
                    org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .authorities(user.getRole().name())
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(!user.isEnabled())
                            .build()
            );
            
            return new LoginResponse(
                    user.getId(),
                    jwtToken,
                    user.getUsername(),
                    user.getRole(),
                    true,
                    "Login successful"
            );
        } catch (AuthenticationException e) {
            return new LoginResponse(
                    null,
                    null,
                    request.getUsername(),
                    null,
                    false,
                    "Invalid username or password"
            );
        }
    }

    public User createAdminUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        user.setEmail(email);
        
        return userRepository.save(user);
    }

    public User createReadOnlyUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.READ_ONLY);
        user.setEnabled(true);
        user.setEmail(email);
        
        return userRepository.save(user);
    }
} 