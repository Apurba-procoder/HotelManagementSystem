package com.hotel.service;

import com.hotel.dao.BillDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.model.Bill;
import com.hotel.model.Reservation;
import com.hotel.util.DateUtil;
import com.hotel.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service handling invoice generation, payment management, and revenue calculations.
 */
public class BillingService {

    private final BillDAO billDAO;
    private final ReservationDAO reservationDAO;

    public BillingService() {
        this.billDAO = new BillDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public BillingService(BillDAO billDAO, ReservationDAO reservationDAO) {
        this.billDAO = billDAO;
        this.reservationDAO = reservationDAO;
    }

    /**
     * Generates a final bill for a reservation.
     * Prevents duplicate invoices for the same reservation.
     */
    public int generateBill(int reservationId, double taxPercent, double discount, String paymentMethod, String paymentStatus)
            throws SQLException, IllegalArgumentException, IllegalStateException {

        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation ID " + reservationId + " not found.");
        }

        if (billDAO.billExistsForReservation(reservationId)) {
            throw new IllegalStateException("An invoice has already been generated for Reservation #" + reservationId);
        }

        if (taxPercent < 0) {
            throw new IllegalArgumentException("Tax percentage cannot be negative.");
        }
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative.");
        }

        int nights = res.getNumberOfNights();
        if (nights <= 0) {
            nights = 1;
        }

        double pricePerNight = res.getPricePerNight();
        double accommodationCharge = nights * pricePerNight;
        double taxAmount = (accommodationCharge * taxPercent) / 100.0;
        double grandTotal = Math.max(0.0, accommodationCharge + taxAmount - discount);

        if (!ValidationUtil.isNotEmpty(paymentMethod)) {
            paymentMethod = "Cash";
        }
        if (!ValidationUtil.isNotEmpty(paymentStatus)) {
            paymentStatus = "PAID";
        }

        Bill bill = new Bill();
        bill.setReservationId(reservationId);
        bill.setNumberOfNights(nights);
        bill.setPricePerNight(pricePerNight);
        bill.setAccommodationCharge(accommodationCharge);
        bill.setTaxAmount(taxAmount);
        bill.setDiscount(discount);
        bill.setGrandTotal(grandTotal);
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentStatus(paymentStatus);
        bill.setBillDate(DateUtil.getCurrentDbDateTime());

        return billDAO.insert(bill);
    }

    public void updatePayment(int billId, String status, String method) throws SQLException, IllegalArgumentException {
        if (billId <= 0) {
            throw new IllegalArgumentException("Invalid bill ID.");
        }
        boolean success = billDAO.updatePaymentStatus(billId, status, method);
        if (!success) {
            throw new SQLException("Failed to update payment status for Bill #" + billId);
        }
    }

    public Bill getBillById(int billId) throws SQLException {
        return billDAO.findById(billId);
    }

    public Bill getBillByReservationId(int reservationId) throws SQLException {
        return billDAO.findByReservationId(reservationId);
    }

    public boolean hasBill(int reservationId) throws SQLException {
        return billDAO.billExistsForReservation(reservationId);
    }

    public List<Bill> getAllBills() throws SQLException {
        return billDAO.findAll();
    }

    public List<Bill> searchBills(String query, String paymentStatusFilter) throws SQLException {
        return billDAO.search(query, paymentStatusFilter);
    }

    public double getTotalRevenue() throws SQLException {
        return billDAO.calculateTotalRevenue();
    }
}
