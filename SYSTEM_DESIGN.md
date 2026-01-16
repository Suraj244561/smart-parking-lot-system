# Smart Parking Lot System - Detailed System Design Document

## Executive Summary

This document provides a comprehensive low-level design (LLD) for a Smart Parking Lot Management System. The system automatically manages vehicle entry/exit, parking space allocation, real-time availability updates, and dynamic fee calculation for a multi-floor parking facility.

---

## 1. System Design Approach

### 1.1 Architecture Principles

1. **Scalability**: Design supports thousands of concurrent vehicles
2. **Thread Safety**: Pessimistic locking at DB level, optimistic locking for performance
3. **Real-Time**: < 500ms latency for availability updates
4. **Consistency**: ACID compliance for all transactions
5. **Extensibility**: Easy to add new vehicle types, pricing models

### 1.2 Design Patterns Used

- **Strategy Pattern**: Different fee calculation strategies
- **Factory Pattern**: Vehicle and ticket creation
- **Repository Pattern**: Data access abstraction
- **Singleton Pattern**: Service layer singletons
- **Observer Pattern**: Real-time notification system

---

## 2. Functional Requirements Deep Dive

### 2.1 Vehicle Entry Process

```
Sequence Diagram: Vehicle Entry

Vehicle Driver              API Server           Database
   |                          |                      |
   |--1. Request Entry------->|                      |
   |   (VehicleType)          |                      |
   |                          |--2. Query Available-->|
   |                          |     Spot             |
   |                          |<--3. Available Spot--|
   |                          |                      |
   |                          |--4. Lock Spot------->|
   |                          |   (SERIALIZABLE)    |
   |                          |<--5. Spot Locked-----|
   |                          |                      |
   |                          |--6. Create Ticket--->|
   |                          |<--7. Ticket Created--|
   |                          |                      |
   |<--8. Issue Ticket--------|                      |
   |   (Spot: F1-C5)          |                      |
   |   (Time: 10:30 AM)       |                      |
```

**Algorithm Steps:**

1. Validate vehicle registration
2. Check vehicle type and required spot size
3. Query parking_space table with SERIALIZABLE isolation
4. Acquire pessimistic lock on available spot
5. Create parking_ticket record
6. Update parking_space (is_available = false)
7. Broadcast WebSocket event to all clients
8. Return ticket to vehicle

**Error Handling:**
- Vehicle not found: Return 404
- No available spot: Return 409 (Conflict)
- Concurrent booking: Retry mechanism with exponential backoff

### 2.2 Vehicle Exit and Fee Calculation

```
Sequence Diagram: Vehicle Exit & Fee Calculation

Vehicle Driver              API Server           Database
   |                          |                      |
   |--1. Request Exit-------->|                      |
   |   (TicketID)             |                      |
   |                          |--2. Get Ticket------>|
   |                          |<--3. Ticket Data-----|
   |                          |                      |
   |                          |--4. Get Current----->|
   |                          |   Occupancy          |
   |                          |<--5. Occupancy-----|-|
   |                          |                      |
   |   6. Calculate Fee (internal logic)            |
   |                          |                      |
   |                          |--7. Update Ticket--->|
   |                          |   (status, fee)     |
   |                          |                      |
   |                          |--8. Update Spot----->|
   |                          |   (available=true)  |
   |                          |                      |
   |                          |--9. Create Trans---->|
   |                          |   (transaction)     |
   |                          |<--10. Trans Created--|
   |                          |                      |
   |<--11. Bill + Receipt-----|                      |
```

**Fee Calculation Steps:**

1. Calculate parking duration in minutes
2. Determine hourly/daily rate based on duration
3. Apply vehicle type base rate
4. Query current occupancy rate
5. Apply occupancy multiplier (0.75x to 1.5x)
6. Apply loyalty/subscriber discounts
7. Ensure minimum charge (₹30)
8. Return itemized bill

---

## 3. Concurrency Handling - Detailed Analysis

### 3.1 Race Condition: Spot Double-Booking

**Problem:**
```
Thread 1                          Thread 2
  |                               |  
  |--Check: Spot F1-C5 available  |  
  |  (Result: Yes)                |  
  |                               |--Check: Spot F1-C5 available
  |                               |  (Result: Yes)
  |  
  |--Book Spot F1-C5              |--Book Spot F1-C5
  |  (CONFLICT - Both booked)     |
```

**Solution: Pessimistic Locking**

```java
// Database Query:
SELECT * FROM parking_space 
WHERE vehicle_type_id = ? 
AND is_available = true
LOCK IN SHARE MODE;  // Pessimistic lock

// Transaction Isolation: SERIALIZABLE
// Prevents: Dirty Reads, Non-Repeatable Reads, Phantom Reads
```

**Lock Timeline:**
```
Time    Thread 1                    Thread 2           Database State
────────────────────────────────────────────────────────────────────
T0      BEGIN TRANSACTION
        SELECT * FROM...            
        LOCK IN SHARE MODE           
        Lock acquired                                   Spot F1-C5 locked
        
T1                                  BEGIN TRANSACTION
                                     SELECT * FROM...
                                     LOCK IN SHARE MODE (wait)
                                     
T2      UPDATE parking_space        [BLOCKED waiting]   Waiting for lock
        is_available = false
        COMMIT                                          Lock released
        
T3                                  Lock acquired       Spot F1-C5 locked
                                     SELECT found spot F1-C5 already taken
                                     ROLLBACK            ✗ Operation failed
```

### 3.2 Transaction Isolation Levels

```
Isolation Level        Dirty Reads  Non-Rep Reads  Phantom Reads  Performance
────────────────────────────────────────────────────────────────────────────────
READ_UNCOMMITTED       ✗ YES        ✗ YES          ✗ YES          ★★★★★ (Fast)
READ_COMMITTED         ✓ NO         ✗ YES          ✗ YES          ★★★★☆
REPEATABLE_READ        ✓ NO         ✓ NO           ✗ YES          ★★★☆☆ (Default)
SERIALIZABLE           ✓ NO         ✓ NO           ✓ NO           ★☆☆☆☆ (Slowest)

Used in Parking System:
- Spot Allocation:     SERIALIZABLE (Critical)
- Fee Calculation:     REPEATABLE_READ (Consistent reads needed)
- Availability Check:  READ_COMMITTED (Speed priority)
```

### 3.3 Optimistic Locking

For non-critical, high-frequency updates:

```java
@Entity
public class ParkingSpace {
    @Version
    private Long version;  // Optimistic lock counter
    
    // When updated, version increments automatically
    // If concurrent update detected: OptimisticLockingFailureException
}
```

**Comparison:**

| Aspect | Pessimistic | Optimistic |
|--------|-------------|------------|
| **Lock Timing** | Acquire lock before read | Check at write time |
| **Conflict Rate** | High contention | Low contention |
| **Use Case** | Critical operations | High frequency ops |
| **Performance** | Slower (locks held) | Faster (no locks) |
| **Example** | Spot allocation | Spot status updates |

---

## 4. Database Optimization

### 4.1 Indexing Strategy

```sql
-- PRIMARY INDEXES (High Priority)
CREATE INDEX idx_available ON parking_space(is_available);
CREATE INDEX idx_available_spots ON parking_space(
    is_available, vehicle_type_id, floor_id
);

-- SECONDARY INDEXES
CREATE INDEX idx_floor_available ON parking_space(floor_id, is_available);
CREATE INDEX idx_vehicle_type ON parking_space(vehicle_type_id);

-- FOREIGN KEY INDEXES
CREATE INDEX idx_lot_id ON floor(lot_id);
CREATE INDEX idx_ticket_vehicle ON parking_ticket(vehicle_id);

-- TIME-SERIES INDEXES
CREATE INDEX idx_exit_time ON parking_transaction(exit_time);
CREATE INDEX idx_transaction_date ON parking_transaction(created_date);
```

**Query Execution Plan:**

```
Query: Find available spot for CAR on Floor 1

SELECT * FROM parking_space 
WHERE is_available = true 
AND vehicle_type_id = 2 
AND floor_id = 1
LIMIT 1;

Execution Plan:
├─ Using index: idx_available_spots
├─ Index range scan: (is_available=true, vehicle_type_id=2, floor_id=1)
├─ Estimated rows: ~2
├─ Cost: ~0.35
└─ Status: ✓ EXCELLENT (Uses composite index)
```

### 4.2 Query Performance Metrics

```
Query Type                  Avg Response Time   Index Used
──────────────────────────────────────────────────────────
Find available spot         < 5ms              idx_available_spots
Get occupancy rate          < 10ms             idx_available
Calculate fee               < 50ms             idx_ticket_vehicle
Daily revenue report        < 200ms            idx_exit_time
Occupancy by floor          < 15ms             idx_floor_available
```

---

## 5. API Design

### 5.1 RESTful Endpoints

```
┌─ PARKING OPERATIONS ────────────────────────────────────┐
│                                                          │
│ POST /api/v1/parking/entry                             │
│ ├─ Request:  { vehicleId, preferredFloorId }           │
│ ├─ Response: { ticketId, spotNumber, entryTime }       │
│ └─ Status:   201 Created / 409 No Spot Available       │
│                                                          │
│ POST /api/v1/parking/exit                              │
│ ├─ Request:  { ticketId, paymentMethod }               │
│ ├─ Response: { transactionId, totalFare, breakdown }   │
│ └─ Status:   200 OK / 404 Not Found                    │
│                                                          │
│ GET /api/v1/parking/availability                       │
│ ├─ Query:    ?floorId=1&vehicleType=CAR                │
│ ├─ Response: { totalSpots, available, byFloor: [] }    │
│ └─ Status:   200 OK                                    │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 5.2 Error Handling

```json
// 409 Conflict - No Spots Available
{
  "error": "NO_AVAILABLE_SPOT",
  "message": "No available parking spots for CAR type",
  "timestamp": "2026-01-16T10:30:00Z",
  "availableAlternatives": [
    { "type": "SUV", "spots": 5 },
    { "type": "BUS", "spots": 2 }
  ]
}

// 400 Bad Request - Invalid Input
{
  "error": "INVALID_REQUEST",
  "message": "Exit time must be after entry time",
  "field": "exitTime",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

---

## 6. Performance Considerations

### 6.1 Throughput Targets

```
Operation               Peak TPS    Avg Response Time  P99 Latency
────────────────────────────────────────────────────────────────
Vehicle Entry           100         95ms               150ms
Vehicle Exit            80          120ms              200ms
Availability Query      500         10ms               30ms
Fee Calculation         150         45ms               100ms
Occupancy Report        10          180ms              300ms
```

### 6.2 Caching Strategy

```
Data                    Cache TTL   Size Estimate   Invalidation
──────────────────────────────────────────────────────────────
Occupancy Summary       30 sec      ~500 bytes      On entry/exit
Rate Card               24 hours    ~2 KB           Manual update
Floor Summary           60 sec      ~5 KB           On entry/exit
User Profile            1 hour      ~10 KB          On update
```

---

## 7. Testing Strategy

### 7.1 Unit Tests

```bash
# Fee Calculation Tests
- Test hourly billing
- Test daily billing
- Test occupancy multiplier (5 scenarios)
- Test subscriber discount
- Test minimum charge enforcement

# Spot Allocation Tests
- Test available spot selection
- Test floor preference
- Test concurrent allocation (race condition)

# Concurrency Tests
- Test pessimistic locking
- Test transaction rollback
- Test concurrent reads (allowed)
- Test concurrent writes (blocked)
```

### 7.2 Integration Tests

```bash
# End-to-End Flow
- Vehicle registration -> Entry -> Exit -> Payment
- Fee calculation with different occupancy rates
- Concurrent vehicle entry/exit
- Database transaction consistency
```

### 7.3 Load Tests

```bash
# Performance Testing
- 1000 concurrent vehicle entries
- 500 concurrent fee calculations
- Database connection pool optimization
```

---

## 8. Deployment Considerations

### 8.1 Infrastructure Requirements

```
Component           Min Spec        Recommended    High Throughput
──────────────────────────────────────────────────────────────────
Application Server  2GB RAM         4GB RAM        8GB RAM
Database Server     4GB RAM         8GB RAM        16GB RAM
Connections/Pool    20              50             100+
Thread Pool         10              50             100+
Cache Server        512MB           1GB            2GB
```

### 8.2 Monitoring & Alerts

```
Metric                          Threshold       Action
──────────────────────────────────────────────────────────
Availability < Threshold        < 5 spots       Alert operators
Response Time (P95)             > 200ms         Investigate DB
Exception Rate                  > 1%            Alert team
Database Connection Pool        > 80%           Scale up
Cache Hit Ratio                 < 70%           Tune cache
```

---

## 9. Security Measures

1. **Input Validation**: All inputs validated against whitelist
2. **SQL Injection Prevention**: Parameterized queries only
3. **Rate Limiting**: 100 requests/min per IP
4. **Authentication**: JWT token-based
5. **Authorization**: Role-based access control (RBAC)
6. **Data Encryption**: TLS 1.3 for transit, AES-256 for sensitive data
7. **Audit Logging**: All transactions logged

---

## 10. Future Enhancements

1. **AI-Based Pricing**: Machine learning for demand prediction
2. **IoT Integration**: Real-time sensor data from parking spots
3. **Mobile App**: Native iOS/Android application
4. **License Plate Recognition**: Automatic vehicle identification
5. **Reservation System**: Pre-book parking spots
6. **Electric Vehicle Charging**: Charging station management
7. **Analytics Dashboard**: Real-time insights and reporting

---

**Document Version**: 1.0  
**Last Updated**: January 16, 2026  
**Author**: System Design Team
