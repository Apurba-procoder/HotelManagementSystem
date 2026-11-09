package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bill entity and invoices.
 */
public class BillDAO {

    private static final String BASE_SELECT = """
        SELECT b.*,
               c.full_name AS customer_name,
               c.phone AS customer_phone,
               r.room_number,
               r.room_type,
               res.check_in_date,
               res.check_out_date
        FROM bills b
        JOIN reservations res ON b.reservation_id = res.reservation_id
        JOIN customers c ON res.customer_id = c.customer_id
        JOIN rooms r ON res.room_id = r.room_id
    """;

    public int insert(Bill bill) throws SQLException {
        String sql = """
            INSERT INTO bills (reservation_id, number_of_nights, price_per_night, accommodation_charge,
                               tax_amount, discount, grand_total, payment_status, payment_method, bill_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bill.getReservationId());
            ps.setInt(2, bill.getNumberOfNights());
            ps.setDouble(3, bill.getPricePerNight());
            ps.setDouble(4, bill.getAccommodationCharge());
            ps.setDouble(5, bill.getTaxAmount());
            ps.setDouble(6, bill.getDiscount());
            ps.setDouble(7, bill.getGrandTotal());
            ps.setString(8, bill.getPaymentStatus());
            ps.setString(9, bill.getPaymentMethod());
            ps.setString(10, bill.getBillDate());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        bill.setBillId(id);
                        return id;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Bill bill) throws SQLException {
        String sql = """
            UPDATE bills
            SET number_of_nights = ?, price_per_night = ?, accommodation_charge = ?,
                tax_amount = ?, discount = ?, grand_total = ?,
                payment_status = ?, payment_method = ?, bill_date = ?
            WHERE bill_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getNumberOfNights());
            ps.setDouble(2, bill.getPricePerNight());
            ps.setDouble(3, bill.getAccommodationCharge());
            ps.setDouble(4, bill.getTaxAmount());
            ps.setDouble(5, bill.getDiscount());
            ps.setDouble(6, bill.getGrandTotal());
            ps.setString(7, bill.getPaymentStatus());
            ps.setString(8, bill.getPaymentMethod());
            ps.setString(9, bill.getBillDate());
            ps.setInt(10, bill.getBillId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaymentStatus(int billId, String status, String method) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ?, payment_method = ? WHERE bill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, method);
            ps.setInt(3, billId);
            return ps.executeUpdate() > 0;
        }
    }

    public Bill findById(int billId) throws SQLException {
        String sql = BASE_SELECT + " WHERE b.bill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Bill findByReservationId(int reservationId) throws SQLException {
        String sql = BASE_SELECT + " WHERE b.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean billExistsForReservation(int reservationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Bill> findAll() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY b.bill_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Bill> search(String query, String paymentStatusFilter) throws SQLException {
        List<Bill> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append(" WHERE 1=1 ");

        if (paymentStatusFilter != null && !paymentStatusFilter.trim().isEmpty() && !paymentStatusFilter.equalsIgnoreCase("All")) {
            sql.append(" AND b.payment_status = ? ");
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("""
                 AND (CAST(b.bill_id AS TEXT) LIKE ?
                      OR CAST(b.reservation_id AS TEXT) LIKE ?
                      OR c.full_name LIKE ?
                      OR c.phone LIKE ?
                      OR r.room_number LIKE ?)
            """);
        }

        sql.append(" ORDER BY b.bill_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int p = 1;
            if (paymentStatusFilter != null && !paymentStatusFilter.trim().isEmpty() && !paymentStatusFilter.equalsIgnoreCase("All")) {
                ps.setString(p++, paymentStatusFilter.trim());
            }
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim() + "%";
                ps.setString(p++, pattern);
                ps.setString(p++, pattern);
                ps.setString(p++, pattern);
                ps.setString(p++, pattern);
                ps.setString(p++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(grand_total) FROM bills WHERE payment_status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill(
            rs.getInt("bill_id"),
            rs.getInt("reservation_id"),
            rs.getInt("number_of_nights"),
            rs.getDouble("price_per_night"),
            rs.getDouble("accommodation_charge"),
            rs.getDouble("tax_amount"),
            rs.getDouble("discount"),
            rs.getDouble("grand_total"),
            rs.getString("payment_status"),
            rs.getString("payment_method"),
            rs.getString("bill_date")
        );
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setRoomNumber(rs.getString("room_number"));
        b.setRoomType(rs.getString("room_type"));
        b.setCheckInDate(rs.getString("check_in_date"));
        b.setCheckOutDate(rs.getString("check_out_date"));
        return b;
    }
}
