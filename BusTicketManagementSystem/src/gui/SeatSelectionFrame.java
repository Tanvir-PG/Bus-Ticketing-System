package gui;

import model.Booking;
import model.Seat;
import model.Trip;
import model.User;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SeatSelectionFrame - Visual Interactive Bus Seat Layout & Booking Dialog
 * 
 * Features:
 * - Color-coded seat matrix:
 *   * Green: Available
 *   * Red: Booked (Disabled)
 *   * Blue: Selected
 * - Aisle layout mimicking physical 2x2 intercity coach
 * - Real-time seat selection & price calculation
 * - Atomic double-booking prevention through transactional JDBC
 * - Direct transition to Digital Ticket generation
 */
public class SeatSelectionFrame extends JDialog {

    private final Trip trip;
    private final BookingService bookingService;
    private final Runnable onBookingSuccess;

    private Seat selectedSeat = null;
    private final Map<Integer, JButton> seatButtonMap = new HashMap<>();

    private JLabel lblSelectedSeat;
    private JLabel lblTotalAmount;
    private JComboBox<String> cmbPaymentMethod;
    private JButton btnConfirm;
    private JPanel seatGridPanel;

    public SeatSelectionFrame(Frame owner, Trip trip, Runnable onBookingSuccess) {
        super(owner, "Seat Reservation - " + (trip != null ? (trip.getDepartureCity() + " to " + trip.getDestinationCity()) : ""), true);
        this.trip = trip;
        this.bookingService = new BookingService();
        this.onBookingSuccess = onBookingSuccess;

        initComponents();
        loadSeatLayout();
        setSize(850, 680);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(UIUtils.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Bus Seat Reservation");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        String subtitle = String.format("Route: %s ➔ %s  |  Date: %s  |  Time: %s  |  Bus: %s (%s)  |  Fare: $%.2f",
                trip.getDepartureCity(), trip.getDestinationCity(),
                trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getBusName(), trip.getBusNumber(),
                trip.getTicketPrice());
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(UIUtils.FONT_REGULAR);
        subLabel.setForeground(new Color(148, 163, 184));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Split (Left: Coach Layout, Right: Summary & Checkout)
        JPanel contentPanel = new JPanel(new BorderLayout(16, 0));
        contentPanel.setOpaque(false);

        // Left Panel: Bus Layout
        JPanel busContainer = new JPanel(new BorderLayout(10, 10));
        busContainer.setBackground(Color.WHITE);
        busContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Legend bar
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", UIUtils.SUCCESS_GREEN));
        legendPanel.add(createLegendItem("Selected", UIUtils.PRIMARY_BLUE));
        legendPanel.add(createLegendItem("Booked", UIUtils.DANGER_RED));
        busContainer.add(legendPanel, BorderLayout.NORTH);

        // Cabin Header (Driver & Door)
        JPanel cabinFront = new JPanel(new BorderLayout());
        cabinFront.setOpaque(false);
        cabinFront.setBorder(new EmptyBorder(4, 8, 12, 8));

        JLabel lblDriver = new JLabel("DRIVING CABIN [ STEERING ]", JLabel.LEFT);
        lblDriver.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDriver.setForeground(UIUtils.TEXT_MUTED);

        JLabel lblDoor = new JLabel("[ ENTRY DOOR ]", JLabel.RIGHT);
        lblDoor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDoor.setForeground(UIUtils.TEXT_MUTED);

        cabinFront.add(lblDriver, BorderLayout.WEST);
        cabinFront.add(lblDoor, BorderLayout.EAST);

        JPanel busBodyPanel = new JPanel(new BorderLayout());
        busBodyPanel.setOpaque(false);
        busBodyPanel.add(cabinFront, BorderLayout.NORTH);

        seatGridPanel = new JPanel();
        seatGridPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        busBodyPanel.add(scrollPane, BorderLayout.CENTER);

        busContainer.add(busBodyPanel, BorderLayout.CENTER);
        contentPanel.add(busContainer, BorderLayout.CENTER);

        // Right Panel: Booking Summary Card
        JPanel rightCard = createSummaryCard();
        contentPanel.add(rightCard, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel box = new JLabel();
        box.setOpaque(true);
        box.setBackground(color);
        box.setPreferredSize(new Dimension(16, 16));
        box.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        JLabel text = new JLabel(label);
        text.setFont(UIUtils.FONT_SMALL);
        text.setForeground(UIUtils.TEXT_DARK);
        p.add(box);
        p.add(text);
        return p;
    }

    private JPanel createSummaryCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setPreferredSize(new Dimension(280, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel title = new JLabel("Reservation Summary");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.NAVY_DARK);
        card.add(title, BorderLayout.NORTH);

        // Form Fields
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        User currentUser = AuthService.getCurrentUser();
        String passengerName = currentUser != null ? currentUser.getFullName() : "Guest";
        String passengerEmail = currentUser != null ? currentUser.getEmail() : "N/A";

        addSummaryField(detailsPanel, "Passenger Name", passengerName);
        addSummaryField(detailsPanel, "Email Address", passengerEmail);
        addSummaryField(detailsPanel, "Route", trip.getDepartureCity() + " ➔ " + trip.getDestinationCity());
        addSummaryField(detailsPanel, "Departure Date", trip.getDepartureDate().toString());
        addSummaryField(detailsPanel, "Departure Time", trip.getDepartureTime().toString());

        lblSelectedSeat = new JLabel("None Selected");
        lblSelectedSeat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedSeat.setForeground(UIUtils.PRIMARY_BLUE);
        addCustomField(detailsPanel, "Selected Seat", lblSelectedSeat);

        lblTotalAmount = new JLabel("$0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalAmount.setForeground(UIUtils.SUCCESS_GREEN);
        addCustomField(detailsPanel, "Total Fare", lblTotalAmount);

        // Payment Method Dropdown
        JLabel lblPay = new JLabel("Payment Method");
        lblPay.setFont(UIUtils.FONT_SUBHEADER);
        lblPay.setForeground(UIUtils.TEXT_MUTED);
        detailsPanel.add(lblPay);
        detailsPanel.add(Box.createVerticalStrut(4));

        cmbPaymentMethod = new JComboBox<>(new String[]{"CASH", "CREDIT_CARD", "DEBIT_CARD", "MOBILE_BANKING"});
        cmbPaymentMethod.setFont(UIUtils.FONT_REGULAR);
        cmbPaymentMethod.setBackground(Color.WHITE);
        cmbPaymentMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        detailsPanel.add(cmbPaymentMethod);
        detailsPanel.add(Box.createVerticalStrut(16));

        card.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        actionPanel.setOpaque(false);

        btnConfirm = UIUtils.createStyledButton("Confirm & Pay", UIUtils.SUCCESS_GREEN, Color.WHITE);
        btnConfirm.setEnabled(false);
        btnConfirm.addActionListener(e -> executeBooking());

        JButton btnBack = UIUtils.createStyledButton("Back", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnBack.addActionListener(e -> dispose());

        actionPanel.add(btnConfirm);
        actionPanel.add(btnBack);

        card.add(actionPanel, BorderLayout.SOUTH);
        return card;
    }

    private void addSummaryField(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.TEXT_MUTED);
        panel.add(l);

        JLabel v = new JLabel(value);
        v.setFont(UIUtils.FONT_REGULAR);
        v.setForeground(UIUtils.TEXT_DARK);
        panel.add(v);

        panel.add(Box.createVerticalStrut(10));
    }

    private void addCustomField(JPanel panel, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.TEXT_MUTED);
        panel.add(l);
        panel.add(Box.createVerticalStrut(2));
        panel.add(comp);
        panel.add(Box.createVerticalStrut(10));
    }

    /**
     * Loads physical seats for the bus and generates 2x2 aisle layout.
     */
    private void loadSeatLayout() {
        seatGridPanel.removeAll();
        seatButtonMap.clear();
        selectedSeat = null;
        updateSummary();

        List<Seat> seats = bookingService.getSeatsForTrip(trip.getTripId());

        if (seats.isEmpty()) {
            seatGridPanel.setLayout(new FlowLayout());
            JLabel noSeats = new JLabel("No seat layout found for this bus.");
            noSeats.setFont(UIUtils.FONT_REGULAR);
            seatGridPanel.add(noSeats);
            seatGridPanel.revalidate();
            seatGridPanel.repaint();
            return;
        }

        // Layout: 5 columns: [Seat 1] [Seat 2] [AISLE GAP] [Seat 3] [Seat 4]
        // Calculate number of rows
        int totalRows = (int) Math.ceil(seats.size() / 4.0);
        seatGridPanel.setLayout(new GridLayout(totalRows, 5, 8, 8));

        for (int row = 0; row < totalRows; row++) {
            // Left seat 1
            int idx1 = row * 4;
            addSeatButtonOrEmpty(seats, idx1);

            // Left seat 2
            int idx2 = row * 4 + 1;
            addSeatButtonOrEmpty(seats, idx2);

            // Aisle spacer
            JLabel aisle = new JLabel("", JLabel.CENTER);
            aisle.setPreferredSize(new Dimension(24, 38));
            seatGridPanel.add(aisle);

            // Right seat 3
            int idx3 = row * 4 + 2;
            addSeatButtonOrEmpty(seats, idx3);

            // Right seat 4
            int idx4 = row * 4 + 3;
            addSeatButtonOrEmpty(seats, idx4);
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private void addSeatButtonOrEmpty(List<Seat> seats, int index) {
        if (index < seats.size()) {
            Seat s = seats.get(index);
            JButton btn = new JButton(s.getSeatNumber());
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(54, 38));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (s.isBooked()) {
                // Booked seat: Red and disabled
                btn.setBackground(UIUtils.DANGER_RED);
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
                btn.setToolTipText("Seat " + s.getSeatNumber() + " is already BOOKED");
            } else {
                // Available seat: Green
                btn.setBackground(UIUtils.SUCCESS_GREEN);
                btn.setForeground(Color.WHITE);
                btn.setToolTipText("Seat " + s.getSeatNumber() + " is AVAILABLE - Click to select");
                btn.addActionListener(e -> selectSeat(s, btn));
            }

            seatButtonMap.put(s.getSeatId(), btn);
            seatGridPanel.add(btn);
        } else {
            JLabel empty = new JLabel();
            seatGridPanel.add(empty);
        }
    }

    private void selectSeat(Seat seat, JButton btn) {
        // If clicking the currently selected seat, unselect it
        if (selectedSeat != null && selectedSeat.getSeatId() == seat.getSeatId()) {
            btn.setBackground(UIUtils.SUCCESS_GREEN);
            selectedSeat = null;
            updateSummary();
            return;
        }

        // Reset previous selected seat to green
        if (selectedSeat != null) {
            JButton prevBtn = seatButtonMap.get(selectedSeat.getSeatId());
            if (prevBtn != null) {
                prevBtn.setBackground(UIUtils.SUCCESS_GREEN);
            }
        }

        // Set new selection to Blue
        selectedSeat = seat;
        btn.setBackground(UIUtils.PRIMARY_BLUE);
        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeat != null) {
            lblSelectedSeat.setText("Seat " + selectedSeat.getSeatNumber());
            lblTotalAmount.setText(String.format("$%.2f", trip.getTicketPrice()));
            btnConfirm.setEnabled(true);
        } else {
            lblSelectedSeat.setText("None Selected");
            lblTotalAmount.setText("$0.00");
            btnConfirm.setEnabled(false);
        }
    }

    private void executeBooking() {
        if (selectedSeat == null) {
            UIUtils.showError(this, "Please select an available seat first.");
            return;
        }

        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Confirm reservation of Seat [%s] for $%.2f via %s?",
                        selectedSeat.getSeatNumber(), trip.getTicketPrice(), paymentMethod),
                "Confirm Reservation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Perform atomic booking via Service layer (ACID transaction)
            Booking booking = bookingService.bookTicket(
                    trip.getTripId(),
                    selectedSeat.getSeatId(),
                    trip.getTicketPrice(),
                    paymentMethod
            );

            UIUtils.showSuccess(this, "Booking Successful!\nReference: " + booking.getBookingReference() + "\nSeat: " + booking.getSeatNumber());

            // Open Digital Ticket Dialog
            TicketFrame ticketDialog = new TicketFrame((Frame) getOwner(), booking);
            ticketDialog.setVisible(true);

            // Notify parent to refresh tables
            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }

            dispose();

        } catch (Exception ex) {
            UIUtils.showError(this, "Booking Failed: " + ex.getMessage());
            // Refresh seat layout in case another user booked the same seat (double booking race condition)
            loadSeatLayout();
        }
    }
}

