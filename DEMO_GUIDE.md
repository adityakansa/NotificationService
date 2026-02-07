# 🎬 Demo Execution Guide

## Visual Guide to Running the 3 Demo Flows

---

## 🎯 Demo 1: Basic Multi-Channel Notifications

### Command
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo1
```

### Expected Console Output

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2026-02-07 14:30:00.123  INFO --- [main] NotificationSystemApplication : Starting...


═══════════════════════════════════════════════════════════════
  DEMO 1: Basic Notification System - Multi-Channel Demo
═══════════════════════════════════════════════════════════════


▶ STEP 1: Registering Users
─────────────────────────────────────────────────────────────
✓ Registered user: alice (ID: 1)
✓ Registered user: bob (ID: 2)
✓ Registered user: charlie (ID: 3)
✓ Successfully registered 3 users


▶ STEP 2: Sending HIGH Priority Email Notification
─────────────────────────────────────────────────────────────
📧 Sending EMAIL to: alice@example.com
   Subject: Critical System Alert
   Body: Your account requires immediate attention...
   Priority: HIGH
✅ Email sent successfully to alice@example.com
✓ HIGH priority email sent to alice


▶ STEP 3: Sending MEDIUM Priority SMS Notification
─────────────────────────────────────────────────────────────
📱 Sending SMS to: +9876543210
   Message: Your verification code is: 123456...
   Priority: MEDIUM
✅ SMS sent successfully to +9876543210
✓ MEDIUM priority SMS sent to bob


▶ STEP 4: Sending LOW Priority Push Notification
─────────────────────────────────────────────────────────────
🔔 Sending PUSH notification to token: charlie-push-token...
   Title: Weekly Summary
   Body: You have 5 new messages and 3 updates this week.
   Priority: LOW
✅ Push notification sent successfully
✓ LOW priority push notification sent to charlie


▶ STEP 5: Bulk Notification Campaign
─────────────────────────────────────────────────────────────
✓ Created 3 bulk notifications
📧 Sending EMAIL to: alice@example.com
   Subject: Important Announcement
✅ Email sent successfully
📧 Sending EMAIL to: bob@example.com
   Subject: Important Announcement
✅ Email sent successfully
📧 Sending EMAIL to: charlie@example.com
   Subject: Important Announcement
✅ Email sent successfully
✓ Sent 3 bulk notifications


═══════════════════════════════════════════════════════════════
  DEMO 1 COMPLETED SUCCESSFULLY!
  - Registered: 3 users
  - Individual notifications: 3 (HIGH, MEDIUM, LOW priority)
  - Bulk notifications: 3
  - Total notifications sent: 6
  - Channels used: EMAIL, SMS, PUSH
═══════════════════════════════════════════════════════════════

```

### Key Observations
- ✅ Multi-channel support working
- ✅ Priority handling demonstrated
- ✅ Bulk operations functional
- ✅ Clean, formatted output

---

## 🔄 Demo 2: Retry Mechanism with Exponential Backoff

### Command
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo2
```

### Expected Console Output

```
═══════════════════════════════════════════════════════════════
  DEMO 2: Retry Mechanism with Exponential Backoff
═══════════════════════════════════════════════════════════════


▶ STEP 1: Setting up Test Environment
─────────────────────────────────────────────────────────────
✓ Test user registered: retrytest


▶ STEP 2: Creating Notifications (10% failure rate simulated)
─────────────────────────────────────────────────────────────
✓ Created 10 notifications


▶ STEP 3: Sending Notifications (some may fail)
─────────────────────────────────────────────────────────────
📧 Sending EMAIL to: retry@example.com
   Subject: Test Notification #1
✅ Email sent successfully to retry@example.com
✓ Notification 1 sent successfully

📧 Sending EMAIL to: retry@example.com
   Subject: Test Notification #2
✅ Email sent successfully to retry@example.com
✓ Notification 2 sent successfully

📧 Sending EMAIL to: retry@example.com
   Subject: Test Notification #3
❌ Failed to send email: SMTP server temporarily unavailable
⚠ Notification 3 failed (will retry)

📧 Sending EMAIL to: retry@example.com
   Subject: Test Notification #4
✅ Email sent successfully to retry@example.com
✓ Notification 4 sent successfully

... (continuing for all 10 notifications) ...

📊 Initial Send Results:
   Sent successfully: 9
   Failed (queued for retry): 1


▶ STEP 4: Retry Statistics
─────────────────────────────────────────────────────────────
📈 Retry Configuration:
   Max Attempts: 3
   Initial Interval: 1000ms
   Backoff Multiplier: 2.0x
   Max Interval: 10000ms

📊 Current Retry Queue:
   Notifications in retry state: 1
   Permanently failed: 0


▶ STEP 5: Manual Retry for Failed Notifications
─────────────────────────────────────────────────────────────
Found 1 notifications to retry
⟳ Retrying notification 3 (attempt 1/3)
📧 Sending EMAIL to: retry@example.com
   Subject: Test Notification #3
✅ Email sent successfully to retry@example.com
✓ Retry successful for notification 3


▶ STEP 6: Final Statistics After Retry
─────────────────────────────────────────────────────────────
📊 Final Retry Statistics:
   Current retry queue: 0
   Permanently failed: 0
   Retries in last 24h: 1


═══════════════════════════════════════════════════════════════
  DEMO 2 COMPLETED SUCCESSFULLY!
  - Total notifications created: 10
  - Initial success rate: 90%
  - Retry mechanism: ACTIVE
  - Exponential backoff: CONFIGURED
  Key Features Demonstrated:
  ✓ Automatic failure detection
  ✓ Exponential backoff retry
  ✓ Manual retry capability
  ✓ Retry statistics tracking
═══════════════════════════════════════════════════════════════
```

### Key Observations
- ✅ Failure simulation working (10% rate)
- ✅ Automatic retry queueing
- ✅ Exponential backoff calculation
- ✅ Successful recovery
- ✅ Comprehensive statistics

---

## 📅 Demo 3: Scheduling, Priority & Batch Processing

### Command
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo3
```

### Expected Console Output

```
═══════════════════════════════════════════════════════════════
  DEMO 3: Scheduling, Priority & Batch Processing
═══════════════════════════════════════════════════════════════


▶ STEP 1: Registering Users
─────────────────────────────────────────────────────────────
✓ Registered: user1
✓ Registered: user2
✓ Registered: user3
✓ Registered: user4
✓ Registered: user5
✓ Total users registered: 5


▶ STEP 2: Creating Notifications with Mixed Priorities
─────────────────────────────────────────────────────────────
✓ Created LOW priority notification for user1
✓ Created LOW priority notification for user2
✓ Created LOW priority notification for user3
✓ Created MEDIUM priority notification for user1
✓ Created MEDIUM priority notification for user2
✓ Created HIGH priority notification for user1


▶ STEP 3: Batch Statistics Before Processing
─────────────────────────────────────────────────────────────
📊 Batch Statistics:
   Batch Size: 100
   Pending: 6
   Processing: 0
   Sent: 0
   Failed: 0
   Retry: 0

📈 Priority Breakdown (Pending):
   HIGH: 1
   MEDIUM: 2
   LOW: 3


▶ STEP 4: Processing Notifications by Priority
─────────────────────────────────────────────────────────────
ℹ Note: HIGH priority processed first, then MEDIUM, then LOW

Processing 1 HIGH priority notifications
🔔 Sending PUSH notification
   Title: Security Alert
   Priority: HIGH
✅ Push notification sent successfully
Completed HIGH priority batch 1: 1/1 successful

Processing 2 MEDIUM priority notifications
📱 Sending SMS to: +1111111111
   Message: Your account settings have been updated.
   Priority: MEDIUM
✅ SMS sent successfully
📱 Sending SMS to: +1222222222
   Message: Your account settings have been updated.
   Priority: MEDIUM
✅ SMS sent successfully
Completed MEDIUM priority batch 1: 2/2 successful

Processing 3 LOW priority notifications
📧 Sending EMAIL to: user1@example.com
   Subject: Newsletter
   Priority: LOW
✅ Email sent successfully
📧 Sending EMAIL to: user2@example.com
   Subject: Newsletter
   Priority: LOW
✅ Email sent successfully
📧 Sending EMAIL to: user3@example.com
   Subject: Newsletter
   Priority: LOW
✅ Email sent successfully
Completed LOW priority batch 1: 3/3 successful

✓ Processed 6 notifications


▶ STEP 5: Batch Statistics After Processing
─────────────────────────────────────────────────────────────
📊 Batch Statistics:
   Batch Size: 100
   Pending: 0
   Processing: 0
   Sent: 6
   Failed: 0
   Retry: 0

📈 Priority Breakdown (Pending):
   HIGH: 0
   MEDIUM: 0
   LOW: 0


▶ STEP 6: Scheduling Future Notification
─────────────────────────────────────────────────────────────
✓ Notification scheduled for: 2026-02-07T14:35:00
✓ Notification ID: 7
✓ Status: SCHEDULED


▶ STEP 7: Creating Recurring Notification
─────────────────────────────────────────────────────────────
✓ Recurring notification created
✓ Frequency: DAILY
✓ Max occurrences: 7
✓ Notification ID: 8


▶ STEP 8: Rescheduling Notification
─────────────────────────────────────────────────────────────
✓ Notification 7 rescheduled
✓ New scheduled time: 2026-02-07T14:38:00


═══════════════════════════════════════════════════════════════
  DEMO 3 COMPLETED SUCCESSFULLY!
  - Users registered: 5
  - Immediate notifications: 6 (3 LOW, 2 MEDIUM, 1 HIGH)
  - Scheduled notifications: 1
  - Recurring notifications: 1
  Key Features Demonstrated:
  ✓ Priority-based processing (HIGH → MEDIUM → LOW)
  ✓ Batch processing with statistics
  ✓ Future scheduling
  ✓ Recurring notifications
  ✓ Reschedule capability
═══════════════════════════════════════════════════════════════
```

### Key Observations
- ✅ Priority-based queue working perfectly
- ✅ HIGH priority notifications sent first
- ✅ Batch processing efficient
- ✅ Scheduling functionality working
- ✅ Recurring notifications configured
- ✅ Statistics tracking accurate

---

## 📊 Comparison Table

| Feature | Demo 1 | Demo 2 | Demo 3 |
|---------|--------|--------|--------|
| **Multi-Channel** | ✅ | ✅ | ✅ |
| **Priority Handling** | ✅ | - | ✅ |
| **Bulk Operations** | ✅ | - | - |
| **Retry Mechanism** | - | ✅ | - |
| **Exponential Backoff** | - | ✅ | - |
| **Scheduling** | - | - | ✅ |
| **Recurring** | - | - | ✅ |
| **Batch Processing** | - | - | ✅ |
| **Statistics** | - | ✅ | ✅ |

---

## 🎓 What Each Demo Teaches

### Demo 1: Foundation
- User registration
- Channel preferences
- Basic notification sending
- Priority levels
- Bulk operations

### Demo 2: Reliability
- Failure handling
- Automatic retry
- Exponential backoff
- Retry statistics
- Manual intervention

### Demo 3: Advanced Features
- Priority-based queue
- Batch processing
- Scheduled delivery
- Recurring notifications
- Rescheduling

---

## 🧪 Testing the Demos

### Success Indicators

**Demo 1**:
- ✅ 3 users created
- ✅ 6 notifications sent
- ✅ All 3 channels used
- ✅ No errors

**Demo 2**:
- ✅ ~90% initial success rate
- ✅ Failed notifications queued
- ✅ Retry successful
- ✅ Final success rate: 100%

**Demo 3**:
- ✅ 5 users created
- ✅ 6 immediate notifications
- ✅ HIGH priority processed first
- ✅ 1 scheduled notification
- ✅ 1 recurring notification

---

## 🛠️ Troubleshooting

### No output appearing?
- Check that correct profile is active
- Verify Spring Boot is running

### Errors during execution?
- Run `mvn clean install` first
- Check Java version (needs 17+)
- Ensure no port conflicts

### Want to see more details?
- Change logging level in application.yml:
```yaml
logging:
  level:
    com.notification: DEBUG
```

---

## 💡 Tips for Best Experience

1. **Run demos in sequence** (1 → 2 → 3) for logical progression
2. **Watch the console output** - it's designed to be informative
3. **Note the emojis** - they indicate action types:
   - 📧 Email
   - 📱 SMS
   - 🔔 Push
   - ✅ Success
   - ❌ Failure
   - ⟳ Retry
4. **Observe the patterns**:
   - Priority ordering in Demo 3
   - Retry attempts in Demo 2
   - Multi-channel in Demo 1

---

## 🎬 Demo Video Script (if recording)

### Demo 1 (2-3 minutes)
1. Show code structure briefly
2. Run demo
3. Point out multi-channel support
4. Highlight priority levels
5. Show bulk operation

### Demo 2 (3-4 minutes)
1. Explain retry importance
2. Run demo
3. Point out failures (10%)
4. Show exponential backoff
5. Demonstrate successful retry

### Demo 3 (4-5 minutes)
1. Explain advanced features
2. Run demo
3. Highlight priority queue
4. Show scheduling
5. Explain recurring notifications

---

**Total Demo Time**: ~10-12 minutes for all three demos

**Perfect for**: Technical interviews, presentations, code reviews, learning sessions

---

Happy Demoing! 🚀
