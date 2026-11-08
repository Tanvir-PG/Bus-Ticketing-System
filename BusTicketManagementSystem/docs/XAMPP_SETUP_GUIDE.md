# XAMPP & phpMyAdmin Setup Guide
## Bus Ticket Booking Management System

This application is built for local deployment using **XAMPP (Apache + MariaDB/MySQL)** and **JDBC**. Follow these steps to configure your database server and import the schema.

---

### Step 1: Start XAMPP Server

1. Open the **XAMPP Control Panel** (`C:\xampp\xampp-control.exe`).
2. Locate the **Apache** and **MySQL** modules in the list.
3. Click the **Start** button next to **Apache** (lights up Green, Port 80, 443).
4. Click the **Start** button next to **MySQL** (lights up Green, Port 3306).

> [!TIP]
> If MySQL fails to start, ensure no other service (such as a standalone MySQL instance or Skype) is occupying port 3306.

---

### Step 2: Open phpMyAdmin

1. Open your web browser (Chrome, Edge, Firefox).
2. Navigate to: [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
3. You will see the phpMyAdmin dashboard.

---

### Step 3: Import the Project Database

There are two easy methods to import the database:

#### Method A: Import SQL File via phpMyAdmin (Recommended)
1. In phpMyAdmin, click on the **Import** tab on the top menu bar.
2. Under **File to import**, click **Choose File** (Browse).
3. Navigate to your project folder and select:
   ```
   C:\Users\Tanvir Ahmed\Desktop\BusTicketManagementSystem\database\bus_ticketing.sql
   ```
4. Scroll to the bottom and click the **Import** (or **Go**) button.
5. You should see a green success banner:
   *"Import has been successfully finished, queries executed."*
6. A database named `bus_ticketing` will appear in the left sidebar containing all 7 tables:
   - `Users`
   - `Buses`
   - `Routes`
   - `Trips`
   - `Seats`
   - `Bookings`
   - `Payments`

#### Method B: Execute via SQL Tab
1. Click the **SQL** tab in phpMyAdmin.
2. Open `bus_ticketing.sql` in any text editor (Notepad, VS Code).
3. Copy the entire contents and paste into the SQL query box.
4. Click **Go**.

---

### Step 4: Verify Default User Credentials

The database script populates academic sample records with initial credentials:

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **System Admin** | Tanvir Admin | `admin@busticket.com` | `admin123` |
| **Customer** | Rahim Ahmed | `rahim@gmail.com` | `rahim123` |
| **Customer** | Fatima Begum | `fatima@gmail.com` | `fatima123` |
| **Customer** | Karim Khan | `karim@gmail.com` | `karim123` |

---

### Step 5: Test Connection from Desktop Application

1. Double-click `run.bat` (or compile via `compile.bat`).
2. On the **Login Screen**, click the **Test XAMPP MySQL** button in the lower-right corner.
3. A dialog will confirm:
   `SUCCESS: Connected to XAMPP MySQL Database 'bus_ticketing' on localhost:3306!`
4. Use the 1-click credential buttons:
   - Click `Admin (admin@busticket.com)` to fill administrator credentials.
   - Click `Customer (rahim@gmail.com)` to fill customer credentials.

