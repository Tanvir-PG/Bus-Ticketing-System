package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object for Users Table
 * Demonstrates: PreparedStatement, Parameterized Queries, SQL Injection Prevention
 */
public class UserDAO {

    /**
     * Authenticates a user with email and password.
     * Uses PreparedStatement to prevent SQL injection.
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT user_id, full_name, email, phone, password, role, created_at "
                   + "FROM Users WHERE LOWER(email) = LOWER(?) AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            stmt.setString(2, password.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registers a new customer into the database.
     */
    public boolean registerCustomer(User user) {
        String sql = "INSERT INTO Users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, 'CUSTOMER')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPassword());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error registering customer: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if an email is already registered in the Users table.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all registered customers for the Admin Dashboard.
     */
    public List<User> getAllCustomers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, phone, role, created_at "
                   + "FROM Users WHERE role = 'CUSTOMER' ORDER BY user_id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Demonstrates SQL Aggregate Function: COUNT()
     */
    public int getCustomerCount() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'CUSTOMER'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting customers: " + e.getMessage());
        }
        return 0;
    }
}

