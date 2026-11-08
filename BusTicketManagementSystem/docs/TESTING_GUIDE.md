# Comprehensive Testing & Verification Guide
## Bus Ticket Booking Management System (DBMS Project)

This document provides a systematic test suite covering all 10 academic testing scenarios required for demonstration and evaluation.

---

### Test Case Matrix

| Test ID | Test Scenario | Expected Outcome | Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Customer Registration & Form Validation | Creates new customer record; rejects duplicate emails and blank fields | **PASS** |
| **TC-02** | Customer Authentication & Role Redirection | Signs in valid customer; routes to Customer Dashboard | **PASS** |
| **TC-03** | Administrator Login & Role Enforcement | Signs in admin; routes to Admin Dashboard with full fleet controls | **PASS** |
| **TC-04** | Bus Fleet Addition & Auto-Seat Generation | Inserts Bus record and automatically generates 40 seats (`A1` - `J4`) | **PASS** |
| **TC-05** | Route Management (CRUD) | Adds, updates, and deletes city routes; validates positive distance | **PASS** |
| **TC-06** | Trip Scheduling & Fare Assignment | Schedules departure trip; links Bus and Route foreign keys | **PASS** |
| **TC-07** | Trip Searching & Real-Time Availability | Filters trips by departure/destination/date; calculates available seats | **PASS** |
| **TC-08** | Visual Seat Selection & Booking Transaction | Colors seats (Green/Red/Blue); creates Booking and Payment records | **PASS** |
| **TC-09** | Double-Booking Prevention | Blocks simultaneous booking of same seat; triggers transaction `ROLLBACK` | **PASS** |
| **TC-10** | Digital Ticket Generation & Cancellation | Displays formatted receipt, downloads `.txt` file, releases seat on cancel | **PASS** |

---

### Step-by-Step Test Execution Instructions

#### TC-01: Customer Registration & Validation
1. Launch application via `run.bat`.
2. Click **Create New Customer Account**.
3. Attempt submitting blank fields $\rightarrow$ Verify error: *"Please fill in all required fields."*
4. Attempt registering existing email `rahim@gmail.com` $\rightarrow$ Verify error: *"This email address is already registered."*
5. Fill valid details:
   - Name: `Tariq Hassan`
   - Email: `tariq@test.com`
   - Phone: `+8801700000000`
   - Password: `password123`
6. Click **Complete Registration** $\rightarrow$ Verify success banner and redirection to Login.

#### TC-02: Customer Authentication
1. On Login screen, click `Customer (rahim@gmail.com)`.
2. Click **Sign In to Portal**.
3. Verify Customer Dashboard opens showing:
   - Header with *"Welcome, Rahim Ahmed"*
   - Search filter bar
   - Available Trips table

#### TC-03: Administrator Login
1. On Login screen, click `Admin (admin@busticket.com)`.
2. Click **Sign In to Portal**.
3. Verify Admin Console opens showing:
   - 6 Tabs: *Overview & Statistics, Bus Management, Route Management, Trip Scheduling, All Bookings, Registered Customers*.
   - Live Aggregate statistics cards (Total Reservations, Revenue, Fleet, Routes, Customers).

#### TC-04: Bus Addition with Auto-Seat Batch
1. In Admin Console, switch to **Bus Management** tab.
2. Enter:
   - Plate: `DHAKA-METRO-BA-9999`
   - Operator: `Green Line Paribahan`
   - Category: `LUXURY`
   - Total Seats: `40`
3. Click **Save Bus**.
4. Verify success dialog: *"Seats A1 to J4 automatically generated in database."*
5. Verify new bus appears in the table with 40 seats.

#### TC-05: Route Management
1. Switch to **Route Management** tab.
2. Enter:
   - Departure: `Dhaka`
   - Destination: `Cox's Bazar`
   - Distance: `395`
   - Duration: `8 hrs 30 mins`
3. Click **Save Route**.
4. Verify route appears in table and is immediately available in dropdowns.

#### TC-06: Trip Scheduling
1. Switch to **Trip Scheduling** tab.
2. Select newly added Bus and Route.
3. Set Departure Date (e.g., `2026-11-15`), Time (`09:00:00`), Fare (`35.00`).
4. Click **Schedule Trip**.
5. Verify trip appears in table with `40 / 40` available seats.

#### TC-07: Trip Search & Available Seat Subquery
1. Log in as Customer (`rahim@gmail.com`).
2. Select Departure City: `Dhaka` and Destination: `Chittagong`.
3. Click **Search Trips**.
4. Verify correlated subquery accurately displays available seat count.

#### TC-08: Visual Interactive Seat Selection
1. Select a trip in the table and click **Select Seat & Book Ticket**.
2. The visual bus layout opens:
   - **Green seats:** Available for reservation.
   - **Red seats:** Already booked by other passengers.
3. Click seat `A3` $\rightarrow$ Button turns **Blue** (Selected).
4. Select Payment Method: `MOBILE_BANKING`.
5. Click **Confirm & Pay**.
6. Verify booking confirmation dialog appears.

#### TC-09: Double-Booking Prevention (ACID Verification)
1. Verify that seat `A3` immediately turns **Red** and is disabled.
2. If another concurrent session or transaction attempts inserting a booking for the same `(trip_id, seat_id)`, the transaction checks `status = 'CONFIRMED' FOR UPDATE`, aborts with an exception, and issues `connection.rollback()`.

#### TC-10: Digital Ticket Display, Download & Cancellation
1. Upon booking completion, the **Digital Ticket Dialog** opens automatically:
   - Displays formatted monospace digital receipt with Booking Reference, Route, Bus Operator, Seat Number, Total Fare, and Boarding instructions.
2. Click **Save / Download Ticket (.txt)**:
   - Choose save directory via file chooser $\rightarrow$ Verify file `Ticket_BK-2026-XXXX.txt` is created on disk.
3. Close the dialog and click **My Bookings & Tickets**:
   - Verify new reservation is listed with status `CONFIRMED`.
4. Click **Cancel Booking** and confirm:
   - Booking status updates to `CANCELLED`.
   - Payment status updates to `REFUNDED`.
   - Reopen seat layout for the trip: allocated seat has turned back to **Green** (Available).

