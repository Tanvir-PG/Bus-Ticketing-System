# Entity-Relationship (ER) Diagram & Relational Schema
## Bus Ticket Booking Management System (DBMS Project)

### 1. Architectural Overview & Normalization
The database `bus_ticketing` is designed and normalized up to **Third Normal Form (3NF)** to eliminate redundancy, avoid update anomalies, and enforce strict relational integrity.

- **1NF Compliance:** All attributes contain atomic values. Repeated groups are eliminated.
- **2NF Compliance:** All non-key attributes are fully functionally dependent on the entire primary key.
- **3NF Compliance:** There are no transitive dependencies; non-key attributes depend only on candidate keys.

---

### 2. Mermaid Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "places"
    BUSES ||--|{ SEATS : "contains"
    BUSES ||--o{ TRIPS : "assigned_to"
    ROUTES ||--o{ TRIPS : "serviced_by"
    TRIPS ||--o{ BOOKINGS : "reserved_for"
    SEATS ||--o{ BOOKINGS : "allocated_to"
    BOOKINGS ||--|| PAYMENTS : "billed_by"

    USERS {
        int user_id PK "AUTO_INCREMENT"
        varchar full_name "NOT NULL"
        varchar email UK "NOT NULL, UNIQUE"
        varchar phone "NOT NULL"
        varchar password "NOT NULL"
        enum role "ADMIN, CUSTOMER"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    BUSES {
        int bus_id PK "AUTO_INCREMENT"
        varchar bus_number UK "NOT NULL, UNIQUE plate"
        varchar bus_name "NOT NULL operator"
        enum bus_type "AC, NON_AC, SLEEPER, LUXURY"
        int total_seats "DEFAULT 40, CHECK > 0"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    ROUTES {
        int route_id PK "AUTO_INCREMENT"
        varchar departure_city "NOT NULL"
        varchar destination_city "NOT NULL"
        decimal distance_km "CHECK > 0"
        varchar estimated_duration "NOT NULL"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    TRIPS {
        int trip_id PK "AUTO_INCREMENT"
        int bus_id FK "REFERENCES Buses(bus_id)"
        int route_id FK "REFERENCES Routes(route_id)"
        date departure_date "NOT NULL"
        time departure_time "NOT NULL"
        decimal ticket_price "CHECK >= 0"
        enum status "SCHEDULED, IN_TRANSIT, COMPLETED, CANCELLED"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    SEATS {
        int seat_id PK "AUTO_INCREMENT"
        int bus_id FK "REFERENCES Buses(bus_id)"
        varchar seat_number "NOT NULL, e.g. A1, A2"
    }

    BOOKINGS {
        int booking_id PK "AUTO_INCREMENT"
        varchar booking_reference UK "NOT NULL, UNIQUE"
        int trip_id FK "REFERENCES Trips(trip_id)"
        int user_id FK "REFERENCES Users(user_id)"
        int seat_id FK "REFERENCES Seats(seat_id)"
        timestamp booking_date "DEFAULT CURRENT_TIMESTAMP"
        decimal total_fare "CHECK >= 0"
        enum status "CONFIRMED, CANCELLED, PENDING"
    }

    PAYMENTS {
        int payment_id PK "AUTO_INCREMENT"
        int booking_id FK "REFERENCES Bookings(booking_id) UNIQUE"
        decimal amount "CHECK >= 0"
        enum payment_method "CASH, CREDIT_CARD, DEBIT_CARD, MOBILE_BANKING"
        enum payment_status "PAID, REFUNDED, FAILED"
        timestamp payment_date "DEFAULT CURRENT_TIMESTAMP"
    }
```

---

### 3. Detailed Relational Tables & Constraints

| Table Name | Primary Key | Foreign Keys | Key Constraints & Integrity Rules |
| :--- | :--- | :--- | :--- |
| **Users** | `user_id` | *None* | `UNIQUE(email)`, `CHECK(role IN ('ADMIN', 'CUSTOMER'))` |
| **Buses** | `bus_id` | *None* | `UNIQUE(bus_number)`, `CHECK(total_seats > 0)` |
| **Routes** | `route_id` | *None* | `CHECK(distance_km > 0)`, Composite unique logic for city pairs |
| **Seats** | `seat_id` | `bus_id` $\rightarrow$ `Buses(bus_id)` | `UNIQUE(bus_id, seat_number)`, `ON DELETE CASCADE` |
| **Trips** | `trip_id` | `bus_id` $\rightarrow$ `Buses(bus_id)`<br>`route_id` $\rightarrow$ `Routes(route_id)` | `ON DELETE CASCADE`, indexed by `departure_date` |
| **Bookings** | `booking_id` | `trip_id` $\rightarrow$ `Trips(trip_id)`<br>`user_id` $\rightarrow$ `Users(user_id)`<br>`seat_id` $\rightarrow$ `Seats(seat_id)` | `UNIQUE(booking_reference)`<br>**Double-Booking Prevention:** Application checks `WHERE trip_id = ? AND seat_id = ? AND status = 'CONFIRMED' FOR UPDATE` in a JDBC transaction. |
| **Payments** | `payment_id` | `booking_id` $\rightarrow$ `Bookings(booking_id)` | `UNIQUE(booking_id)` (1-to-1 Relationship), `ON DELETE CASCADE` |

---

### 4. Relational Cardinalities & Multiplicities

1. **Users to Bookings (1 : M):**
   - One registered customer can make zero or many reservations over time.
   - Each booking record belongs strictly to exactly one customer.

2. **Buses to Seats (1 : M):**
   - Each bus fleet contains exactly 40 seats (`A1` through `J4`).
   - Each physical seat belongs strictly to one bus.

3. **Buses to Trips (1 : M):**
   - One physical bus can be scheduled across multiple distinct trips over different dates and times.
   - Each trip is operated by exactly one designated bus.

4. **Routes to Trips (1 : M):**
   - One geographic route (e.g., Dhaka to Chittagong) can have multiple daily departures.
   - Each trip corresponds to exactly one origin-destination route.

5. **Trips to Bookings (1 : M):**
   - A single trip departure can have up to 40 passenger reservations.
   - Each booking is bound to a single scheduled trip.

6. **Bookings to Payments (1 : 1):**
   - Each confirmed booking maps to exactly one payment receipt.
   - The foreign key `Payments.booking_id` has a `UNIQUE` constraint enforcing strict one-to-one correspondence.

