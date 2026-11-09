package com.hotel.service;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service handling business logic and validations for Room management.
 */
public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    public RoomService(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    public void addRoom(Room room) throws SQLException, IllegalArgumentException {
        validateRoom(room, 0);
        roomDAO.insert(room);
    }

    public void updateRoom(Room room) throws SQLException, IllegalArgumentException {
        if (room.getRoomId() <= 0) {
            throw new IllegalArgumentException("Invalid room ID for update.");
        }
        validateRoom(room, room.getRoomId());
        boolean success = roomDAO.update(room);
        if (!success) {
            throw new SQLException("Failed to update room. Room may not exist.");
        }
    }

    public void deleteRoom(int roomId) throws SQLException, IllegalStateException {
        if (roomDAO.hasActiveReservations(roomId)) {
            throw new IllegalStateException("Cannot delete room: It has active reservations (Confirmed or Checked-In).");
        }
        if (roomDAO.hasAnyReservations(roomId)) {
            throw new IllegalStateException("Cannot delete room: Past reservation history exists. You can set its status to 'Maintenance' instead.");
        }
        boolean success = roomDAO.delete(roomId);
        if (!success) {
            throw new SQLException("Failed to delete room. Room may not exist.");
        }
    }

    public Room getRoomById(int roomId) throws SQLException {
        return roomDAO.findById(roomId);
    }

    public Room getRoomByNumber(String roomNumber) throws SQLException {
        return roomDAO.findByRoomNumber(roomNumber);
    }

    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }

    public List<Room> searchRooms(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllRooms();
        }
        return roomDAO.search(query);
    }

    public List<Room> getAvailableRooms(String checkInDate, String checkOutDate, String roomType) throws SQLException {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        return roomDAO.findAvailableRooms(checkInDate, checkOutDate, roomType);
    }

    private void validateRoom(Room room, int excludeRoomId) throws SQLException, IllegalArgumentException {
        if (room == null) {
            throw new IllegalArgumentException("Room details cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(room.getRoomNumber())) {
            throw new IllegalArgumentException("Room number is required.");
        }
        if (!ValidationUtil.isNotEmpty(room.getRoomType())) {
            throw new IllegalArgumentException("Room type is required.");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
        if (room.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than zero.");
        }
        if (roomDAO.roomNumberExists(room.getRoomNumber(), excludeRoomId)) {
            throw new IllegalArgumentException("Room number '" + room.getRoomNumber() + "' already exists.");
        }
    }
}
