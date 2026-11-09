package com.hotel.model;

import java.util.Objects;

/**
 * Model representing a customer bill/invoice and payment record.
 */
public class Bill {

    private int billId;
    private int reservationId;
    private int numberOfNights;
    private double pricePerNight;
    private double accommodationCharge;
    private double taxAmount;
    private double discount;
    private double grandTotal;
    private String paymentStatus; // PAID, UNPAID, PARTIAL
    private String paymentMethod; // Cash, Card, UPI
    private String billDate; // YYYY-MM-DD HH:MM:SS

    // Joined / Presentation fields for invoice rendering
    private String customerName;
    private String customerPhone;
    private String roomNumber;
    private String roomType;
    private String checkInDate;
    private String checkOutDate;

    public Bill() {
        this.paymentStatus = "PAID";
        this.paymentMethod = "Cash";
    }

    public Bill(int billId, int reservationId, int numberOfNights, double pricePerNight,
                double accommodationCharge, double taxAmount, double discount,
                double grandTotal, String paymentStatus, String paymentMethod, String billDate) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.numberOfNights = numberOfNights;
        this.pricePerNight = pricePerNight;
        this.accommodationCharge = accommodationCharge;
        this.taxAmount = taxAmount;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : "PAID";
        this.paymentMethod = (paymentMethod != null) ? paymentMethod : "Cash";
        this.billDate = billDate;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(int numberOfNights) {
        this.numberOfNights = numberOfNights;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getAccommodationCharge() {
        return accommodationCharge;
    }

    public void setAccommodationCharge(double accommodationCharge) {
        this.accommodationCharge = accommodationCharge;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill bill)) return false;
        return billId == bill.billId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }
}
