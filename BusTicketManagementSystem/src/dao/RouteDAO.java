package dao;

import database.DBConnection;
import model.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * RouteDAO - Data Access Object for Routes Table
 * Demonstrates: Distinct Select, Parameterized Filters, CRUD Operations
 */
public class RouteDAO {

    public boolean addRoute(Route route) {
        String sql = "INSERT INTO Routes (departure_city, destination_city, distance_km, estimated_duration) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, route.getDepartureCity().trim());
            stmt.setString(2, route.getDestinationCity().trim());
            stmt.setDouble(3, route.getDistanceKm());
            stmt.setString(4, route.getEstimatedDuration().trim());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        route.setRouteId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding route: " + e.getMessage());
        }
        return false;
    }

    public boolean updateRoute(Route route) {
        String sql = "UPDATE Routes SET departure_city = ?, destination_city = ?, distance_km = ?, estimated_duration = ? WHERE route_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, route.getDepartureCity().trim());
            stmt.setString(2, route.getDestinationCity().trim());
            stmt.setDouble(3, route.getDistanceKm());
            stmt.setString(4, route.getEstimatedDuration().trim());
            stmt.setInt(5, route.getRouteId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating route: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRoute(int routeId) {
        String sql = "DELETE FROM Routes WHERE route_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting route: " + e.getMessage());
        }
        return false;
    }

    public List<Route> getAllRoutes() {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT route_id, departure_city, destination_city, distance_km, estimated_duration, created_at FROM Routes ORDER BY departure_city ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Route r = new Route();
                r.setRouteId(rs.getInt("route_id"));
                r.setDepartureCity(rs.getString("departure_city"));
                r.setDestinationCity(rs.getString("destination_city"));
                r.setDistanceKm(rs.getDouble("distance_km"));
                r.setEstimatedDuration(rs.getString("estimated_duration"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching routes: " + e.getMessage());
        }
        return list;
    }

    /**
     * Demonstrates DISTINCT keyword in SQL.
     */
    public List<String> getDepartureCities() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT departure_city FROM Routes ORDER BY departure_city ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("departure_city"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departure cities: " + e.getMessage());
        }
        return list;
    }

    /**
     * Demonstrates Parameterized Filtering of Cities.
     */
    public List<String> getDestinationCities(String departureCity) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT destination_city FROM Routes WHERE departure_city = ? ORDER BY destination_city ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, departureCity);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("destination_city"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching destination cities: " + e.getMessage());
        }
        return list;
    }

    public int getRouteCount() {
        String sql = "SELECT COUNT(*) FROM Routes";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting routes: " + e.getMessage());
        }
        return 0;
    }
}

