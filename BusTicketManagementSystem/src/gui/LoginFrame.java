package gui;

import database.DBConnection;
import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginFrame - Authentication & Portal Entry Screen
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Bus Ticket Booking Management System - User Login");
        setSize(480, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.BG_LIGHT);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UIUtils.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblTitle = new JLabel("BUS TICKET BOOKING SYSTEM", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("University DBMS Project • MariaDB / XAMPP", JLabel.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(147, 197, 253)); // Soft blue

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UIUtils.BG_LIGHT);
        formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // Form Title
        JLabel lblFormTitle = new JLabel("Sign In to Your Account");
        lblFormTitle.setFont(UIUtils.FONT_HEADER);
        lblFormTitle.setForeground(UIUtils.TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblFormTitle);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Email Label & Field
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UIUtils.FONT_SUBHEADER);
        lblEmail.setForeground(UIUtils.TEXT_DARK);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblEmail);
        formCard.add(Box.createRigidArea(new Dimension(0, 5)));

        txtEmail = UIUtils.createStyledTextField();
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(txtEmail);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // Password Label & Field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UIUtils.FONT_SUBHEADER);
        lblPassword.setForeground(UIUtils.TEXT_DARK);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblPassword);
        formCard.add(Box.createRigidArea(new Dimension(0, 5)));

        txtPassword = UIUtils.createStyledPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(txtPassword);
        formCard.add(Box.createRigidArea(new Dimension(0, 18)));

        // Login Button
        JButton btnLogin = UIUtils.createStyledButton("Sign In to Portal", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(this::handleLogin);
        formCard.add(btnLogin);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // Register Button
        JButton btnRegister = UIUtils.createStyledButton("Create New Customer Account", UIUtils.NAVY_HEADER, Color.WHITE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            this.setVisible(false);
        });
        formCard.add(btnRegister);

        // Status Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UIUtils.FONT_SMALL);
        lblStatus.setForeground(UIUtils.DANGER_RED);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(lblStatus);

        formContainer.add(formCard);
        add(formContainer, BorderLayout.CENTER);

        // Footer with Quick Fill buttons and DB Connection Tester
        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        footerPanel.setBackground(new Color(241, 245, 249));
        footerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JLabel lblQuickFill = new JLabel("Academic Demo 1-Click Credentials:", JLabel.CENTER);
        lblQuickFill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQuickFill.setForeground(UIUtils.TEXT_MUTED);
        footerPanel.add(lblQuickFill, BorderLayout.NORTH);

        JPanel quickButtonsPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        quickButtonsPanel.setBackground(new Color(241, 245, 249));

        JButton btnFillAdmin = new JButton("Admin (admin@busticket.com)");
        btnFillAdmin.setFont(UIUtils.FONT_SMALL);
        btnFillAdmin.addActionListener(e -> {
            txtEmail.setText("admin@busticket.com");
            txtPassword.setText("admin123");
            lblStatus.setText(" ");
        });

        JButton btnFillCustomer = new JButton("Customer (rahim@gmail.com)");
        btnFillCustomer.setFont(UIUtils.FONT_SMALL);
        btnFillCustomer.addActionListener(e -> {
            txtEmail.setText("rahim@gmail.com");
            txtPassword.setText("rahim123");
            lblStatus.setText(" ");
        });

        JButton btnTestDB = new JButton("Test XAMPP MySQL");
        btnTestDB.setFont(UIUtils.FONT_SMALL);
        btnTestDB.addActionListener(e -> testXamppDatabase());

        quickButtonsPanel.add(btnFillAdmin);
        quickButtonsPanel.add(btnFillCustomer);
        quickButtonsPanel.add(btnTestDB);

        footerPanel.add(quickButtonsPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void handleLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        try {
            User user = authService.login(email, password);
            lblStatus.setText(" ");
            
            // Navigate based on Role
            if (user.isAdmin()) {
                new AdminDashboard().setVisible(true);
            } else {
                new CustomerDashboard().setVisible(true);
            }
            this.dispose();

        } catch (Exception ex) {
            lblStatus.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testXamppDatabase() {
        boolean connected = DBConnection.testConnection();
        if (connected) {
            JOptionPane.showMessageDialog(this,
                    "SUCCESS: Connected to XAMPP MySQL Database 'bus_ticketing' on localhost:3306!\n"
                    + "All tables and credentials are ready.",
                    "Database Status: ONLINE",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "CONNECTION NOTICE:\n"
                    + "Could not reach MySQL on localhost:3306.\n\n"
                    + "Steps to activate:\n"
                    + "1. Open XAMPP Control Panel.\n"
                    + "2. Click 'Start' next to MySQL.\n"
                    + "3. Open phpMyAdmin (http://localhost/phpmyadmin).\n"
                    + "4. Import 'database/bus_ticketing.sql'.",
                    "Database Status: OFFLINE",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}

