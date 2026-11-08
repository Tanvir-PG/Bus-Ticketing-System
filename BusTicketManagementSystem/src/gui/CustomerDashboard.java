package gui;

import dao.RouteDAO;
import dao.TripDAO;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * CustomerDashboard - Main GUI Portal for Customers
 * 
 * Features:
 * - City-to-City and Date trip searching (using dynamic DB cities)
 * - Available Trips table displaying Route, Schedule, Bus Operator, Coach Type, Seat count, and Fare
 * - One-click launch of visual Seat Selection dialog
 * - Direct access to personal Booking History and Digital Ticket Receipts
 * - Instant logout and session cleanup
 */
public class CustomerDashboard extends JFrame {

    private final TripDAO tripDAO;
    private final RouteDAO routeDAO;

    private JComboBox<String> cmbDeparture;
    private JComboBox<String> cmbDestination;
    private JTextField txtTravelDate;
    private JTable tripsTable;
    private DefaultTableModel tableModel;
    private List<Trip> currentTrips;
    private JLabel lblTripCount;

    public CustomerDashboard() {
        super("Bus Ticket Booking System - Customer Portal");
        this.tripDAO = new TripDAO();
        this.routeDAO = new RouteDAO();

        initComponents();
        loadCityDropdowns();
        loadTrips(null, null, null);

        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UIUtils.BG_LIGHT);

        // 1. Top Navbar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIUtils.NAVY_DARK);
        navBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        User currentUser = AuthService.getCurrentUser();
        String welcome = "Welcome, " + (currentUser != null ? currentUser.getFullName() : "Customer");
        JLabel lblBrand = new JLabel("Bus Ticket Booking Portal");
        lblBrand.setFont(UIUtils.FONT_TITLE);
        lblBrand.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel(welcome);
        lblUser.setFont(UIUtils.FONT_REGULAR);
        lblUser.setForeground(new Color(148, 163, 184));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        brandPanel.add(lblBrand);
        brandPanel.add(lblUser);

        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        navActions.setOpaque(false);

        JButton btnMyBookings = UIUtils.createStyledButton("My Bookings & Tickets", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnMyBookings.addActionListener(e -> openBookingHistory());

        JButton btnLogout = UIUtils.createStyledButton("Logout", new Color(220, 38, 38), Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        navActions.add(btnMyBookings);
        navActions.add(btnLogout);

        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(navActions, BorderLayout.EAST);
        mainPanel.add(navBar, BorderLayout.NORTH);

        // 2. Center Content (Search Filter Box + Trips Table)
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 24, 20, 24));

        // Filter Card
        JPanel filterCard = createFilterCard();
        contentPanel.add(filterCard, BorderLayout.NORTH);

        // Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Table Header summary
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Available Trips & Coaches");
        lblTableTitle.setFont(UIUtils.FONT_HEADER);
        lblTableTitle.setForeground(UIUtils.NAVY_DARK);

        lblTripCount = new JLabel("Showing 0 trips");
        lblTripCount.setFont(UIUtils.FONT_REGULAR);
        lblTripCount.setForeground(UIUtils.TEXT_MUTED);

        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(lblTripCount, BorderLayout.EAST);
        tableContainer.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Trip ID", "Bus Operator", "Coach Type", "Route", "Date", "Departure Time", "Est. Duration", "Fare", "Available Seats"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tripsTable = new JTable(tableModel);
        UIUtils.styleTable(tripsTable);
        JScrollPane scrollPane = new JScrollPane(tripsTable);
        scrollPane.setBorder(new LineBorder(UIUtils.BORDER_COLOR, 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar below Table
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        tableActions.setOpaque(false);

        JButton btnBookSeat = UIUtils.createStyledButton("Select Seat & Book Ticket", UIUtils.SUCCESS_GREEN, Color.WHITE);
        btnBookSeat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBookSeat.addActionListener(e -> bookSelectedTrip());

        JButton btnRefresh = UIUtils.createStyledButton("Refresh Trips", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnRefresh.addActionListener(e -> onSearch());

        tableActions.add(btnRefresh);
        tableActions.add(btnBookSeat);
        tableContainer.add(tableActions, BorderLayout.SOUTH);

        contentPanel.add(tableContainer, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createFilterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel formGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        formGrid.setOpaque(false);

        // Departure City
        JPanel p1 = new JPanel(new BorderLayout(0, 4));
        p1.setOpaque(false);
        JLabel l1 = new JLabel("Departure City");
        l1.setFont(UIUtils.FONT_SMALL);
        l1.setForeground(UIUtils.TEXT_MUTED);
        cmbDeparture = new JComboBox<>();
        cmbDeparture.setFont(UIUtils.FONT_REGULAR);
        cmbDeparture.setBackground(Color.WHITE);
        p1.add(l1, BorderLayout.NORTH);
        p1.add(cmbDeparture, BorderLayout.CENTER);
        formGrid.add(p1);

        // Destination City
        JPanel p2 = new JPanel(new BorderLayout(0, 4));
        p2.setOpaque(false);
        JLabel l2 = new JLabel("Destination City");
        l2.setFont(UIUtils.FONT_SMALL);
        l2.setForeground(UIUtils.TEXT_MUTED);
        cmbDestination = new JComboBox<>();
        cmbDestination.setFont(UIUtils.FONT_REGULAR);
        cmbDestination.setBackground(Color.WHITE);
        p2.add(l2, BorderLayout.NORTH);
        p2.add(cmbDestination, BorderLayout.CENTER);
        formGrid.add(p2);

        // Travel Date (YYYY-MM-DD or empty)
        JPanel p3 = new JPanel(new BorderLayout(0, 4));
        p3.setOpaque(false);
        JLabel l3 = new JLabel("Travel Date (YYYY-MM-DD) [Optional]");
        l3.setFont(UIUtils.FONT_SMALL);
        l3.setForeground(UIUtils.TEXT_MUTED);
        txtTravelDate = UIUtils.createStyledTextField();
        txtTravelDate.setToolTipText("Enter date in YYYY-MM-DD format (leave blank for all upcoming trips)");
        p3.add(l3, BorderLayout.NORTH);
        p3.add(txtTravelDate, BorderLayout.CENTER);
        formGrid.add(p3);

        // Buttons
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 16));
        p4.setOpaque(false);

        JButton btnSearch = UIUtils.createStyledButton("Search Trips", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnSearch.addActionListener(e -> onSearch());

        JButton btnReset = UIUtils.createStyledButton("Reset", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnReset.addActionListener(e -> {
            cmbDeparture.setSelectedIndex(0);
            cmbDestination.setSelectedIndex(0);
            txtTravelDate.setText("");
            loadTrips(null, null, null);
        });

        p4.add(btnSearch);
        p4.add(btnReset);
        formGrid.add(p4);

        card.add(formGrid, BorderLayout.CENTER);
        return card;
    }

    private void loadCityDropdowns() {
        cmbDeparture.removeAllItems();
        cmbDestination.removeAllItems();

        cmbDeparture.addItem("All Cities");
        cmbDestination.addItem("All Cities");

        List<String> departures = routeDAO.getDistinctDepartureCities();
        for (String city : departures) {
            cmbDeparture.addItem(city);
        }

        List<String> destinations = routeDAO.getDistinctDestinationCities();
        for (String city : destinations) {
            cmbDestination.addItem(city);
        }
    }

    private void onSearch() {
        String dep = (String) cmbDeparture.getSelectedItem();
        String dest = (String) cmbDestination.getSelectedItem();
        String dateStr = txtTravelDate.getText().trim();

        Date travelDate = null;
        if (!dateStr.isEmpty()) {
            try {
                travelDate = Date.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                UIUtils.showError(this, "Please enter a valid date in YYYY-MM-DD format (e.g., " + LocalDate.now() + ")");
                return;
            }
        }

        loadTrips(dep, dest, travelDate);
    }

    public void loadTrips(String departure, String destination, Date travelDate) {
        tableModel.setRowCount(0);
        currentTrips = tripDAO.searchTrips(departure, destination, travelDate);

        for (Trip t : currentTrips) {
            String route = t.getDepartureCity() + " ➔ " + t.getDestinationCity();
            String duration = t.getEstimatedDuration() != null ? t.getEstimatedDuration() : "-";
            String fare = String.format("$%.2f", t.getTicketPrice());
            String avail = t.getAvailableSeats() + " / " + t.getTotalSeats();

            tableModel.addRow(new Object[]{
                    t.getTripId(),
                    t.getBusName() + " (" + t.getBusNumber() + ")",
                    t.getBusType(),
                    route,
                    t.getDepartureDate(),
                    t.getDepartureTime(),
                    duration,
                    fare,
                    avail
            });
        }

        lblTripCount.setText("Showing " + currentTrips.size() + " trip(s)");
    }

    private void bookSelectedTrip() {
        int selectedRow = tripsTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a trip from the table to book seats.");
            return;
        }

        Trip selectedTrip = currentTrips.get(selectedRow);
        if (selectedTrip.getAvailableSeats() <= 0) {
            UIUtils.showError(this, "Sorry, this coach is completely SOLD OUT! Please select another trip.");
            return;
        }

        SeatSelectionFrame seatFrame = new SeatSelectionFrame(this, selectedTrip, () -> {
            // Refresh trips table after booking
            onSearch();
        });
        seatFrame.setVisible(true);
    }

    private void openBookingHistory() {
        BookingHistoryFrame historyFrame = new BookingHistoryFrame(this, () -> {
            // Refresh trips table when booking cancelled
            onSearch();
        });
        historyFrame.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
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

