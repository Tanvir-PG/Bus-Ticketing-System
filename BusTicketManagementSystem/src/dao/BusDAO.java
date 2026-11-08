package dao;

import database.DBConnection;
import model.Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * BusDAO - Data Access Object for Buses and Physical Seats Generation
 * Demonstrates: PreparedStatement, Batch Operations, Auto-generated keys
 */
public class BusDAO {

    /**
     * Adds a new bus and automatically generates its standard 40 seats (A1-J4) in the Seats table.
     */
    public boolean addBus(Bus bus) {
        String insertBusSql = "INSERT INTO Buses (bus_number, bus_name, bus_type, total_seats) VALUES (?, ?, ?, ?)";
        String insertSeatSql = "INSERT INTO Seats (bus_id, seat_number) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int newBusId = -1;
            try (PreparedStatement busStmt = conn.prepareStatement(insertBusSql, Statement.RETURN_GENERATED_KEYS)) {
                busStmt.setString(1, bus.getBusNumber().trim().toUpperCase());
                busStmt.setString(2, bus.getBusName().trim());
                busStmt.setString(3, bus.getBusType());
                busStmt.setInt(4, bus.getTotalSeats());
                busStmt.executeUpdate();

                try (ResultSet rs = busStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newBusId = rs.getInt(1);
                        bus.setBusId(newBusId);
                    }
                }
            }

            if (newBusId <= 0) {
                conn.rollback();
                return false;
            }

            // Generate 40 seats (Rows A-J, Columns 1-4)
            try (PreparedStatement seatStmt = conn.prepareStatement(insertSeatSql)) {
                for (char row = 'A'; row <= 'J'; row++) {
                    for (int col = 1; col <= 4; col++) {
                        seatStmt.setInt(1, newBusId);
                        seatStmt.setString(2, "" + row + col);
                        seatStmt.addBatch();
                    }
                }
                seatStmt.executeBatch();
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding bus with seats: " + e.getMessage());
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
     * Updates an existing bus record.
     */
    public boolean updateBus(Bus bus) {
        String sql = "UPDATE Buses SET bus_number = ?, bus_name = ?, bus_type = ?, total_seats = ? WHERE bus_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bus.getBusNumber().trim().toUpperCase());
            stmt.setString(2, bus.getBusName().trim());
            stmt.setString(3, bus.getBusType());
            stmt.setInt(4, bus.getTotalSeats());
            stmt.setInt(5, bus.getBusId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bus: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a bus by ID (Foreign key cascades to Seats and Trips).
     */
    public boolean deleteBus(int busId) {
        String sql = "DELETE FROM Buses WHERE bus_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, busId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting bus: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all buses ordered by ID.
     */
    public List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT bus_id, bus_number, bus_name, bus_type, total_seats, created_at FROM Buses ORDER BY bus_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bus bus = new Bus();
                bus.setBusId(rs.getInt("bus_id"));
                bus.setBusNumber(rs.getString("bus_number"));
                bus.setBusName(rs.getString("bus_name"));
                bus.setBusType(rs.getString("bus_type"));
                bus.setTotalSeats(rs.getInt("total_seats"));
                bus.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching buses: " + e.getMessage());
        }
        return list;
    }

    public Bus getBusById(int busId) {
        String sql = "SELECT bus_id, bus_number, bus_name, bus_type, total_seats, created_at FROM Buses WHERE bus_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, busId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Bus bus = new Bus();
                    bus.setBusId(rs.getInt("bus_id"));
                    bus.setBusNumber(rs.getString("bus_number"));
                    bus.setBusName(rs.getString("bus_name"));
                    bus.setBusType(rs.getString("bus_type"));
                    bus.setTotalSeats(rs.getInt("total_seats"));
                    bus.setCreatedAt(rs.getTimestamp("created_at"));
                    return bus;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bus by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Demonstrates SQL Aggregate Function: COUNT()
     */
    public int getBusCount() {
        String sql = "SELECT COUNT(*) FROM Buses";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting buses: " + e.getMessage());
        }
        return 0;
    }
}

