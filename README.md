# Notification System - Enterprise-Grade Microservice

A scalable, extensible notification system built with Spring Boot following SOLID principles and design patterns. Features comprehensive test coverage with 272+ unit and integration tests.

## 🎯 Features

### Core Functionality
- ✅ **Multi-Channel Support**: Email, SMS, Push Notifications, WhatsApp, Slack
- ✅ **User Management**: Registration, preferences, channel configuration
- ✅ **Priority Levels**: HIGH, MEDIUM, LOW priority notifications
- ✅ **Scheduling**: Immediate, scheduled, and recurring notifications
- ✅ **Batch Processing**: Efficient bulk notification handling
- ✅ **Retry Mechanism**: Exponential backoff with configurable retry attempts
- ✅ **Failure Tracking**: Comprehensive history and audit trail
- ✅ **RESTful APIs**: Complete API suite with JSON payloads
- ✅ **Validation**: Comprehensive input validation with Jakarta Bean Validation
- ✅ **Testing**: 272 tests including unit, integration, and API tests

### Design Principles Applied

#### SOLID Principles
1. **Single Responsibility Principle (SRP)**
   - Each service class handles one specific concern
   - `NotificationService` - notification lifecycle
   - `NotificationRetryService` - retry logic only
   - `NotificationSchedulerService` - scheduling only
   - `NotificationBatchService` - batch processing only

2. **Open/Closed Principle (OCP)**
   - New notification channels can be added without modifying existing code
   - Simply implement `NotificationChannelStrategy` interface
   - Factory pattern manages channel creation

3. **Liskov Substitution Principle (LSP)**
   - All channel strategies are interchangeable
   - Any `NotificationChannelStrategy` implementation works with the system

4. **Interface Segregation Principle (ISP)**
   - Clean, focused interfaces (e.g., `NotificationChannelStrategy`)
   - Clients depend only on methods they use

5. **Dependency Inversion Principle (DIP)**
   - Services depend on abstractions (interfaces), not concrete implementations
   - `NotificationService` depends on `NotificationChannelStrategy` interface
   - Spring's dependency injection enforces this principle

### Design Patterns Used

1. **Strategy Pattern**
   - `NotificationChannelStrategy` interface with multiple implementations
   - Allows runtime selection of notification delivery strategy

2. **Factory Pattern**
   - `NotificationChannelFactory` creates appropriate channel strategies
   - Centralizes channel strategy instantiation

3. **Repository Pattern**
   - JPA repositories abstract data access layer
   - Clean separation between business logic and data persistence

4. **Builder Pattern**
   - DTOs and entities use Lombok's `@Builder` for clean object creation

5. **DTO Pattern**
   - Separates API layer from domain layer
   - `NotificationRequest`, `NotificationResponse` for API communication

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/notification/
│   │   ├── NotificationSystemApplication.java
│   │   ├── channel/                    # Strategy Pattern - Channel implementations
│   │   │   ├── NotificationChannelStrategy.java (Interface)
│   │   │   ├── NotificationChannelFactory.java
│   │   │   ├── DeliveryResult.java
│   │   │   └── impl/
│   │   │       ├── EmailChannelStrategy.java
│   │   │       ├── SmsChannelStrategy.java
│   │   │       ├── PushNotificationChannelStrategy.java
│   │   │       ├── WhatsAppChannelStrategy.java
│   │   │       └── SlackChannelStrategy.java
│   │   ├── controller/                 # REST API Layer
│   │   │   ├── UserController.java
│   │   │   └── NotificationController.java
│   │   ├── domain/                     # Domain Model
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Notification.java
│   │   │   │   └── NotificationHistory.java
│   │   │   ├── enums/
│   │   │   │   ├── NotificationChannel.java
│   │   │   │   ├── NotificationPriority.java
│   │   │   │   ├── NotificationStatus.java
│   │   │   │   ├── ScheduleType.java
│   │   │   │   └── RecurrenceFrequency.java
│   │   │   └── model/
│   │   │       ├── NotificationContent.java
│   │   │       ├── ScheduleConfig.java
│   │   │       └── RetryConfig.java
│   │   ├── dto/                        # Data Transfer Objects
│   │   │   ├── NotificationRequest.java
│   │   │   ├── NotificationResponse.java
│   │   │   └── BulkNotificationRequest.java
│   │   ├── exception/                  # Exception Handling
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ErrorResponse.java
│   │   ├── repository/                 # Data Access Layer
│   │   │   ├── UserRepository.java
│   │   │   ├── NotificationRepository.java
│   │   │   └── NotificationHistoryRepository.java
│   │   ├── service/                    # Business Logic Layer
│   │   │   ├── NotificationService.java
│   │   │   ├── NotificationSchedulerService.java
│   │   │   ├── NotificationBatchService.java
│   │   │   ├── NotificationRetryService.java
│   │   │   ├── NotificationQueueService.java
│   │   │   ├── AsyncNotificationProcessor.java
│   │   │   └── UserService.java
│   │   └── validation/                 # Custom Validators
│   │       └── ScheduledTimeValidator.java
│   └── resources/
│       ├── application.yml
│       └── application-test.yml
└── test/                               # Comprehensive Test Suite
    └── java/com/notification/
        ├── channel/impl/               # Channel Tests (37 tests)
        ├── controller/                 # Controller Tests (21 tests)
        ├── service/                    # Service Tests (87 tests)
        ├── domain/                     # Domain Tests (82 tests)
        │   ├── entity/                 # Entity Tests (47 tests)
        │   └── enums/                  # Enum Tests (35 tests)
        ├── exception/                  # Exception Handler Tests (9 tests)
        └── integration/                # Integration Tests (29 tests)
            ├── NotificationFlowIntegrationTest.java
            ├── RestApiIntegrationTest.java
            └── ValidationIntegrationTest.java
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

Run all tests (272 tests):
```bash
mvn test
```

Run specific test suites:
```bash
# Service layer tests
mvn test -Dtest=*ServiceTest

# Integration tests
mvn test -Dtest=*IntegrationTest

# REST API tests
mvn test -Dtest=RestApiIntegrationTest

# Validation tests
mvn test -Dtest=ValidationIntegrationTest
```

### API Testing with cURL

**Register a user:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "preferredChannels": ["EMAIL", "SMS"]
  }'
```

**Create a notification:**
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "channel": "EMAIL",
    "subject": "Welcome!",
    "body": "Welcome to our notification system",
    "priority": "HIGH"
  }'
```

**Schedule a notification:**
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "channel": "EMAIL",
    "subject": "Scheduled Reminder",
    "body": "Your meeting is tomorrow",
    "priority": "MEDIUM",
    "scheduleType": "SCHEDULED",
    "scheduledTime": "2026-02-10T10:00:00"
  }'
```

## 📡 REST API Endpoints

### User Management
- `POST /api/users` - Register new user
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{userId}/preferences` - Update channel preferences
- `PUT /api/users/{userId}/channels/{channel}` - Update channel contact info
- `PATCH /api/users/{userId}/channels/{channel}` - Enable channel
- `DELETE /api/users/{userId}/channels/{channel}` - Disable channel
- `DELETE /api/users/{userId}` - Deactivate user

### Notification Operations
- `POST /api/notifications` - Create single notification
- `POST /api/notifications/bulk` - Create bulk notifications
- `POST /api/notifications/{id}/send` - Send notification immediately
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/{id}` - Get notification details
- `GET /api/notifications/user/{userId}` - Get user notifications
- `GET /api/notifications/{id}/history` - Get notification history

### Scheduling
- `DELETE /api/notifications/{id}/schedule` - Cancel scheduled notification
- `PUT /api/notifications/{id}/schedule` - Reschedule notification

### Retry Management
- `POST /api/notifications/{id}/retry` - Manual retry
- `POST /api/notifications/{id}/reset` - Reset for retry

### Batch Operations
- `POST /api/notifications/batch` - Process batch notifications
- `GET /api/notifications/statistics/batch` - Batch statistics

### Queue Management
- `POST /api/notifications/queue/process` - Process notification queue
- `POST /api/notifications/queue/priority` - Process by priority
- `GET /api/notifications/statistics/retry` - Retry statistics

## 🔧 Configuration

Key configuration properties in `application.yml`:

```yaml
spring:
  application:
    name: notification-system
  datasource:
    url: jdbc:h2:mem:notificationdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  h2:
    console:
      enabled: true

notification:
  retry:
    max-attempts: 3              # Maximum retry attempts
    initial-interval: 1000       # Initial retry delay (ms)
    multiplier: 2.0              # Exponential backoff multiplier
    max-interval: 10000          # Maximum retry delay (ms)
  batch:
    size: 100                    # Batch processing size
  scheduling:
    enabled: true                # Enable scheduled processing
```

## ✅ Input Validation

The system includes comprehensive validation:

### User Validation
- **Email**: Must be valid format (`@Email`)
- **Username**: 3-50 characters, alphanumeric with dots/underscores/hyphens only
- **Phone Number**: E.164 format (e.g., `+1234567890`)
- **WhatsApp Number**: E.164 format
- **Push Token**: Max 255 characters
- **Slack Handle**: 1-100 characters

### Notification Validation
- **User ID**: Must be positive number
- **Subject**: 1-200 characters
- **Body**: 1-5000 characters
- **Scheduled Time**: Must be in future (for SCHEDULED type)
- **Recurrence End Time**: Must be after start time
- **Max Occurrences**: 1-1000 for recurring notifications

### Custom Validators
- `ScheduledTimeValidator`: Validates scheduling constraints
- Ensures SCHEDULED notifications have future times
- Validates RECURRING notifications have proper end conditions
- Prevents scheduling notifications in the past

## 🎨 Extensibility

### Adding a New Notification Channel

1. Create a new strategy class implementing `NotificationChannelStrategy`:

```java
@Component("whatsappChannel")
public class WhatsAppChannelStrategy implements NotificationChannelStrategy {
    
    @Override
    public DeliveryResult send(Notification notification, User user) {
        // Implement WhatsApp sending logic
    }
    
    @Override
    public boolean canDeliver(User user) {
        return user.getWhatsappNumber() != null;
    }
    
    @Override
    public String getChannelName() {
        return "WHATSAPP";
    }
}
```

2. Add the channel to the `NotificationChannel` enum
3. No changes needed to existing code! ✨

## 📊 Database Schema

### Users Table
- User information and contact details
- Channel preferences
- Personalization variables

### Notifications Table
- Notification content and metadata
- Status tracking
- Scheduling information
- Retry configuration

### Notification History Table
- Audit trail of all notification attempts
- Error tracking
- Delivery status history

## 🧪 Testing

The project includes comprehensive test coverage with **272 tests**:

### Test Suite Breakdown

#### Unit Tests (243 tests)
- **Channel Tests** (37 tests): All 5 channel strategy implementations
- **Service Tests** (87 tests): Core business logic validation
  - NotificationService: Creation, sending, validation
  - NotificationRetryService: Retry logic and exponential backoff
  - NotificationSchedulerService: Scheduling and recurring notifications
  - NotificationBatchService: Batch processing
  - NotificationQueueService: Queue management
  - AsyncNotificationProcessor: Async processing
  - UserService: User management
- **Controller Tests** (21 tests): REST API endpoints with MockMvc
- **Domain Entity Tests** (47 tests): 
  - User: JPA lifecycle, channel validation
  - Notification: Business logic, status transitions
  - NotificationHistory: Audit trail
- **Enum Tests** (35 tests): All 5 enum types with business methods
- **Exception Handler Tests** (9 tests): Global exception handling

#### Integration Tests (29 tests)
- **NotificationFlowIntegrationTest** (9 tests): End-to-end workflows
  - Complete user → notification → send lifecycle
  - Priority ordering validation
  - Bulk operations
  - Recurring notifications
  - Retry mechanisms
- **RestApiIntegrationTest** (11 tests): JSON-based REST API testing
  - User registration with JSON payloads
  - Notification creation via HTTP
  - Scheduling and rescheduling
  - Bulk notifications
  - Error scenarios
- **ValidationIntegrationTest** (18 tests): Input validation
  - Email/phone format validation
  - Username constraints
  - Subject/body length limits
  - Scheduled time validation
  - Recurring notification rules
  - Bulk request validation

### Test Technologies
- **JUnit 5**: Testing framework
- **Mockito**: Mocking dependencies
- **Spring Test**: Integration testing with @SpringBootTest
- **MockMvc**: REST API testing
- **H2 Database**: In-memory database for integration tests
- **@Transactional**: Automatic test data cleanup

### Running Specific Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=NotificationServiceTest

# Test pattern
mvn test -Dtest=*IntegrationTest

# With coverage report
mvn clean test jacoco:report
```

## 📝 Key Design Decisions

1. **H2 In-Memory Database**: Used for development and testing. Can be easily replaced with PostgreSQL/MySQL for production by updating `application.yml`.

2. **Scheduled Tasks**: Using Spring's `@Scheduled` for periodic processing of scheduled/recurring notifications and retry mechanisms. Runs every 60 seconds. In production, consider using a distributed scheduler like Quartz with clustering.

3. **Simulated Delivery**: Channel implementations simulate network delays and occasional failures (10% for email, 5% for SMS, 3% for push) to demonstrate retry mechanisms. Replace with actual provider integrations for production.

4. **Batch Size**: Configurable via properties (default: 100). Adjust based on system capacity and requirements.

5. **Priority Queue**: Implemented via database query ordering (`findByStatusAndPriorityOrderByCreatedAtAsc`). For high-scale systems, consider Redis or RabbitMQ with priority queues.

6. **Validation Layer**: Jakarta Bean Validation at DTO level + custom validators at service level. Catches invalid data early and returns HTTP 400 with clear error messages.

7. **Async Processing**: Uses `@Async` with separate thread pool for non-blocking notification processing. Configure thread pool size based on load.

8. **Transactional Boundaries**: Service methods use `@Transactional` for consistency. Each notification creation/update is atomic.

9. **Audit Trail**: Every notification attempt is logged in `NotificationHistory` for compliance and debugging.

10. **Test Strategy**: Comprehensive pyramid approach - many unit tests, fewer integration tests, ensuring fast feedback and high confidence.

## 🔒 Production Considerations

For production deployment, consider:

### Infrastructure
- ✅ Use persistent database (PostgreSQL, MySQL) - update datasource config
- ✅ Implement actual notification providers:
  - Email: SendGrid, AWS SES, Mailgun
  - SMS: Twilio, AWS SNS, Vonage
  - Push: Firebase Cloud Messaging (FCM), Apple Push Notification Service (APNS)
  - WhatsApp: WhatsApp Business API
  - Slack: Slack Web API
- ✅ Use message queues (Kafka, RabbitMQ) for high-volume scenarios
- ✅ Implement distributed caching (Redis) for user preferences and rate limiting

### Security
- ✅ Add authentication and authorization (Spring Security with JWT)
- ✅ Implement rate limiting per user/channel
- ✅ Encrypt sensitive data (phone numbers, push tokens)
- ✅ Use HTTPS for all endpoints
- ✅ Add API keys for external integrations

### Monitoring & Observability
- ✅ Add metrics collection (Micrometer + Prometheus)
- ✅ Set up dashboards (Grafana)
- ✅ Implement distributed tracing (Jaeger, Zipkin)
- ✅ Centralized logging (ELK stack: Elasticsearch, Logstash, Kibana)
- ✅ Add health checks and readiness probes
- ✅ Configure alerts for failures, high retry rates, queue backlogs

### Scalability
- ✅ Containerize with Docker
- ✅ Orchestrate with Kubernetes
- ✅ Use horizontal pod autoscaling
- ✅ Implement connection pooling
- ✅ Add circuit breakers (Resilience4j) for external services
- ✅ Consider event-driven architecture for high throughput

### DevOps
- ✅ Set up CI/CD pipelines (GitHub Actions, Jenkins)
- ✅ Implement blue-green or canary deployments
- ✅ Add automated testing in pipeline
- ✅ Use infrastructure as code (Terraform, Helm charts)
- ✅ Implement proper secret management (Vault, AWS Secrets Manager)

### Performance
- ✅ Database indexing on frequently queried fields
- ✅ Query optimization and pagination
- ✅ Async processing for all I/O operations
- ✅ Bulk operations for batch processing
- ✅ Connection pooling (HikariCP)
- ✅ Load testing and capacity planning

## 👨‍💻 Technical Stack

- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM
- **H2 Database** - In-memory database (dev/test)
- **Maven** - Build tool
- **Lombok** - Boilerplate reduction
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Test** - Integration testing
- **Jakarta Bean Validation** - Input validation

## 📊 Project Metrics

- **Total Lines of Code**: ~8,000+
- **Test Coverage**: 272 tests covering all layers
- **Services**: 9 service classes
- **Controllers**: 2 REST controllers
- **Repositories**: 3 JPA repositories
- **Domain Entities**: 3 entities
- **Enums**: 5 enums with business logic
- **Channel Strategies**: 5 implementations
- **DTOs**: 3 data transfer objects
- **Custom Validators**: 1 scheduling validator

## 📄 License

This is an educational/portfolio project demonstrating enterprise software design principles and patterns.

---

**Built with ❤️ using Spring Boot, following SOLID principles and design patterns**
