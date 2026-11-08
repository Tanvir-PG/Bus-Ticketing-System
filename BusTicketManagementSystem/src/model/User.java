package model;

import java.sql.Timestamp;

/**
 * User Model - Represents Users (Admin / Customer) in the system.
 */
public class User {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role; // "ADMIN" or "CUSTOMER"
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String fullName, String email, String phone, String password, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    public User(String fullName, String email, String phone, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return fullName + " (" + email + ") - " + role;
    }
}

