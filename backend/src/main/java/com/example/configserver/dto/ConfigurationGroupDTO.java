package com.example.configserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfigurationGroupDTO {
    private Long id;
    private String name;
    private String description;
} 