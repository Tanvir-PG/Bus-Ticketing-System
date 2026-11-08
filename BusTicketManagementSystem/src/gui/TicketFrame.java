package gui;

import model.Booking;
import service.BookingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

/**
 * TicketFrame - Modal Dialog displaying Digital Ticket Receipt inside Java Swing.
 * Features:
 * - Formatted Monospace Digital Receipt
 * - Instant Ticket Download / Export to .txt file via JFileChooser
 * - High contrast, legible typography and action controls
 */
public class TicketFrame extends JDialog {

    private final Booking booking;

    public TicketFrame(Frame owner, Booking booking) {
        super(owner, "Digital Boarding Ticket - " + (booking != null ? booking.getBookingReference() : ""), true);
        this.booking = booking;

        initComponents();
        setSize(560, 680);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtils.BG_LIGHT);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Official Digital Ticket");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Booking Ref: " + (booking != null ? booking.getBookingReference() : "N/A"));
        subLabel.setFont(UIUtils.FONT_REGULAR);
        subLabel.setForeground(new Color(148, 163, 184));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Content - Text Area formatted as Digital Receipt
        JPanel receiptPanel = new JPanel(new BorderLayout());
        receiptPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        receiptPanel.setBackground(UIUtils.BG_LIGHT);

        String receiptText = (booking != null) ? BookingService.generateDigitalReceiptText(booking) : "No booking data available.";

        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(new Color(30, 41, 59));
        receiptArea.setCaretPosition(0);
        receiptArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(new LineBorder(UIUtils.BORDER_COLOR, 1));
        receiptPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(receiptPanel, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(new LineBorder(UIUtils.BORDER_COLOR, 1));

        JButton btnDownload = UIUtils.createStyledButton("Save / Download Ticket (.txt)", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnDownload.addActionListener(e -> downloadTicket());

        JButton btnClose = UIUtils.createStyledButton("Close", new Color(226, 232, 240), UIUtils.TEXT_DARK);
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnDownload);
        bottomBar.add(btnClose);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void downloadTicket() {
        if (booking == null) {
            UIUtils.showError(this, "No ticket data available to download.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Digital Ticket Receipt");
        String defaultFileName = "Ticket_" + booking.getBookingReference() + ".txt";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .txt extension
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".txt");
            }

            try {
                BookingService.saveReceiptToFile(booking, fileToSave);
                UIUtils.showSuccess(this, "Digital Ticket saved successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                UIUtils.showError(this, "Failed to save ticket: " + ex.getMessage());
            }
        }
    }
}

