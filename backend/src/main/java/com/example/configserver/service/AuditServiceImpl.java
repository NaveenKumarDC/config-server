package com.example.configserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.configserver.model.ConfigurationItem;

@Service
public class AuditServiceImpl implements AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    
    @Override
    public void logItemCreation(ConfigurationItem item, String userId) {
        logger.info("AUDIT: User {} created item {} with key={}, env={}, groupId={}",
                userId, item.getId(), item.getKey(), item.getEnvironment(), item.getGroupId());
    }
    
    @Override
    public void logItemUpdate(ConfigurationItem oldItem, ConfigurationItem newItem, String userId) {
        logger.info("AUDIT: User {} updated item {} from [key={}, value={}, env={}] to [key={}, value={}, env={}]",
                userId, newItem.getId(), 
                oldItem.getKey(), oldItem.getValue(), oldItem.getEnvironment(),
                newItem.getKey(), newItem.getValue(), newItem.getEnvironment());
    }
    
    @Override
    public void logItemDeletion(ConfigurationItem item, String userId) {
        logger.info("AUDIT: User {} deleted item {} with key={}, env={}, groupId={}",
                userId, item.getId(), item.getKey(), item.getEnvironment(), item.getGroupId());
    }
} 