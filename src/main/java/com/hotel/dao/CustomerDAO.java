package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer entity.
 */
public class CustomerDAO {

    public int insert(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (full_name, phone, email, gender, address, identity_proof_type, identity_proof_number)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getGender());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getIdentityProofType());
            ps.setString(7, customer.getIdentityProofNumber());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        customer.setCustomerId(id);
                        return id;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = """
            UPDATE customers SET full_name = ?, phone = ?, email = ?, gender = ?, address = ?,
                                 identity_proof_type = ?, identity_proof_number = ?
            WHERE customer_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getGender());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getIdentityProofType());
            ps.setString(7, customer.getIdentityProofNumber());
            ps.setInt(8, customer.getCustomerId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Customer> search(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = """
            SELECT * FROM customers
            WHERE full_name LIKE ?
               OR phone LIKE ?
               OR CAST(customer_id AS TEXT) LIKE ?
               OR email LIKE ?
            ORDER BY full_name ASC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean hasActiveReservations(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE customer_id = ? AND reservation_status IN ('CONFIRMED', 'CHECKED_IN')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean hasAnyReservations(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("gender"),
            rs.getString("address"),
            rs.getString("identity_proof_type"),
            rs.getString("identity_proof_number")
        );
    }
}
