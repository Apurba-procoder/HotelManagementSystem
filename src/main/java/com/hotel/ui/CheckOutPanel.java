package com.hotel.ui;

import com.hotel.model.Reservation;
import com.hotel.service.BillingService;
import com.hotel.service.ReservationService;
import com.hotel.util.DateUtil;
import com.hotel.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import java.util.List;

/**
 * Module 6: Check-Out Management
 * Currently checked-in guests, stay verification, room release, and invoice transition.
 */
public class CheckOutPanel extends JPanel {

    private final ReservationService reservationService;
    private final BillingService billingService;
    private final MainFrame mainFrame;

    private JTable checkedInTable;
    private DefaultTableModel tableModel;

    // Reservation Details Card Fields
    private JLabel lblResId;
    private JLabel lblGuestName;
    private JLabel lblPhone;
    private JLabel lblRoomInfo;
    private JLabel lblActualCheckIn;
    private JLabel lblStayDuration;
    private JLabel lblNightlyRate;
    private JLabel lblAccommodationCharge;

    private JTextField txtSearch;
    private Reservation selectedReservation;

    public CheckOutPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.reservationService = new ReservationService();
        this.billingService = new BillingService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        loadCheckedInGuests();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Guest Check-Out Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Process departures, record check-out timestamp, release room inventory, and generate invoice");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content: Left = Table of CHECKED_IN guests, Right = Departure Card (380px)
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Search Toolbar & Table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setOpaque(false);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchCard.add(new JLabel("Search Active Guest:"));
        txtSearch = new JTextField(18);
        UITheme.styleTextField(txtSearch);
        searchCard.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchCard.add(btnSearch);

        JButton btnReset = UITheme.createOutlineButton("Refresh List");
        btnReset.addActionListener(e -> loadCheckedInGuests());
        searchCard.add(btnReset);

        leftPanel.add(searchCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] cols = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-in Date", "Check-out Date", "Nights", "Amount"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        checkedInTable = new JTable(tableModel);
        UITheme.styleTable(checkedInTable);
        checkedInTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        checkedInTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        checkedInTable.getColumnModel().getColumn(3).setPreferredWidth(55);

        checkedInTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && checkedInTable.getSelectedRow() != -1) {
                int selectedRow = checkedInTable.getSelectedRow();
                int resId = (int) tableModel.getValueAt(selectedRow, 0);
                displayReservationDetails(resId);
            }
        });

        JScrollPane scrollPane = new JScrollPane(checkedInTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(tableCard, BorderLayout.CENTER);

        contentPanel.add(leftPanel, BorderLayout.CENTER);

        // RIGHT: Departure & Bill Calculation Card
        JPanel departureCard = UITheme.createCardPanel();
        departureCard.setPreferredSize(new Dimension(390, 600));
        departureCard.setLayout(new BorderLayout(0, 16));

        JLabel depTitle = new JLabel("Departure & Billing Verification");
        depTitle.setFont(UITheme.FONT_HEADER);
        depTitle.setForeground(UITheme.TEXT_PRIMARY);
        departureCard.add(depTitle, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;

        lblResId = createDetailValue("-");
        lblGuestName = createDetailValue("-");
        lblPhone = createDetailValue("-");
        lblRoomInfo = createDetailValue("-");
        lblActualCheckIn = createDetailValue("-");
        lblStayDuration = createDetailValue("-");
        lblNightlyRate = createDetailValue("-");
        lblAccommodationCharge = createDetailValue("-");
        lblAccommodationCharge.setFont(UITheme.FONT_METRIC);
        lblAccommodationCharge.setForeground(UITheme.PRIMARY_BLUE);

        addDetailRow(detailsGrid, gbc, 0, "Reservation ID:", lblResId);
        addDetailRow(detailsGrid, gbc, 1, "Guest Name:", lblGuestName);
        addDetailRow(detailsGrid, gbc, 2, "Phone Contact:", lblPhone);
        addDetailRow(detailsGrid, gbc, 3, "Occupied Room:", lblRoomInfo);
        addDetailRow(detailsGrid, gbc, 4, "Arrival Check-in Time:", lblActualCheckIn);
        addDetailRow(detailsGrid, gbc, 5, "Total Stay Nights:", lblStayDuration);
        addDetailRow(detailsGrid, gbc, 6, "Nightly Rate:", lblNightlyRate);
        addDetailRow(detailsGrid, gbc, 7, "Accommodation Subtotal:", lblAccommodationCharge);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        actionPanel.setOpaque(false);

        JButton btnCheckOut = UITheme.createButton("✓ Confirm Check-Out", new Color(13, 148, 136), new Color(15, 118, 110), Color.WHITE);
        btnCheckOut.setPreferredSize(new Dimension(btnCheckOut.getPreferredSize().width, 40));
        btnCheckOut.addActionListener(e -> handleCheckOut());

        JButton btnOpenBilling = UITheme.createPrimaryButton("➜ Go to Invoices & Payments");
        btnOpenBilling.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_BILLING));

        actionPanel.add(btnCheckOut);
        actionPanel.add(btnOpenBilling);

        departureCard.add(detailsGrid, BorderLayout.CENTER);
        departureCard.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(departureCard, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createDetailValue(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valLbl) {
        gbc.gridy = row * 2;
        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLbl, gbc);

        gbc.gridy = row * 2 + 1;
        panel.add(valLbl, gbc);
    }

    public void loadCheckedInGuests() {
        try {
            List<Reservation> list = reservationService.getCheckedInReservations();
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
                    r.getNumberOfNights() + " nights",
                    "$" + String.format("%.2f", r.getTotalEstimatedAmount())
                });
            }
            clearDetails();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load checked-in guests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayReservationDetails(int resId) {
        try {
            selectedReservation = reservationService.getReservationById(resId);
            if (selectedReservation != null) {
                lblResId.setText("#" + selectedReservation.getReservationId());
                lblGuestName.setText(selectedReservation.getCustomerName());
                lblPhone.setText(selectedReservation.getCustomerPhone());
                lblRoomInfo.setText("Room " + selectedReservation.getRoomNumber() + " (" + selectedReservation.getRoomType() + ")");
                lblActualCheckIn.setText(DateUtil.formatDisplayDateTime(selectedReservation.getActualCheckIn()));
                lblStayDuration.setText(selectedReservation.getNumberOfNights() + " Night(s)");
                lblNightlyRate.setText("$" + String.format("%.2f", selectedReservation.getPricePerNight()) + " / night");
                lblAccommodationCharge.setText("$" + String.format("%.2f", selectedReservation.getTotalEstimatedAmount()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckOut() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this, "Please select an active checked-in guest from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Departure & Check-Out for:\n"
            + "• Guest: " + selectedReservation.getCustomerName() + "\n"
            + "• Room: " + selectedReservation.getRoomNumber() + "\n"
            + "• Stay Duration: " + selectedReservation.getNumberOfNights() + " nights\n"
            + "• Accommodation Cost: $" + String.format("%.2f", selectedReservation.getTotalEstimatedAmount()) + "\n\n"
            + "Key returned and room marked vacant. Proceed?",
            "Confirm Guest Check-Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int resId = selectedReservation.getReservationId();
                reservationService.checkOut(resId);

                // Ask if user wants to generate invoice right away
                int genBill = JOptionPane.showConfirmDialog(this,
                    "Check-out confirmed successfully for Room " + selectedReservation.getRoomNumber() + "!\n"
                    + "Would you like to generate and view the customer bill now?",
                    "Generate Invoice", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                loadCheckedInGuests();
                mainFrame.notifyDataChanged();

                if (genBill == JOptionPane.YES_OPTION) {
                    mainFrame.showBillingForReservation(resId);
                }

            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-Out Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        try {
            List<Reservation> list = reservationService.searchReservations(query, "CHECKED_IN");
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
                    r.getNumberOfNights() + " nights",
                    "$" + String.format("%.2f", r.getTotalEstimatedAmount())
                });
            }
            clearDetails();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDetails() {
        selectedReservation = null;
        lblResId.setText("-");
        lblGuestName.setText("-");
        lblPhone.setText("-");
        lblRoomInfo.setText("-");
        lblActualCheckIn.setText("-");
        lblStayDuration.setText("-");
        lblNightlyRate.setText("-");
        lblAccommodationCharge.setText("-");
        checkedInTable.clearSelection();
    }
}
