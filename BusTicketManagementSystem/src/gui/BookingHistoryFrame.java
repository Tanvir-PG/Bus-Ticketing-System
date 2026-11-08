package gui;

import dao.BookingDAO;
import model.Booking;
import model.User;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * BookingHistoryFrame - Displays customer reservation history.
 * Allows viewing digital tickets and performing transactional cancellations.
 */
public class BookingHistoryFrame extends JDialog {

    private final BookingDAO bookingDAO;
    private final BookingService bookingService;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private List<Booking> currentBookings;
    private final Runnable onHistoryChanged;

    public BookingHistoryFrame(Frame owner, Runnable onHistoryChanged) {
        super(owner, "My Booking History", true);
        this.bookingDAO = new BookingDAO();
        this.bookingService = new BookingService();
        this.onHistoryChanged = onHistoryChanged;

        initComponents();
        loadBookings();
        setSize(960, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(UIUtils.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("My Booking History");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        User user = AuthService.getCurrentUser();
        String sub = "Logged in as: " + (user != null ? user.getFullName() + " (" + user.getEmail() + ")" : "Guest");
        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(UIUtils.FONT_REGULAR);
        subLabel.setForeground(new Color(148, 163, 184));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columnNames = {"Booking Ref", "Route", "Date & Time", "Bus & Type", "Seat", "Fare", "Status", "Payment"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        UIUtils.styleTable(bookingTable);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(new LineBorder(UIUtils.BORDER_COLOR, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(new LineBorder(UIUtils.BORDER_COLOR, 1));

        JButton btnViewTicket = UIUtils.createStyledButton("View Digital Ticket", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnViewTicket.addActionListener(e -> viewSelectedTicket());

        JButton btnCancel = UIUtils.createStyledButton("Cancel Booking", UIUtils.DANGER_RED, Color.WHITE);
        btnCancel.addActionListener(e -> cancelSelectedBooking());

        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.NAVY_HEADER, Color.WHITE);
        btnRefresh.addActionListener(e -> loadBookings());

        JButton btnClose = UIUtils.createStyledButton("Close", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnViewTicket);
        bottomBar.add(btnCancel);
        bottomBar.add(btnRefresh);
        bottomBar.add(btnClose);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        User user = AuthService.getCurrentUser();
        if (user == null) {
            return;
        }

        currentBookings = bookingDAO.getBookingsByUser(user.getUserId());
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a");

        for (Booking b : currentBookings) {
            String route = b.getDepartureCity() + " ➔ " + b.getDestinationCity();
            String dateTime = b.getDepartureDate() + " " + (b.getDepartureTime() != null ? b.getDepartureTime() : "");
            String bus = b.getBusName() + " (" + b.getBusType() + ")";
            String fare = String.format("$%.2f", b.getTotalFare());

            tableModel.addRow(new Object[]{
                    b.getBookingReference(),
                    route,
                    dateTime,
                    bus,
                    b.getSeatNumber(),
                    fare,
                    b.getStatus(),
                    b.getPaymentMethod() != null ? b.getPaymentMethod() : "N/A"
            });
        }
    }

    private void viewSelectedTicket() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a booking from the table to view the digital ticket.");
            return;
        }

        Booking b = currentBookings.get(selectedRow);
        TicketFrame ticketDialog = new TicketFrame((Frame) getOwner(), b);
        ticketDialog.setVisible(true);
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a booking from the table to cancel.");
            return;
        }

        Booking b = currentBookings.get(selectedRow);
        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
            UIUtils.showError(this, "This booking is already CANCELLED.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel booking [" + b.getBookingReference() + "] for seat " + b.getSeatNumber() + "?\n"
                + "This seat will immediately become available for other passengers.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = bookingService.cancelTicket(b.getBookingId());
            if (success) {
                UIUtils.showSuccess(this, "Booking " + b.getBookingReference() + " has been successfully CANCELLED.\nPayment marked as REFUNDED.");
                loadBookings();
                if (onHistoryChanged != null) {
                    onHistoryChanged.run();
                }
            } else {
                UIUtils.showError(this, "Could not cancel booking.");
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Cancellation failed: " + ex.getMessage());
        }
    }
}

