package gui;

import dao.BookingDAO;
import dao.BusDAO;
import dao.RouteDAO;
import dao.TripDAO;
import dao.UserDAO;
import model.Booking;
import model.Bus;
import model.Route;
import model.Trip;
import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboard - Comprehensive Administrative Management Console
 * 
 * Includes 6 Core Functional Modules:
 * 1. Overview & SQL Aggregate Statistics (COUNT, SUM, GROUP BY)
 * 2. Bus Fleet Management (CRUD + Automatic 40-seat batch generation)
 * 3. Route Management (CRUD + Distance & Duration)
 * 4. Trip Scheduling (Assign Bus, Route, Departure Date/Time, Fare)
 * 5. All Bookings Oversight & Digital Ticket Inspector
 * 6. Registered Customers Viewer
 */
public class AdminDashboard extends JFrame {

    private final BusDAO busDAO;
    private final RouteDAO routeDAO;
    private final TripDAO tripDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;

    // Stat labels
    private JLabel lblTotalBookings;
    private JLabel lblTotalRevenue;
    private JLabel lblTotalBuses;
    private JLabel lblTotalRoutes;
    private JLabel lblTotalCustomers;
    private JLabel lblConfirmedBookings;

    // Tables & Models
    private JTable busTable;
    private DefaultTableModel busModel;
    private JTable routeTable;
    private DefaultTableModel routeModel;
    private JTable tripTable;
    private DefaultTableModel tripModel;
    private JTable bookingTable;
    private DefaultTableModel bookingModel;
    private JTable customerTable;
    private DefaultTableModel customerModel;

    // Bus Form Fields
    private JTextField txtBusNumber;
    private JTextField txtBusName;
    private JComboBox<String> cmbBusType;
    private JTextField txtTotalSeats;
    private int selectedBusId = -1;

    // Route Form Fields
    private JTextField txtDepartureCity;
    private JTextField txtDestinationCity;
    private JTextField txtDistance;
    private JTextField txtDuration;
    private int selectedRouteId = -1;

    // Trip Form Fields
    private JComboBox<String> cmbTripBus;
    private JComboBox<String> cmbTripRoute;
    private JTextField txtTripDate;
    private JTextField txtTripTime;
    private JTextField txtTripFare;
    private JComboBox<String> cmbTripStatus;
    private List<Bus> busCache;
    private List<Route> routeCache;
    private List<Trip> tripCache;
    private List<Booking> bookingCache;

    public AdminDashboard() {
        super("Bus Ticket Booking Management System - Admin Control Center");
        this.busDAO = new BusDAO();
        this.routeDAO = new RouteDAO();
        this.tripDAO = new TripDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();

        initComponents();
        refreshAllData();

        setSize(1150, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIUtils.BG_LIGHT);

        // 1. Top Navbar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIUtils.NAVY_DARK);
        navBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        User currentUser = AuthService.getCurrentUser();
        String adminName = (currentUser != null ? currentUser.getFullName() : "Administrator");
        JLabel lblBrand = new JLabel("System Administration Console");
        lblBrand.setFont(UIUtils.FONT_TITLE);
        lblBrand.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel("Signed in as: " + adminName + " (ADMIN)");
        lblUser.setFont(UIUtils.FONT_REGULAR);
        lblUser.setForeground(new Color(148, 163, 184));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        brandPanel.add(lblBrand);
        brandPanel.add(lblUser);

        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        navActions.setOpaque(false);

        JButton btnRefreshAll = UIUtils.createStyledButton("Refresh All", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnRefreshAll.addActionListener(e -> refreshAllData());

        JButton btnLogout = UIUtils.createStyledButton("Logout", new Color(220, 38, 38), Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        navActions.add(btnRefreshAll);
        navActions.add(btnLogout);

        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(navActions, BorderLayout.EAST);
        mainPanel.add(navBar, BorderLayout.NORTH);

        // 2. Tabbed Management Center
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("  Overview & Statistics  ", createOverviewTab());
        tabbedPane.addTab("  Bus Management  ", createBusTab());
        tabbedPane.addTab("  Route Management  ", createRouteTab());
        tabbedPane.addTab("  Trip Scheduling  ", createTripTab());
        tabbedPane.addTab("  All Bookings  ", createBookingTab());
        tabbedPane.addTab("  Registered Customers  ", createCustomerTab());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ==========================================
    // TAB 1: OVERVIEW & STATISTICS
    // ==========================================
    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);

        lblTotalBookings = new JLabel("0");
        lblTotalRevenue = new JLabel("$0.00");
        lblConfirmedBookings = new JLabel("0");
        lblTotalBuses = new JLabel("0");
        lblTotalRoutes = new JLabel("0");
        lblTotalCustomers = new JLabel("0");

        statsGrid.add(UIUtils.createStatCard("Total Reservations", lblTotalBookings, UIUtils.PRIMARY_BLUE));
        statsGrid.add(UIUtils.createStatCard("Total Revenue", lblTotalRevenue, UIUtils.SUCCESS_GREEN));
        statsGrid.add(UIUtils.createStatCard("Confirmed Bookings", lblConfirmedBookings, new Color(13, 148, 136)));
        statsGrid.add(UIUtils.createStatCard("Fleet Size (Buses)", lblTotalBuses, UIUtils.WARNING_AMBER));
        statsGrid.add(UIUtils.createStatCard("Active Routes", lblTotalRoutes, new Color(147, 51, 234)));
        statsGrid.add(UIUtils.createStatCard("Registered Customers", lblTotalCustomers, new Color(79, 70, 229)));

        panel.add(statsGrid, BorderLayout.NORTH);

        // System Description & DBMS Highlights Card
        JPanel infoCard = new JPanel(new BorderLayout(10, 10));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel infoTitle = new JLabel("Database Architecture & Academic Highlights");
        infoTitle.setFont(UIUtils.FONT_HEADER);
        infoTitle.setForeground(UIUtils.NAVY_DARK);
        infoCard.add(infoTitle, BorderLayout.NORTH);

        JTextArea desc = new JTextArea();
        desc.setFont(UIUtils.FONT_REGULAR);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setText(
                "• Relational Schema: 7 Normalized 3NF Tables (Users, Buses, Routes, Trips, Seats, Bookings, Payments)\n"
                + "• Atomic Transactions: ACID guarantees with connection.setAutoCommit(false), commit(), and rollback()\n"
                + "• Double-Booking Prevention: Pessimistic locking (SELECT FOR UPDATE) and unique constraint checks\n"
                + "• Dynamic Queries: Correlated Subqueries calculate available seats in real-time\n"
                + "• Advanced SQL Joins: Multi-table INNER JOIN and LEFT JOIN across 7 entities\n"
                + "• Security: PreparedStatement parameterized SQL prevents SQL injection across all inputs"
        );
        infoCard.add(desc, BorderLayout.CENTER);

        panel.add(infoCard, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // TAB 2: BUS MANAGEMENT
    // ==========================================
    private JPanel createBusTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Left Form Panel
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setPreferredSize(new Dimension(300, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel formTitle = new JLabel("Add / Edit Bus");
        formTitle.setFont(UIUtils.FONT_HEADER);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(12, 0, 12, 0));

        txtBusNumber = UIUtils.createStyledTextField();
        txtBusName = UIUtils.createStyledTextField();
        cmbBusType = new JComboBox<>(new String[]{"AC", "NON_AC", "SLEEPER", "LUXURY"});
        cmbBusType.setFont(UIUtils.FONT_REGULAR);
        cmbBusType.setBackground(Color.WHITE);
        txtTotalSeats = UIUtils.createStyledTextField();
        txtTotalSeats.setText("40");

        addLabeledField(formFields, "Bus Reg Number (Plate)", txtBusNumber);
        addLabeledField(formFields, "Bus Operator / Name", txtBusName);
        addLabeledField(formFields, "Coach Category", cmbBusType);
        addLabeledField(formFields, "Total Seats (Default 40)", txtTotalSeats);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        btnPanel.setOpaque(false);

        JButton btnSaveBus = UIUtils.createStyledButton("Save Bus (Auto-Generates 40 Seats)", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnSaveBus.addActionListener(e -> saveBus());

        JButton btnDeleteBus = UIUtils.createStyledButton("Delete Selected Bus", UIUtils.DANGER_RED, Color.WHITE);
        btnDeleteBus.addActionListener(e -> deleteSelectedBus());

        JButton btnClearBus = UIUtils.createStyledButton("Clear Form", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnClearBus.addActionListener(e -> clearBusForm());

        btnPanel.add(btnSaveBus);
        btnPanel.add(btnDeleteBus);
        btnPanel.add(btnClearBus);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        panel.add(formCard, BorderLayout.WEST);

        // Right Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"Bus ID", "Plate Number", "Bus Operator", "Category", "Total Seats", "Registration Date"};
        busModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        busTable = new JTable(busModel);
        UIUtils.styleTable(busTable);
        busTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && busTable.getSelectedRow() >= 0) {
                populateBusForm(busTable.getSelectedRow());
            }
        });

        tableContainer.add(new JScrollPane(busTable), BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 3: ROUTE MANAGEMENT
    // ==========================================
    private JPanel createRouteTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Left Form Panel
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setPreferredSize(new Dimension(300, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel formTitle = new JLabel("Add / Edit Route");
        formTitle.setFont(UIUtils.FONT_HEADER);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(12, 0, 12, 0));

        txtDepartureCity = UIUtils.createStyledTextField();
        txtDestinationCity = UIUtils.createStyledTextField();
        txtDistance = UIUtils.createStyledTextField();
        txtDuration = UIUtils.createStyledTextField();

        addLabeledField(formFields, "Departure City", txtDepartureCity);
        addLabeledField(formFields, "Destination City", txtDestinationCity);
        addLabeledField(formFields, "Distance (km)", txtDistance);
        addLabeledField(formFields, "Estimated Duration (e.g. 5 hrs)", txtDuration);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        btnPanel.setOpaque(false);

        JButton btnSaveRoute = UIUtils.createStyledButton("Save Route", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnSaveRoute.addActionListener(e -> saveRoute());

        JButton btnDeleteRoute = UIUtils.createStyledButton("Delete Selected Route", UIUtils.DANGER_RED, Color.WHITE);
        btnDeleteRoute.addActionListener(e -> deleteSelectedRoute());

        JButton btnClearRoute = UIUtils.createStyledButton("Clear Form", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnClearRoute.addActionListener(e -> clearRouteForm());

        btnPanel.add(btnSaveRoute);
        btnPanel.add(btnDeleteRoute);
        btnPanel.add(btnClearRoute);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        panel.add(formCard, BorderLayout.WEST);

        // Right Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"Route ID", "Departure City", "Destination City", "Distance (km)", "Est. Duration", "Created Date"};
        routeModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        routeTable = new JTable(routeModel);
        UIUtils.styleTable(routeTable);
        routeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && routeTable.getSelectedRow() >= 0) {
                populateRouteForm(routeTable.getSelectedRow());
            }
        });

        tableContainer.add(new JScrollPane(routeTable), BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 4: TRIP SCHEDULING
    // ==========================================
    private JPanel createTripTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Left Form Panel
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setPreferredSize(new Dimension(320, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel formTitle = new JLabel("Schedule New Trip");
        formTitle.setFont(UIUtils.FONT_HEADER);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(12, 0, 12, 0));

        cmbTripBus = new JComboBox<>();
        cmbTripBus.setFont(UIUtils.FONT_REGULAR);
        cmbTripBus.setBackground(Color.WHITE);

        cmbTripRoute = new JComboBox<>();
        cmbTripRoute.setFont(UIUtils.FONT_REGULAR);
        cmbTripRoute.setBackground(Color.WHITE);

        txtTripDate = UIUtils.createStyledTextField();
        txtTripDate.setText(LocalDate.now().plusDays(1).toString());

        txtTripTime = UIUtils.createStyledTextField();
        txtTripTime.setText("08:00:00");

        txtTripFare = UIUtils.createStyledTextField();
        txtTripFare.setText("25.00");

        cmbTripStatus = new JComboBox<>(new String[]{"SCHEDULED", "CANCELLED", "COMPLETED"});
        cmbTripStatus.setFont(UIUtils.FONT_REGULAR);
        cmbTripStatus.setBackground(Color.WHITE);

        addLabeledField(formFields, "Select Bus Fleet", cmbTripBus);
        addLabeledField(formFields, "Select Route", cmbTripRoute);
        addLabeledField(formFields, "Departure Date (YYYY-MM-DD)", txtTripDate);
        addLabeledField(formFields, "Departure Time (HH:MM:SS)", txtTripTime);
        addLabeledField(formFields, "Ticket Fare ($)", txtTripFare);
        addLabeledField(formFields, "Status", cmbTripStatus);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        btnPanel.setOpaque(false);

        JButton btnScheduleTrip = UIUtils.createStyledButton("Schedule Trip", UIUtils.SUCCESS_GREEN, Color.WHITE);
        btnScheduleTrip.addActionListener(e -> scheduleNewTrip());

        JButton btnCancelTrip = UIUtils.createStyledButton("Cancel Selected Trip", UIUtils.DANGER_RED, Color.WHITE);
        btnCancelTrip.addActionListener(e -> cancelSelectedTrip());

        btnPanel.add(btnScheduleTrip);
        btnPanel.add(btnCancelTrip);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        panel.add(formCard, BorderLayout.WEST);

        // Right Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"Trip ID", "Bus Operator", "Route", "Departure Date", "Time", "Price", "Status", "Available Seats"};
        tripModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tripTable = new JTable(tripModel);
        UIUtils.styleTable(tripTable);

        tableContainer.add(new JScrollPane(tripTable), BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 5: ALL BOOKINGS
    // ==========================================
    private JPanel createBookingTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"Booking Ref", "Customer Name", "Phone", "Route", "Date & Time", "Bus & Type", "Seat", "Fare", "Status", "Payment"};
        bookingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        UIUtils.styleTable(bookingTable);

        tableContainer.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionPanel.setOpaque(false);

        JButton btnViewTicket = UIUtils.createStyledButton("View Digital Ticket Receipt", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnViewTicket.addActionListener(e -> viewAdminSelectedTicket());

        JButton btnRefreshBookings = UIUtils.createStyledButton("Refresh Bookings", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnRefreshBookings.addActionListener(e -> refreshBookings());

        actionPanel.add(btnRefreshBookings);
        actionPanel.add(btnViewTicket);
        tableContainer.add(actionPanel, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // TAB 6: CUSTOMERS
    // ==========================================
    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"Customer ID", "Full Name", "Email Address", "Phone Number", "Role", "Registered Date"};
        customerModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        customerTable = new JTable(customerModel);
        UIUtils.styleTable(customerTable);

        tableContainer.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // DATA REFRESHING & CRUD IMPLEMENTATIONS
    // ==========================================

    public void refreshAllData() {
        refreshStatistics();
        refreshBuses();
        refreshRoutes();
        refreshTrips();
        refreshBookings();
        refreshCustomers();
    }

    private void refreshStatistics() {
        Map<String, Object> stats = bookingDAO.getBookingStatistics();
        int totalBk = (Integer) stats.getOrDefault("totalBookings", 0);
        double totalRev = (Double) stats.getOrDefault("totalRevenue", 0.0);
        int confBk = (Integer) stats.getOrDefault("confirmedBookings", 0);

        lblTotalBookings.setText(String.valueOf(totalBk));
        lblTotalRevenue.setText(String.format("$%.2f", totalRev));
        lblConfirmedBookings.setText(String.valueOf(confBk));

        lblTotalBuses.setText(String.valueOf(busDAO.getAllBuses().size()));
        lblTotalRoutes.setText(String.valueOf(routeDAO.getRouteCount()));
        lblTotalCustomers.setText(String.valueOf(userDAO.getCustomerCount()));
    }

    private void refreshBuses() {
        busModel.setRowCount(0);
        busCache = busDAO.getAllBuses();
        for (Bus b : busCache) {
            busModel.addRow(new Object[]{
                    b.getBusId(),
                    b.getBusNumber(),
                    b.getBusName(),
                    b.getBusType(),
                    b.getTotalSeats(),
                    b.getCreatedAt()
            });
        }
        updateTripBusDropdown();
    }

    private void refreshRoutes() {
        routeModel.setRowCount(0);
        routeCache = routeDAO.getAllRoutes();
        for (Route r : routeCache) {
            routeModel.addRow(new Object[]{
                    r.getRouteId(),
                    r.getDepartureCity(),
                    r.getDestinationCity(),
                    r.getDistanceKm(),
                    r.getEstimatedDuration(),
                    r.getCreatedAt()
            });
        }
        updateTripRouteDropdown();
    }

    private void refreshTrips() {
        tripModel.setRowCount(0);
        tripCache = tripDAO.getAllTrips();
        for (Trip t : tripCache) {
            String route = t.getDepartureCity() + " ➔ " + t.getDestinationCity();
            tripModel.addRow(new Object[]{
                    t.getTripId(),
                    t.getBusName() + " (" + t.getBusNumber() + ")",
                    route,
                    t.getDepartureDate(),
                    t.getDepartureTime(),
                    String.format("$%.2f", t.getTicketPrice()),
                    t.getStatus(),
                    t.getAvailableSeats() + " / " + t.getTotalSeats()
            });
        }
    }

    private void refreshBookings() {
        bookingModel.setRowCount(0);
        bookingCache = bookingDAO.getAllBookings();
        for (Booking b : bookingCache) {
            String route = b.getDepartureCity() + " ➔ " + b.getDestinationCity();
            String dateTime = b.getDepartureDate() + " " + (b.getDepartureTime() != null ? b.getDepartureTime() : "");
            bookingModel.addRow(new Object[]{
                    b.getBookingReference(),
                    b.getPassengerName(),
                    b.getPassengerPhone(),
                    route,
                    dateTime,
                    b.getBusName() + " (" + b.getBusType() + ")",
                    b.getSeatNumber(),
                    String.format("$%.2f", b.getTotalFare()),
                    b.getStatus(),
                    b.getPaymentMethod() != null ? b.getPaymentMethod() : "N/A"
            });
        }
    }

    private void refreshCustomers() {
        customerModel.setRowCount(0);
        List<User> customers = userDAO.getAllCustomers();
        for (User u : customers) {
            customerModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getRole(),
                    u.getCreatedAt()
            });
        }
    }

    private void updateTripBusDropdown() {
        cmbTripBus.removeAllItems();
        if (busCache != null) {
            for (Bus b : busCache) {
                cmbTripBus.addItem(b.getBusId() + " - " + b.getBusName() + " (" + b.getBusNumber() + ")");
            }
        }
    }

    private void updateTripRouteDropdown() {
        cmbTripRoute.removeAllItems();
        if (routeCache != null) {
            for (Route r : routeCache) {
                cmbTripRoute.addItem(r.getRouteId() + " - " + r.getDepartureCity() + " to " + r.getDestinationCity());
            }
        }
    }

    // Bus Actions
    private void saveBus() {
        String number = txtBusNumber.getText().trim();
        String name = txtBusName.getText().trim();
        String type = (String) cmbBusType.getSelectedItem();
        String seatsStr = txtTotalSeats.getText().trim();

        if (number.isEmpty() || name.isEmpty() || seatsStr.isEmpty()) {
            UIUtils.showError(this, "Please fill in all bus fields.");
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0 || seats > 60) {
                UIUtils.showError(this, "Total seats must be between 1 and 60.");
                return;
            }
        } catch (NumberFormatException e) {
            UIUtils.showError(this, "Total seats must be a valid number.");
            return;
        }

        Bus bus = new Bus();
        bus.setBusNumber(number);
        bus.setBusName(name);
        bus.setBusType(type);
        bus.setTotalSeats(seats);

        if (selectedBusId > 0) {
            bus.setBusId(selectedBusId);
            if (busDAO.updateBus(bus)) {
                UIUtils.showSuccess(this, "Bus updated successfully!");
                clearBusForm();
                refreshBuses();
            } else {
                UIUtils.showError(this, "Failed to update bus.");
            }
        } else {
            if (busDAO.addBus(bus)) {
                UIUtils.showSuccess(this, "Bus added successfully!\nSeats A1 to J4 automatically generated in database.");
                clearBusForm();
                refreshBuses();
                refreshStatistics();
            } else {
                UIUtils.showError(this, "Failed to add bus. Ensure registration number is unique.");
            }
        }
    }

    private void deleteSelectedBus() {
        int row = busTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a bus from the table to delete.");
            return;
        }

        int busId = (int) busModel.getValueAt(row, 0);
        String name = (String) busModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete bus: " + name + " (ID: " + busId + ")?\n"
                + "Warning: This will cascade and delete associated seats and scheduled trips!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (busDAO.deleteBus(busId)) {
                UIUtils.showSuccess(this, "Bus deleted successfully.");
                clearBusForm();
                refreshBuses();
                refreshStatistics();
            } else {
                UIUtils.showError(this, "Could not delete bus.");
            }
        }
    }

    private void populateBusForm(int row) {
        if (busCache == null || row >= busCache.size()) return;
        Bus b = busCache.get(row);
        selectedBusId = b.getBusId();
        txtBusNumber.setText(b.getBusNumber());
        txtBusName.setText(b.getBusName());
        cmbBusType.setSelectedItem(b.getBusType());
        txtTotalSeats.setText(String.valueOf(b.getTotalSeats()));
    }

    private void clearBusForm() {
        selectedBusId = -1;
        txtBusNumber.setText("");
        txtBusName.setText("");
        cmbBusType.setSelectedIndex(0);
        txtTotalSeats.setText("40");
        busTable.clearSelection();
    }

    // Route Actions
    private void saveRoute() {
        String dep = txtDepartureCity.getText().trim();
        String dest = txtDestinationCity.getText().trim();
        String distStr = txtDistance.getText().trim();
        String dur = txtDuration.getText().trim();

        if (dep.isEmpty() || dest.isEmpty() || distStr.isEmpty()) {
            UIUtils.showError(this, "Please fill in departure, destination, and distance.");
            return;
        }

        double distance;
        try {
            distance = Double.parseDouble(distStr);
        } catch (NumberFormatException e) {
            UIUtils.showError(this, "Distance must be a valid number.");
            return;
        }

        Route r = new Route();
        r.setDepartureCity(dep);
        r.setDestinationCity(dest);
        r.setDistanceKm(distance);
        r.setEstimatedDuration(dur.isEmpty() ? "N/A" : dur);

        if (selectedRouteId > 0) {
            r.setRouteId(selectedRouteId);
            if (routeDAO.updateRoute(r)) {
                UIUtils.showSuccess(this, "Route updated successfully!");
                clearRouteForm();
                refreshRoutes();
            } else {
                UIUtils.showError(this, "Failed to update route.");
            }
        } else {
            if (routeDAO.addRoute(r)) {
                UIUtils.showSuccess(this, "Route added successfully!");
                clearRouteForm();
                refreshRoutes();
                refreshStatistics();
            } else {
                UIUtils.showError(this, "Failed to add route.");
            }
        }
    }

    private void deleteSelectedRoute() {
        int row = routeTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a route from the table to delete.");
            return;
        }

        int routeId = (int) routeModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Route ID: " + routeId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (routeDAO.deleteRoute(routeId)) {
                UIUtils.showSuccess(this, "Route deleted successfully.");
                clearRouteForm();
                refreshRoutes();
                refreshStatistics();
            } else {
                UIUtils.showError(this, "Could not delete route.");
            }
        }
    }

    private void populateRouteForm(int row) {
        if (routeCache == null || row >= routeCache.size()) return;
        Route r = routeCache.get(row);
        selectedRouteId = r.getRouteId();
        txtDepartureCity.setText(r.getDepartureCity());
        txtDestinationCity.setText(r.getDestinationCity());
        txtDistance.setText(String.valueOf(r.getDistanceKm()));
        txtDuration.setText(r.getEstimatedDuration());
    }

    private void clearRouteForm() {
        selectedRouteId = -1;
        txtDepartureCity.setText("");
        txtDestinationCity.setText("");
        txtDistance.setText("");
        txtDuration.setText("");
        routeTable.clearSelection();
    }

    // Trip Actions
    private void scheduleNewTrip() {
        if (cmbTripBus.getSelectedIndex() < 0 || cmbTripRoute.getSelectedIndex() < 0) {
            UIUtils.showError(this, "Please ensure at least one bus and one route exist in the system.");
            return;
        }

        Bus bus = busCache.get(cmbTripBus.getSelectedIndex());
        Route route = routeCache.get(cmbTripRoute.getSelectedIndex());

        String dateStr = txtTripDate.getText().trim();
        String timeStr = txtTripTime.getText().trim();
        String fareStr = txtTripFare.getText().trim();
        String status = (String) cmbTripStatus.getSelectedItem();

        Date depDate;
        Time depTime;
        double fare;

        try {
            depDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            UIUtils.showError(this, "Invalid Departure Date format. Use YYYY-MM-DD.");
            return;
        }

        try {
            depTime = Time.valueOf(timeStr);
        } catch (IllegalArgumentException e) {
            UIUtils.showError(this, "Invalid Departure Time format. Use HH:MM:SS.");
            return;
        }

        try {
            fare = Double.parseDouble(fareStr);
            if (fare <= 0) {
                UIUtils.showError(this, "Ticket fare must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            UIUtils.showError(this, "Invalid fare format.");
            return;
        }

        Trip t = new Trip();
        t.setBusId(bus.getBusId());
        t.setRouteId(route.getRouteId());
        t.setDepartureDate(depDate);
        t.setDepartureTime(depTime);
        t.setTicketPrice(fare);
        t.setStatus(status);

        if (tripDAO.scheduleTrip(t)) {
            UIUtils.showSuccess(this, "Trip scheduled successfully with ID: " + t.getTripId());
            refreshTrips();
        } else {
            UIUtils.showError(this, "Failed to schedule trip.");
        }
    }

    private void cancelSelectedTrip() {
        int row = tripTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a trip from the table to cancel.");
            return;
        }

        int tripId = (int) tripModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel scheduled trip ID: " + tripId + "?",
                "Confirm Cancel Trip",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (tripDAO.cancelTrip(tripId)) {
                UIUtils.showSuccess(this, "Trip has been marked as CANCELLED.");
                refreshTrips();
            } else {
                UIUtils.showError(this, "Could not cancel trip.");
            }
        }
    }

    private void viewAdminSelectedTicket() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select a booking from the table.");
            return;
        }

        Booking b = bookingCache.get(row);
        TicketFrame ticketDialog = new TicketFrame(this, b);
        ticketDialog.setVisible(true);
    }

    private void addLabeledField(JPanel parent, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.TEXT_MUTED);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(2));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        parent.add(field);
        parent.add(Box.createVerticalStrut(8));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Admin Console?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}

