package gui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * RegisterFrame - Customer Account Registration Screen
 */
public class RegisterFrame extends JFrame {

    private final AuthService authService;
    private final JFrame parentLoginFrame;

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public RegisterFrame(JFrame parentLoginFrame) {
        this.authService = new AuthService();
        this.parentLoginFrame = parentLoginFrame;
        initComponents();
    }

    private void initComponents() {
        setTitle("Customer Registration - Bus Ticket Booking Management System");
        setSize(480, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.BG_LIGHT);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UIUtils.NAVY_DARK);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("CREATE CUSTOMER ACCOUNT", JLabel.CENTER);
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Join and book your bus tickets online easily", JLabel.CENTER);
        lblSubtitle.setFont(UIUtils.FONT_SMALL);
        lblSubtitle.setForeground(new Color(147, 197, 253));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UIUtils.BG_LIGHT);
        formContainer.setBorder(new EmptyBorder(15, 30, 15, 30));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Full Name
        formCard.add(createLabel("Full Name *"));
        txtFullName = UIUtils.createStyledTextField();
        formCard.add(txtFullName);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Email Address
        formCard.add(createLabel("Email Address *"));
        txtEmail = UIUtils.createStyledTextField();
        formCard.add(txtEmail);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Phone Number
        formCard.add(createLabel("Phone Number *"));
        txtPhone = UIUtils.createStyledTextField();
        formCard.add(txtPhone);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Password
        formCard.add(createLabel("Password * (Min 4 chars)"));
        txtPassword = UIUtils.createStyledPasswordField();
        formCard.add(txtPassword);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Confirm Password
        formCard.add(createLabel("Confirm Password *"));
        txtConfirmPassword = UIUtils.createStyledPasswordField();
        formCard.add(txtConfirmPassword);
        formCard.add(Box.createRigidArea(new Dimension(0, 18)));

        // Action Buttons
        JButton btnRegister = UIUtils.createStyledButton("Register Now", UIUtils.PRIMARY_BLUE, Color.WHITE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(this::handleRegister);
        formCard.add(btnRegister);

        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnBack = UIUtils.createStyledButton("Cancel & Back to Login", new Color(100, 116, 139), Color.WHITE);
        btnBack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> {
            this.dispose();
            if (parentLoginFrame != null) {
                parentLoginFrame.setVisible(true);
            }
        });
        formCard.add(btnBack);

        formContainer.add(formCard);
        add(formContainer, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_SUBHEADER);
        label.setForeground(UIUtils.TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void handleRegister(ActionEvent e) {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

        try {
            authService.register(fullName, email, phone, password, confirmPassword);
            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now log in with your credentials.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            if (parentLoginFrame != null) {
                parentLoginFrame.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

