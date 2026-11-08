package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection - Database Connection Manager for XAMPP MySQL/MariaDB
 * 
 * Provides centralized JDBC connection handling for the Bus Ticket Booking System.
 * Uses standard XAMPP configuration:
 *   Host: localhost
 *   Port: 3306
 *   Database: bus_ticketing
 *   Username: root
 *   Password: (empty)
 */
public class DBConnection {

    // JDBC Driver & Database Connection URL
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "bus_ticketing";
    
    // Connection URL with timezone and SSL configuration
    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    
    // Server URL used to create database if it doesn't exist yet
    private static final String SERVER_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Standard XAMPP default credentials
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Static initializer to load the MySQL JDBC driver
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found! Ensure mysql-connector-j.jar is in the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Establishes and returns a live connection to the bus_ticketing database.
     * 
     * @return java.sql.Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException ex) {
            // If database does not exist, try to auto-create database
            if (ex.getErrorCode() == 1049) { // Unknown database error code in MySQL
                try {
                    autoCreateDatabase();
                    return DriverManager.getConnection(URL, USER, PASSWORD);
                } catch (Exception creationEx) {
                    throw ex;
                }
            }
            throw ex;
        }
    }

    /**
     * Tests whether MySQL server in XAMPP is active and accessible.
     * 
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Safely closes a database connection.
     * 
     * @param conn the Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Helper to auto-create database if not yet imported via phpMyAdmin.
     */
    private static void autoCreateDatabase() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME 
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            System.out.println("✅ Database '" + DB_NAME + "' auto-created on local MySQL server.");
        } catch (SQLException e) {
            System.err.println("Could not auto-create database: " + e.getMessage());
        }
    }
}

