package com.example.configserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuration_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"key", "environment", "group_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String key;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
    
    @Column(nullable = false)
    private String environment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ConfigurationGroup group;
} 