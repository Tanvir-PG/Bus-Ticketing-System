# Bus Ticket Booking Management System
### University Academic DBMS Course Project

A complete, beginner-friendly desktop application developed in **Java (Java Swing)** and **XAMPP (MariaDB/MySQL)** with **JDBC (MySQL Connector/J)**. Built following a strict three-layer architecture to demonstrate practical Database Management System (DBMS) concepts, relational normalization, ACID transactions, and visual seat booking.

---

## 1. Technology Stack

- **Programming Language:** Java (JDK 17+ / JDK 21+ / JDK 26)
- **GUI Framework:** Java Swing (`javax.swing`, `java.awt`)
- **Database Server:** XAMPP (MariaDB / MySQL on `localhost:3306`)
- **Database Connectivity:** JDBC (`com.mysql.cj.jdbc.Driver`)
- **JDBC Driver:** Official MySQL Connector/J 8.3.0 (`lib/mysql-connector-j-8.3.0.jar`)
- **Database Administration:** phpMyAdmin (`http://localhost/phpmyadmin`)
- **Architecture:** Clean Three-Tier Architecture (Presentation $\rightarrow$ Business Logic $\rightarrow$ Persistence)

---

## 2. Key Academic DBMS Concepts Implemented

1. **Relational Schema Normalization (3NF):**
   - 7 normalized tables: `Users`, `Buses`, `Routes`, `Trips`, `Seats`, `Bookings`, `Payments`.
   - Primary Keys, Foreign Keys with `ON DELETE CASCADE`, `NOT NULL`, `UNIQUE`, and `CHECK` constraints.
2. **ACID Transactions (`COMMIT` & `ROLLBACK`):**
   - Implemented via JDBC `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`.
   - Used in booking creation (inserts booking + payment atomically) and fleet addition (inserts bus + generates 40 seats in batch).
3. **Double-Booking Prevention:**
   - Pessimistic locking check (`SELECT FOR UPDATE`) within the transaction to prevent race conditions.
4. **Advanced SQL Joins & Subqueries:**
   - Multi-table `INNER JOIN` (7-table join for digital boarding ticket details).
   - `LEFT OUTER JOIN` with `CASE WHEN` to determine physical seat status.
   - Correlated Subqueries calculating live remaining seats.
5. **SQL Injection Prevention:**
   - Strict use of `PreparedStatement` parameterized queries for all dynamic inputs.
6. **Aggregate Reporting:**
   - SQL functions `COUNT()`, `SUM()`, and conditional aggregation for dashboard statistics.

---

## 3. Core Modules & Features

### A. Authentication & User Management
- Administrator & Customer login.
- Customer account self-registration with duplicate email verification.
- Role-based dashboard redirection.
- 1-Click academic demo credential filler buttons on login screen.
- Real-time XAMPP MySQL database connection tester button.

### B. Admin Control Center (Tabbed Dashboard)
- **Overview & Statistics:** Metric cards for Total Bookings, Total Revenue, Fleet Count, Active Routes, Registered Customers.
- **Bus Fleet Management:** Add, update, delete buses. Adding a bus automatically generates its standard 40 physical seats (`A1` through `J4`) in a batch transaction.
- **Route Management:** Add, update, and remove routes with departure city, destination city, distance, and duration.
- **Trip Scheduling:** Assign bus and route to a travel date and time with ticket pricing and status.
- **All Bookings:** Comprehensive table of all system reservations with customer contact, seat, fare, status, and digital ticket inspector.
- **Registered Customers:** Customer roster with registration timestamps.

### C. Customer Portal & Interactive Booking
- **Trip Search:** Filter scheduled trips by departure city, destination city, and travel date.
- **Visual Bus Seat Matrix:**
  - 2x2 intercity coach layout with central aisle.
  - **Green:** Available seat (Click to select).
  - **Red:** Already booked by another passenger (Disabled).
  - **Blue:** Currently selected seat.
- **Payment Method Selection:** Cash, Credit Card, Debit Card, Mobile Banking.
- **Digital Boarding Pass / Ticket Receipt:**
  - Formatted monospace boarding pass viewable directly inside the application.
  - One-click **Download / Save Ticket (.txt)** using `JFileChooser`.
- **Booking History & Cancellation:** View personal reservations and cancel tickets with automatic refund status update.

---

## 4. Default Demo Credentials

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **System Admin** | Tanvir Admin | `admin@busticket.com` | `admin123` |
| **Customer** | Rahim Ahmed | `rahim@gmail.com` | `rahim123` |
| **Customer** | Fatima Begum | `fatima@gmail.com` | `fatima123` |
| **Customer** | Karim Khan | `karim@gmail.com` | `karim123` |

---

## 5. Directory Structure

```
BusTicketManagementSystem/
│
├── database/
│   └── bus_ticketing.sql          # Full MySQL DDL schema + realistic sample data
│
├── docs/
│   ├── ER_DIAGRAM.md              # Mermaid ER diagram, cardinalities & constraints
│   ├── XAMPP_SETUP_GUIDE.md       # Step-by-step XAMPP & phpMyAdmin setup guide
│   └── TESTING_GUIDE.md           # 10 comprehensive testing scenarios
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar # Official MySQL JDBC Driver
│
├── src/
│   ├── Main.java                  # Application entry point & L&F initialization
│   │
│   ├── database/
│   │   └── DBConnection.java      # JDBC connection provider & auto-create logic
│   │
│   ├── model/
│   │   ├── User.java              # User entity
│   │   ├── Bus.java               # Bus entity
│   │   ├── Route.java             # Route entity
│   │   ├── Trip.java              # Trip entity with joined display attributes
│   │   ├── Seat.java              # Seat entity with availability flag
│   │   └── Booking.java           # Booking & ticket details entity
│   │
│   ├── dao/
│   │   ├── UserDAO.java           # Authentication & customer queries
│   │   ├── BusDAO.java            # Fleet CRUD & 40-seat batch transaction
│   │   ├── RouteDAO.java          # Route CRUD & distinct city lookups
│   │   ├── TripDAO.java           # Trip queries, multi-table joins & subqueries
│   │   ├── SeatDAO.java           # Seat layout matrix & availability queries
│   │   └── BookingDAO.java        # ACID transactions, bookings & aggregate stats
│   │
│   ├── service/
│   │   ├── AuthService.java       # User authentication & session state
│   │   └── BookingService.java    # Reservation business logic & ticket formatting
│   │
│   └── gui/
│       ├── UIUtils.java           # Shared design system, colors & components
│       ├── LoginFrame.java        # Login screen with quick-fill & DB tester
│       ├── RegisterFrame.java     # Customer account registration
│       ├── AdminDashboard.java    # Tabbed administrative control console
│       ├── CustomerDashboard.java # Trip search & booking portal
│       ├── SeatSelectionFrame.java # Visual color-coded seat layout dialog
│       ├── TicketFrame.java       # Digital ticket display & .txt exporter
│       └── BookingHistoryFrame.java # Customer reservation history & cancellation
│
├── bin/                           # Compiled .class files
├── compile.bat                    # One-click Windows compilation script
├── run.bat                        # One-click application launch script
└── README.md                      # Project documentation
```

---

## 6. How to Run the Application

### Prerequisites
1. **Java Development Kit (JDK 17+)** installed on your computer.
2. **XAMPP** installed (typically at `C:\xampp`).

---

### Step-by-Step Instructions

#### 1. Start XAMPP MySQL
1. Open **XAMPP Control Panel**.
2. Click **Start** next to **Apache** and **MySQL**.
3. Open your browser to [http://localhost/phpmyadmin](http://localhost/phpmyadmin).
4. Click **Import**, choose `database/bus_ticketing.sql`, and click **Go**.

#### 2. Compile the Project
Open a command prompt in the project root and run:
```cmd
compile.bat
```
*(Or manually: `javac -cp "lib/mysql-connector-j-8.3.0.jar;src" -d bin src\Main.java src\database\DBConnection.java src\model\*.java src\dao\*.java src\service\*.java src\gui\*.java`)*

#### 3. Launch the Application
Run:
```cmd
run.bat
```
*(Or manually: `java -cp "bin;lib/mysql-connector-j-8.3.0.jar" Main`)*

#### 4. Sign In & Explore
- Click **Test XAMPP MySQL** to confirm database connectivity.
- Click **Admin** or **Customer** quick-fill buttons to test each role.

