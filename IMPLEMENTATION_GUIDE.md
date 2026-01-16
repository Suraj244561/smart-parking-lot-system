# Smart Parking Lot System - Implementation Guide

## Complete End-to-End Implementation Instructions

---

## 📋 Phase 1: Project Setup

### 1.1 Prerequisites

**System Requirements:**
- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6.3 or higher
- MySQL 5.7+ or PostgreSQL 12+
- Git version control
- IDE: IntelliJ IDEA or Eclipse (optional but recommended)
- Postman or similar API testing tool

**Installation Steps:**

```bash
# Verify Java installation
java -version
# Should show: java version "11" or higher

# Verify Maven installation  
mvn -version
# Should show: Maven 3.6.3 or higher

# Verify Git installation
git --version
# Should show: git version 2.x or higher
```

### 1.2 Clone Repository

```bash
# Clone the smart parking lot system repository
git clone https://github.com/Suraj244561/smart-parking-lot-system.git
cd smart-parking-lot-system

# Create feature branch for your implementation
git checkout -b feature/implementation
```

---

## 🗄️ Phase 2: Database Setup

### 2.1 MySQL Setup (Recommended)

**For Windows/Mac/Linux:**

```bash
# Option 1: Using MySQL Command Line

# Start MySQL service
# Windows:
net start MySQL80

# Mac:
brew services start mysql

# Linux:
sudo systemctl start mysql

# Connect to MySQL
mysql -u root -p
# Enter your root password

# Create database
CREATE DATABASE smart_parking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user with permissions
CREATE USER 'parking_user'@'localhost' IDENTIFIED BY 'parking_password_123';
GRANT ALL PRIVILEGES ON smart_parking_db.* TO 'parking_user'@'localhost';
FLUSH PRIVILEGES;

# Exit MySQL
EXIT;
```

**Option 2: Using MySQL Workbench**

1. Open MySQL Workbench
2. Create new MySQL connection with:
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: [your root password]
3. Create new schema: `smart_parking_db`
4. Execute `src/main/resources/schema.sql` script

### 2.2 PostgreSQL Setup (Alternative)

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE smart_parking_db;

# Create user
CREATE USER parking_user WITH PASSWORD 'parking_password_123';
GRANT ALL PRIVILEGES ON DATABASE smart_parking_db TO parking_user;

# Exit
\q

# Load schema
psql -U parking_user -d smart_parking_db -f src/main/resources/schema.sql
```

### 2.3 Verify Database Setup

```bash
# Connect and verify
mysql -u parking_user -p smart_parking_db -e "SELECT * FROM parking_lot;"

# Should return empty result set (tables created successfully)
```

---

## 🔧 Phase 3: Application Configuration

### 3.1 Configure Application Properties

**File:** `src/main/resources/application.properties`

```properties
# ==========================================
# Server Configuration
# ==========================================
server.port=8080
server.servlet.context-path=/parking-api
server.servlet.session.timeout=30m

# ==========================================
# Database Configuration - MySQL
# ==========================================
spring.datasource.url=jdbc:mysql://localhost:3306/smart_parking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=parking_user
spring.datasource.password=parking_password_123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ==========================================
# JPA/Hibernate Configuration
# ==========================================
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# ==========================================
# Connection Pool Configuration
# ==========================================
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000

# ==========================================
# Logging Configuration
# ==========================================
logging.level.root=INFO
logging.level.com.parking=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# ==========================================
# Application Configuration
# ==========================================
app.name=Smart Parking Lot Management System
app.version=1.0.0
app.timezone=Asia/Kolkata
```

**For PostgreSQL Configuration:**

```properties
# Replace MySQL config with:
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_parking_db
spring.datasource.username=parking_user
spring.datasource.password=parking_password_123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
```

### 3.2 Create Application Properties File

```bash
# Create the properties file if it doesn't exist
touch src/main/resources/application.properties

# Copy the above configuration to the file
```

---

## 🏗️ Phase 4: Build the Project

### 4.1 Maven Build

```bash
# Clean previous builds
mvn clean

# Compile the project
mvn compile

# Run unit tests
mvn test

# Build JAR file
mvn package

# Full build with all phases
mvn clean package -DskipTests
```

### 4.2 Verify Build

```bash
# Check if JAR was created
ls -la target/smart-parking-lot-system-1.0.0.jar

# Output should show the JAR file with size
```

---

## 🚀 Phase 5: Running the Application

### 5.1 Option 1: Using Maven

```bash
# Run Spring Boot application via Maven
mvn spring-boot:run

# Application should start and display:
# Started Application in X.XXX seconds
# Server is running on port 8080
```

### 5.2 Option 2: Using Java Command

```bash
# Run the JAR file directly
java -jar target/smart-parking-lot-system-1.0.0.jar

# Or with custom properties
java -jar target/smart-parking-lot-system-1.0.0.jar \
  --spring.datasource.password=parking_password_123 \
  --server.port=8080
```

### 5.3 Option 3: Using IDE

**IntelliJ IDEA:**
1. Open project in IntelliJ
2. Right-click on `ParkingLotApplication.java`
3. Select "Run 'ParkingLotApplication'" or press `Shift + F10`

**Eclipse:**
1. Right-click on project
2. Run As → Spring Boot App

### 5.4 Verify Application is Running

```bash
# Test the application health endpoint
curl http://localhost:8080/parking-api/health

# Expected response:
{"status":"UP"}

# Or using Postman:
# GET http://localhost:8080/parking-api/health
```

---

## 🧪 Phase 6: Testing the Application

### 6.1 API Testing with Postman

**Import Collection:**
1. Open Postman
2. Click "Import" → "Link"
3. Paste: `https://raw.githubusercontent.com/Suraj244561/smart-parking-lot-system/main/postman_collection.json`

**Manual Testing Steps:**

**1. Create Vehicle**
```http
POST http://localhost:8080/parking-api/api/v1/vehicles
Content-Type: application/json

{
  "licensePlate": "DL-01-AB-5555",
  "vehicleType": "CAR",
  "ownerName": "Rajesh Kumar",
  "ownerEmail": "rajesh@example.com",
  "ownerPhone": "9876543210"
}
```

**2. Vehicle Entry**
```http
POST http://localhost:8080/parking-api/api/v1/parking/entry
Content-Type: application/json

{
  "vehicleId": 1,
  "preferredFloorId": 1
}

Expected Response:
{
  "ticketId": "T001",
  "spotNumber": "F1-C1",
  "entryTime": "2026-01-16T10:30:00",
  "spotDetails": {...}
}
```

**3. Check Availability**
```http
GET http://localhost:8080/parking-api/api/v1/parking/availability?vehicleType=CAR&floorId=1

Expected Response:
{
  "totalSpots": 20,
  "availableSpots": 19,
  "byFloor": {...}
}
```

**4. Vehicle Exit & Fee Calculation**
```http
POST http://localhost:8080/parking-api/api/v1/parking/exit
Content-Type: application/json

{
  "ticketId": "T001",
  "paymentMethod": "CARD"
}

Expected Response:
{
  "transactionId": "TR001",
  "totalFare": 100.00,
  "feeBreakdown": {
    "baseFare": 100.00,
    "occupancyMultiplier": 1.0,
    "discountApplied": 0.00,
    "durationMinutes": 60
  }
}
```

### 6.2 Run Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FeeCalculationServiceTest

# Run with coverage
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### 6.3 Run Integration Tests

```bash
# Run integration tests
mvn verify

# Run specific integration test
mvn verify -Dtest=ParkingSystemIntegrationTest
```

---

## 📊 Phase 7: Monitoring & Verification

### 7.1 Database Verification

```sql
-- Check parking lot setup
SELECT * FROM parking_lot;
SELECT * FROM floor;
SELECT COUNT(*) as total_spots FROM parking_space;

-- Check data distribution
SELECT vehicle_type_id, COUNT(*) as spot_count 
FROM parking_space 
GROUP BY vehicle_type_id;

-- View occupancy summary
SELECT * FROM parking_occupancy_summary;
```

### 7.2 Application Logs

```bash
# View application logs in real-time
tail -f logs/parking-system.log

# Search for errors
grep "ERROR" logs/parking-system.log

# Search for specific service logs
grep "AllocationService" logs/parking-system.log
```

### 7.3 Performance Monitoring

```bash
# Monitor Java process
jps -l

# Check memory usage
jps -v | grep ParkingLotApplication

# Monitor database connections
mysql -u parking_user -p smart_parking_db \
  -e "SHOW PROCESSLIST; SHOW STATUS LIKE 'Threads%';"
```

---

## 🐛 Phase 8: Troubleshooting

### Common Issues and Solutions

**Issue 1: Database Connection Failed**
```
Error: java.sql.SQLException: Access denied for user 'parking_user'

Solution:
1. Verify database credentials in application.properties
2. Ensure database user exists: SELECT * FROM mysql.user;
3. Check user permissions: SHOW GRANTS FOR 'parking_user'@'localhost';
4. Verify database exists: SHOW DATABASES;
```

**Issue 2: Port Already in Use**
```
Error: Caused by: java.net.BindException: Address already in use

Solution:
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

**Issue 3: Optimistic Lock Exception During Concurrent Entry**
```
Error: org.springframework.orm.ObjectOptimisticLockingFailureException

Solution:
This is expected when concurrent vehicles try to book same spot.
The system will retry with exponential backoff (implement retry logic in controller).
```

---

## 📈 Phase 9: Production Deployment

### 9.1 Pre-Deployment Checklist

- [ ] All unit tests passing (mvn test)
- [ ] All integration tests passing (mvn verify)
- [ ] Database backup created
- [ ] Performance load tests completed
- [ ] Security audit completed
- [ ] Database indexes verified
- [ ] Connection pool configured correctly
- [ ] Logging configured for production
- [ ] Environment variables set
- [ ] Firewall rules configured

### 9.2 Build Production JAR

```bash
# Build optimized JAR
mvn clean package -DskipTests -P prod

# Verify JAR size
ls -lh target/smart-parking-lot-system-1.0.0.jar
```

### 9.3 Deploy to Server

```bash
# Copy to production server
scp target/smart-parking-lot-system-1.0.0.jar \
    user@production-server:/opt/parking/

# SSH to server
ssh user@production-server

# Navigate to application directory
cd /opt/parking

# Create systemd service file
sudo tee /etc/systemd/system/parking-system.service << EOF
[Unit]
Description=Smart Parking Lot System
After=network.target

[Service]
Type=simple
User=parking
WorkingDirectory=/opt/parking
ExecStart=/usr/bin/java -jar smart-parking-lot-system-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable parking-system
sudo systemctl start parking-system

# Check status
sudo systemctl status parking-system
```

---

## 📚 Additional Resources

- **System Design Document**: See `SYSTEM_DESIGN.md`
- **Database Schema**: See `src/main/resources/schema.sql`
- **API Documentation**: See `README.md`
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **JPA Documentation**: https://docs.oracle.com/cd/E19226-01/820-7627/
- **MySQL Documentation**: https://dev.mysql.com/doc/

---

## ✅ Verification Checklist

- [ ] Project cloned successfully
- [ ] Database created and populated
- [ ] Application properties configured
- [ ] Project builds without errors
- [ ] Application starts successfully
- [ ] Health check endpoint responds
- [ ] API endpoints tested with Postman
- [ ] Unit tests pass
- [ ] Database queries work correctly
- [ ] Logging configured properly
- [ ] Performance metrics within target

---

**For Questions or Issues**: Please raise an issue on GitHub repository.

**Last Updated**: January 16, 2026
