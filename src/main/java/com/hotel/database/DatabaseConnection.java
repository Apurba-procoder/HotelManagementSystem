package com.hotel.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC database connections.
 * Enforces foreign keys and provides centralized connection management.
 */
public class DatabaseConnection {

    private static final String DB_FILE_NAME = "hotel_booking.db";
    private static String dbPath = DB_FILE_NAME;

    private DatabaseConnection() {}

    /**
     * Override default DB path (useful for testing).
     */
    public static void setDatabasePath(String path) {
        dbPath = path;
    }

    public static String getDatabasePath() {
        return dbPath;
    }

    public static String getDatabaseUrl() {
        return "jdbc:sqlite:" + dbPath;
    }

    /**
     * Obtains a fresh database connection with SQLite foreign key support enabled.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath", e);
        }

        Connection conn = DriverManager.getConnection(getDatabaseUrl());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Resets database path to default.
     */
    public static void resetDatabasePath() {
        dbPath = DB_FILE_NAME;
    }
}
