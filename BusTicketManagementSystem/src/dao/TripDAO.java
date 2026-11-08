package dao;

import database.DBConnection;
import model.Trip;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDAO - Data Access Object for Scheduled Trips
 * Demonstrates: Complex Multi-Table INNER JOIN, Subqueries, Aggregate calculations
 */
public class TripDAO {

    public boolean scheduleTrip(Trip trip) {
        String sql = "INSERT INTO Trips (bus_id, route_id, departure_date, departure_time, ticket_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, trip.getBusId());
            stmt.setInt(2, trip.getRouteId());
            stmt.setDate(3, trip.getDepartureDate());
            stmt.setTime(4, trip.getDepartureTime());
            stmt.setDouble(5, trip.getTicketPrice());
            stmt.setString(6, trip.getStatus() != null ? trip.getStatus() : "SCHEDULED");

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        trip.setTripId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error scheduling trip: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTrip(Trip trip) {
        String sql = "UPDATE Trips SET bus_id = ?, route_id = ?, departure_date = ?, departure_time = ?, ticket_price = ?, status = ? WHERE trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trip.getBusId());
            stmt.setInt(2, trip.getRouteId());
            stmt.setDate(3, trip.getDepartureDate());
            stmt.setTime(4, trip.getDepartureTime());
            stmt.setDouble(5, trip.getTicketPrice());
            stmt.setString(6, trip.getStatus());
            stmt.setInt(7, trip.getTripId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating trip: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelTrip(int tripId) {
        String sql = "UPDATE Trips SET status = 'CANCELLED' WHERE trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tripId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling trip: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all trips using 3-table INNER JOIN across Trips, Buses, and Routes.
     * Computes available seats using correlated subquery.
     */
    public List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT t.trip_id, t.bus_id, t.route_id, t.departure_date, t.departure_time, t.ticket_price, t.status, "
                   + "       b.bus_number, b.bus_name, b.bus_type, b.total_seats, "
                   + "       r.departure_city, r.destination_city, r.estimated_duration, "
                   + "       (b.total_seats - (SELECT COUNT(*) FROM Bookings bk WHERE bk.trip_id = t.trip_id AND bk.status = 'CONFIRMED')) AS available_seats "
                   + "FROM Trips t "
                   + "INNER JOIN Buses b ON t.bus_id = b.bus_id "
                   + "INNER JOIN Routes r ON t.route_id = r.route_id "
                   + "ORDER BY t.departure_date ASC, t.departure_time ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapTrip(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all trips: " + e.getMessage());
        }
        return list;
    }

    /**
     * Customer Search: Searches available trips by departure, destination, and travel date.
     * Demonstrates: Parameterized INNER JOIN, Correlated Subquery, WHERE filtering.
     */
    public List<Trip> searchTrips(String departureCity, String destinationCity, Date travelDate) {
        List<Trip> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.trip_id, t.bus_id, t.route_id, t.departure_date, t.departure_time, t.ticket_price, t.status, ")
           .append("       b.bus_number, b.bus_name, b.bus_type, b.total_seats, ")
           .append("       r.departure_city, r.destination_city, r.estimated_duration, ")
           .append("       (b.total_seats - (SELECT COUNT(*) FROM Bookings bk WHERE bk.trip_id = t.trip_id AND bk.status = 'CONFIRMED')) AS available_seats ")
           .append("FROM Trips t ")
           .append("INNER JOIN Buses b ON t.bus_id = b.bus_id ")
           .append("INNER JOIN Routes r ON t.route_id = r.route_id ")
           .append("WHERE t.status = 'SCHEDULED' ");

        List<Object> params = new ArrayList<>();

        if (departureCity != null && !departureCity.trim().isEmpty() && !departureCity.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(r.departure_city) = LOWER(?) ");
            params.add(departureCity.trim());
        }

        if (destinationCity != null && !destinationCity.trim().isEmpty() && !destinationCity.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(r.destination_city) = LOWER(?) ");
            params.add(destinationCity.trim());
        }

        if (travelDate != null) {
            sql.append("AND t.departure_date = ? ");
            params.add(travelDate);
        } else {
            // Default to today and future trips
            sql.append("AND t.departure_date >= CURDATE() ");
        }

        sql.append("ORDER BY t.departure_date ASC, t.departure_time ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    stmt.setString(i + 1, (String) p);
                } else if (p instanceof Date) {
                    stmt.setDate(i + 1, (Date) p);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrip(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching trips: " + e.getMessage());
        }
        return list;
    }

    public Trip getTripById(int tripId) {
        String sql = "SELECT t.trip_id, t.bus_id, t.route_id, t.departure_date, t.departure_time, t.ticket_price, t.status, "
                   + "       b.bus_number, b.bus_name, b.bus_type, b.total_seats, "
                   + "       r.departure_city, r.destination_city, r.estimated_duration, "
                   + "       (b.total_seats - (SELECT COUNT(*) FROM Bookings bk WHERE bk.trip_id = t.trip_id AND bk.status = 'CONFIRMED')) AS available_seats "
                   + "FROM Trips t "
                   + "INNER JOIN Buses b ON t.bus_id = b.bus_id "
                   + "INNER JOIN Routes r ON t.route_id = r.route_id "
                   + "WHERE t.trip_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tripId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTrip(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trip by ID: " + e.getMessage());
        }
        return null;
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        Trip trip = new Trip();
        trip.setTripId(rs.getInt("trip_id"));
        trip.setBusId(rs.getInt("bus_id"));
        trip.setRouteId(rs.getInt("route_id"));
        trip.setDepartureDate(rs.getDate("departure_date"));
        trip.setDepartureTime(rs.getTime("departure_time"));
        trip.setTicketPrice(rs.getDouble("ticket_price"));
        trip.setStatus(rs.getString("status"));
        
        trip.setBusNumber(rs.getString("bus_number"));
        trip.setBusName(rs.getString("bus_name"));
        trip.setBusType(rs.getString("bus_type"));
        trip.setTotalSeats(rs.getInt("total_seats"));
        
        trip.setDepartureCity(rs.getString("departure_city"));
        trip.setDestinationCity(rs.getString("destination_city"));
        trip.setEstimatedDuration(rs.getString("estimated_duration"));
        trip.setAvailableSeatsCount(rs.getInt("available_seats"));
        return trip;
    }
}

