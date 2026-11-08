-- ============================================================================
-- BUS TICKET BOOKING MANAGEMENT SYSTEM
-- University DBMS Project Database Schema
-- Target Database: MariaDB / MySQL (XAMPP Environment)
-- Engine: InnoDB (Supports ACID Transactions, Foreign Keys & Constraints)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS bus_ticketing
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bus_ticketing;

-- ----------------------------------------------------------------------------
-- Drop tables in reverse foreign key dependency order for clean re-runs
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Seats;
DROP TABLE IF EXISTS Trips;
DROP TABLE IF EXISTS Routes;
DROP TABLE IF EXISTS Buses;
DROP TABLE IF EXISTS Users;

-- ============================================================================
-- 1. USERS TABLE (Admin & Customer Authentication)
-- ============================================================================
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 2. BUSES TABLE (Bus Fleet Information)
-- ============================================================================
CREATE TABLE Buses (
    bus_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_number VARCHAR(50) UNIQUE NOT NULL,
    bus_name VARCHAR(100) NOT NULL,
    bus_type ENUM('AC', 'NON_AC', 'SLEEPER', 'LUXURY') NOT NULL DEFAULT 'AC',
    total_seats INT NOT NULL DEFAULT 40,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_total_seats CHECK (total_seats > 0 AND total_seats <= 60)
) ENGINE=InnoDB;

-- ============================================================================
-- 3. ROUTES TABLE (City-to-City Route Details)
-- ============================================================================
CREATE TABLE Routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    departure_city VARCHAR(50) NOT NULL,
    destination_city VARCHAR(50) NOT NULL,
    distance_km DECIMAL(6, 2) NOT NULL,
    estimated_duration VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_distance CHECK (distance_km > 0),
    CONSTRAINT uq_route_pair UNIQUE (departure_city, destination_city)
) ENGINE=InnoDB;

-- ============================================================================
-- 4. TRIPS TABLE (Scheduled Bus Journeys with Pricing)
-- ============================================================================
CREATE TABLE Trips (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    route_id INT NOT NULL,
    departure_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL,
    status ENUM('SCHEDULED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ticket_price CHECK (ticket_price > 0),
    CONSTRAINT fk_trips_bus FOREIGN KEY (bus_id) REFERENCES Buses(bus_id) ON DELETE CASCADE,
    CONSTRAINT fk_trips_route FOREIGN KEY (route_id) REFERENCES Routes(route_id) ON DELETE CASCADE,
    CONSTRAINT uq_bus_schedule UNIQUE (bus_id, departure_date, departure_time)
) ENGINE=InnoDB;

-- ============================================================================
-- 5. SEATS TABLE (Individual Physical Seats per Bus)
-- Standard layout: 10 rows (A-J), 4 seats per row (1, 2 [Aisle] 3, 4) = 40 seats
-- ============================================================================
CREATE TABLE Seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_bus FOREIGN KEY (bus_id) REFERENCES Buses(bus_id) ON DELETE CASCADE,
    CONSTRAINT uq_bus_seat UNIQUE (bus_id, seat_number)
) ENGINE=InnoDB;

-- ============================================================================
-- 6. BOOKINGS TABLE (Customer Reservations with Unique Seat-Trip Constraint)
-- ============================================================================
CREATE TABLE Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) UNIQUE NOT NULL,
    trip_id INT NOT NULL,
    user_id INT NOT NULL,
    seat_id INT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_fare DECIMAL(10, 2) NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED') DEFAULT 'CONFIRMED',
    CONSTRAINT chk_total_fare CHECK (total_fare >= 0),
    CONSTRAINT fk_bookings_trip FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_seat FOREIGN KEY (seat_id) REFERENCES Seats(seat_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- 7. PAYMENTS TABLE (Transaction & Payment Details)
-- ============================================================================
CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'BKASH', 'NAGAD', 'ONLINE') DEFAULT 'CASH',
    amount_paid DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('PAID', 'REFUNDED') DEFAULT 'PAID',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_amount_paid CHECK (amount_paid >= 0),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================================================
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_trips_date ON Trips(departure_date);
CREATE INDEX idx_trips_route ON Trips(route_id);
CREATE INDEX idx_bookings_trip ON Bookings(trip_id);
CREATE INDEX idx_bookings_user ON Bookings(user_id);
CREATE INDEX idx_bookings_seat ON Bookings(seat_id);
CREATE INDEX idx_bookings_status ON Bookings(status);

-- ============================================================================
-- SAMPLE DATA INSERTION (FOR TESTING AND EVALUATION)
-- ============================================================================

-- 1. Insert Users (1 Admin + 3 Customers)
INSERT INTO Users (user_id, full_name, email, phone, password, role) VALUES
(1, 'System Administrator', 'admin@busticket.com', '+8801700000001', 'admin123', 'ADMIN'),
(2, 'Rahim Ahmed', 'rahim@gmail.com', '+8801711111111', 'rahim123', 'CUSTOMER'),
(3, 'Karim Ullah', 'karim@gmail.com', '+8801722222222', 'karim123', 'CUSTOMER'),
(4, 'Fatima Begum', 'fatima@gmail.com', '+8801733333333', 'fatima123', 'CUSTOMER');

-- 2. Insert Buses
INSERT INTO Buses (bus_id, bus_number, bus_name, bus_type, total_seats) VALUES
(1, 'BUS-101', 'GreenLine Super Deluxe VIP', 'LUXURY', 40),
(2, 'BUS-102', 'Shohagh Scania Business Class', 'AC', 40),
(3, 'BUS-103', 'Hanif Enterprise Hino 1J', 'NON_AC', 40),
(4, 'BUS-104', 'Ena Royal Sleeper Coach', 'SLEEPER', 40);

-- 3. Pre-populate 40 Seats for each bus (A1..A4 to J1..J4)
-- Helper procedure to populate standard 40-seat bus layout
DELIMITER $$
CREATE PROCEDURE PopulateSeatsForBuses()
BEGIN
    DECLARE b_id INT;
    DECLARE row_char CHAR(1);
    DECLARE seat_idx INT;
    DECLARE bus_cursor CURSOR FOR SELECT bus_id FROM Buses;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET b_id = NULL;

    OPEN bus_cursor;
    bus_loop: LOOP
        FETCH bus_cursor INTO b_id;
        IF b_id IS NULL THEN
            LEAVE bus_loop;
        END IF;

        -- 10 Rows (A through J), 4 seats each (1, 2, 3, 4)
        SET row_char = 'A';
        WHILE ASCII(row_char) <= ASCII('J') DO
            SET seat_idx = 1;
            WHILE seat_idx <= 4 DO
                INSERT IGNORE INTO Seats (bus_id, seat_number)
                VALUES (b_id, CONCAT(row_char, seat_idx));
                SET seat_idx = seat_idx + 1;
            END WHILE;
            SET row_char = CHAR(ASCII(row_char) + 1);
        END WHILE;

    END LOOP bus_loop;
    CLOSE bus_cursor;
END$$
DELIMITER ;

CALL PopulateSeatsForBuses();
DROP PROCEDURE IF EXISTS PopulateSeatsForBuses;

-- 4. Insert Routes
INSERT INTO Routes (route_id, departure_city, destination_city, distance_km, estimated_duration) VALUES
(1, 'Dhaka', 'Cox\'s Bazar', 398.50, '9 hrs 30 mins'),
(2, 'Dhaka', 'Chittagong', 246.00, '5 hrs 45 mins'),
(3, 'Dhaka', 'Sylhet', 242.00, '6 hrs 00 mins'),
(4, 'Chittagong', 'Sylhet', 375.00, '8 hrs 15 mins'),
(5, 'Dhaka', 'Rajshahi', 256.00, '5 hrs 30 mins');

-- 5. Insert Scheduled Trips (Using dynamic dates around current calendar period)
INSERT INTO Trips (trip_id, bus_id, route_id, departure_date, departure_time, ticket_price, status) VALUES
(1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', 1600.00, 'SCHEDULED'),
(2, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '22:30:00', 1800.00, 'SCHEDULED'),
(3, 2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 1200.00, 'SCHEDULED'),
(4, 3, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 750.00,  'SCHEDULED'),
(5, 4, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:00:00', 2200.00, 'SCHEDULED'),
(6, 2, 5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', 1100.00, 'SCHEDULED');

-- 6. Insert Sample Confirmed Bookings (Seats A1 and A2 booked on Trip 1)
-- Bus 1, Trip 1: Seat A1 (ID 1), Seat A2 (ID 2)
INSERT INTO Bookings (booking_id, booking_reference, trip_id, user_id, seat_id, total_fare, status) VALUES
(1, 'BK-2026-1001', 1, 2, 1, 1600.00, 'CONFIRMED'),
(2, 'BK-2026-1002', 1, 2, 2, 1600.00, 'CONFIRMED'),
(3, 'BK-2026-1003', 1, 3, 5, 1600.00, 'CONFIRMED'); -- Seat B1

-- 7. Insert Payments for Bookings
INSERT INTO Payments (payment_id, booking_id, payment_method, amount_paid, payment_status) VALUES
(1, 1, 'BKASH', 1600.00, 'PAID'),
(2, 2, 'BKASH', 1600.00, 'PAID'),
(3, 3, 'CARD',  1600.00, 'PAID');

