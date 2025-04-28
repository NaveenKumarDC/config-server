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
            
            // Using constants for configuration keys and descriptions for better maintainability
            final String API_TIMEOUT_KEY = "api.timeout";
            final String API_TIMEOUT_DESC = "Connection timeout in seconds";
            final String API_TIMEOUT_VALUE_DEV = "30";
            final String USER_PASSWORD_EXPIRY_KEY = "user.password.expiry";
            final String USER_PASSWORD_EXPIRY_DESC = "Password expiry in days";
            final String API_MAX_CONNECTIONS_KEY = "api.max-connections";
            final String API_MAX_CONNECTIONS_DESC = "Maximum number of concurrent connections";
            final String USER_SESSION_TIMEOUT_KEY = "user.session.timeout";
            final String USER_SESSION_TIMEOUT_DESC = "User session timeout in minutes";
            final String PAYMENT_RETRY_COUNT_KEY = "payment.retry.count";
            final String PAYMENT_RETRY_COUNT_DESC = "Number of payment retry attempts";
            final String PAYMENT_GATEWAY_URL_KEY = "payment.gateway.url";
            final String PAYMENT_GATEWAY_URL_DESC = "Payment gateway URL";
            final String NOTIFICATION_EMAIL_FROM_KEY = "notification.email.from";
            final String NOTIFICATION_EMAIL_FROM_DESC = "From email address for notifications";
            final String NOTIFICATION_SMS_ENABLED_KEY = "notification.sms.enabled";
            final String NOTIFICATION_SMS_ENABLED_DESC = "Flag to enable SMS notifications";
            
            // Create sample items for DEV environment
            createItem(API_TIMEOUT_KEY, API_TIMEOUT_VALUE_DEV, API_TIMEOUT_DESC, Environment.DEV.name(), apiGroup.getId(), apiGroup.getName());
            createItem(API_MAX_CONNECTIONS_KEY, "100", API_MAX_CONNECTIONS_DESC, Environment.DEV.name(), apiGroup.getId(), apiGroup.getName());
            createItem(USER_SESSION_TIMEOUT_KEY, "60", USER_SESSION_TIMEOUT_DESC, Environment.DEV.name(), userGroup.getId(), userGroup.getName());
            createItem(USER_PASSWORD_EXPIRY_KEY, "90", USER_PASSWORD_EXPIRY_DESC, Environment.DEV.name(), userGroup.getId(), userGroup.getName());
            createItem(PAYMENT_RETRY_COUNT_KEY, "3", PAYMENT_RETRY_COUNT_DESC, Environment.DEV.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(PAYMENT_GATEWAY_URL_KEY, "https://dev-payment-gateway.example.com", PAYMENT_GATEWAY_URL_DESC, Environment.DEV.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(NOTIFICATION_EMAIL_FROM_KEY, "dev-noreply@example.com", NOTIFICATION_EMAIL_FROM_DESC, Environment.DEV.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem(NOTIFICATION_SMS_ENABLED_KEY, "true", NOTIFICATION_SMS_ENABLED_DESC, Environment.DEV.name(), notificationGroup.getId(), notificationGroup.getName());
            
            // Create sample items for STAGE environment
            createItem(API_TIMEOUT_KEY, "20", API_TIMEOUT_DESC, Environment.STAGE.name(), apiGroup.getId(), apiGroup.getName());
            createItem(API_MAX_CONNECTIONS_KEY, "200", API_MAX_CONNECTIONS_DESC, Environment.STAGE.name(), apiGroup.getId(), apiGroup.getName());
            createItem(USER_SESSION_TIMEOUT_KEY, "45", USER_SESSION_TIMEOUT_DESC, Environment.STAGE.name(), userGroup.getId(), userGroup.getName());
            createItem(USER_PASSWORD_EXPIRY_KEY, "60", USER_PASSWORD_EXPIRY_DESC, Environment.STAGE.name(), userGroup.getId(), userGroup.getName());
            createItem(PAYMENT_RETRY_COUNT_KEY, "3", PAYMENT_RETRY_COUNT_DESC, Environment.STAGE.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(PAYMENT_GATEWAY_URL_KEY, "https://stage-payment-gateway.example.com", PAYMENT_GATEWAY_URL_DESC, Environment.STAGE.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(NOTIFICATION_EMAIL_FROM_KEY, "stage-noreply@example.com", NOTIFICATION_EMAIL_FROM_DESC, Environment.STAGE.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem(NOTIFICATION_SMS_ENABLED_KEY, "true", NOTIFICATION_SMS_ENABLED_DESC, Environment.STAGE.name(), notificationGroup.getId(), notificationGroup.getName());
            
            // Create sample items for PROD environment
            createItem(API_TIMEOUT_KEY, "10", API_TIMEOUT_DESC, Environment.PROD.name(), apiGroup.getId(), apiGroup.getName());
            createItem(API_MAX_CONNECTIONS_KEY, "500", API_MAX_CONNECTIONS_DESC, Environment.PROD.name(), apiGroup.getId(), apiGroup.getName());
            createItem(USER_SESSION_TIMEOUT_KEY, "30", USER_SESSION_TIMEOUT_DESC, Environment.PROD.name(), userGroup.getId(), userGroup.getName());
            createItem(USER_PASSWORD_EXPIRY_KEY, "30", USER_PASSWORD_EXPIRY_DESC, Environment.PROD.name(), userGroup.getId(), userGroup.getName());
            createItem(PAYMENT_RETRY_COUNT_KEY, "5", PAYMENT_RETRY_COUNT_DESC, Environment.PROD.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(PAYMENT_GATEWAY_URL_KEY, "https://payment-gateway.example.com", PAYMENT_GATEWAY_URL_DESC, Environment.PROD.name(), paymentGroup.getId(), paymentGroup.getName());
            createItem(NOTIFICATION_EMAIL_FROM_KEY, "noreply@example.com", NOTIFICATION_EMAIL_FROM_DESC, Environment.PROD.name(), notificationGroup.getId(), notificationGroup.getName());
            createItem(NOTIFICATION_SMS_ENABLED_KEY, "true", NOTIFICATION_SMS_ENABLED_DESC, Environment.PROD.name(), notificationGroup.getId(), notificationGroup.getName());
            
            log.info("Sample configuration data initialized successfully!");
        } catch (Exception e) {
            log.error("Error initializing sample data: {}", e.getMessage(), e);
        }
    }
    
    private ConfigurationGroupDTO createGroup(String name, String description) {
        ConfigurationGroupDTO groupDTO = new ConfigurationGroupDTO(null, name, description);
        return configurationService.createGroup(groupDTO);
    }
    
    private ConfigurationItemDTO createItem(String key, String value, String description, String environment, Long groupId, String groupName) {
        ConfigurationItemDTO itemDTO = new ConfigurationItemDTO(null, key, value, description, environment, groupId, groupName);
        return configurationService.createItem(itemDTO);
    }
} 