package model;

/**
 * Seat Model - Represents an individual seat in a bus.
 */
public class Seat {
    private int seatId;
    private int busId;
    private String seatNumber; // e.g. "A1", "A2", "B1", etc.
    private boolean isBooked;  // Calculated state for a specific trip

    public Seat() {}

    public Seat(int seatId, int busId, String seatNumber) {
        this.seatId = seatId;
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.isBooked = false;
    }

    public Seat(int seatId, int busId, String seatNumber, boolean isBooked) {
        this.seatId = seatId;
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.isBooked = isBooked;
    }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean isBooked) { this.isBooked = isBooked; }

    @Override
    public String toString() {
        return seatNumber + (isBooked ? " [Booked]" : " [Available]");
    }
}

