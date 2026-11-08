import database.DBConnection;
import gui.LoginFrame;

import javax.swing.*;

/**
 * Main - Application Entry Point for Bus Ticket Booking Management System
 * 
 * Technology Stack:
 * - Java Swing GUI
 * - JDBC Driver: com.mysql.cj.jdbc.Driver (MySQL Connector/J)
 * - XAMPP MariaDB/MySQL Database (bus_ticketing)
 * - Three-tier Architecture: Presentation (GUI), Business Logic (Service), Persistence (DAO/Model)
 */
public class Main {

    public static void main(String[] args) {
        // Set modern native desktop Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Note: Using default Swing Look and Feel.");
        }

        // Print academic startup banner
        printStartupBanner();

        // Launch GUI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void printStartupBanner() {
        System.out.println("==================================================================");
        System.out.println("       BUS TICKET BOOKING MANAGEMENT SYSTEM - UNIVERSITY DBMS     ");
        System.out.println("==================================================================");
        System.out.println(" Programming Language : Java (JDK 21+)");
        System.out.println(" GUI Framework        : Java Swing Desktop");
        System.out.println(" Database             : XAMPP MariaDB / MySQL (bus_ticketing)");
        System.out.println(" Connectivity         : JDBC (com.mysql.cj.jdbc.Driver)");
        System.out.println(" Architecture         : Three-Tier (GUI -> Service -> DAO -> DB)");
        System.out.println(" Database URL         : " + DBConnection.getJdbcUrl());
        System.out.println(" Default Admin        : admin@busticket.com / admin123");
        System.out.println(" Default Customer     : rahim@gmail.com / rahim123");
        System.out.println("==================================================================");
    }
}

