package com.hotel;

import com.hotel.database.DatabaseConnection;
import com.hotel.database.DatabaseInitializer;
import com.hotel.model.Bill;
import com.hotel.model.Customer;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.service.BillingService;
import com.hotel.service.CustomerService;
import com.hotel.service.DashboardService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.util.BillPrinter;
import com.hotel.util.DateUtil;
import com.hotel.util.ValidationUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HotelSystemComprehensiveTest {

    private static final String TEST_DB_PATH = "test_hotel_booking.db";
    private static RoomService roomService;
    private static CustomerService customerService;
    private static ReservationService reservationService;
    private static BillingService billingService;
    private static DashboardService dashboardService;

    private static int createdRoomId;
    private static int createdCustomerId;
    private static int createdReservationId;
    private static int createdBillId;

    @BeforeAll
    static void setUp() {
        File testDb = new File(TEST_DB_PATH);
        if (testDb.exists()) {
            testDb.delete();
        }

        DatabaseConnection.setDatabasePath(TEST_DB_PATH);
        DatabaseInitializer.initialize();

        roomService = new RoomService();
        customerService = new CustomerService();
        reservationService = new ReservationService();
        billingService = new BillingService();
        dashboardService = new DashboardService();
    }

    @AfterAll
    static void tearDown() {
        DatabaseConnection.resetDatabasePath();
        File testDb = new File(TEST_DB_PATH);
        if (testDb.exists()) {
            testDb.delete();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Verify Initial Database Seed Data")
    void testInitialSeedData() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        assertFalse(rooms.isEmpty(), "Initial rooms should be seeded");

        List<Customer> customers = customerService.getAllCustomers();
        assertFalse(customers.isEmpty(), "Initial customers should be seeded");

        List<Reservation> reservations = reservationService.getAllReservations();
        assertFalse(reservations.isEmpty(), "Initial reservations should be seeded");

        List<Bill> bills = billingService.getAllBills();
        assertFalse(bills.isEmpty(), "Initial bills should be seeded");

        DashboardService.DashboardStats stats = dashboardService.getStats();
        assertTrue(stats.totalRooms() >= 10);
        assertTrue(stats.totalCustomers() >= 5);
        assertTrue(stats.totalRevenue() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Module 2: Add, Update, Search, and Validate Rooms")
    void testRoomOperations() throws SQLException {
        // Add Room
        Room newRoom = new Room("501", "Deluxe", 2, 195.0, "Active");
        roomService.addRoom(newRoom);
        assertTrue(newRoom.getRoomId() > 0);
        createdRoomId = newRoom.getRoomId();

        // Duplicate room number must be rejected
        Room duplicateRoom = new Room("501", "Single", 1, 80.0, "Active");
        assertThrows(IllegalArgumentException.class, () -> roomService.addRoom(duplicateRoom));

        // Update Room
        newRoom.setPricePerNight(210.0);
        newRoom.setCapacity(3);
        roomService.updateRoom(newRoom);

        Room fetched = roomService.getRoomById(createdRoomId);
        assertEquals(210.0, fetched.getPricePerNight());
        assertEquals(3, fetched.getCapacity());

        // Search Room
        List<Room> searchRes = roomService.searchRooms("501");
        assertEquals(1, searchRes.size());
        assertEquals("501", searchRes.get(0).getRoomNumber());
    }

    @Test
    @Order(3)
    @DisplayName("Module 3: Register, Update, Search, and Validate Customer")
    void testCustomerOperations() throws SQLException {
        // Register Customer
        Customer cust = new Customer("Jonathan Archer", "+1-555-9876", "j.archer@starfleet.org", "Male",
                "602 Enterprise Way, San Francisco, CA", "Passport", "USA-99887766");
        createdCustomerId = customerService.registerCustomer(cust);
        assertTrue(createdCustomerId > 0);

        // Invalid phone validation
        Customer invalidPhoneCust = new Customer("Bad Phone", "123", "test@test.com", "Male", "Address", "Passport", "123");
        assertThrows(IllegalArgumentException.class, () -> customerService.registerCustomer(invalidPhoneCust));

        // Update Customer
        cust.setCustomerId(createdCustomerId);
        cust.setPhone("+1-555-9999");
        customerService.updateCustomer(cust);

        Customer fetched = customerService.getCustomerById(createdCustomerId);
        assertEquals("+1-555-9999", fetched.getPhone());

        // Search Customer
        List<Customer> found = customerService.searchCustomers("Jonathan");
        assertFalse(found.isEmpty());
        assertEquals("Jonathan Archer", found.get(0).getFullName());
    }

    @Test
    @Order(4)
    @DisplayName("Module 4: Room Availability and Overlap Validation Rule")
    void testBookingAndDateOverlap() throws SQLException {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(14); // 4 nights

        // Check availability before booking
        List<Room> availableBefore = roomService.getAvailableRooms(
            DateUtil.formatDbDate(checkIn), DateUtil.formatDbDate(checkOut), "Deluxe");
        boolean containsRoom = availableBefore.stream().anyMatch(r -> r.getRoomId() == createdRoomId);
        assertTrue(containsRoom, "Room 501 should be available");

        // Create reservation
        createdReservationId = reservationService.createReservation(createdCustomerId, createdRoomId, checkIn, checkOut);
        assertTrue(createdReservationId > 0);

        Reservation res = reservationService.getReservationById(createdReservationId);
        assertEquals(4, res.getNumberOfNights());
        assertEquals(4 * 210.0, res.getTotalEstimatedAmount());
        assertEquals("CONFIRMED", res.getReservationStatus());

        // Test Overlapping Reservation Rule:
        // Existing Check-in < New Check-out AND Existing Check-out > New Check-in
        // Scenario A: Exact same dates
        assertThrows(IllegalStateException.class, () ->
            reservationService.createReservation(createdCustomerId, createdRoomId, checkIn, checkOut)
        );

        // Scenario B: Overlap in the middle (Day 11 to Day 13)
        assertThrows(IllegalStateException.class, () ->
            reservationService.createReservation(createdCustomerId, createdRoomId, checkIn.plusDays(1), checkOut.minusDays(1))
        );

        // Scenario C: Overlap starts before and ends inside (Day 8 to Day 12)
        assertThrows(IllegalStateException.class, () ->
            reservationService.createReservation(createdCustomerId, createdRoomId, checkIn.minusDays(2), checkIn.plusDays(2))
        );

        // Scenario D: Overlap starts inside and ends after (Day 12 to Day 16)
        assertThrows(IllegalStateException.class, () ->
            reservationService.createReservation(createdCustomerId, createdRoomId, checkIn.plusDays(2), checkOut.plusDays(2))
        );

        // Scenario E: Non-overlapping contiguous after (Check-in = old Check-out)
        // Day 14 to Day 16 should be permitted!
        int nextResId = reservationService.createReservation(createdCustomerId, createdRoomId, checkOut, checkOut.plusDays(2));
        assertTrue(nextResId > 0, "Contiguous booking without overlap should succeed");

        // Cancel the extra contiguous reservation
        reservationService.cancelReservation(nextResId);
        Reservation cancelled = reservationService.getReservationById(nextResId);
        assertEquals("CANCELLED", cancelled.getReservationStatus());
    }

    @Test
    @Order(5)
    @DisplayName("Module 5: Check-in Operations")
    void testCheckIn() throws SQLException {
        // Cannot check-in a non-existent or cancelled reservation
        assertThrows(IllegalArgumentException.class, () -> reservationService.checkIn(999999));

        // Perform check-in on confirmed reservation
        reservationService.checkIn(createdReservationId);

        Reservation res = reservationService.getReservationById(createdReservationId);
        assertEquals("CHECKED_IN", res.getReservationStatus());
        assertNotNull(res.getActualCheckIn());

        // Cannot check-in twice (duplicate check-in prevention)
        assertThrows(IllegalStateException.class, () -> reservationService.checkIn(createdReservationId));

        // Cannot cancel a currently checked-in reservation directly
        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(createdReservationId));
    }

    @Test
    @Order(6)
    @DisplayName("Module 6: Check-out Operations")
    void testCheckOut() throws SQLException {
        // Check-out checked-in reservation
        reservationService.checkOut(createdReservationId);

        Reservation res = reservationService.getReservationById(createdReservationId);
        assertEquals("CHECKED_OUT", res.getReservationStatus());
        assertNotNull(res.getActualCheckOut());

        // Cannot check-out twice
        assertThrows(IllegalStateException.class, () -> reservationService.checkOut(createdReservationId));
    }

    @Test
    @Order(7)
    @DisplayName("Module 7: Billing, Calculation, Payment and Duplicate Prevention")
    void testBillingAndPayments() throws SQLException {
        // 4 nights at $210/night = $840 subtotal
        // Tax 10% = $84.00
        // Discount = $40.00
        // Grand Total = $840 + $84 - $40 = $884.00
        createdBillId = billingService.generateBill(createdReservationId, 10.0, 40.0, "Card", "PAID");
        assertTrue(createdBillId > 0);

        Bill bill = billingService.getBillById(createdBillId);
        assertNotNull(bill);
        assertEquals(4, bill.getNumberOfNights());
        assertEquals(210.0, bill.getPricePerNight());
        assertEquals(840.0, bill.getAccommodationCharge());
        assertEquals(84.0, bill.getTaxAmount());
        assertEquals(40.0, bill.getDiscount());
        assertEquals(884.0, bill.getGrandTotal());
        assertEquals("PAID", bill.getPaymentStatus());
        assertEquals("Card", bill.getPaymentMethod());

        // Prevent duplicate invoice for the same reservation
        assertThrows(IllegalStateException.class, () ->
            billingService.generateBill(createdReservationId, 10.0, 40.0, "Cash", "PAID")
        );

        // Test HTML invoice generation and printer formatting
        String invoiceHtml = BillPrinter.generateInvoiceHtml(bill);
        assertTrue(invoiceHtml.contains("INV-"));
        assertTrue(invoiceHtml.contains("Jonathan Archer"));
        assertTrue(invoiceHtml.contains("884.00"));
    }

    @Test
    @Order(8)
    @DisplayName("Data Integrity: Prevent Deletion of Room/Customer with Linked Records")
    void testIntegrityConstraints() throws SQLException {
        // Room has reservation history, so deleting should be blocked
        assertThrows(IllegalStateException.class, () -> roomService.deleteRoom(createdRoomId));

        // Customer has reservation history, so deleting should be blocked
        assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(createdCustomerId));
    }
}
