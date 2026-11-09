package com.hotel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Initializes SQLite database schemas, indices, and sample test data.
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createTables(conn);
            seedSampleDataIfEmpty(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Rooms Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number TEXT NOT NULL UNIQUE,
                    room_type TEXT NOT NULL,
                    capacity INTEGER NOT NULL,
                    price_per_night REAL NOT NULL,
                    room_status TEXT NOT NULL DEFAULT 'Active'
                );
            """);

            // 2. Customers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT,
                    gender TEXT NOT NULL,
                    address TEXT,
                    identity_proof_type TEXT NOT NULL,
                    identity_proof_number TEXT NOT NULL
                );
            """);

            // 3. Reservations Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    room_id INTEGER NOT NULL,
                    booking_date TEXT NOT NULL,
                    check_in_date TEXT NOT NULL,
                    check_out_date TEXT NOT NULL,
                    actual_check_in TEXT,
                    actual_check_out TEXT,
                    number_of_nights INTEGER NOT NULL,
                    total_estimated_amount REAL NOT NULL,
                    reservation_status TEXT NOT NULL DEFAULT 'CONFIRMED',
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
                    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE RESTRICT
                );
            """);

            // 4. Bills Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bills (
                    bill_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservation_id INTEGER NOT NULL UNIQUE,
                    number_of_nights INTEGER NOT NULL,
                    price_per_night REAL NOT NULL,
                    accommodation_charge REAL NOT NULL,
                    tax_amount REAL NOT NULL,
                    discount REAL NOT NULL,
                    grand_total REAL NOT NULL,
                    payment_status TEXT NOT NULL DEFAULT 'PAID',
                    payment_method TEXT NOT NULL DEFAULT 'Cash',
                    bill_date TEXT NOT NULL,
                    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT
                );
            """);

            // Performance indices
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_res_dates ON reservations(check_in_date, check_out_date, reservation_status);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_res_room ON reservations(room_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_res_cust ON reservations(customer_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cust_phone ON customers(phone);");
        }
    }

    private static void seedSampleDataIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS cnt FROM rooms")) {
            if (rs.next() && rs.getInt("cnt") > 0) {
                return; // Data already exists
            }
        }

        conn.setAutoCommit(false);
        try {
            // Seed Rooms
            String insertRoom = "INSERT INTO rooms (room_number, room_type, capacity, price_per_night, room_status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRoom)) {
                Object[][] rooms = {
                    {"101", "Single", 1, 65.0, "Active"},
                    {"102", "Single", 1, 70.0, "Active"},
                    {"103", "Single", 1, 65.0, "Maintenance"},
                    {"201", "Double", 2, 110.0, "Active"},
                    {"202", "Double", 2, 115.0, "Active"},
                    {"203", "Double", 2, 110.0, "Active"},
                    {"301", "Deluxe", 3, 175.0, "Active"},
                    {"302", "Deluxe", 3, 185.0, "Active"},
                    {"303", "Deluxe", 3, 175.0, "Active"},
                    {"401", "Suite", 4, 280.0, "Active"},
                    {"402", "Suite", 4, 320.0, "Active"}
                };
                for (Object[] r : rooms) {
                    ps.setString(1, (String) r[0]);
                    ps.setString(2, (String) r[1]);
                    ps.setInt(3, (Integer) r[2]);
                    ps.setDouble(4, (Double) r[3]);
                    ps.setString(5, (String) r[4]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Seed Customers
            String insertCust = "INSERT INTO customers (full_name, phone, email, gender, address, identity_proof_type, identity_proof_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertCust)) {
                String[][] customers = {
                    {"Alexander Wright", "+1-555-0101", "alex.wright@example.com", "Male", "452 Maple Ave, Boston, MA", "Passport", "P98765432"},
                    {"Sophia Martinez", "+1-555-0102", "sophia.m@example.com", "Female", "782 Elm St, Austin, TX", "Driver License", "DL-908124"},
                    {"Rajesh Sharma", "+1-555-0103", "rajesh.sharma@example.com", "Male", "12 Park Street, Chicago, IL", "National ID", "ID-4421980"},
                    {"Emily Chen", "+1-555-0104", "emily.chen@example.com", "Female", "90 Sunset Blvd, Los Angeles, CA", "Passport", "E11223344"},
                    {"David Miller", "+1-555-0105", "d.miller@example.com", "Male", "332 Pine Rd, Seattle, WA", "Aadhaar", "AA-7788990"}
                };
                for (String[] c : customers) {
                    ps.setString(1, c[0]);
                    ps.setString(2, c[1]);
                    ps.setString(3, c[2]);
                    ps.setString(4, c[3]);
                    ps.setString(5, c[4]);
                    ps.setString(6, c[5]);
                    ps.setString(7, c[6]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Seed Reservations
            LocalDate today = LocalDate.now();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayStr = today.format(df);
            String yesterdayStr = today.minusDays(1).format(df);
            String twoDaysAgoStr = today.minusDays(2).format(df);
            String threeDaysAgoStr = today.minusDays(3).format(df);
            String tomorrowStr = today.plusDays(1).format(df);
            String dayAfterTomorrowStr = today.plusDays(2).format(df);
            String threeDaysLaterStr = today.plusDays(3).format(df);

            String insertRes = """
                INSERT INTO reservations (customer_id, room_id, booking_date, check_in_date, check_out_date,
                                          actual_check_in, actual_check_out, number_of_nights,
                                          total_estimated_amount, reservation_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertRes)) {
                // 1. Confirmed upcoming reservation (Room 101, Customer 1)
                ps.setInt(1, 1);
                ps.setInt(2, 1);
                ps.setString(3, yesterdayStr);
                ps.setString(4, tomorrowStr);
                ps.setString(5, threeDaysLaterStr);
                ps.setString(6, null);
                ps.setString(7, null);
                ps.setInt(8, 2);
                ps.setDouble(9, 130.0);
                ps.setString(10, "CONFIRMED");
                ps.executeUpdate();

                // 2. Currently checked-in reservation (Room 201, Customer 2)
                ps.setInt(1, 2);
                ps.setInt(2, 4); // Room 201 (Double)
                ps.setString(3, twoDaysAgoStr);
                ps.setString(4, yesterdayStr);
                ps.setString(5, dayAfterTomorrowStr);
                ps.setString(6, yesterdayStr + " 14:30:00");
                ps.setString(7, null);
                ps.setInt(8, 3);
                ps.setDouble(9, 330.0);
                ps.setString(10, "CHECKED_IN");
                ps.executeUpdate();

                // 3. Checked-out reservation with bill (Room 301, Customer 3)
                ps.setInt(1, 3);
                ps.setInt(2, 7); // Room 301 (Deluxe)
                ps.setString(3, threeDaysAgoStr);
                ps.setString(4, twoDaysAgoStr);
                ps.setString(5, todayStr);
                ps.setString(6, twoDaysAgoStr + " 15:10:00");
                ps.setString(7, todayStr + " 10:15:00");
                ps.setInt(8, 2);
                ps.setDouble(9, 350.0);
                ps.setString(10, "CHECKED_OUT");
                ps.executeUpdate();
            }

            // Seed Bill for checked-out reservation (Reservation ID: 3)
            String insertBill = """
                INSERT INTO bills (reservation_id, number_of_nights, price_per_night, accommodation_charge,
                                   tax_amount, discount, grand_total, payment_status, payment_method, bill_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertBill)) {
                ps.setInt(1, 3);
                ps.setInt(2, 2);
                ps.setDouble(3, 175.0);
                ps.setDouble(4, 350.0);
                ps.setDouble(5, 42.0); // 12% tax
                ps.setDouble(6, 20.0); // $20 discount
                ps.setDouble(7, 372.0);
                ps.setString(8, "PAID");
                ps.setString(9, "Card");
                ps.setString(10, todayStr + " 10:20:00");
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
