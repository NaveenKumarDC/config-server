package com.example.configserver.service;

import com.example.configserver.dto.PasswordResetRequest;
import com.example.configserver.dto.UserCreationRequest;
import com.example.configserver.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
  User createUser(UserCreationRequest request);

  Optional<User> updateUser(Long id, UserCreationRequest request);

  void generatePasswordResetToken(String email);

  boolean validatePasswordResetToken(String token);

  void resetPassword(String token, PasswordResetRequest request);

  List<User> getAllUsers();

  Optional<User> getUserById(long l);

  boolean deleteUser(Long id);
} 