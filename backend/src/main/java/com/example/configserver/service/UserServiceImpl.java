package com.example.configserver.service;

import com.example.configserver.dto.PasswordResetRequest;
import com.example.configserver.dto.UserCreationRequest;
import com.example.configserver.model.PasswordResetToken;
import com.example.configserver.model.User;
import com.example.configserver.repository.PasswordResetTokenRepository;
import com.example.configserver.repository.UserRepository;
import com.example.configserver.service.EmailService;
import com.example.configserver.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.url}")
    private String appUrl;
    
    @Value("${app.password-reset-expiry}")
    private int passwordResetExpiryHours;

    @Override
    @Transactional
    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }
        
        // Create the user with a temporary password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Set a random password that will be reset immediately
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(request.getRole());
        user.setEnabled(true);
        user = userRepository.save(user);
        
        // Generate a password reset token
        String token = generateTokenForUser(user);
        
        // Send welcome email with password setup link
        String resetLink = appUrl + "/set-password?token=" + token;
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), resetLink);
        
        return user;
    }

    @Override
    public Optional<User> updateUser(Long id, UserCreationRequest request) {

        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("Username no found exists");
        }

        Optional<User> user = userRepository.findById(id);
        if(user.isPresent()) {
            // Update the user with a temporary password
            user.get().setEmail(request.getEmail());
            user.get().setRole(request.getRole());
            user.get().setEnabled(true);
            User savedUser = userRepository.save(user.get());
            return Optional.ofNullable(savedUser);

        }
        return null;
    }

    @Override
    @Transactional
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        
        String token = generateTokenForUser(user);
        
        String resetLink = appUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isExpired())
                .orElse(false);
    }

    @Override
    @Transactional
    public void resetPassword(String token, PasswordResetRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid token"));
        
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalStateException("Token has expired");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        tokenRepository.delete(resetToken);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(long l) {
        Optional<User> user =  userRepository.findById(l);
        return user;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id).get();
            // Delete any associated password reset tokens
            tokenRepository.deleteByUser(user);
            // Delete the user
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generateTokenForUser(User user) {
        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);
        
        // Create a new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(passwordResetExpiryHours));
        
        tokenRepository.save(resetToken);
        
        return token;
    }
} 