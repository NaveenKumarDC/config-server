package com.example.configserver.config;

import com.example.configserver.dto.ConfigurationGroupDTO;
import com.example.configserver.dto.ConfigurationItemDTO;
import com.example.configserver.model.Environment;
import com.example.configserver.service.ConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ConfigurationService configurationService;

    @PostConstruct
    public void initialize() {
        log.info("Initializing sample configuration data...");
        
        try {
            // Create sample groups
            ConfigurationGroupDTO apiGroup = createGroup("api-service", "API Gateway Configuration");
            ConfigurationGroupDTO userGroup = createGroup("user-service", "User Management Service Configuration");
            ConfigurationGroupDTO paymentGroup = createGroup("payment-service", "Payment Processing Service Configuration");
            ConfigurationGroupDTO notificationGroup = createGroup("notification-service", "Notification Service Configuration");
            
            // Create sample items for DEV environment
            createItem("api.timeout", "30", Environment.DEV.name(), apiGroup.getId(), apiGroup.getName());
            createItem("api.max-connections", "100", Environment.DEV.name(), apiGroup.getId(), apiGroup.getName());
            createItem("user.session.timeout", "60", Environment.DEV.name(), userGroup.getId(), userGroup.getName());
            createItem("user.password.expiry", "90", Environment.DEV.name(), userGroup.getId(), userGroup.getName());
            createItem("payment.retry.count", "3", Environment.DEV.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("payment.gateway.url", "https://dev-payment-gateway.example.com", Environment.DEV.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("notification.email.from", "dev-noreply@example.com", Environment.DEV.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem("notification.sms.enabled", "true", Environment.DEV.name(), notificationGroup.getId(), notificationGroup.getName());
            
            // Create sample items for STAGE environment
            createItem("api.timeout", "20", Environment.STAGE.name(), apiGroup.getId(), apiGroup.getName());
            createItem("api.max-connections", "200", Environment.STAGE.name(), apiGroup.getId(), apiGroup.getName());
            createItem("user.session.timeout", "45", Environment.STAGE.name(), userGroup.getId(), userGroup.getName());
            createItem("user.password.expiry", "60", Environment.STAGE.name(), userGroup.getId(), userGroup.getName());
            createItem("payment.retry.count", "3", Environment.STAGE.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("payment.gateway.url", "https://stage-payment-gateway.example.com", Environment.STAGE.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("notification.email.from", "stage-noreply@example.com", Environment.STAGE.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem("notification.sms.enabled", "true", Environment.STAGE.name(), notificationGroup.getId(), notificationGroup.getName());
            
            // Create sample items for PROD environment
            createItem("api.timeout", "10", Environment.PROD.name(), apiGroup.getId(), apiGroup.getName());
            createItem("api.max-connections", "500", Environment.PROD.name(), apiGroup.getId(), apiGroup.getName());
            createItem("user.session.timeout", "30", Environment.PROD.name(), userGroup.getId(), userGroup.getName());
            createItem("user.password.expiry", "30", Environment.PROD.name(), userGroup.getId(), userGroup.getName());
            createItem("payment.retry.count", "5", Environment.PROD.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("payment.gateway.url", "https://payment-gateway.example.com", Environment.PROD.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem("notification.email.from", "noreply@example.com", Environment.PROD.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem("notification.sms.enabled", "true", Environment.PROD.name(), notificationGroup.getId(), notificationGroup.getName());
            
            log.info("Sample configuration data initialized successfully!");
        } catch (Exception e) {
            log.error("Error initializing sample data: {}", e.getMessage(), e);
        }
    }
    
    private ConfigurationGroupDTO createGroup(String name, String description) {
        ConfigurationGroupDTO groupDTO = new ConfigurationGroupDTO(null, name, description);
        return configurationService.createGroup(groupDTO);
    }
    
    private ConfigurationItemDTO createItem(String key, String value, String environment, Long groupId, String groupName) {
        ConfigurationItemDTO itemDTO = new ConfigurationItemDTO(null, key, value, environment, groupId, groupName);
        return configurationService.createItem(itemDTO);
    }
} 