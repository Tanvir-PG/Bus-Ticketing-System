package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Booking Model - Represents a customer seat reservation.
 * Includes supplementary joined fields for complete Digital Ticket rendering.
 */
public class Booking {
    private int bookingId;
    private String bookingReference; // e.g. "BK-2026-9042"
    private int tripId;
    private int userId;
    private int seatId;
    private Timestamp bookingDate;
    private double totalFare;
    private String status; // "CONFIRMED", "CANCELLED"

    // Supplementary joined fields for Digital Ticket details
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private String busName;
    private String busNumber;
    private String busType;
    private String seatNumber;
    private String departureCity;
    private String destinationCity;
    private Date departureDate;
    private Time departureTime;
    private String paymentMethod;
    private String paymentStatus;

    public Booking() {}

    public Booking(int bookingId, String bookingReference, int tripId, int userId, int seatId, double totalFare, String status) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.tripId = tripId;
        this.userId = userId;
        this.seatId = seatId;
        this.totalFare = totalFare;
        this.status = status;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Joined ticket fields
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public Date getDepartureDate() { return departureDate; }
    public void setDepartureDate(Date departureDate) { this.departureDate = departureDate; }

    public Time getDepartureTime() { return departureTime; }
    public void setDepartureTime(Time departureTime) { this.departureTime = departureTime; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}

