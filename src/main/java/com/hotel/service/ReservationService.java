package com.hotel.service;

import com.hotel.dao.CustomerDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Customer;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.util.DateUtil;
import com.hotel.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling business logic, transaction workflows, and validations for Bookings, Check-ins, and Check-outs.
 */
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final CustomerDAO customerDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.customerDAO = new CustomerDAO();
    }

    public ReservationService(ReservationDAO reservationDAO, RoomDAO roomDAO, CustomerDAO customerDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO = roomDAO;
        this.customerDAO = customerDAO;
    }

    /**
     * Creates a new reservation after validating dates, guest, room, and room availability.
     */
    public int createReservation(int customerId, int roomId, LocalDate checkInDate, LocalDate checkOutDate)
            throws SQLException, IllegalArgumentException, IllegalStateException {

        // Validate Customer
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " not found.");
        }

        // Validate Room
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room with ID " + roomId + " not found.");
        }
        if (!room.isActive()) {
            throw new IllegalStateException("Room " + room.getRoomNumber() + " is currently in maintenance and cannot be booked.");
        }

        // Validate Dates
        if (!ValidationUtil.isValidDateRange(checkInDate, checkOutDate)) {
            throw new IllegalArgumentException("Check-out date must be strictly after check-in date.");
        }

        String checkInStr = DateUtil.formatDbDate(checkInDate);
        String checkOutStr = DateUtil.formatDbDate(checkOutDate);

        // Check date overlap
        boolean isAvailable = reservationDAO.isRoomAvailable(roomId, checkInStr, checkOutStr, 0);
        if (!isAvailable) {
            throw new IllegalStateException("Room " + room.getRoomNumber() + " is not available for the selected dates ("
                    + checkInStr + " to " + checkOutStr + ") due to an overlapping reservation.");
        }

        int nights = DateUtil.calculateNights(checkInDate, checkOutDate);
        if (nights <= 0) {
            nights = 1;
        }

        double totalEstimated = nights * room.getPricePerNight();

        Reservation res = new Reservation();
        res.setCustomerId(customerId);
        res.setRoomId(roomId);
        res.setBookingDate(DateUtil.getCurrentDbDate());
        res.setCheckInDate(checkInStr);
        res.setCheckOutDate(checkOutStr);
        res.setNumberOfNights(nights);
        res.setTotalEstimatedAmount(totalEstimated);
        res.setReservationStatus("CONFIRMED");

        return reservationDAO.insert(res);
    }

    /**
     * Checks in a confirmed guest.
     */
    public void checkIn(int reservationId) throws SQLException, IllegalStateException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation ID " + reservationId + " not found.");
        }

        if ("CHECKED_IN".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Reservation #" + reservationId + " is already checked in.");
        }

        if (!"CONFIRMED".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Cannot check in: Reservation status is '" + res.getReservationStatus() + "'. Only CONFIRMED reservations can check in.");
        }

        String actualTime = DateUtil.formatDbDateTime(LocalDateTime.now());
        boolean success = reservationDAO.checkIn(reservationId, actualTime);
        if (!success) {
            throw new SQLException("Failed to process check-in for reservation #" + reservationId);
        }
    }

    /**
     * Checks out a currently checked-in guest.
     */
    public void checkOut(int reservationId) throws SQLException, IllegalStateException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation ID " + reservationId + " not found.");
        }

        if ("CHECKED_OUT".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Reservation #" + reservationId + " is already checked out.");
        }

        if (!"CHECKED_IN".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Cannot check out: Reservation status is '" + res.getReservationStatus() + "'. Only currently CHECKED_IN guests can check out.");
        }

        String actualTime = DateUtil.formatDbDateTime(LocalDateTime.now());
        boolean success = reservationDAO.checkOut(reservationId, actualTime);
        if (!success) {
            throw new SQLException("Failed to process check-out for reservation #" + reservationId);
        }
    }

    /**
     * Cancels a confirmed reservation.
     */
    public void cancelReservation(int reservationId) throws SQLException, IllegalStateException {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation ID " + reservationId + " not found.");
        }

        if ("CHECKED_IN".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Cannot cancel an active reservation that is currently CHECKED_IN. Please perform Check-out instead.");
        }

        if ("CHECKED_OUT".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Cannot cancel a completed reservation that has already checked out.");
        }

        if ("CANCELLED".equalsIgnoreCase(res.getReservationStatus())) {
            throw new IllegalStateException("Reservation #" + reservationId + " is already cancelled.");
        }

        boolean success = reservationDAO.cancel(reservationId);
        if (!success) {
            throw new SQLException("Failed to cancel reservation #" + reservationId);
        }
    }

    public Reservation getReservationById(int reservationId) throws SQLException {
        return reservationDAO.findById(reservationId);
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.findAll();
    }

    public List<Reservation> getCheckedInReservations() throws SQLException {
        return reservationDAO.findByStatus("CHECKED_IN");
    }

    public List<Reservation> getConfirmedReservations() throws SQLException {
        return reservationDAO.findByStatus("CONFIRMED");
    }

    public List<Reservation> searchReservations(String query, String statusFilter) throws SQLException {
        return reservationDAO.search(query, statusFilter);
    }

    public List<Reservation> getRecentReservations(int limit) throws SQLException {
        return reservationDAO.findRecent(limit);
    }
}
