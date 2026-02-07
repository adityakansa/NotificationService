# Notification System - Architecture & Design

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        REST API Layer                            │
│  ┌──────────────────┐         ┌──────────────────────┐         │
│  │ UserController   │         │NotificationController│         │
│  └────────┬─────────┘         └──────────┬───────────┘         │
└───────────┼────────────────────────────────┼───────────────────┘
            │                                │
┌───────────┼────────────────────────────────┼───────────────────┐
│           │      Service Layer             │                    │
│  ┌────────▼─────────┐            ┌─────────▼──────────┐        │
│  │   UserService    │            │ NotificationService │        │
│  └──────────────────┘            └─────────┬───────────┘        │
│                                            │                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              NotificationChannelFactory                   │  │
│  │           (Factory Pattern - DIP)                         │  │
│  └──────────────────────┬───────────────────────────────────┘  │
│                         │                                       │
│  ┌──────────────────────┼───────────────────────────────────┐  │
│  │  NotificationChannelStrategy (Strategy Pattern - OCP)    │  │
│  └──────────────────────┬───────────────────────────────────┘  │
│            ┌────────────┼────────────┐                          │
│  ┌─────────▼─────┐  ┌──▼──────┐  ┌─▼─────────────┐           │
│  │EmailChannel   │  │SmsChannel│  │PushChannel    │           │
│  │Strategy       │  │Strategy  │  │Strategy       │           │
│  └───────────────┘  └──────────┘  └───────────────┘           │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Specialized Services (SRP)                        │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ • NotificationSchedulerService - Scheduling Logic        │  │
│  │ • NotificationBatchService - Batch Processing            │  │
│  │ • NotificationRetryService - Retry with Exp. Backoff     │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────┐
│                    Repository Layer (Repository Pattern)         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐│
│  │ UserRepository   │  │NotificationRepo  │  │HistoryRepo    ││
│  └──────────────────┘  └──────────────────┘  └───────────────┘│
└─────────────────────────────────┬───────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────┐
│                       Database Layer (H2)                        │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────────┐     │
│  │  Users   │  │Notifications │  │NotificationHistory   │     │
│  └──────────┘  └──────────────┘  └──────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

## SOLID Principles Application

### 1. Single Responsibility Principle (SRP)
Each class has ONE reason to change:

```
NotificationService
└─ Responsibility: Manage notification lifecycle (create, send, query)

NotificationRetryService
└─ Responsibility: Handle retry logic with exponential backoff

NotificationSchedulerService
└─ Responsibility: Manage scheduled and recurring notifications

NotificationBatchService
└─ Responsibility: Process notifications in batches

UserService
└─ Responsibility: Manage user registration and preferences

EmailChannelStrategy
└─ Responsibility: Send notifications via Email channel
```

### 2. Open/Closed Principle (OCP)
System is OPEN for extension, CLOSED for modification:

```java
// Adding new channel WITHOUT modifying existing code
@Component("slackChannel")
public class SlackChannelStrategy implements NotificationChannelStrategy {
    // New functionality
}

// Factory automatically picks it up via Spring's DI
// NO changes needed to:
// - NotificationService
// - NotificationChannelFactory
// - Other channel implementations
```

### 3. Liskov Substitution Principle (LSP)
All channel strategies are interchangeable:

```java
NotificationChannelStrategy strategy = channelFactory.getStrategy(channel);
// Can be EmailChannelStrategy, SmsChannelStrategy, or ANY new implementation
// All work the same way - substitutable

DeliveryResult result = strategy.send(notification, user);
// Behavior is consistent regardless of concrete implementation
```

### 4. Interface Segregation Principle (ISP)
Clean, focused interfaces:

```java
public interface NotificationChannelStrategy {
    DeliveryResult send(Notification notification, User user);
    boolean canDeliver(User user);
    String getChannelName();
    default boolean supportsBatching() { return false; }
}
// Clients only depend on methods they use
// No fat interfaces with unused methods
```

### 5. Dependency Inversion Principle (DIP)
Depend on abstractions, not concretions:

```java
public class NotificationService {
    // Depends on INTERFACE, not concrete implementation
    private final NotificationChannelFactory channelFactory;
    
    // Factory provides the abstraction
    NotificationChannelStrategy strategy = channelFactory.getStrategy(channel);
}

// High-level module (NotificationService) doesn't depend on
// low-level modules (EmailChannelStrategy, SmsChannelStrategy)
// Both depend on abstraction (NotificationChannelStrategy)
```

## Design Patterns Implementation

### 1. Strategy Pattern
**Purpose**: Select algorithm at runtime

```
NotificationChannelStrategy (Interface)
        ↑
        ├── EmailChannelStrategy
        ├── SmsChannelStrategy
        ├── PushNotificationChannelStrategy
        └── [Future: WhatsAppChannelStrategy, SlackChannelStrategy]
```

**Benefits**:
- Easy to add new channels
- Runtime channel selection
- Clean separation of channel-specific logic

### 2. Factory Pattern
**Purpose**: Centralize object creation

```java
@Component
public class NotificationChannelFactory {
    private final Map<NotificationChannel, NotificationChannelStrategy> strategies;
    
    public NotificationChannelStrategy getStrategy(NotificationChannel channel) {
        return strategies.get(channel);
    }
}
```

**Benefits**:
- Single point for strategy creation
- Automatic registration via Spring DI
- Easy to extend

### 3. Repository Pattern
**Purpose**: Abstract data access

```
Service Layer
    ↓
Repository Interface (JPA)
    ↓
Database
```

**Benefits**:
- Clean separation of concerns
- Easy to test (mock repositories)
- Database-agnostic business logic

### 4. Builder Pattern
**Purpose**: Construct complex objects

```java
User user = User.builder()
    .username("john")
    .email("john@example.com")
    .preferredChannels(Set.of(EMAIL, SMS))
    .build();
```

**Benefits**:
- Readable object construction
- Immutable objects
- Optional parameters

### 5. DTO Pattern
**Purpose**: Separate API layer from domain

```
API Layer (DTOs)           Domain Layer (Entities)
NotificationRequest   →    Notification
NotificationResponse  ←    Notification
```

**Benefits**:
- API evolution without domain changes
- Validation at API boundary
- Security (don't expose internal structure)

## Retry Mechanism - Exponential Backoff

```
Attempt 1: Fail → Wait 1s  (1000ms * 2^0)
Attempt 2: Fail → Wait 2s  (1000ms * 2^1)
Attempt 3: Fail → Wait 4s  (1000ms * 2^2)
Attempt 4: Fail → Wait 8s  (1000ms * 2^3)
Attempt 5: Fail → Wait 10s (capped at maxInterval)
```

**Formula**: `delay = min(initialInterval * multiplier^attemptNumber, maxInterval)`

**Benefits**:
- Reduces load on failing services
- Increases chance of recovery
- Prevents thundering herd problem

## Priority-Based Processing

```
Priority Queue (Database-backed)
┌─────────────────────────────────┐
│ HIGH Priority Notifications     │ → Processed First
│  └─ Critical alerts, security   │
├─────────────────────────────────┤
│ MEDIUM Priority Notifications   │ → Processed Second
│  └─ Updates, verifications      │
├─────────────────────────────────┤
│ LOW Priority Notifications      │ → Processed Last
│  └─ Newsletters, summaries      │
└─────────────────────────────────┘
```

## Batch Processing Flow

```
1. Fetch pending notifications
   └─ Order by: priority ASC, createdAt ASC

2. Group by priority
   ├─ HIGH priority group
   ├─ MEDIUM priority group
   └─ LOW priority group

3. Process each priority group
   └─ Split into batches (size: 100)
   
4. For each batch:
   ├─ Send notifications
   ├─ Track success/failure
   └─ Update statistics
```

## Scheduling Architecture

```
@Scheduled Tasks
├─ processScheduledNotifications() - Every 60s
│  └─ Find notifications where scheduledTime <= now
│  
├─ processRecurringNotifications() - Every 60m
│  └─ Create next occurrence for recurring notifications
│  
├─ processBatchNotifications() - Every 30s
│  └─ Batch process pending notifications
│  
└─ processRetryNotifications() - Every 60s
   └─ Retry failed notifications with exponential backoff
```

## Extensibility Examples

### Adding WhatsApp Channel

```java
// Step 1: Implement Strategy
@Component("whatsappChannel")
public class WhatsAppChannelStrategy implements NotificationChannelStrategy {
    @Override
    public DeliveryResult send(Notification notification, User user) {
        // WhatsApp API integration
        return DeliveryResult.success("Sent via WhatsApp");
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

// Step 2: Add to enum
public enum NotificationChannel {
    EMAIL, SMS, PUSH, WHATSAPP, SLACK
}

// That's it! No other changes needed ✨
```

### Adding Slack Channel

```java
@Component("slackChannel")
public class SlackChannelStrategy implements NotificationChannelStrategy {
    @Autowired
    private SlackWebClient slackClient;
    
    @Override
    public DeliveryResult send(Notification notification, User user) {
        slackClient.sendMessage(user.getSlackHandle(), notification.getBody());
        return DeliveryResult.success("Sent to Slack");
    }
    
    @Override
    public boolean canDeliver(User user) {
        return user.getSlackHandle() != null;
    }
    
    @Override
    public String getChannelName() {
        return "SLACK";
    }
}
```

## Scalability Considerations

### Current Implementation (Demo)
- Single instance
- H2 in-memory database
- Scheduled tasks
- Suitable for: 1K-10K notifications/day

### Production Scale (Recommendations)

#### For 100K-1M notifications/day:
- Persistent database (PostgreSQL with replication)
- Redis for caching and distributed locks
- Multiple application instances (load balanced)
- Quartz scheduler with database backend

#### For 10M+ notifications/day:
- Message queue (Kafka/RabbitMQ)
- Distributed processing (multiple consumers)
- Separate read/write databases
- Horizontal scaling with Kubernetes
- Dedicated retry service instances
- Circuit breakers for external services

## Monitoring & Observability

### Key Metrics to Track
```
Notification Metrics:
- Total sent (by channel, priority)
- Success rate (%)
- Failure rate (%)
- Average delivery time
- Retry attempts distribution

System Metrics:
- Queue depth
- Processing throughput
- Database connection pool
- API response times
```

### Health Checks
```
/actuator/health
├─ Database connectivity
├─ External service status
└─ Queue health
```

## Security Considerations

### API Security (Production)
- JWT/OAuth2 authentication
- Rate limiting per user/IP
- Input validation and sanitization
- SQL injection prevention (JPA helps)
- XSS prevention

### Data Security
- Encrypt sensitive user data
- PII handling compliance (GDPR, CCPA)
- Audit logs for compliance
- Secure credential storage (secrets management)

## Testing Strategy

### Unit Tests
- Service layer logic
- Channel strategies
- Retry mechanism
- Priority handling

### Integration Tests
- End-to-end notification flow
- Database operations
- REST API endpoints

### Load Tests
- Concurrent notifications
- Batch processing performance
- Retry mechanism under load

## Deployment Architecture

```
┌────────────────────────────────────────────────┐
│                Load Balancer                    │
└───────────────┬────────────────────────────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│ App Instance │  │ App Instance │
│      #1      │  │      #2      │
└──────┬───────┘  └───────┬──────┘
       │                  │
       └────────┬─────────┘
                ▼
       ┌────────────────┐
       │   PostgreSQL   │
       │  (Primary +    │
       │   Replicas)    │
       └────────────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│    Redis     │  │  Message     │
│   (Cache)    │  │   Queue      │
└──────────────┘  └──────────────┘
```

This architecture document demonstrates the thoughtful design and extensibility of the Notification System.
