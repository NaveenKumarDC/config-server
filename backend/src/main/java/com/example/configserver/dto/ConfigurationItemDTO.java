package com.example.configserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationItemDTO {
    private Long id;
    private String key;
    private String value;
    private String description;
    private String environment;
    private Long groupId;
    private String groupName;
} 