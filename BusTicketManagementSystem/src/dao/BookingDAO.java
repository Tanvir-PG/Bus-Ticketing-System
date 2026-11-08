package dao;

import database.DBConnection;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * BookingDAO - Data Access Object for Bookings and Payments
 * Demonstrates: ACID Transactions (COMMIT & ROLLBACK), Pessimistic Concurrency Check,
 * Multi-Table INNER JOINs, Aggregate Functions (COUNT, SUM).
 */
public class BookingDAO {

    /**
     * Books a seat using a strict JDBC Transaction.
     * Prevents double-booking race conditions by checking existing confirmed bookings
     * before inserting, all within a single atomic transaction block.
     * 
     * @return Booking object if successful, throws SQLException on conflict or failure
     */
    public Booking bookSeatWithTransaction(int tripId, int userId, int seatId, double fare, String paymentMethod) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 1. BEGIN TRANSACTION

            // 2. Concurrency Check: Check if seat is already booked for this trip
            String checkSql = "SELECT booking_id FROM Bookings "
                            + "WHERE trip_id = ? AND seat_id = ? AND status = 'CONFIRMED' FOR UPDATE";
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, tripId);
                checkStmt.setInt(2, seatId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Seat is already booked! Rollback transaction
                        conn.rollback();
                        throw new SQLException("DOUBLE BOOKING PREVENTED: This seat has just been booked by another passenger for this trip.");
                    }
                }
            }

            // 3. Generate unique Booking Reference (e.g. BK-2026-8492)
            String reference = "BK-" + java.time.Year.now().getValue() + "-" + (1000 + new Random().nextInt(9000));

            // 4. Insert into Bookings table
            String bookingSql = "INSERT INTO Bookings (booking_reference, trip_id, user_id, seat_id, total_fare, status) "
                              + "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
            
            int newBookingId = -1;
            try (PreparedStatement bStmt = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS)) {
                bStmt.setString(1, reference);
                bStmt.setInt(2, tripId);
                bStmt.setInt(3, userId);
                bStmt.setInt(4, seatId);
                bStmt.setDouble(5, fare);
                bStmt.executeUpdate();

                try (ResultSet rs = bStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newBookingId = rs.getInt(1);
                    }
                }
            }

            if (newBookingId <= 0) {
                conn.rollback();
                throw new SQLException("Failed to generate booking record. Transaction rolled back.");
            }

            // 5. Insert into Payments table (linked by foreign key booking_id)
            String paymentSql = "INSERT INTO Payments (booking_id, payment_method, amount_paid, payment_status) "
                              + "VALUES (?, ?, ?, 'PAID')";
            
            try (PreparedStatement pStmt = conn.prepareStatement(paymentSql)) {
                pStmt.setInt(1, newBookingId);
                pStmt.setString(2, paymentMethod != null ? paymentMethod : "CASH");
                pStmt.setDouble(3, fare);
                pStmt.executeUpdate();
            }

            // 6. All operations succeeded: COMMIT TRANSACTION
            conn.commit();

            // Fetch and return the complete digital ticket record
            return getBookingById(newBookingId);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK ON FAILURE
                    System.err.println("⚠️ Transaction rolled back successfully: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Cancels a booking within a transaction and marks payment as REFUNDED.
     */
    public boolean cancelBookingWithTransaction(int bookingId, int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            String updateBookingSql = "UPDATE Bookings SET status = 'CANCELLED' WHERE booking_id = ? AND user_id = ?";
            try (PreparedStatement bStmt = conn.prepareStatement(updateBookingSql)) {
                bStmt.setInt(1, bookingId);
                bStmt.setInt(2, userId);
                int affected = bStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String updatePaymentSql = "UPDATE Payments SET payment_status = 'REFUNDED' WHERE booking_id = ?";
            try (PreparedStatement pStmt = conn.prepareStatement(updatePaymentSql)) {
                pStmt.setInt(1, bookingId);
                pStmt.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
            return true;
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return false;
    }

    /**
     * Retrieves all bookings for a specific customer with complete ticket details.
     * Demonstrates 7-Table INNER JOIN across relational tables.
     */
    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT bk.booking_id, bk.booking_reference, bk.trip_id, bk.user_id, bk.seat_id, "
                   + "       bk.booking_date, bk.total_fare, bk.status, "
                   + "       u.full_name AS passenger_name, u.email AS passenger_email, u.phone AS passenger_phone, "
                   + "       b.bus_name, b.bus_number, b.bus_type, "
                   + "       s.seat_number, "
                   + "       r.departure_city, r.destination_city, "
                   + "       t.departure_date, t.departure_time, "
                   + "       p.payment_method, p.payment_status "
                   + "FROM Bookings bk "
                   + "INNER JOIN Users u ON bk.user_id = u.user_id "
                   + "INNER JOIN Trips t ON bk.trip_id = t.trip_id "
                   + "INNER JOIN Buses b ON t.bus_id = b.bus_id "
                   + "INNER JOIN Routes r ON t.route_id = r.route_id "
                   + "INNER JOIN Seats s ON bk.seat_id = s.seat_id "
                   + "LEFT JOIN Payments p ON bk.booking_id = p.booking_id "
                   + "WHERE bk.user_id = ? "
                   + "ORDER BY bk.booking_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer bookings: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all bookings for the Admin Dashboard.
     */
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT bk.booking_id, bk.booking_reference, bk.trip_id, bk.user_id, bk.seat_id, "
                   + "       bk.booking_date, bk.total_fare, bk.status, "
                   + "       u.full_name AS passenger_name, u.email AS passenger_email, u.phone AS passenger_phone, "
                   + "       b.bus_name, b.bus_number, b.bus_type, "
                   + "       s.seat_number, "
                   + "       r.departure_city, r.destination_city, "
                   + "       t.departure_date, t.departure_time, "
                   + "       p.payment_method, p.payment_status "
                   + "FROM Bookings bk "
                   + "INNER JOIN Users u ON bk.user_id = u.user_id "
                   + "INNER JOIN Trips t ON bk.trip_id = t.trip_id "
                   + "INNER JOIN Buses b ON t.bus_id = b.bus_id "
                   + "INNER JOIN Routes r ON t.route_id = r.route_id "
                   + "INNER JOIN Seats s ON bk.seat_id = s.seat_id "
                   + "LEFT JOIN Payments p ON bk.booking_id = p.booking_id "
                   + "ORDER BY bk.booking_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
        }
        return list;
    }

    public Booking getBookingById(int bookingId) {
        String sql = "SELECT bk.booking_id, bk.booking_reference, bk.trip_id, bk.user_id, bk.seat_id, "
                   + "       bk.booking_date, bk.total_fare, bk.status, "
                   + "       u.full_name AS passenger_name, u.email AS passenger_email, u.phone AS passenger_phone, "
                   + "       b.bus_name, b.bus_number, b.bus_type, "
                   + "       s.seat_number, "
                   + "       r.departure_city, r.destination_city, "
                   + "       t.departure_date, t.departure_time, "
                   + "       p.payment_method, p.payment_status "
                   + "FROM Bookings bk "
                   + "INNER JOIN Users u ON bk.user_id = u.user_id "
                   + "INNER JOIN Trips t ON bk.trip_id = t.trip_id "
                   + "INNER JOIN Buses b ON t.bus_id = b.bus_id "
                   + "INNER JOIN Routes r ON t.route_id = r.route_id "
                   + "INNER JOIN Seats s ON bk.seat_id = s.seat_id "
                   + "LEFT JOIN Payments p ON bk.booking_id = p.booking_id "
                   + "WHERE bk.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Demonstrates SQL Aggregate Functions: COUNT(), SUM(), GROUP BY / CASE
     */
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) AS total_bookings, "
                   + "       COALESCE(SUM(CASE WHEN status = 'CONFIRMED' THEN total_fare ELSE 0 END), 0) AS total_revenue, "
                   + "       COUNT(CASE WHEN status = 'CONFIRMED' THEN 1 END) AS confirmed_bookings, "
                   + "       COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) AS cancelled_bookings "
                   + "FROM Bookings";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("totalBookings", rs.getInt("total_bookings"));
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                stats.put("confirmedBookings", rs.getInt("confirmed_bookings"));
                stats.put("cancelledBookings", rs.getInt("cancelled_bookings"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking stats: " + e.getMessage());
        }
        return stats;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setTripId(rs.getInt("trip_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setSeatId(rs.getInt("seat_id"));
        b.setBookingDate(rs.getTimestamp("booking_date"));
        b.setTotalFare(rs.getDouble("total_fare"));
        b.setStatus(rs.getString("status"));

        b.setPassengerName(rs.getString("passenger_name"));
        b.setPassengerEmail(rs.getString("passenger_email"));
        b.setPassengerPhone(rs.getString("passenger_phone"));
        b.setBusName(rs.getString("bus_name"));
        b.setBusNumber(rs.getString("bus_number"));
        b.setBusType(rs.getString("bus_type"));
        b.setSeatNumber(rs.getString("seat_number"));
        b.setDepartureCity(rs.getString("departure_city"));
        b.setDestinationCity(rs.getString("destination_city"));
        b.setDepartureDate(rs.getDate("departure_date"));
        b.setDepartureTime(rs.getTime("departure_time"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setPaymentStatus(rs.getString("payment_status"));
        return b;
    }
}

