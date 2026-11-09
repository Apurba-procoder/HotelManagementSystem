package com.hotel.ui;

import com.hotel.model.Reservation;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 5: Check-In Management
 * Guest arrival verification, room assignment confirmation, and check-in processing.
 */
public class CheckInPanel extends JPanel {

    private final ReservationService reservationService;
    private final MainFrame mainFrame;

    private JTable confirmedTable;
    private DefaultTableModel tableModel;

    // Reservation Details Card Fields
    private JLabel lblResId;
    private JLabel lblGuestName;
    private JLabel lblPhone;
    private JLabel lblRoomInfo;
    private JLabel lblDates;
    private JLabel lblNights;
    private JLabel lblEstimatedAmount;
    private JLabel lblStatus;

    private JTextField txtSearch;
    private Reservation selectedReservation;

    public CheckInPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.reservationService = new ReservationService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        loadConfirmedReservations();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Guest Check-In Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Verify confirmed bookings upon guest arrival and record official check-in timestamp");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content: Left = Table of CONFIRMED reservations, Right = Verification Card (360px)
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Search Toolbar & Table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setOpaque(false);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchCard.add(new JLabel("Search Confirmed Reservation:"));
        txtSearch = new JTextField(18);
        UITheme.styleTextField(txtSearch);
        searchCard.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchCard.add(btnSearch);

        JButton btnReset = UITheme.createOutlineButton("Refresh List");
        btnReset.addActionListener(e -> loadConfirmedReservations());
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

        confirmedTable = new JTable(tableModel);
        UITheme.styleTable(confirmedTable);
        confirmedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        confirmedTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        confirmedTable.getColumnModel().getColumn(3).setPreferredWidth(55);

        confirmedTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && confirmedTable.getSelectedRow() != -1) {
                int selectedRow = confirmedTable.getSelectedRow();
                int resId = (int) tableModel.getValueAt(selectedRow, 0);
                displayReservationDetails(resId);
            }
        });

        JScrollPane scrollPane = new JScrollPane(confirmedTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(tableCard, BorderLayout.CENTER);

        contentPanel.add(leftPanel, BorderLayout.CENTER);

        // RIGHT: Verification & Check-in Details Card
        JPanel verifyCard = UITheme.createCardPanel();
        verifyCard.setPreferredSize(new Dimension(380, 600));
        verifyCard.setLayout(new BorderLayout(0, 16));

        JLabel verifyTitle = new JLabel("Arrival Verification & Confirmation");
        verifyTitle.setFont(UITheme.FONT_HEADER);
        verifyTitle.setForeground(UITheme.TEXT_PRIMARY);
        verifyCard.add(verifyTitle, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        lblResId = createDetailValue("-");
        lblGuestName = createDetailValue("-");
        lblPhone = createDetailValue("-");
        lblRoomInfo = createDetailValue("-");
        lblDates = createDetailValue("-");
        lblNights = createDetailValue("-");
        lblEstimatedAmount = createDetailValue("-");
        lblStatus = createDetailValue("-");

        addDetailRow(detailsGrid, gbc, 0, "Reservation ID:", lblResId);
        addDetailRow(detailsGrid, gbc, 1, "Guest Name:", lblGuestName);
        addDetailRow(detailsGrid, gbc, 2, "Phone Contact:", lblPhone);
        addDetailRow(detailsGrid, gbc, 3, "Room Assigned:", lblRoomInfo);
        addDetailRow(detailsGrid, gbc, 4, "Scheduled Dates:", lblDates);
        addDetailRow(detailsGrid, gbc, 5, "Stay Duration:", lblNights);
        addDetailRow(detailsGrid, gbc, 6, "Total Est. Cost:", lblEstimatedAmount);
        addDetailRow(detailsGrid, gbc, 7, "Booking Status:", lblStatus);

        JButton btnConfirmCheckIn = UITheme.createSuccessButton("✓ Confirm Guest Check-In");
        btnConfirmCheckIn.setFont(UITheme.FONT_BOLD);
        btnConfirmCheckIn.setPreferredSize(new Dimension(btnConfirmCheckIn.getPreferredSize().width, 42));
        btnConfirmCheckIn.addActionListener(e -> handleCheckIn());

        verifyCard.add(detailsGrid, BorderLayout.CENTER);
        verifyCard.add(btnConfirmCheckIn, BorderLayout.SOUTH);

        contentPanel.add(verifyCard, BorderLayout.EAST);
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

    public void loadConfirmedReservations() {
        try {
            List<Reservation> list = reservationService.getConfirmedReservations();
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
            JOptionPane.showMessageDialog(this, "Failed to load check-in list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                lblDates.setText(DateUtil.formatDisplayDate(selectedReservation.getCheckInDate()) + " to " + DateUtil.formatDisplayDate(selectedReservation.getCheckOutDate()));
                lblNights.setText(selectedReservation.getNumberOfNights() + " Night(s)");
                lblEstimatedAmount.setText("$" + String.format("%.2f", selectedReservation.getTotalEstimatedAmount()));
                lblStatus.setText(selectedReservation.getReservationStatus());
                lblStatus.setForeground(new Color(2, 132, 199));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckIn() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this, "Please select a confirmed reservation from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Check-In for Guest:\n"
            + "• Name: " + selectedReservation.getCustomerName() + "\n"
            + "• Room: " + selectedReservation.getRoomNumber() + "\n"
            + "• Check-In Time: " + DateUtil.formatDisplayDateTime(DateUtil.getCurrentDbDateTime()) + "\n\n"
            + "Key will be issued to guest. Proceed?",
            "Confirm Guest Check-In", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reservationService.checkIn(selectedReservation.getReservationId());
                JOptionPane.showMessageDialog(this,
                    "Check-In successful! Guest " + selectedReservation.getCustomerName() + " checked into Room " + selectedReservation.getRoomNumber() + ".",
                    "Check-In Confirmed", JOptionPane.INFORMATION_MESSAGE);

                loadConfirmedReservations();
                mainFrame.notifyDataChanged();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-In Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        try {
            List<Reservation> list = reservationService.searchReservations(query, "CONFIRMED");
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
        lblDates.setText("-");
        lblNights.setText("-");
        lblEstimatedAmount.setText("-");
        lblStatus.setText("-");
        confirmedTable.clearSelection();
    }
}
