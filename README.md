# Configuration Server

A centralized configuration management system for microservices, enabling dynamic configuration updates across multiple environments.

## 🔍 Overview

The Configuration Server provides a unified interface to manage configuration properties across different environments (DEV, STAGE, PROD) for various microservices. It supports hierarchical grouping of configuration items and tracks changes through audit logs.

## 🏗️ Architecture

### Core Domain Objects:
- **ConfigurationGroup**: Logical grouping (e.g., service name)
- **ConfigurationItem**: Actual config key-value pair (environment scoped)
- **AuditLog**: Track changes to configurations
- **Environment**: DEV, STAGE, PROD

### SOLID Principles Implementation:
- **Single Responsibility**: Each class has a distinct responsibility (ConfigService, AuditService)
- **Open/Closed**: Interfaces abstract away implementation details
- **Liskov Substitution**: All implementations are substitutable for their base interfaces
- **Interface Segregation**: Services expose relevant interfaces only
- **Dependency Inversion**: Uses constructor-based dependency injection

## 🔧 Tech Stack

### Frontend:
- React with TypeScript
- Axios for API communication
- TailwindCSS for styling
- React Query for data fetching

### Backend:
- Java 17+
- Spring Boot 3.x
- PostgreSQL 15
- JPA/Hibernate
- Lombok

### DevOps:
- Docker & Docker Compose
- GitHub Actions (CI/CD)

### Testing:
- JUnit 5
- Mockito
- Testcontainers

### Logging:
- SLF4J + Logback with MDC for distributed tracing

## 🚀 Getting Started

### Prerequisites:
- Docker and Docker Compose
- Java 17+ (for development)
- Node.js 18+ (for frontend development)

### Running with Docker Compose:

```bash
# Start all services
docker-compose up -d

# Access the application
# Frontend: http://localhost:3001
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html

# Stop all services
docker-compose down
```

### Accessing PostgreSQL Database:

```bash
# Connect to PostgreSQL container
docker-compose exec postgres psql -U postgres -d configserver

# Inside PostgreSQL shell
# List tables
\dt

# Query configuration groups
SELECT * FROM configuration_groups;

# Query configuration items
SELECT * FROM configuration_items;

# Query audit logs
SELECT * FROM audit_logs;
```

## 📊 Data Model

### Configuration Groups
Logical grouping of configuration items, typically representing a microservice or application component.

| Field       | Type         | Description                         |
|-------------|--------------|-------------------------------------|
| id          | Long         | Primary key                         |
| name        | String       | Unique name (e.g., "user-service")  |
| description | String       | Purpose of the configuration group  |

### Configuration Items
Individual configuration properties with environment-specific values.

| Field       | Type         | Description                         |
|-------------|--------------|-------------------------------------|
| id          | Long         | Primary key                         |
| key         | String       | Property key (e.g., "db.timeout")   |
| value       | String       | Property value                      |
| environment | String       | Environment (DEV, STAGE, PROD)      |
| group_id    | Long         | Reference to configuration group    |

### Audit Logs
Tracks all changes to configuration items for compliance and troubleshooting.

| Field       | Type         | Description                         |
|-------------|--------------|-------------------------------------|
| id          | Long         | Primary key                         |
| action      | String       | Type of action (CREATE, UPDATE, DELETE) |
| entity_type | String       | Type of entity affected             |
| entity_id   | Long         | ID of affected entity               |
| old_value   | String       | Previous value (null for CREATE)    |
| new_value   | String       | New value (null for DELETE)         |
| user_id     | String       | User who made the change            |
| timestamp   | DateTime     | When the change occurred            |

## 🌐 API Endpoints

### Configuration Groups

| Method | Endpoint              | Description                           |
|--------|----------------------|---------------------------------------|
| GET    | /api/groups          | Get all groups                        |
| GET    | /api/groups/{id}     | Get group by ID                       |
| GET    | /api/groups/name/{name} | Get group by name                  |
| POST   | /api/groups          | Create new group                      |
| PUT    | /api/groups/{id}     | Update existing group                 |
| DELETE | /api/groups/{id}     | Delete group                          |

### Configuration Items

| Method | Endpoint              | Description                           |
|--------|----------------------|---------------------------------------|
| GET    | /api/items           | Get all items                         |
| GET    | /api/items/{id}      | Get item by ID                        |
| GET    | /api/items/group/{groupId} | Get items by group              |
| GET    | /api/items/group/{groupId}/environment/{env} | Get items by group and environment |
| POST   | /api/items           | Create new item                       |
| PUT    | /api/items/{id}      | Update existing item                  |
| DELETE | /api/items/{id}      | Delete item                           |

### Environments

| Method | Endpoint              | Description                           |
|--------|----------------------|---------------------------------------|
| GET    | /api/environments    | Get all available environments        |

## 🧪 Sample Data

The system is pre-populated with the following sample data:

### Configuration Groups:
- api-service (API Gateway Configuration)
- user-service (User Management Service Configuration)
- payment-service (Payment Processing Service Configuration)
- notification-service (Notification Service Configuration)

### Sample Configuration Items:
- Environment-specific values for timeouts, connection limits, URLs, etc.
- Each group has configurations across DEV, STAGE, and PROD environments

## 🔐 Security

The system is designed with OAuth2 integration in mind for future implementation. Currently uses basic Spring Security settings for local development.

## 📝 Logging Strategy

Uses SLF4J with Logback and MDC for distributed tracing:

- **Keys**: traceId, userId, env
- **Pattern**: `%d{yyyy-MM-dd HH:mm:ss} [%X{traceId}] [%X{userId}] [%X{env}] %-5level %logger{36} - %msg%n`
- **Best Practice**: MDC values are cleared at the end of each request

## 🔄 CI/CD Pipeline

GitHub Actions workflows are configured for:
- Building and testing the application
- Running code quality checks
- Building Docker images
- Deploying to environments

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/docs/getting-started)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.