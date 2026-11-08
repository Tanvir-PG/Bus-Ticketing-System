package service;

import dao.BookingDAO;
import dao.SeatDAO;
import dao.TripDAO;
import model.Booking;
import model.Seat;
import model.Trip;
import model.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingService - Business Logic Layer for Seat Reservation, Digital Ticket Generation and Cancellation
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final SeatDAO seatDAO;
    private final TripDAO tripDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.seatDAO = new SeatDAO();
        this.tripDAO = new TripDAO();
    }

    /**
     * Retrieves seat matrix for a trip, calculating real-time availability.
     */
    public List<Seat> getSeatsForTrip(int tripId) {
        return seatDAO.getSeatsForTrip(tripId);
    }

    /**
     * Executes atomic booking of a seat for the logged-in customer.
     */
    public Booking bookTicket(int tripId, int seatId, double fare, String paymentMethod) throws Exception {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            throw new Exception("Authentication required. Please sign in to book a seat.");
        }

        Trip trip = tripDAO.getTripById(tripId);
        if (trip == null) {
            throw new Exception("Selected trip could not be found.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(trip.getStatus())) {
            throw new Exception("This trip is " + trip.getStatus() + " and no longer accepting reservations.");
        }

        try {
            return bookingDAO.bookSeatWithTransaction(tripId, user.getUserId(), seatId, fare, paymentMethod);
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Cancels an active booking for a customer.
     */
    public boolean cancelTicket(int bookingId) throws Exception {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            throw new Exception("Authentication required.");
        }

        boolean success = bookingDAO.cancelBookingWithTransaction(bookingId, user.getUserId());
        if (!success) {
            throw new Exception("Could not cancel this booking. Please ensure the booking is in CONFIRMED status.");
        }
        return true;
    }

    /**
     * Formats a digital receipt text document for inside-app viewing and downloading.
     */
    public static String generateDigitalReceiptText(Booking b) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = b.getBookingDate() != null ? sdf.format(b.getBookingDate()) : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================================\n");
        sb.append("               BUS TICKET BOOKING MANAGEMENT SYSTEM            \n");
        sb.append("                    OFFICIAL DIGITAL RECEIPT                   \n");
        sb.append("===============================================================\n");
        sb.append(String.format("  BOOKING REFERENCE : %-40s\n", b.getBookingReference()));
        sb.append(String.format("  DATE OF ISSUE     : %-40s\n", formattedDate));
        sb.append(String.format("  BOOKING STATUS    : %-40s\n", b.getStatus()));
        sb.append("---------------------------------------------------------------\n");
        sb.append("  PASSENGER DETAILS\n");
        sb.append(String.format("  Name              : %-40s\n", b.getPassengerName()));
        sb.append(String.format("  Email             : %-40s\n", b.getPassengerEmail()));
        sb.append(String.format("  Phone Number      : %-40s\n", b.getPassengerPhone()));
        sb.append("---------------------------------------------------------------\n");
        sb.append("  JOURNEY & COACH DETAILS\n");
        sb.append(String.format("  Route             : %s ➔ %s\n", b.getDepartureCity(), b.getDestinationCity()));
        sb.append(String.format("  Departure Date    : %-40s\n", b.getDepartureDate()));
        sb.append(String.format("  Departure Time    : %-40s\n", b.getDepartureTime()));
        sb.append(String.format("  Bus Operator      : %-40s\n", b.getBusName()));
        sb.append(String.format("  Bus Reg. Number   : %-40s\n", b.getBusNumber()));
        sb.append(String.format("  Coach Category    : %-40s\n", b.getBusType()));
        sb.append(String.format("  ALLOCATED SEAT    : [%s]\n", b.getSeatNumber()));
        sb.append("---------------------------------------------------------------\n");
        sb.append("  PAYMENT INFORMATION\n");
        sb.append(String.format("  Payment Method    : %-40s\n", b.getPaymentMethod()));
        sb.append(String.format("  Payment Status    : %-40s\n", b.getPaymentStatus()));
        sb.append(String.format("  TOTAL FARE PAID   : $%.2f\n", b.getTotalFare()));
        sb.append("===============================================================\n");
        sb.append("   PLEASE PRESENT THIS DIGITAL RECEIPT AT THE BOARDING GATE.   \n");
        sb.append("              THANK YOU FOR CHOOSING OUR SERVICE!              \n");
        sb.append("===============================================================\n");
        return sb.toString();
    }

    /**
     * Allows customer to download / export the digital ticket as a text document.
     */
    public static File saveReceiptToFile(Booking booking, File destinationFile) throws IOException {
        String receiptContent = generateDigitalReceiptText(booking);
        try (FileWriter writer = new FileWriter(destinationFile)) {
            writer.write(receiptContent);
        }
        return destinationFile;
    }
}

