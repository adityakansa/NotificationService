# 🚀 Quick Start Guide - Notification System

## Prerequisites
- Java 17+ installed
- Maven 3.6+ installed
- Terminal/Command prompt

## Step 1: Verify Installation

```bash
java -version    # Should show Java 17 or higher
mvn -version     # Should show Maven 3.6 or higher
```

## Step 2: Build the Project

```bash
cd "/Users/aditya/MyProjects/Notification System"
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

## Step 3: Run Demo Flows

### 🎯 Demo 1: Basic Multi-Channel Notifications

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo1
```

**What you'll see:**
```
═══════════════════════════════════════════════════════════════
  DEMO 1: Basic Notification System - Multi-Channel Demo
═══════════════════════════════════════════════════════════════

▶ STEP 1: Registering Users
─────────────────────────────────────────────────────────────
✓ Registered user: alice (ID: 1)
✓ Registered user: bob (ID: 2)
✓ Registered user: charlie (ID: 3)

▶ STEP 2: Sending HIGH Priority Email Notification
─────────────────────────────────────────────────────────────
📧 Sending EMAIL to: alice@example.com
   Subject: Critical System Alert
   Priority: HIGH
✅ Email sent successfully

▶ STEP 3: Sending MEDIUM Priority SMS Notification
─────────────────────────────────────────────────────────────
📱 Sending SMS to: +9876543210
   Message: Your verification code is: 123456
✅ SMS sent successfully

▶ STEP 4: Sending LOW Priority Push Notification
─────────────────────────────────────────────────────────────
🔔 Sending PUSH notification
   Title: Weekly Summary
✅ Push notification sent successfully

▶ STEP 5: Bulk Notification Campaign
─────────────────────────────────────────────────────────────
✓ Created 3 bulk notifications
✓ Sent 3 bulk notifications

═══════════════════════════════════════════════════════════════
  DEMO 1 COMPLETED SUCCESSFULLY!
  - Registered: 3 users
  - Total notifications sent: 6
  - Channels used: EMAIL, SMS, PUSH
═══════════════════════════════════════════════════════════════
```

Press `Ctrl+C` to stop.

---

### 🔄 Demo 2: Retry Mechanism with Exponential Backoff

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo2
```

**What you'll see:**
```
═══════════════════════════════════════════════════════════════
  DEMO 2: Retry Mechanism with Exponential Backoff
═══════════════════════════════════════════════════════════════

▶ STEP 2: Creating Notifications (10% failure rate simulated)
✓ Created 10 notifications

▶ STEP 3: Sending Notifications (some may fail)
✓ Notification 1 sent successfully
✓ Notification 2 sent successfully
⚠ Notification 3 failed (will retry)
...

📊 Initial Send Results:
   Sent successfully: 9
   Failed (queued for retry): 1

▶ STEP 4: Retry Statistics
📈 Retry Configuration:
   Max Attempts: 3
   Initial Interval: 1000ms
   Backoff Multiplier: 2.0x
   Max Interval: 10000ms

▶ STEP 5: Manual Retry for Failed Notifications
⟳ Retrying notification 3 (attempt 1/3)
✓ Retry successful

═══════════════════════════════════════════════════════════════
  Key Features Demonstrated:
  ✓ Automatic failure detection
  ✓ Exponential backoff retry
  ✓ Manual retry capability
  ✓ Retry statistics tracking
═══════════════════════════════════════════════════════════════
```

Press `Ctrl+C` to stop.

---

### 📅 Demo 3: Scheduling and Batch Processing

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo3
```

**What you'll see:**
```
═══════════════════════════════════════════════════════════════
  DEMO 3: Scheduling, Priority & Batch Processing
═══════════════════════════════════════════════════════════════

▶ STEP 2: Creating Notifications with Mixed Priorities
✓ Created LOW priority notification (3x)
✓ Created MEDIUM priority notification (2x)
✓ Created HIGH priority notification (1x)

▶ STEP 3: Batch Statistics Before Processing
📊 Batch Statistics:
   Pending: 6
   Priority Breakdown:
   HIGH: 1
   MEDIUM: 2
   LOW: 3

▶ STEP 4: Processing Notifications by Priority
ℹ Note: HIGH priority processed first, then MEDIUM, then LOW
✓ Processed 6 notifications

▶ STEP 6: Scheduling Future Notification
✓ Notification scheduled for: 2026-02-07T15:35:00
✓ Status: SCHEDULED

▶ STEP 7: Creating Recurring Notification
✓ Recurring notification created
✓ Frequency: DAILY
✓ Max occurrences: 7

═══════════════════════════════════════════════════════════════
  Key Features Demonstrated:
  ✓ Priority-based processing (HIGH → MEDIUM → LOW)
  ✓ Batch processing with statistics
  ✓ Future scheduling
  ✓ Recurring notifications
═══════════════════════════════════════════════════════════════
```

Press `Ctrl+C` to stop.

---

## Step 4: Run Unit Tests

```bash
mvn test
```

Expected output:
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Step 5: Explore the REST APIs (Optional)

### Start the application in API mode:

```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### Test with curl:

#### 1. Register a user:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "+1234567890",
    "pushToken": "test-token",
    "preferredChannels": ["EMAIL", "SMS"]
  }'
```

#### 2. Create a notification:
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "subject": "Test Notification",
    "body": "This is a test message",
    "channel": "EMAIL",
    "priority": "HIGH"
  }'
```

#### 3. Send the notification:
```bash
curl -X POST http://localhost:8080/api/notifications/1/send
```

#### 4. Get batch statistics:
```bash
curl http://localhost:8080/api/notifications/statistics/batch
```

#### 5. Get retry statistics:
```bash
curl http://localhost:8080/api/notifications/statistics/retry
```

---

## 📁 Project Structure Overview

```
Notification System/
├── pom.xml                           # Maven configuration
├── README.md                         # Comprehensive documentation
├── ARCHITECTURE.md                   # Architecture & design patterns
├── QUICK_START.md                    # This file
└── src/
    ├── main/
    │   ├── java/com/notification/
    │   │   ├── channel/              # Channel strategies (Strategy Pattern)
    │   │   ├── controller/           # REST API endpoints
    │   │   ├── demo/                 # 3 Demo flows
    │   │   ├── domain/               # Entities, enums, models
    │   │   ├── dto/                  # Data Transfer Objects
    │   │   ├── service/              # Business logic (SRP)
    │   │   └── repository/           # Data access layer
    │   └── resources/
    │       └── application.yml       # Configuration
    └── test/                         # Unit tests
```

---

## 🎓 Understanding the Code

### Key Design Patterns:

1. **Strategy Pattern** - `NotificationChannelStrategy` interface
   - Location: `src/main/java/com/notification/channel/`
   - Easy to add new channels (WhatsApp, Slack, etc.)

2. **Factory Pattern** - `NotificationChannelFactory`
   - Location: `src/main/java/com/notification/channel/NotificationChannelFactory.java`
   - Centralized channel creation

3. **Repository Pattern** - JPA repositories
   - Location: `src/main/java/com/notification/repository/`
   - Clean data access abstraction

4. **Builder Pattern** - Lombok's `@Builder`
   - Used throughout entities and DTOs
   - Clean object construction

### SOLID Principles Applied:

- **Single Responsibility**: Each service handles ONE concern
- **Open/Closed**: Add new channels without modifying existing code
- **Liskov Substitution**: All channel strategies are interchangeable
- **Interface Segregation**: Clean, focused interfaces
- **Dependency Inversion**: Depend on abstractions, not implementations

---

## 🐛 Troubleshooting

### Build fails with "Java version" error:
```bash
# Check Java version
java -version

# If < Java 17, install Java 17+
# macOS: brew install openjdk@17
```

### Port 8080 already in use:
```bash
# Change port in application.yml:
server:
  port: 8081
```

### Tests fail:
```bash
# Run with verbose output
mvn test -X
```

---

## 📚 Next Steps

1. **Read the Documentation**:
   - [README.md](README.md) - Full feature documentation
   - [ARCHITECTURE.md](ARCHITECTURE.md) - Design patterns & architecture

2. **Explore the Code**:
   - Start with `NotificationSystemApplication.java`
   - Check demo flows in `src/main/java/com/notification/demo/`
   - Review channel strategies in `src/main/java/com/notification/channel/impl/`

3. **Extend the System**:
   - Add a new channel (WhatsApp, Slack)
   - Implement additional retry strategies
   - Add more comprehensive tests

4. **Production Readiness**:
   - Replace H2 with PostgreSQL
   - Add authentication/authorization
   - Implement actual email/SMS providers
   - Add monitoring and observability

---

## 💡 Key Learnings

This project demonstrates:
- ✅ All SOLID principles in practice
- ✅ Multiple design patterns (Strategy, Factory, Repository, Builder, DTO)
- ✅ Clean architecture and separation of concerns
- ✅ Extensible design (easy to add new channels)
- ✅ Production-ready patterns (retry, batching, scheduling)
- ✅ Comprehensive testing approach
- ✅ RESTful API design
- ✅ Spring Boot best practices

---

## 📞 Support

For questions or issues:
1. Check the [README.md](README.md) for detailed documentation
2. Review the [ARCHITECTURE.md](ARCHITECTURE.md) for design details
3. Examine the demo flows for usage examples

---

**Happy Coding! 🚀**
