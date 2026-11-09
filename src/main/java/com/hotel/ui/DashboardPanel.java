package com.hotel.ui;

import com.hotel.model.Reservation;
import com.hotel.service.DashboardService;
import com.hotel.util.DateUtil;
import com.hotel.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;

/**
 * Dashboard Panel displaying summary KPI metrics, recent activity, and quick navigation actions.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;
    private final MainFrame mainFrame;

    private JLabel totalRoomsLabel;
    private JLabel availableRoomsLabel;
    private JLabel occupiedRoomsLabel;
    private JLabel totalCustomersLabel;
    private JLabel activeReservationsLabel;
    private JLabel totalRevenueLabel;

    private DefaultTableModel recentTableModel;
    private JTable recentTable;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardService = new DashboardService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Operations Dashboard");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Real-time hotel occupancy, customer metrics, and booking overview");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton refreshBtn = UITheme.createOutlineButton("↻ Refresh Data");
        refreshBtn.addActionListener(e -> refreshData());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Center Content with KPI Cards & Recent Activity
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Grid of 6 Metric Cards
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsGrid.setOpaque(false);

        totalRoomsLabel = new JLabel("0");
        availableRoomsLabel = new JLabel("0");
        occupiedRoomsLabel = new JLabel("0");
        totalCustomersLabel = new JLabel("0");
        activeReservationsLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("$0.00");

        cardsGrid.add(createMetricCard("TOTAL ROOMS", totalRoomsLabel, "Hotel inventory capacity", new Color(37, 99, 235)));
        cardsGrid.add(createMetricCard("AVAILABLE ROOMS", availableRoomsLabel, "Vacant for booking today", new Color(22, 163, 74)));
        cardsGrid.add(createMetricCard("OCCUPIED ROOMS", occupiedRoomsLabel, "Currently checked-in guests", new Color(217, 119, 6)));
        cardsGrid.add(createMetricCard("TOTAL CUSTOMERS", totalCustomersLabel, "Registered guest profiles", new Color(124, 58, 237)));
        cardsGrid.add(createMetricCard("ACTIVE RESERVATIONS", activeReservationsLabel, "Confirmed & checked-in", new Color(14, 165, 233)));
        cardsGrid.add(createMetricCard("TOTAL REVENUE", totalRevenueLabel, "Collected paid invoices", new Color(5, 150, 105)));

        centerPanel.add(cardsGrid);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Quick Actions Bar
        JPanel quickActionsCard = UITheme.createCardPanel();
        quickActionsCard.setLayout(new BorderLayout());

        JLabel qaTitle = new JLabel("Quick Management Actions");
        qaTitle.setFont(UITheme.FONT_HEADER);
        qaTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        buttonsBar.setOpaque(false);

        JButton btnBook = UITheme.createPrimaryButton("+ New Room Booking");
        btnBook.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_BOOKING));

        JButton btnCheckIn = UITheme.createSuccessButton("✓ Check-in Guest");
        btnCheckIn.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_CHECKIN));

        JButton btnCheckOut = UITheme.createButton("➜ Check-out & Bill", new Color(13, 148, 136), new Color(15, 118, 110), Color.WHITE);
        btnCheckOut.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_CHECKOUT));

        JButton btnRoomMgmt = UITheme.createOutlineButton("Manage Rooms");
        btnRoomMgmt.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_ROOMS));

        JButton btnCustMgmt = UITheme.createOutlineButton("Customer Directory");
        btnCustMgmt.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_CUSTOMERS));

        buttonsBar.add(btnBook);
        buttonsBar.add(btnCheckIn);
        buttonsBar.add(btnCheckOut);
        buttonsBar.add(btnRoomMgmt);
        buttonsBar.add(btnCustMgmt);

        quickActionsCard.add(qaTitle, BorderLayout.NORTH);
        quickActionsCard.add(buttonsBar, BorderLayout.CENTER);

        centerPanel.add(quickActionsCard);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Recent Activity Card with Table
        JPanel recentCard = UITheme.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 12));

        JLabel recentTitle = new JLabel("Recent Reservation Activity");
        recentTitle.setFont(UITheme.FONT_HEADER);
        recentTitle.setForeground(UITheme.TEXT_PRIMARY);

        String[] columns = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-in", "Check-out", "Nights", "Amount", "Status"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(recentTableModel);
        UITheme.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(9).setCellRenderer(new UITheme.StatusBadgeRenderer());
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        recentTable.getColumnModel().getColumn(7).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(recentCard.getWidth(), 220));

        recentCard.add(recentTitle, BorderLayout.NORTH);
        recentCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(recentCard);

        JScrollPane mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String description, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Top accent strip
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(new Color(148, 163, 184));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLbl, BorderLayout.SOUTH);

        return card;
    }

    public void refreshData() {
        try {
            DashboardService.DashboardStats stats = dashboardService.getStats();
            totalRoomsLabel.setText(String.valueOf(stats.totalRooms()));
            availableRoomsLabel.setText(String.valueOf(stats.availableRooms()));
            occupiedRoomsLabel.setText(String.valueOf(stats.occupiedRooms()));
            totalCustomersLabel.setText(String.valueOf(stats.totalCustomers()));
            activeReservationsLabel.setText(String.valueOf(stats.activeReservations()));
            totalRevenueLabel.setText("$" + String.format("%,.2f", stats.totalRevenue()));

            // Reload recent bookings table
            recentTableModel.setRowCount(0);
            List<Reservation> recentList = dashboardService.getRecentReservations(8);
            for (Reservation r : recentList) {
                recentTableModel.addRow(new Object[]{
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
        } catch (SQLException e) {
            System.err.println("Error refreshing dashboard stats: " + e.getMessage());
        }
    }
}
