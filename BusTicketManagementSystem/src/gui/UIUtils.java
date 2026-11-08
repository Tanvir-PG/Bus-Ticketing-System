package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UIUtils - Centralized Design & Styling Helper for Java Swing
 * Provides a clean, modern, cohesive color palette and custom components.
 */
public class UIUtils {

    // Modern Enterprise Theme Colors
    public static final Color NAVY_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color NAVY_HEADER = new Color(30, 41, 59);    // Slate 800
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);  // Royal Blue
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216); // Darker Blue
    public static final Color SUCCESS_GREEN = new Color(22, 163, 74); // Available / Confirmed
    public static final Color DANGER_RED = new Color(220, 38, 38);    // Booked / Cancelled
    public static final Color WARNING_AMBER = new Color(217, 119, 6); // Pending / AC
    public static final Color BG_LIGHT = new Color(248, 250, 252);    // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200
    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // Standard Modern Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 12);

    /**
     * Styles a standard modern button with solid background.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        return button;
    }

    /**
     * Styles a text field with clean padding and subtle border.
     */
    public static JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * Styles a password field.
     */
    public static JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_DARK);
        pf.setBackground(Color.WHITE);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    /**
     * Styles a JTable with alternating row colors and bold headers.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(219, 234, 254)); // Light Blue
        table.setSelectionForeground(NAVY_DARK);
        table.setShowVerticalLines(false);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(NAVY_DARK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Center alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    /**
     * Creates a decorative metric card for statistics.
     */
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
}

