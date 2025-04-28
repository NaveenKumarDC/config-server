package com.example.configserver.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);
    void sendWelcomeEmail(String email, String username, String resetLink);
} 