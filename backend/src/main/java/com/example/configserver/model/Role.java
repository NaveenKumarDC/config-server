package com.example.configserver.model;

public enum Role {
    ADMIN,    // Can view, add, edit, and delete configurations
    EDITOR,
    READ_ONLY // Can only view configurations
} 