-- ==========================================
-- Smart Parking Lot Management System
-- Database Schema
-- ==========================================

-- Drop existing tables if they exist (for clean slate)
DROP TABLE IF EXISTS parking_transaction;
DROP TABLE IF EXISTS parking_ticket;
DROP TABLE IF EXISTS parking_space;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS floor;
DROP TABLE IF EXISTS vehicle_type;
DROP TABLE IF EXISTS parking_lot;

-- ==========================================
-- PARKING LOT TABLE
-- ==========================================
CREATE TABLE parking_lot (
    lot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    total_floors INT NOT NULL,
    total_spots INT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- FLOOR TABLE
-- ==========================================
CREATE TABLE floor (
    floor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    total_spots_floor INT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lot_id) REFERENCES parking_lot(lot_id),
    UNIQUE KEY unique_floor_per_lot (lot_id, floor_number),
    INDEX idx_lot_id (lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- VEHICLE TYPE TABLE (REFERENCE DATA)
-- ==========================================
CREATE TABLE vehicle_type (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    spot_size VARCHAR(20) NOT NULL,
    base_rate_hourly DECIMAL(10, 2) NOT NULL,
    base_rate_daily DECIMAL(10, 2) NOT NULL,
    max_hours_daily INT DEFAULT 24
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert vehicle types with pricing
INSERT INTO vehicle_type (type_name, spot_size, base_rate_hourly, base_rate_daily, max_hours_daily) VALUES
('MOTORCYCLE', 'SMALL', 50.00, 300.00, 24),
('CAR', 'MEDIUM', 100.00, 800.00, 24),
('SUV', 'LARGE', 150.00, 1200.00, 24),
('BUS', 'XLARGE', 200.00, 1500.00, 24);

-- ==========================================
-- PARKING SPACE TABLE
-- ==========================================
CREATE TABLE parking_space (
    spot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    floor_id BIGINT NOT NULL,
    spot_number VARCHAR(10) NOT NULL,
    vehicle_type_id INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    current_vehicle_id BIGINT,
    entry_time TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (floor_id) REFERENCES floor(floor_id),
    FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_type(type_id),
    UNIQUE KEY unique_spot_per_floor (floor_id, spot_number),
    INDEX idx_floor_available (floor_id, is_available),
    INDEX idx_available (is_available),
    INDEX idx_vehicle_type (vehicle_type_id),
    INDEX idx_available_spots (is_available, vehicle_type_id, floor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- VEHICLE TABLE
-- ==========================================
CREATE TABLE vehicle (
    vehicle_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type_id INT NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    owner_email VARCHAR(100),
    owner_phone VARCHAR(20),
    registered_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_type(type_id),
    INDEX idx_license_plate (license_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- PARKING TICKET TABLE
-- ==========================================
CREATE TABLE parking_ticket (
    ticket_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spot_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
    fee_paid DECIMAL(10, 2),
    payment_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spot_id) REFERENCES parking_space(spot_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_spot_id (spot_id),
    INDEX idx_entry_time (entry_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- PARKING TRANSACTION TABLE
-- ==========================================
CREATE TABLE parking_transaction (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL UNIQUE,
    vehicle_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    duration_minutes INT,
    base_fare DECIMAL(10, 2) NOT NULL,
    total_fare DECIMAL(10, 2),
    discount_applied DECIMAL(10, 2) DEFAULT 0,
    payment_method ENUM('CASH', 'CARD', 'ONLINE', 'SUBSCRIPTION') DEFAULT 'CASH',
    payment_date TIMESTAMP,
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES parking_ticket(ticket_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_status (status),
    INDEX idx_payment_date (payment_date),
    INDEX idx_exit_time (exit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- SAMPLE DATA INSERTION
-- ==========================================

-- Insert sample parking lot
INSERT INTO parking_lot (name, total_floors, total_spots) VALUES
('Premium Downtown Parking', 4, 240),
('Airport Parking', 6, 400);

-- Insert floors for first lot
INSERT INTO floor (lot_id, floor_number, total_spots_floor) VALUES
(1, 1, 60),
(1, 2, 60),
(1, 3, 60),
(1, 4, 60);

-- Insert parking spaces for floor 1
-- 15 motorcycle spots, 20 car spots, 15 SUV spots, 10 bus spots
INSERT INTO parking_space (floor_id, spot_number, vehicle_type_id) VALUES
-- Motorcycle spots (F1: 1-15)
(1, 'F1-M1', 1), (1, 'F1-M2', 1), (1, 'F1-M3', 1), (1, 'F1-M4', 1), (1, 'F1-M5', 1),
(1, 'F1-M6', 1), (1, 'F1-M7', 1), (1, 'F1-M8', 1), (1, 'F1-M9', 1), (1, 'F1-M10', 1),
(1, 'F1-M11', 1), (1, 'F1-M12', 1), (1, 'F1-M13', 1), (1, 'F1-M14', 1), (1, 'F1-M15', 1),
-- Car spots (F1: C1-20)
(1, 'F1-C1', 2), (1, 'F1-C2', 2), (1, 'F1-C3', 2), (1, 'F1-C4', 2), (1, 'F1-C5', 2),
(1, 'F1-C6', 2), (1, 'F1-C7', 2), (1, 'F1-C8', 2), (1, 'F1-C9', 2), (1, 'F1-C10', 2),
(1, 'F1-C11', 2), (1, 'F1-C12', 2), (1, 'F1-C13', 2), (1, 'F1-C14', 2), (1, 'F1-C15', 2),
(1, 'F1-C16', 2), (1, 'F1-C17', 2), (1, 'F1-C18', 2), (1, 'F1-C19', 2), (1, 'F1-C20', 2),
-- SUV spots (F1: S1-15)
(1, 'F1-S1', 3), (1, 'F1-S2', 3), (1, 'F1-S3', 3), (1, 'F1-S4', 3), (1, 'F1-S5', 3),
(1, 'F1-S6', 3), (1, 'F1-S7', 3), (1, 'F1-S8', 3), (1, 'F1-S9', 3), (1, 'F1-S10', 3),
(1, 'F1-S11', 3), (1, 'F1-S12', 3), (1, 'F1-S13', 3), (1, 'F1-S14', 3), (1, 'F1-S15', 3),
-- Bus spots (F1: B1-10)
(1, 'F1-B1', 4), (1, 'F1-B2', 4), (1, 'F1-B3', 4), (1, 'F1-B4', 4), (1, 'F1-B5', 4),
(1, 'F1-B6', 4), (1, 'F1-B7', 4), (1, 'F1-B8', 4), (1, 'F1-B9', 4), (1, 'F1-B10', 4);

-- Insert sample vehicles
INSERT INTO vehicle (license_plate, vehicle_type_id, owner_name, owner_email, owner_phone) VALUES
('DL-01-AB-1234', 2, 'Rajesh Kumar', 'rajesh@email.com', '9876543210'),
('DL-02-CD-5678', 3, 'Priya Singh', 'priya@email.com', '9876543211'),
('DL-03-EF-9012', 2, 'Amit Patel', 'amit@email.com', '9876543212'),
('DL-04-GH-3456', 1, 'Neha Sharma', 'neha@email.com', '9876543213'),
('DL-05-IJ-7890', 4, 'Vijay Transport', 'vijay@transport.com', '9876543214');

-- ==========================================
-- VIEWS FOR REPORTING
-- ==========================================

-- Current parking occupancy summary
CREATE VIEW parking_occupancy_summary AS
SELECT 
    l.lot_id,
    l.name as lot_name,
    f.floor_number,
    vt.type_name,
    COUNT(ps.spot_id) as total_spots,
    SUM(CASE WHEN ps.is_available = 0 THEN 1 ELSE 0 END) as occupied_spots,
    SUM(CASE WHEN ps.is_available = 1 THEN 1 ELSE 0 END) as available_spots,
    ROUND(SUM(CASE WHEN ps.is_available = 0 THEN 1 ELSE 0 END) / COUNT(ps.spot_id) * 100, 2) as occupancy_percentage
FROM parking_lot l
JOIN floor f ON l.lot_id = f.lot_id
JOIN parking_space ps ON f.floor_id = ps.floor_id
JOIN vehicle_type vt ON ps.vehicle_type_id = vt.type_id
GROUP BY l.lot_id, l.name, f.floor_number, vt.type_name
ORDER BY l.lot_id, f.floor_number, vt.type_id;

-- Daily revenue report
CREATE VIEW daily_revenue_report AS
SELECT 
    DATE(pt.created_date) as transaction_date,
    COUNT(pt.transaction_id) as total_transactions,
    SUM(pt.base_fare) as total_base_fare,
    SUM(pt.discount_applied) as total_discounts,
    SUM(pt.total_fare) as total_revenue,
    AVG(pt.total_fare) as average_fee,
    MIN(pt.total_fare) as minimum_fee,
    MAX(pt.total_fare) as maximum_fee
FROM parking_transaction pt
WHERE pt.status = 'COMPLETED'
GROUP BY DATE(pt.created_date)
ORDER BY transaction_date DESC;

-- Vehicle type revenue
CREATE VIEW vehicle_type_revenue AS
SELECT 
    vt.type_name,
    COUNT(pt.transaction_id) as total_parkings,
    SUM(pt.duration_minutes) as total_minutes_parked,
    ROUND(SUM(pt.duration_minutes) / 60, 2) as total_hours_parked,
    SUM(pt.base_fare) as total_base_fare,
    SUM(pt.discount_applied) as total_discounts,
    SUM(pt.total_fare) as total_revenue,
    ROUND(AVG(pt.total_fare), 2) as average_fee
FROM parking_transaction pt
JOIN vehicle v ON pt.vehicle_id = v.vehicle_id
JOIN vehicle_type vt ON v.vehicle_type_id = vt.type_id
WHERE pt.status = 'COMPLETED'
GROUP BY vt.type_name
ORDER BY total_revenue DESC;
