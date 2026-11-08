package dao;

import database.DBConnection;
import model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SeatDAO - Data Access Object for Seat Layout and Availability
 * Demonstrates: LEFT OUTER JOIN, Conditional CASE statements, ORDER BY
 */
public class SeatDAO {

    /**
     * Retrieves all physical seats for the bus assigned to this trip,
     * computing whether each seat is already BOOKED or AVAILABLE.
     * Uses LEFT JOIN with Bookings filtering on trip_id and status='CONFIRMED'.
     */
    public List<Seat> getSeatsForTrip(int tripId) {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT s.seat_id, s.bus_id, s.seat_number, "
                   + "       CASE WHEN bk.booking_id IS NOT NULL THEN 1 ELSE 0 END AS is_booked "
                   + "FROM Seats s "
                   + "INNER JOIN Trips t ON s.bus_id = t.bus_id "
                   + "LEFT JOIN Bookings bk ON s.seat_id = bk.seat_id "
                   + "                       AND bk.trip_id = t.trip_id "
                   + "                       AND bk.status = 'CONFIRMED' "
                   + "WHERE t.trip_id = ? "
                   + "ORDER BY LENGTH(s.seat_number) ASC, s.seat_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tripId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setSeatId(rs.getInt("seat_id"));
                    seat.setBusId(rs.getInt("bus_id"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setBooked(rs.getInt("is_booked") == 1);
                    list.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seats for trip: " + e.getMessage());
        }
        return list;
    }

    /**
     * Checks if a specific seat on a trip is currently free.
     * Can be invoked with an existing transactional Connection.
     */
    public boolean isSeatAvailable(int tripId, int seatId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE trip_id = ? AND seat_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tripId);
            stmt.setInt(2, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }
}

