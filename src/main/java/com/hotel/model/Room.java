package com.hotel.model;

import java.util.Objects;

/**
 * Model representing a hotel room.
 */
public class Room {

    private int roomId;
    private String roomNumber;
    private String roomType; // Single, Double, Deluxe, Suite
    private int capacity;
    private double pricePerNight;
    private String roomStatus; // Active, Maintenance

    public Room() {
        this.roomStatus = "Active";
    }

    public Room(int roomId, String roomNumber, String roomType, int capacity, double pricePerNight, String roomStatus) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.roomStatus = (roomStatus != null && !roomStatus.trim().isEmpty()) ? roomStatus : "Active";
    }

    public Room(String roomNumber, String roomType, int capacity, double pricePerNight, String roomStatus) {
        this(0, roomNumber, roomType, capacity, pricePerNight, roomStatus);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(roomStatus);
    }

    @Override
    public String toString() {
        return roomNumber + " (" + roomType + " - $" + String.format("%.2f", pricePerNight) + "/night)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        return roomId == room.roomId && Objects.equals(roomNumber, room.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, roomNumber);
    }
}
