package com.hotel.model;

import java.util.Objects;

/**
 * Model representing a room booking/reservation.
 */
public class Reservation {

    private int reservationId;
    private int customerId;
    private int roomId;
    private String bookingDate; // YYYY-MM-DD
    private String checkInDate; // YYYY-MM-DD
    private String checkOutDate; // YYYY-MM-DD
    private String actualCheckIn; // YYYY-MM-DD HH:MM:SS
    private String actualCheckOut; // YYYY-MM-DD HH:MM:SS
    private int numberOfNights;
    private double totalEstimatedAmount;
    private String reservationStatus; // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    // Joined / Presentation fields
    private String customerName;
    private String customerPhone;
    private String roomNumber;
    private String roomType;
    private double pricePerNight;

    public Reservation() {
        this.reservationStatus = "CONFIRMED";
    }

    public Reservation(int reservationId, int customerId, int roomId, String bookingDate,
                       String checkInDate, String checkOutDate, String actualCheckIn,
                       String actualCheckOut, int numberOfNights, double totalEstimatedAmount,
                       String reservationStatus) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.roomId = roomId;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.actualCheckIn = actualCheckIn;
        this.actualCheckOut = actualCheckOut;
        this.numberOfNights = numberOfNights;
        this.totalEstimatedAmount = totalEstimatedAmount;
        this.reservationStatus = (reservationStatus != null) ? reservationStatus : "CONFIRMED";
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getActualCheckIn() {
        return actualCheckIn;
    }

    public void setActualCheckIn(String actualCheckIn) {
        this.actualCheckIn = actualCheckIn;
    }

    public String getActualCheckOut() {
        return actualCheckOut;
    }

    public void setActualCheckOut(String actualCheckOut) {
        this.actualCheckOut = actualCheckOut;
    }

    public int getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(int numberOfNights) {
        this.numberOfNights = numberOfNights;
    }

    public double getTotalEstimatedAmount() {
        return totalEstimatedAmount;
    }

    public void setTotalEstimatedAmount(double totalEstimatedAmount) {
        this.totalEstimatedAmount = totalEstimatedAmount;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return reservationId == that.reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}
