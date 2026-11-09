package com.hotel.service;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;
import com.hotel.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service handling business logic and validations for Customer management.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public int registerCustomer(Customer customer) throws SQLException, IllegalArgumentException {
        validateCustomer(customer);
        return customerDAO.insert(customer);
    }

    public void updateCustomer(Customer customer) throws SQLException, IllegalArgumentException {
        if (customer.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID for update.");
        }
        validateCustomer(customer);
        boolean success = customerDAO.update(customer);
        if (!success) {
            throw new SQLException("Failed to update customer. Customer record not found.");
        }
    }

    public void deleteCustomer(int customerId) throws SQLException, IllegalStateException {
        if (customerDAO.hasActiveReservations(customerId)) {
            throw new IllegalStateException("Cannot delete customer: Guest has active reservations (Confirmed or Checked-In).");
        }
        if (customerDAO.hasAnyReservations(customerId)) {
            throw new IllegalStateException("Cannot delete customer: Reservation history is linked to this customer account.");
        }
        boolean success = customerDAO.delete(customerId);
        if (!success) {
            throw new SQLException("Failed to delete customer. Customer record not found.");
        }
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomers(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.search(query);
    }

    private void validateCustomer(Customer customer) throws IllegalArgumentException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer details cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(customer.getFullName())) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!ValidationUtil.isValidPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Valid phone number (7-15 digits) is required.");
        }
        if (!ValidationUtil.isValidEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!ValidationUtil.isNotEmpty(customer.getGender())) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (!ValidationUtil.isNotEmpty(customer.getIdentityProofType())) {
            throw new IllegalArgumentException("Identity proof type is required.");
        }
        if (!ValidationUtil.isNotEmpty(customer.getIdentityProofNumber())) {
            throw new IllegalArgumentException("Identity proof number is required.");
        }
    }
}
