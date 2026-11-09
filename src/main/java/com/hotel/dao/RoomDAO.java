package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Room entity.
 */
public class RoomDAO {

    public int insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, capacity, price_per_night, room_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getRoomStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        room.setRoomId(id);
                        return id;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, capacity = ?, price_per_night = ?, room_status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getRoomStatus());
            ps.setInt(6, room.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Room findByRoomNumber(String roomNumber) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE UPPER(room_number) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean roomNumberExists(String roomNumber, int excludeRoomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE UPPER(room_number) = UPPER(?) AND room_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber.trim());
            ps.setInt(2, excludeRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean hasActiveReservations(int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ? AND reservation_status IN ('CONFIRMED', 'CHECKED_IN')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean hasAnyReservations(int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Room> search(String query) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_number LIKE ? OR room_type LIKE ? ORDER BY room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds active rooms available between newCheckIn and newCheckOut.
     * Uses overlap condition: Existing Check-in < New Check-out AND Existing Check-out > New Check-in
     */
    public List<Room> findAvailableRooms(String newCheckIn, String newCheckOut, String roomType) throws SQLException {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM rooms r WHERE r.room_status = 'Active' ");
        if (roomType != null && !roomType.trim().isEmpty() && !roomType.equalsIgnoreCase("All")) {
            sql.append("AND r.room_type = ? ");
        }
        sql.append("AND r.room_id NOT IN ( ");
        sql.append("    SELECT res.room_id FROM reservations res ");
        sql.append("    WHERE res.reservation_status IN ('CONFIRMED', 'CHECKED_IN') ");
        sql.append("    AND res.check_in_date < ? ");
        sql.append("    AND res.check_out_date > ? ");
        sql.append(") ORDER BY r.room_number ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (roomType != null && !roomType.trim().isEmpty() && !roomType.equalsIgnoreCase("All")) {
                ps.setString(paramIndex++, roomType.trim());
            }
            ps.setString(paramIndex++, newCheckOut);
            ps.setString(paramIndex++, newCheckIn);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countTotalRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countAvailableRoomsToday(String todayStr) throws SQLException {
        // Rooms active and not occupied or confirmed today
        String sql = """
            SELECT COUNT(*) FROM rooms r
            WHERE r.room_status = 'Active'
            AND r.room_id NOT IN (
                SELECT res.room_id FROM reservations res
                WHERE res.reservation_status IN ('CONFIRMED', 'CHECKED_IN')
                AND res.check_in_date <= ? AND res.check_out_date > ?
            )
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr);
            ps.setString(2, todayStr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countOccupiedRoomsToday(String todayStr) throws SQLException {
        // Rooms currently checked in
        String sql = "SELECT COUNT(DISTINCT room_id) FROM reservations WHERE reservation_status = 'CHECKED_IN'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getInt("capacity"),
            rs.getDouble("price_per_night"),
            rs.getString("room_status")
        );
    }
}
