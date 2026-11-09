package com.hotel.service;

import com.hotel.dao.BillDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Reservation;
import com.hotel.util.DateUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service providing aggregated metrics and quick-view data for the Dashboard.
 */
public class DashboardService {

    private final RoomDAO roomDAO;
    private final CustomerDAO customerDAO;
    private final ReservationDAO reservationDAO;
    private final BillDAO billDAO;

    public DashboardService() {
        this.roomDAO = new RoomDAO();
        this.customerDAO = new CustomerDAO();
        this.reservationDAO = new ReservationDAO();
        this.billDAO = new BillDAO();
    }

    public DashboardService(RoomDAO roomDAO, CustomerDAO customerDAO, ReservationDAO reservationDAO, BillDAO billDAO) {
        this.roomDAO = roomDAO;
        this.customerDAO = customerDAO;
        this.reservationDAO = reservationDAO;
        this.billDAO = billDAO;
    }

    public record DashboardStats(
        int totalRooms,
        int availableRooms,
        int occupiedRooms,
        int totalCustomers,
        int activeReservations,
        double totalRevenue
    ) {}

    public DashboardStats getStats() throws SQLException {
        String today = DateUtil.getCurrentDbDate();
        int totalRooms = roomDAO.countTotalRooms();
        int availableRooms = roomDAO.countAvailableRoomsToday(today);
        int occupiedRooms = roomDAO.countOccupiedRoomsToday(today);
        int totalCustomers = customerDAO.countTotalCustomers();
        int activeReservations = reservationDAO.countActiveReservations();
        double totalRevenue = billDAO.calculateTotalRevenue();

        return new DashboardStats(
            totalRooms,
            availableRooms,
            occupiedRooms,
            totalCustomers,
            activeReservations,
            totalRevenue
        );
    }

    public List<Reservation> getRecentReservations(int limit) throws SQLException {
        return reservationDAO.findRecent(limit);
    }
}
