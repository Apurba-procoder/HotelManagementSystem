package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.RoomService;
import com.hotel.util.DateUtil;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Module 2: Room Management
 * Full CRUD, availability checking by date range, search, and validation.
 */
public class RoomPanel extends JPanel {

    private final RoomService roomService;
    private final MainFrame mainFrame;

    private JTable roomTable;
    private DefaultTableModel tableModel;

    // Form Fields
    private JTextField txtRoomId;
    private JTextField txtRoomNumber;
    private JComboBox<String> cmbRoomType;
    private JTextField txtCapacity;
    private JTextField txtPricePerNight;
    private JComboBox<String> cmbRoomStatus;

    // Search and Filter Fields
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterType;

    // Date Availability Check
    private JTextField txtCheckInDate;
    private JTextField txtCheckOutDate;

    public RoomPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.roomService = new RoomService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        loadAllRooms();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Room Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Manage room inventory, pricing tiers, operational status, and check date availability");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Split: Form Panel on Left (340px), Table & Filters on Right
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setPreferredSize(new Dimension(360, 600));
        formCard.setLayout(new BorderLayout(0, 16));

        JLabel formTitle = new JLabel("Room Details");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        txtRoomId = new JTextField();
        txtRoomId.setEditable(false);
        txtRoomId.setBackground(new Color(241, 245, 249));
        UITheme.styleTextField(txtRoomId);

        txtRoomNumber = new JTextField();
        UITheme.styleTextField(txtRoomNumber);

        cmbRoomType = new JComboBox<>(new String[]{"Single", "Double", "Deluxe", "Suite"});
        UITheme.styleComboBox(cmbRoomType);

        txtCapacity = new JTextField();
        UITheme.styleTextField(txtCapacity);

        txtPricePerNight = new JTextField();
        UITheme.styleTextField(txtPricePerNight);

        cmbRoomStatus = new JComboBox<>(new String[]{"Active", "Maintenance"});
        UITheme.styleComboBox(cmbRoomStatus);

        addFormField(formFields, gbc, 0, "Room ID (Auto):", txtRoomId);
        addFormField(formFields, gbc, 1, "Room Number *:", txtRoomNumber);
        addFormField(formFields, gbc, 2, "Room Type *:", cmbRoomType);
        addFormField(formFields, gbc, 3, "Guest Capacity *:", txtCapacity);
        addFormField(formFields, gbc, 4, "Price Per Night ($) *:", txtPricePerNight);
        addFormField(formFields, gbc, 5, "Operational Status *:", cmbRoomStatus);

        // Form Action Buttons
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonGrid.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Room");
        btnAdd.addActionListener(e -> handleAddRoom());

        JButton btnUpdate = UITheme.createButton("Update", new Color(14, 116, 144), new Color(8, 93, 117), Color.WHITE);
        btnUpdate.addActionListener(e -> handleUpdateRoom());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDeleteRoom());

        JButton btnClear = UITheme.createOutlineButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        buttonGrid.add(btnAdd);
        buttonGrid.add(btnUpdate);
        buttonGrid.add(btnDelete);
        buttonGrid.add(btnClear);

        JPanel formCenter = new JPanel(new BorderLayout());
        formCenter.setOpaque(false);
        formCenter.add(formFields, BorderLayout.NORTH);

        formCard.add(formCenter, BorderLayout.CENTER);
        formCard.add(buttonGrid, BorderLayout.SOUTH);

        contentPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Search, Availability Filter, and Table Card
        JPanel rightPanel = new JPanel(new BorderLayout(0, 16));
        rightPanel.setOpaque(false);

        // Top Toolbar inside Card: Search & Date Range Availability
        JPanel toolCard = UITheme.createCardPanel();
        toolCard.setLayout(new BoxLayout(toolCard, BoxLayout.Y_AXIS));

        // Row 1: Search by Room Number / Type
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchRow.setOpaque(false);

        searchRow.add(new JLabel("Search Room:"));
        txtSearch = new JTextField(12);
        UITheme.styleTextField(txtSearch);
        searchRow.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchRow.add(btnSearch);

        JButton btnReset = UITheme.createOutlineButton("View All");
        btnReset.addActionListener(e -> loadAllRooms());
        searchRow.add(btnReset);

        // Row 2: Date-based Availability Filter
        JPanel dateFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        dateFilterRow.setOpaque(false);

        dateFilterRow.add(new JLabel("Check Availability:"));
        dateFilterRow.add(new JLabel("In:"));
        txtCheckInDate = new JTextField(DateUtil.getCurrentDbDate(), 9);
        UITheme.styleTextField(txtCheckInDate);
        dateFilterRow.add(txtCheckInDate);

        dateFilterRow.add(new JLabel("Out:"));
        txtCheckOutDate = new JTextField(DateUtil.formatDbDate(LocalDate.now().plusDays(1)), 9);
        UITheme.styleTextField(txtCheckOutDate);
        dateFilterRow.add(txtCheckOutDate);

        dateFilterRow.add(new JLabel("Type:"));
        cmbFilterType = new JComboBox<>(new String[]{"All", "Single", "Double", "Deluxe", "Suite"});
        UITheme.styleComboBox(cmbFilterType);
        dateFilterRow.add(cmbFilterType);

        JButton btnCheckAvail = UITheme.createSuccessButton("Find Available");
        btnCheckAvail.addActionListener(e -> handleCheckAvailability());
        dateFilterRow.add(btnCheckAvail);

        toolCard.add(searchRow);
        toolCard.add(Box.createRigidArea(new Dimension(0, 4)));
        toolCard.add(dateFilterRow);

        rightPanel.add(toolCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {"Room ID", "Room Number", "Room Type", "Capacity", "Price / Night", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        UITheme.styleTable(roomTable);
        roomTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && roomTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane tableScroll = new JScrollPane(roomTable);
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

    public void loadAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            displayRoomsInTable(rooms);
            txtSearch.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load rooms: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayRoomsInTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                r.getRoomId(),
                r.getRoomNumber(),
                r.getRoomType(),
                r.getCapacity() + " Person(s)",
                "$" + String.format("%.2f", r.getPricePerNight()),
                r.getRoomStatus()
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtRoomId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtRoomNumber.setText(tableModel.getValueAt(selectedRow, 1).toString());
            cmbRoomType.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());

            String capStr = tableModel.getValueAt(selectedRow, 3).toString().replaceAll("[^0-9]", "");
            txtCapacity.setText(capStr);

            String priceStr = tableModel.getValueAt(selectedRow, 4).toString().replace("$", "");
            txtPricePerNight.setText(priceStr);

            cmbRoomStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());
        }
    }

    private void handleAddRoom() {
        try {
            Room room = buildRoomFromForm();
            roomService.addRoom(room);
            JOptionPane.showMessageDialog(this, "Room " + room.getRoomNumber() + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllRooms();
            mainFrame.notifyDataChanged();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateRoom() {
        if (!ValidationUtil.isNotEmpty(txtRoomId.getText())) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Room room = buildRoomFromForm();
            room.setRoomId(Integer.parseInt(txtRoomId.getText().trim()));
            roomService.updateRoom(room);
            JOptionPane.showMessageDialog(this, "Room " + room.getRoomNumber() + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllRooms();
            mainFrame.notifyDataChanged();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteRoom() {
        if (!ValidationUtil.isNotEmpty(txtRoomId.getText())) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int roomId = Integer.parseInt(txtRoomId.getText().trim());
        String roomNumber = txtRoomNumber.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Room " + roomNumber + "?\nThis operation cannot be undone.",
            "Confirm Room Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                roomService.deleteRoom(roomId);
                JOptionPane.showMessageDialog(this, "Room " + roomNumber + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAllRooms();
                mainFrame.notifyDataChanged();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Room", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        try {
            List<Room> results = roomService.searchRooms(query);
            displayRoomsInTable(results);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No rooms matched query: '" + query + "'", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckAvailability() {
        String inStr = txtCheckInDate.getText().trim();
        String outStr = txtCheckOutDate.getText().trim();
        String typeFilter = (String) cmbFilterType.getSelectedItem();

        LocalDate inDate = DateUtil.parseDbDate(inStr);
        LocalDate outDate = DateUtil.parseDbDate(outStr);

        if (!ValidationUtil.isValidDateRange(inDate, outDate)) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates (YYYY-MM-DD) where check-out is after check-in.", "Date Range Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Room> avail = roomService.getAvailableRooms(inStr, outStr, typeFilter);
            displayRoomsInTable(avail);
            JOptionPane.showMessageDialog(this,
                "Found " + avail.size() + " available room(s) for period " + inStr + " to " + outStr + ".",
                "Availability Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Availability query failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Room buildRoomFromForm() {
        String roomNumber = txtRoomNumber.getText().trim();
        String roomType = (String) cmbRoomType.getSelectedItem();
        String capStr = txtCapacity.getText().trim();
        String priceStr = txtPricePerNight.getText().trim();
        String status = (String) cmbRoomStatus.getSelectedItem();

        if (!ValidationUtil.isNotEmpty(roomNumber)) {
            throw new IllegalArgumentException("Room number cannot be empty.");
        }
        if (!ValidationUtil.isPositiveInteger(capStr)) {
            throw new IllegalArgumentException("Capacity must be a positive integer.");
        }
        if (!ValidationUtil.isPositiveNumber(priceStr)) {
            throw new IllegalArgumentException("Price per night must be a positive number.");
        }

        int capacity = Integer.parseInt(capStr);
        double price = Double.parseDouble(priceStr);

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setPricePerNight(price);
        room.setRoomStatus(status);
        return room;
    }

    private void clearForm() {
        txtRoomId.setText("");
        txtRoomNumber.setText("");
        cmbRoomType.setSelectedIndex(0);
        txtCapacity.setText("");
        txtPricePerNight.setText("");
        cmbRoomStatus.setSelectedIndex(0);
        roomTable.clearSelection();
    }
}
