package com.hotel.ui;

import com.hotel.model.Customer;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.service.CustomerService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.util.DateUtil;
import com.hotel.util.UITheme;
import com.hotel.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
 * Module 4: Room Booking and Reservation Management
 * Room availability check, date overlap validation, cost calculation, and cancellation.
 */
public class BookingPanel extends JPanel {

    private final ReservationService reservationService;
    private final CustomerService customerService;
    private final RoomService roomService;
    private final MainFrame mainFrame;

    // Reservation Table
    private JTable resTable;
    private DefaultTableModel tableModel;

    // Booking Creation Fields
    private JComboBox<CustomerItem> cmbCustomer;
    private JTextField txtCheckInDate;
    private JTextField txtCheckOutDate;
    private JComboBox<RoomItem> cmbAvailableRooms;
    private JLabel lblCalculatedNights;
    private JLabel lblRatePerNight;
    private JLabel lblEstimatedTotal;

    // Search & Filter
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;

    public BookingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.reservationService = new ReservationService();
        this.customerService = new CustomerService();
        this.roomService = new RoomService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        refreshCustomerDropdown();
        refreshAvailableRooms();
        loadAllReservations();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Room Booking & Reservations");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Create new guest reservations with automated date overlap validation and cost calculation");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content: Left = Booking Form (390px), Right = Reservations Table
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Booking Card
        JPanel bookingCard = UITheme.createCardPanel();
        bookingCard.setPreferredSize(new Dimension(410, 620));
        bookingCard.setLayout(new BorderLayout(0, 16));

        JLabel formTitle = new JLabel("New Room Reservation");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        bookingCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;

        // Customer selection row with "+ New Guest" button
        cmbCustomer = new JComboBox<>();
        UITheme.styleComboBox(cmbCustomer);

        JButton btnQuickAddCust = UITheme.createOutlineButton("+ New");
        btnQuickAddCust.setToolTipText("Register new customer instantly");
        btnQuickAddCust.addActionListener(e -> showQuickCustomerDialog());

        JPanel custRow = new JPanel(new BorderLayout(6, 0));
        custRow.setOpaque(false);
        custRow.add(cmbCustomer, BorderLayout.CENTER);
        custRow.add(btnQuickAddCust, BorderLayout.EAST);

        // Dates
        LocalDate today = LocalDate.now();
        txtCheckInDate = new JTextField(DateUtil.formatDbDate(today));
        UITheme.styleTextField(txtCheckInDate);

        txtCheckOutDate = new JTextField(DateUtil.formatDbDate(today.plusDays(1)));
        UITheme.styleTextField(txtCheckOutDate);

        // Date Change Listeners to automatically recheck availability and calculate nights
        DocumentListener dateDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onDatesChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onDatesChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onDatesChanged(); }
        };
        txtCheckInDate.getDocument().addDocumentListener(dateDocListener);
        txtCheckOutDate.getDocument().addDocumentListener(dateDocListener);

        // Available Rooms dropdown
        cmbAvailableRooms = new JComboBox<>();
        UITheme.styleComboBox(cmbAvailableRooms);
        cmbAvailableRooms.addActionListener(e -> calculateCosts());

        // Calculation labels
        lblCalculatedNights = new JLabel("1 night(s)");
        lblCalculatedNights.setFont(UITheme.FONT_BOLD);
        lblCalculatedNights.setForeground(UITheme.TEXT_PRIMARY);

        lblRatePerNight = new JLabel("$0.00 / night");
        lblRatePerNight.setFont(UITheme.FONT_REGULAR);
        lblRatePerNight.setForeground(UITheme.TEXT_SECONDARY);

        lblEstimatedTotal = new JLabel("$0.00");
        lblEstimatedTotal.setFont(UITheme.FONT_METRIC);
        lblEstimatedTotal.setForeground(UITheme.PRIMARY_BLUE);

        addFormField(formFields, gbc, 0, "Select Guest / Customer *:", custRow);
        addFormField(formFields, gbc, 1, "Check-in Date (YYYY-MM-DD) *:", txtCheckInDate);
        addFormField(formFields, gbc, 2, "Check-out Date (YYYY-MM-DD) *:", txtCheckOutDate);

        JButton btnRefreshRooms = UITheme.createOutlineButton("↻ Check Vacant Rooms");
        btnRefreshRooms.addActionListener(e -> refreshAvailableRooms());
        JPanel roomRow = new JPanel(new BorderLayout(6, 0));
        roomRow.setOpaque(false);
        roomRow.add(cmbAvailableRooms, BorderLayout.CENTER);
        roomRow.add(btnRefreshRooms, BorderLayout.EAST);

        addFormField(formFields, gbc, 3, "Available Room (Non-overlapping) *:", roomRow);

        // Cost Calculation Summary Box
        JPanel summaryBox = new JPanel(new GridLayout(3, 2, 8, 6));
        summaryBox.setBackground(new Color(241, 245, 249));
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        summaryBox.add(new JLabel("Duration of Stay:"));
        summaryBox.add(lblCalculatedNights);
        summaryBox.add(new JLabel("Room Nightly Rate:"));
        summaryBox.add(lblRatePerNight);
        summaryBox.add(new JLabel("Estimated Charge:"));
        summaryBox.add(lblEstimatedTotal);

        gbc.gridy = 8;
        gbc.insets = new Insets(12, 4, 12, 4);
        formFields.add(summaryBox, gbc);

        // Actions
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnConfirmBooking = UITheme.createPrimaryButton("Confirm Reservation");
        btnConfirmBooking.addActionListener(e -> handleCreateReservation());

        JButton btnClear = UITheme.createOutlineButton("Reset Form");
        btnClear.addActionListener(e -> resetForm());

        actionPanel.add(btnConfirmBooking);
        actionPanel.add(btnClear);

        bookingCard.add(formFields, BorderLayout.CENTER);
        bookingCard.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(bookingCard, BorderLayout.WEST);

        // RIGHT: Search Toolbar, Table & Cancel Reservation Action
        JPanel rightPanel = new JPanel(new BorderLayout(0, 16));
        rightPanel.setOpaque(false);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchCard.add(new JLabel("Filter Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"});
        UITheme.styleComboBox(cmbStatusFilter);
        cmbStatusFilter.addActionListener(e -> handleSearch());
        searchCard.add(cmbStatusFilter);

        searchCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        UITheme.styleTextField(txtSearch);
        searchCard.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchCard.add(btnSearch);

        JButton btnReset = UITheme.createOutlineButton("View All");
        btnReset.addActionListener(e -> loadAllReservations());
        searchCard.add(btnReset);

        rightPanel.add(searchCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] cols = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-in", "Check-out", "Nights", "Amount", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resTable = new JTable(tableModel);
        UITheme.styleTable(resTable);
        resTable.getColumnModel().getColumn(9).setCellRenderer(new UITheme.StatusBadgeRenderer());
        resTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        resTable.getColumnModel().getColumn(3).setPreferredWidth(55);
        resTable.getColumnModel().getColumn(7).setPreferredWidth(55);

        JScrollPane tableScroll = new JScrollPane(resTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);

        // Bottom Table Toolbar: Cancel Reservation & View Bill
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton btnCancelRes = UITheme.createDangerButton("✕ Cancel Selected Reservation");
        btnCancelRes.addActionListener(e -> handleCancelReservation());

        JButton btnGoToCheckIn = UITheme.createSuccessButton("➜ Proceed to Check-In");
        btnGoToCheckIn.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_CHECKIN));

        bottomBar.add(btnCancelRes);
        bottomBar.add(btnGoToCheckIn);

        tableCard.add(tableScroll, BorderLayout.CENTER);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

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

    public void refreshCustomerDropdown() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            cmbCustomer.removeAllItems();
            for (Customer c : customers) {
                cmbCustomer.addItem(new CustomerItem(c.getCustomerId(), c.getFullName(), c.getPhone()));
            }
        } catch (SQLException e) {
            System.err.println("Failed to populate customer list: " + e.getMessage());
        }
    }

    private void onDatesChanged() {
        refreshAvailableRooms();
        calculateCosts();
    }

    public void refreshAvailableRooms() {
        String inStr = txtCheckInDate.getText().trim();
        String outStr = txtCheckOutDate.getText().trim();

        LocalDate inDate = DateUtil.parseDbDate(inStr);
        LocalDate outDate = DateUtil.parseDbDate(outStr);

        cmbAvailableRooms.removeAllItems();

        if (ValidationUtil.isValidDateRange(inDate, outDate)) {
            try {
                List<Room> rooms = roomService.getAvailableRooms(inStr, outStr, "All");
                for (Room r : rooms) {
                    cmbAvailableRooms.addItem(new RoomItem(r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight()));
                }
            } catch (SQLException e) {
                System.err.println("Failed to fetch available rooms: " + e.getMessage());
            }
        }
        calculateCosts();
    }

    private void calculateCosts() {
        LocalDate inDate = DateUtil.parseDbDate(txtCheckInDate.getText().trim());
        LocalDate outDate = DateUtil.parseDbDate(txtCheckOutDate.getText().trim());

        if (ValidationUtil.isValidDateRange(inDate, outDate)) {
            int nights = DateUtil.calculateNights(inDate, outDate);
            lblCalculatedNights.setText(nights + " night(s)");

            RoomItem item = (RoomItem) cmbAvailableRooms.getSelectedItem();
            if (item != null) {
                lblRatePerNight.setText("$" + String.format("%.2f", item.price()) + " / night");
                double total = nights * item.price();
                lblEstimatedTotal.setText("$" + String.format("%.2f", total));
            } else {
                lblRatePerNight.setText("No room selected");
                lblEstimatedTotal.setText("$0.00");
            }
        } else {
            lblCalculatedNights.setText("Invalid date range");
            lblRatePerNight.setText("-");
            lblEstimatedTotal.setText("$0.00");
        }
    }

    public void loadAllReservations() {
        try {
            List<Reservation> list = reservationService.getAllReservations();
            displayReservations(list);
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load reservations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayReservations(List<Reservation> list) {
        tableModel.setRowCount(0);
        for (Reservation r : list) {
            tableModel.addRow(new Object[]{
                r.getReservationId(),
                r.getCustomerName(),
                r.getCustomerPhone(),
                r.getRoomNumber(),
                r.getRoomType(),
                DateUtil.formatDisplayDate(r.getCheckInDate()),
                DateUtil.formatDisplayDate(r.getCheckOutDate()),
                r.getNumberOfNights(),
                "$" + String.format("%.2f", r.getTotalEstimatedAmount()),
                r.getReservationStatus()
            });
        }
    }

    private void handleCreateReservation() {
        CustomerItem cust = (CustomerItem) cmbCustomer.getSelectedItem();
        if (cust == null) {
            JOptionPane.showMessageDialog(this, "Please select or register a customer first.", "Customer Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RoomItem room = (RoomItem) cmbAvailableRooms.getSelectedItem();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "No available room selected. Please choose a valid date range with available rooms.", "Room Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate inDate = DateUtil.parseDbDate(txtCheckInDate.getText().trim());
        LocalDate outDate = DateUtil.parseDbDate(txtCheckOutDate.getText().trim());

        if (!ValidationUtil.isValidDateRange(inDate, outDate)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be strictly after check-in date (Format: YYYY-MM-DD).", "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int nights = DateUtil.calculateNights(inDate, outDate);
        double estTotal = nights * room.price();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Reservation Details:\n"
            + "• Guest: " + cust.name() + "\n"
            + "• Room: " + room.roomNumber() + " (" + room.type() + ")\n"
            + "• Dates: " + DateUtil.formatDbDate(inDate) + " to " + DateUtil.formatDbDate(outDate) + " (" + nights + " nights)\n"
            + "• Estimated Amount: $" + String.format("%.2f", estTotal) + "\n\nProceed with booking?",
            "Confirm Booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int resId = reservationService.createReservation(cust.id(), room.roomId(), inDate, outDate);
                JOptionPane.showMessageDialog(this,
                    "Reservation #" + resId + " successfully confirmed for Room " + room.roomNumber() + "!",
                    "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);

                loadAllReservations();
                refreshAvailableRooms();
                mainFrame.notifyDataChanged();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCancelReservation() {
        int selectedRow = resTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resId = (int) tableModel.getValueAt(selectedRow, 0);
        String guestName = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 9);

        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only CONFIRMED reservations can be cancelled.\nCurrent status is '" + status + "'.", "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel reservation #" + resId + " for guest '" + guestName + "'?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reservationService.cancelReservation(resId);
                JOptionPane.showMessageDialog(this, "Reservation #" + resId + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadAllReservations();
                refreshAvailableRooms();
                mainFrame.notifyDataChanged();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        String status = (String) cmbStatusFilter.getSelectedItem();
        try {
            List<Reservation> list = reservationService.searchReservations(query, status);
            displayReservations(list);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        LocalDate today = LocalDate.now();
        txtCheckInDate.setText(DateUtil.formatDbDate(today));
        txtCheckOutDate.setText(DateUtil.formatDbDate(today.plusDays(1)));
        if (cmbCustomer.getItemCount() > 0) cmbCustomer.setSelectedIndex(0);
        refreshAvailableRooms();
    }

    private void showQuickCustomerDialog() {
        JDialog dlg = new JDialog(mainFrame, "Quick Guest Registration", true);
        dlg.setLayout(new BorderLayout(16, 16));
        dlg.setSize(420, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();
        JTextField emailF = new JTextField();
        JComboBox<String> genF = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JComboBox<String> idTypeF = new JComboBox<>(new String[]{"Passport", "Driver License", "National ID", "Aadhaar"});
        JTextField idNumF = new JTextField();

        p.add(new JLabel("Full Name *:")); p.add(nameF);
        p.add(new JLabel("Phone *:")); p.add(phoneF);
        p.add(new JLabel("Email:")); p.add(emailF);
        p.add(new JLabel("Gender *:")); p.add(genF);
        p.add(new JLabel("ID Proof Type *:")); p.add(idTypeF);
        p.add(new JLabel("ID Proof Number *:")); p.add(idNumF);

        JButton saveBtn = UITheme.createPrimaryButton("Save & Select");
        saveBtn.addActionListener(e -> {
            try {
                Customer c = new Customer(nameF.getText().trim(), phoneF.getText().trim(), emailF.getText().trim(),
                        (String) genF.getSelectedItem(), "", (String) idTypeF.getSelectedItem(), idNumF.getText().trim());
                int newId = customerService.registerCustomer(c);
                refreshCustomerDropdown();
                // Select newly added
                for (int i = 0; i < cmbCustomer.getItemCount(); i++) {
                    if (cmbCustomer.getItemAt(i).id() == newId) {
                        cmbCustomer.setSelectedIndex(i);
                        break;
                    }
                }
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Guest registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.notifyDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        dlg.add(p, BorderLayout.CENTER);
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.add(saveBtn);
        dlg.add(btm, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public record CustomerItem(int id, String name, String phone) {
        @Override
        public String toString() {
            return name + " (" + phone + ") [ID: " + id + "]";
        }
    }

    public record RoomItem(int roomId, String roomNumber, String type, double price) {
        @Override
        public String toString() {
            return "Room " + roomNumber + " (" + type + " - $" + String.format("%.2f", price) + "/nt)";
        }
    }
}
