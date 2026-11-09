package com.hotel.ui;

import com.hotel.model.Customer;
import com.hotel.service.CustomerService;
import com.hotel.util.UITheme;
import com.hotel.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/**
 * Module 3: Customer Management
 * Guest registration, updates, search, and validation.
 */
public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private final MainFrame mainFrame;

    private JTable customerTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField txtCustomerId;
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JComboBox<String> cmbGender;
    private JTextArea txtAddress;
    private JComboBox<String> cmbIdType;
    private JTextField txtIdNumber;

    // Search field
    private JTextField txtSearch;

    public CustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.customerService = new CustomerService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        loadAllCustomers();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Register guests, maintain profile records, verify identity documents, and manage contact info");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content split: Form on Left (360px), Search & Table on Right
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setPreferredSize(new Dimension(380, 600));
        formCard.setLayout(new BorderLayout(0, 16));

        JLabel formTitle = new JLabel("Guest Profile Details");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;

        txtCustomerId = new JTextField();
        txtCustomerId.setEditable(false);
        txtCustomerId.setBackground(new Color(241, 245, 249));
        UITheme.styleTextField(txtCustomerId);

        txtFullName = new JTextField();
        UITheme.styleTextField(txtFullName);

        txtPhone = new JTextField();
        UITheme.styleTextField(txtPhone);

        txtEmail = new JTextField();
        UITheme.styleTextField(txtEmail);

        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        UITheme.styleComboBox(cmbGender);

        cmbIdType = new JComboBox<>(new String[]{"Passport", "Driver License", "National ID", "Aadhaar", "Voter ID"});
        UITheme.styleComboBox(cmbIdType);

        txtIdNumber = new JTextField();
        UITheme.styleTextField(txtIdNumber);

        txtAddress = new JTextArea(3, 20);
        txtAddress.setFont(UITheme.FONT_REGULAR);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane addressScroll = new JScrollPane(txtAddress);
        addressScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));

        addFormField(formFields, gbc, 0, "Customer ID (Auto):", txtCustomerId);
        addFormField(formFields, gbc, 1, "Full Name *:", txtFullName);
        addFormField(formFields, gbc, 2, "Phone Number *:", txtPhone);
        addFormField(formFields, gbc, 3, "Email Address:", txtEmail);
        addFormField(formFields, gbc, 4, "Gender *:", cmbGender);
        addFormField(formFields, gbc, 5, "Identity Proof Type *:", cmbIdType);
        addFormField(formFields, gbc, 6, "ID Proof Number *:", txtIdNumber);
        addFormField(formFields, gbc, 7, "Residential Address:", addressScroll);

        // Form Action Buttons
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonGrid.setOpaque(false);

        JButton btnRegister = UITheme.createPrimaryButton("Register Guest");
        btnRegister.addActionListener(e -> handleRegister());

        JButton btnUpdate = UITheme.createButton("Update", new Color(14, 116, 144), new Color(8, 93, 117), Color.WHITE);
        btnUpdate.addActionListener(e -> handleUpdate());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnClear = UITheme.createOutlineButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        buttonGrid.add(btnRegister);
        buttonGrid.add(btnUpdate);
        buttonGrid.add(btnDelete);
        buttonGrid.add(btnClear);

        JScrollPane formScroll = new JScrollPane(formFields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);

        formCard.add(formScroll, BorderLayout.CENTER);
        formCard.add(buttonGrid, BorderLayout.SOUTH);

        contentPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Search Toolbar and Table
        JPanel rightPanel = new JPanel(new BorderLayout(0, 16));
        rightPanel.setOpaque(false);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchCard.add(new JLabel("Search Customers:"));
        txtSearch = new JTextField(20);
        UITheme.styleTextField(txtSearch);
        searchCard.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchCard.add(btnSearch);

        JButton btnViewAll = UITheme.createOutlineButton("View All");
        btnViewAll.addActionListener(e -> loadAllCustomers());
        searchCard.add(btnViewAll);

        rightPanel.add(searchCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {"ID", "Full Name", "Phone", "Email", "Gender", "ID Type", "ID Number", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        UITheme.styleTable(customerTable);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane tableScroll = new JScrollPane(customerTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableScroll, BorderLayout.CENTER);
        rightPanel.add(tableCard, BorderLayout.CENTER);

        contentPanel.add(rightPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        panel.add(comp, gbc);
    }

    public void loadAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            displayCustomersInTable(customers);
            txtSearch.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayCustomersInTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getPhone(),
                c.getEmail() != null ? c.getEmail() : "-",
                c.getGender(),
                c.getIdentityProofType(),
                c.getIdentityProofNumber(),
                c.getAddress() != null ? c.getAddress() : "-"
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtCustomerId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtFullName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtPhone.setText(tableModel.getValueAt(selectedRow, 2).toString());
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            txtEmail.setText(email.equals("-") ? "" : email);
            cmbGender.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
            cmbIdType.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
            txtIdNumber.setText(tableModel.getValueAt(selectedRow, 6).toString());
            String addr = tableModel.getValueAt(selectedRow, 7).toString();
            txtAddress.setText(addr.equals("-") ? "" : addr);
        }
    }

    private void handleRegister() {
        try {
            Customer c = buildCustomerFromForm();
            int newId = customerService.registerCustomer(c);
            JOptionPane.showMessageDialog(this, "Guest '" + c.getFullName() + "' registered successfully (ID: " + newId + ")!", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllCustomers();
            mainFrame.notifyDataChanged();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (!ValidationUtil.isNotEmpty(txtCustomerId.getText())) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Customer c = buildCustomerFromForm();
            c.setCustomerId(Integer.parseInt(txtCustomerId.getText().trim()));
            customerService.updateCustomer(c);
            JOptionPane.showMessageDialog(this, "Customer profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllCustomers();
            mainFrame.notifyDataChanged();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (!ValidationUtil.isNotEmpty(txtCustomerId.getText())) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int custId = Integer.parseInt(txtCustomerId.getText().trim());
        String name = txtFullName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete customer record for '" + name + "' (ID: " + custId + ")?",
            "Confirm Customer Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                customerService.deleteCustomer(custId);
                JOptionPane.showMessageDialog(this, "Customer record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAllCustomers();
                mainFrame.notifyDataChanged();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Customer", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        try {
            List<Customer> list = customerService.searchCustomers(query);
            displayCustomersInTable(list);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No customers found matching '" + query + "'", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Customer buildCustomerFromForm() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String idType = (String) cmbIdType.getSelectedItem();
        String idNum = txtIdNumber.getText().trim();
        String address = txtAddress.getText().trim();

        return new Customer(fullName, phone, email, gender, address, idType, idNum);
    }

    private void clearForm() {
        txtCustomerId.setText("");
        txtFullName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        cmbGender.setSelectedIndex(0);
        cmbIdType.setSelectedIndex(0);
        txtIdNumber.setText("");
        txtAddress.setText("");
        customerTable.clearSelection();
    }
}
