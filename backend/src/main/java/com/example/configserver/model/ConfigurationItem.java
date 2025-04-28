package com.example.configserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuration_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"key", "environment", "group_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfigurationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String key;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String environment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private ConfigurationGroup group;

    @Transient
    private Long groupId;

    // Helper method to get groupId from group if available
    public Long getGroupId() {
        if (groupId != null) {
            return groupId;
        }
        return group != null ? group.getId() : null;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    // Helper method to set the environment as an enum
    public void setEnvironment(Environment environment) {
        this.environment = environment.name();
    }
    
    // Helper method to set the environment directly as a string
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
} 