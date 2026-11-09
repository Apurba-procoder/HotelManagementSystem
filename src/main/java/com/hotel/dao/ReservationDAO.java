package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reservation entity with room availability check and status workflows.
 */
public class ReservationDAO {

    private static final String BASE_SELECT = """
        SELECT res.*,
               c.full_name AS customer_name,
               c.phone AS customer_phone,
               r.room_number,
               r.room_type,
               r.price_per_night
        FROM reservations res
        JOIN customers c ON res.customer_id = c.customer_id
        JOIN rooms r ON res.room_id = r.room_id
    """;

    public int insert(Reservation res) throws SQLException {
        String sql = """
            INSERT INTO reservations (customer_id, room_id, booking_date, check_in_date, check_out_date,
                                      actual_check_in, actual_check_out, number_of_nights,
                                      total_estimated_amount, reservation_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, res.getCustomerId());
            ps.setInt(2, res.getRoomId());
            ps.setString(3, res.getBookingDate());
            ps.setString(4, res.getCheckInDate());
            ps.setString(5, res.getCheckOutDate());
            ps.setString(6, res.getActualCheckIn());
            ps.setString(7, res.getActualCheckOut());
            ps.setInt(8, res.getNumberOfNights());
            ps.setDouble(9, res.getTotalEstimatedAmount());
            ps.setString(10, res.getReservationStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        res.setReservationId(id);
                        return id;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Reservation res) throws SQLException {
        String sql = """
            UPDATE reservations
            SET customer_id = ?, room_id = ?, booking_date = ?, check_in_date = ?, check_out_date = ?,
                actual_check_in = ?, actual_check_out = ?, number_of_nights = ?,
                total_estimated_amount = ?, reservation_status = ?
            WHERE reservation_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, res.getCustomerId());
            ps.setInt(2, res.getRoomId());
            ps.setString(3, res.getBookingDate());
            ps.setString(4, res.getCheckInDate());
            ps.setString(5, res.getCheckOutDate());
            ps.setString(6, res.getActualCheckIn());
            ps.setString(7, res.getActualCheckOut());
            ps.setInt(8, res.getNumberOfNights());
            ps.setDouble(9, res.getTotalEstimatedAmount());
            ps.setString(10, res.getReservationStatus());
            ps.setInt(11, res.getReservationId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkIn(int reservationId, String actualCheckInTime) throws SQLException {
        String sql = """
            UPDATE reservations
            SET reservation_status = 'CHECKED_IN',
                actual_check_in = ?
            WHERE reservation_id = ? AND reservation_status = 'CONFIRMED'
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actualCheckInTime);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkOut(int reservationId, String actualCheckOutTime) throws SQLException {
        String sql = """
            UPDATE reservations
            SET reservation_status = 'CHECKED_OUT',
                actual_check_out = ?
            WHERE reservation_id = ? AND reservation_status = 'CHECKED_IN'
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actualCheckOutTime);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancel(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED' WHERE reservation_id = ? AND reservation_status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public Reservation findById(int reservationId) throws SQLException {
        String sql = BASE_SELECT + " WHERE res.reservation_id = ?";
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

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY res.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Reservation> findByStatus(String status) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE res.reservation_status = ? ORDER BY res.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Reservation> search(String query, String statusFilter) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append(" WHERE 1=1 ");

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("All")) {
            sql.append(" AND res.reservation_status = ? ");
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("""
                 AND (CAST(res.reservation_id AS TEXT) LIKE ?
                      OR c.full_name LIKE ?
                      OR c.phone LIKE ?
                      OR r.room_number LIKE ?)
            """);
        }

        sql.append(" ORDER BY res.reservation_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int p = 1;
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("All")) {
                ps.setString(p++, statusFilter.trim());
            }
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim() + "%";
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

    /**
     * Checks if a room has overlapping active reservations for given dates.
     * Overlap condition:
     * Existing Check-in < New Check-out AND Existing Check-out > New Check-in
     */
    public boolean isRoomAvailable(int roomId, String checkInDate, String checkOutDate, int excludeReservationId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE room_id = ?
              AND reservation_id != ?
              AND reservation_status IN ('CONFIRMED', 'CHECKED_IN')
              AND check_in_date < ?
              AND check_out_date > ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeReservationId);
            ps.setString(3, checkOutDate);
            ps.setString(4, checkInDate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public int countActiveReservations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE reservation_status IN ('CONFIRMED', 'CHECKED_IN')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<Reservation> findRecent(int limit) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY res.reservation_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation res = new Reservation(
            rs.getInt("reservation_id"),
            rs.getInt("customer_id"),
            rs.getInt("room_id"),
            rs.getString("booking_date"),
            rs.getString("check_in_date"),
            rs.getString("check_out_date"),
            rs.getString("actual_check_in"),
            rs.getString("actual_check_out"),
            rs.getInt("number_of_nights"),
            rs.getDouble("total_estimated_amount"),
            rs.getString("reservation_status")
        );
        res.setCustomerName(rs.getString("customer_name"));
        res.setCustomerPhone(rs.getString("customer_phone"));
        res.setRoomNumber(rs.getString("room_number"));
        res.setRoomType(rs.getString("room_type"));
        res.setPricePerNight(rs.getDouble("price_per_night"));
        return res;
    }
}
