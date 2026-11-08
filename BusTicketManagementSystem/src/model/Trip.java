package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Trip Model - Represents a scheduled bus journey.
 */
public class Trip {
    private int tripId;
    private int busId;
    private int routeId;
    private Date departureDate;
    private Time departureTime;
    private double ticketPrice;
    private String status; // "SCHEDULED", "IN_TRANSIT", "COMPLETED", "CANCELLED"
    private Timestamp createdAt;

    // Supplementary joined fields for UI display
    private String busName;
    private String busNumber;
    private String busType;
    private int totalSeats;
    private String departureCity;
    private String destinationCity;
    private String estimatedDuration;
    private int availableSeatsCount;

    public Trip() {}

    public Trip(int tripId, int busId, int routeId, Date departureDate, Time departureTime, double ticketPrice, String status) {
        this.tripId = tripId;
        this.busId = busId;
        this.routeId = routeId;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.ticketPrice = ticketPrice;
        this.status = status;
    }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }

    public Date getDepartureDate() { return departureDate; }
    public void setDepartureDate(Date departureDate) { this.departureDate = departureDate; }

    public Time getDepartureTime() { return departureTime; }
    public void setDepartureTime(Time departureTime) { this.departureTime = departureTime; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Joined fields
    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public int getAvailableSeatsCount() { return availableSeatsCount; }
    public void setAvailableSeatsCount(int availableSeatsCount) { this.availableSeatsCount = availableSeatsCount; }

    @Override
    public String toString() {
        return "Trip #" + tripId + " [" + departureCity + " ➔ " + destinationCity + "] " 
                + departureDate + " " + departureTime + " ($" + ticketPrice + ")";
    }
}

