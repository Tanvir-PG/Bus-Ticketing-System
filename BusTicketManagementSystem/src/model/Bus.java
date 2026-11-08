package model;

import java.sql.Timestamp;

/**
 * Bus Model - Represents a bus vehicle in the fleet.
 */
public class Bus {
    private int busId;
    private String busNumber;
    private String busName;
    private String busType; // "AC", "NON_AC", "SLEEPER", "LUXURY"
    private int totalSeats;
    private Timestamp createdAt;

    public Bus() {}

    public Bus(int busId, String busNumber, String busName, String busType, int totalSeats) {
        this.busId = busId;
        this.busNumber = busNumber;
        this.busName = busName;
        this.busType = busType;
        this.totalSeats = totalSeats;
    }

    public Bus(String busNumber, String busName, String busType, int totalSeats) {
        this.busNumber = busNumber;
        this.busName = busName;
        this.busType = busType;
        this.totalSeats = totalSeats;
    }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return busName + " (" + busNumber + " - " + busType + ")";
    }
}

