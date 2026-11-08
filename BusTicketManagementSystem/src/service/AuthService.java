package service;

import dao.UserDAO;
import model.User;

/**
 * AuthService - Business Logic Layer for User Authentication and Session Management
 */
public class AuthService {

    private final UserDAO userDAO;
    private static User currentLoggedInUser = null;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates user credentials and maintains active session.
     */
    public User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Please enter your email address.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Please enter your password.");
        }

        User user = userDAO.authenticate(email, password);
        if (user == null) {
            throw new Exception("Invalid email or password. Please verify your credentials.");
        }

        currentLoggedInUser = user;
        return user;
    }

    /**
     * Validates and registers a new customer account.
     */
    public User register(String fullName, String email, String phone, String password, String confirmPassword) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("Full Name is required.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Please enter a valid email address (e.g. name@domain.com).");
        }
        if (phone == null || phone.trim().length() < 7) {
            throw new Exception("Please enter a valid phone number.");
        }
        if (password == null || password.length() < 4) {
            throw new Exception("Password must be at least 4 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new Exception("Password and Confirm Password do not match.");
        }

        if (userDAO.emailExists(email)) {
            throw new Exception("An account with email '" + email + "' already exists.");
        }

        User newUser = new User(fullName.trim(), email.trim().toLowerCase(), phone.trim(), password, "CUSTOMER");
        boolean success = userDAO.registerCustomer(newUser);
        if (!success) {
            throw new Exception("Registration failed due to a database error. Please try again.");
        }

        return newUser;
    }

    public static User getCurrentUser() {
        return currentLoggedInUser;
    }

    public static void setCurrentUser(User user) {
        currentLoggedInUser = user;
    }

    public static void logout() {
        currentLoggedInUser = null;
    }

    public static boolean isLoggedIn() {
        return currentLoggedInUser != null;
    }

    public static boolean isAdmin() {
        return currentLoggedInUser != null && currentLoggedInUser.isAdmin();
    }
}

