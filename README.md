# Notification System - Enterprise-Grade Microservice

A scalable, extensible notification system built with Spring Boot following SOLID principles and design patterns.

## 🎯 Features

### Core Functionality
- ✅ **Multi-Channel Support**: Email, SMS, Push Notifications (extensible to WhatsApp, Slack)
- ✅ **User Management**: Registration, preferences, channel configuration
- ✅ **Priority Levels**: HIGH, MEDIUM, LOW priority notifications
- ✅ **Scheduling**: Immediate, scheduled, and recurring notifications
- ✅ **Batch Processing**: Efficient bulk notification handling
- ✅ **Retry Mechanism**: Exponential backoff with configurable retry attempts
- ✅ **Failure Tracking**: Comprehensive history and audit trail
- ✅ **RESTful APIs**: Complete API suite for all operations

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
│   │   │       └── PushNotificationChannelStrategy.java
│   │   ├── controller/                 # REST API Layer
│   │   │   ├── UserController.java
│   │   │   └── NotificationController.java
│   │   ├── demo/                       # Demo Flows
│   │   │   ├── Demo1BasicNotifications.java
│   │   │   ├── Demo2RetryMechanism.java
│   │   │   └── Demo3SchedulingAndBatching.java
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
│   │   └── service/                    # Business Logic Layer
│   │       ├── NotificationService.java
│   │       ├── NotificationSchedulerService.java
│   │       ├── NotificationBatchService.java
│   │       ├── NotificationRetryService.java
│   │       └── UserService.java
│   └── resources/
│       └── application.yml
└── test/                               # Unit Tests
    └── java/com/notification/
        ├── service/
        │   ├── NotificationServiceTest.java
        │   └── NotificationRetryServiceTest.java
        └── channel/impl/
            └── EmailChannelStrategyTest.java
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean install
```

### Running Demo Flows

#### Demo 1: Basic Multi-Channel Notifications
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo1
```
**Demonstrates:**
- User registration
- Email, SMS, and Push notifications
- Priority handling (HIGH, MEDIUM, LOW)
- Bulk notification campaigns

#### Demo 2: Retry Mechanism with Exponential Backoff
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo2
```
**Demonstrates:**
- Automatic failure detection
- Exponential backoff retry strategy
- Retry statistics and monitoring
- Manual retry capability

#### Demo 3: Scheduling and Batch Processing
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo3
```
**Demonstrates:**
- Priority-based processing
- Batch statistics
- Scheduled notifications
- Recurring notifications
- Reschedule capability

### Running Tests

```bash
mvn test
```

## 📡 REST API Endpoints

### User Management
- `POST /api/users` - Register new user
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{userId}/preferences` - Update channel preferences
- `DELETE /api/users/{userId}` - Deactivate user

### Notification Operations
- `POST /api/notifications` - Create single notification
- `POST /api/notifications/bulk` - Create bulk notifications
- `POST /api/notifications/{id}/send` - Send notification immediately
- `GET /api/notifications/{id}` - Get notification details
- `GET /api/notifications/user/{userId}` - Get user notifications
- `GET /api/notifications/{id}/history` - Get notification history

### Scheduling
- `DELETE /api/notifications/{id}/schedule` - Cancel scheduled notification
- `PUT /api/notifications/{id}/schedule` - Reschedule notification

### Retry Management
- `POST /api/notifications/{id}/retry` - Manual retry
- `POST /api/notifications/{id}/reset` - Reset for retry

### Statistics
- `GET /api/notifications/statistics/batch` - Batch statistics
- `GET /api/notifications/statistics/retry` - Retry statistics

## 🔧 Configuration

Key configuration properties in `application.yml`:

```yaml
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

The project includes comprehensive unit tests covering:
- ✅ Notification creation and sending
- ✅ Channel strategy implementations
- ✅ Retry logic with exponential backoff
- ✅ Priority handling
- ✅ Error scenarios and edge cases

## 📝 Key Design Decisions

1. **H2 In-Memory Database**: For demo purposes. Can be easily replaced with PostgreSQL/MySQL for production.

2. **Scheduled Tasks**: Using Spring's `@Scheduled` for periodic processing. In production, consider using a distributed scheduler like Quartz with clustering.

3. **Simulated Delivery**: Channel implementations simulate network delays and occasional failures (10% for email, 5% for SMS, 3% for push) to demonstrate retry mechanisms.

4. **Batch Size**: Configurable via properties. Adjust based on system capacity and requirements.

5. **Priority Queue**: Implemented via database query ordering. For high-scale systems, consider Redis or RabbitMQ with priority queues.

## 🔒 Production Considerations

For production deployment, consider:
- ✅ Use persistent database (PostgreSQL, MySQL)
- ✅ Implement actual email/SMS/push notification providers (SendGrid, Twilio, FCM)
- ✅ Add authentication and authorization
- ✅ Implement rate limiting
- ✅ Add monitoring and alerting (Prometheus, Grafana)
- ✅ Use message queues (Kafka, RabbitMQ) for high-volume scenarios
- ✅ Implement distributed caching (Redis)
- ✅ Add comprehensive logging and tracing (ELK stack, Jaeger)
- ✅ Set up CI/CD pipelines
- ✅ Containerize with Docker and orchestrate with Kubernetes

## 👨‍💻 Author

Notification System - Enterprise Microservice Demo
Built with Spring Boot 3.2.0, Java 17

## 📄 License

This is an educational project for demonstrating design principles and patterns.
# NotificationService
