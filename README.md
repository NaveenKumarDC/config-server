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

## 💻 User Interface Features

### Modern Blue and Gray Design
- Blue header with white text for strong visual hierarchy
- Grey and white content areas for readability
- Consistent focus states and responsive design
- Optimized for both desktop and mobile views

### Configuration Dashboard
- **Group Management**: 
  - Create, view, and delete configuration groups
  - Each group has a name and description
  - Visual indication of the currently selected group

- **Environment Selection**:
  - Dropdown to switch between environments (DEV, STAGE, PROD)
  - Environment-specific configuration values

- **Configuration Items**:
  - Table view of all configuration items for the selected group and environment
  - Inline editing for configuration values (for admin users)
  - Add new configuration items with key-value pairs
  - Delete existing configuration items

### User Management System
- **Access Control**:
  - Dedicated "Users" button in navigation for admin users
  - Full-page User Management interface
  - Role-based access control (ADMIN vs READ_ONLY)

- **User Listing**:
  - Interactive table of all users with sortable columns
  - Click column headers to sort by username, email, or role
  - Search functionality to filter users by any property
  - Color-coded role badges for quick identification

- **Inline Editing**:
  - Hover over username or role to reveal edit icon (✎)
  - Click to edit username directly in the table
  - Dropdown selector for changing user roles
  - Keyboard shortcuts: Enter to save, Escape to cancel
  - Visible save (✓) and cancel (✕) buttons

- **User CRUD Operations**:
  - Create new users with username, email, and role
  - Edit users through both inline editing and a dedicated form
  - Delete users with confirmation dialog
  - Form validation and error messages

### Authentication
- **Login Screen**:
  - Clean, modern login interface
  - Displays default credentials for testing
  - Error handling for invalid login attempts

- **Session Management**:
  - JWT-based authentication
  - Persistent login across browser sessions
  - Proper session expiration handling
  - Multi-tab logout support

### Responsive Design
- Adapts to different screen sizes and devices
- Mobile-friendly interface with appropriate touch targets
- Consistent experience across desktop, tablet, and mobile

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

### Users

| Method | Endpoint              | Description                           |
|--------|----------------------|---------------------------------------|
| GET    | /api/users           | Get all users (admin only)            |
| GET    | /api/users/{id}      | Get user by ID (admin only)           |
| POST   | /api/users           | Create new user (admin only)          |
| PUT    | /api/users/{id}      | Update existing user (admin only)     |
| DELETE | /api/users/{id}      | Delete user (admin only)              |

### Authentication

| Method | Endpoint              | Description                           |
|--------|----------------------|---------------------------------------|
| POST   | /api/auth/login      | Authenticate user and get JWT token   |
| POST   | /api/auth/logout     | Invalidate user session               |
| POST   | /api/auth/forgot-password | Request password reset           |
| POST   | /api/auth/reset-password | Reset password using token        |

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

### Backend Authentication System:
User entity with ADMIN and READ_ONLY roles
JWT-based authentication with token generation and validation
Spring Security configuration to protect write operations
User initializer to create default admin and read-only users

### Frontend Authentication Integration:
Login form with user/password authentication
JWT token storage in localStorage
Role-based UI that hides edit/delete functionality for non-admin users
Authentication service for managing login state

### Default Users:

Admin user: username: admin, password: admin123
Read-only user: username: user, password: user123

### Security Policies:
Public endpoints: login and authentication
Read-only endpoints: GET operations for configurations (accessible by both ADMIN and READ_ONLY users)
Admin-only endpoints: POST, PUT, DELETE operations (only accessible by ADMIN users)

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
    ### Stop and build

    docker-compose down && docker-compose up --build

    ### Build and start frontend
    docker-compose build frontend && docker-compose up -d frontend
    
    ### Build and start backend
    docker-compose build backend && docker-compose up -d backend 

    ### Run without executing the tests
    docker-compose build --build-arg MAVEN_OPTS="-DskipTests" && docker-compose up -d

- Deploying to environments

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/docs/getting-started)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.