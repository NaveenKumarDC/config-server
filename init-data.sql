-- Sample Configuration Groups
INSERT INTO configuration_groups (id, name, description) VALUES 
(1, 'api-service', 'API Gateway Configuration'),
(2, 'user-service', 'User Management Service Configuration'),
(3, 'payment-service', 'Payment Processing Service Configuration'),
(4, 'notification-service', 'Notification Service Configuration');

-- Sample Configuration Items for DEV environment
INSERT INTO configuration_items (id, key, value, environment, group_id) VALUES 
(1, 'api.timeout', '30', 'DEV', 1),
(2, 'api.max-connections', '100', 'DEV', 1),
(3, 'user.session.timeout', '60', 'DEV', 2),
(4, 'user.password.expiry', '90', 'DEV', 2),
(5, 'payment.retry.count', '3', 'DEV', 3),
(6, 'payment.gateway.url', 'https://dev-payment-gateway.example.com', 'DEV', 3),
(7, 'notification.email.from', 'dev-noreply@example.com', 'DEV', 4),
(8, 'notification.sms.enabled', 'true', 'DEV', 4);

-- Sample Configuration Items for STAGE environment
INSERT INTO configuration_items (id, key, value, environment, group_id) VALUES 
(9, 'api.timeout', '20', 'STAGE', 1),
(10, 'api.max-connections', '200', 'STAGE', 1),
(11, 'user.session.timeout', '45', 'STAGE', 2),
(12, 'user.password.expiry', '60', 'STAGE', 2),
(13, 'payment.retry.count', '3', 'STAGE', 3),
(14, 'payment.gateway.url', 'https://stage-payment-gateway.example.com', 'STAGE', 3),
(15, 'notification.email.from', 'stage-noreply@example.com', 'STAGE', 4),
(16, 'notification.sms.enabled', 'true', 'STAGE', 4);

-- Sample Configuration Items for PROD environment
INSERT INTO configuration_items (id, key, value, environment, group_id) VALUES 
(17, 'api.timeout', '10', 'PROD', 1),
(18, 'api.max-connections', '500', 'PROD', 1),
(19, 'user.session.timeout', '30', 'PROD', 2),
(20, 'user.password.expiry', '30', 'PROD', 2),
(21, 'payment.retry.count', '5', 'PROD', 3),
(22, 'payment.gateway.url', 'https://payment-gateway.example.com', 'PROD', 3),
(23, 'notification.email.from', 'noreply@example.com', 'PROD', 4),
(24, 'notification.sms.enabled', 'true', 'PROD', 4); 