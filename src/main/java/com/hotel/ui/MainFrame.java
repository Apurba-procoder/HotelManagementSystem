package com.hotel.ui;

import com.hotel.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Application Window.
 * Contains the navy sidebar navigation and hosts the CardLayout modules.
 */
public class MainFrame extends JFrame {

    public static final String PANEL_DASHBOARD = "Dashboard";
    public static final String PANEL_ROOMS = "Room Management";
    public static final String PANEL_CUSTOMERS = "Customer Management";
    public static final String PANEL_BOOKING = "Room Booking";
    public static final String PANEL_CHECKIN = "Check-in";
    public static final String PANEL_CHECKOUT = "Check-out";
    public static final String PANEL_BILLING = "Billing and Payments";

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;
    private final Map<String, SidebarButton> navButtons;
    private String currentPanelName;

    // Module Panels
    private DashboardPanel dashboardPanel;
    private RoomPanel roomPanel;
    private CustomerPanel customerPanel;
    private BookingPanel bookingPanel;
    private CheckInPanel checkInPanel;
    private CheckOutPanel checkOutPanel;
    private BillingPanel billingPanel;

    public MainFrame() {
        super("Grand Horizon - Hotel Room Booking Management System");
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
        this.navButtons = new HashMap<>();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmExit();
            }
        });

        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        initModules();
        initLayout();

        showPanel(PANEL_DASHBOARD);
    }

    private void initModules() {
        dashboardPanel = new DashboardPanel(this);
        roomPanel = new RoomPanel(this);
        customerPanel = new CustomerPanel(this);
        bookingPanel = new BookingPanel(this);
        checkInPanel = new CheckInPanel(this);
        checkOutPanel = new CheckOutPanel(this);
        billingPanel = new BillingPanel(this);

        cardsPanel.add(dashboardPanel, PANEL_DASHBOARD);
        cardsPanel.add(roomPanel, PANEL_ROOMS);
        cardsPanel.add(customerPanel, PANEL_CUSTOMERS);
        cardsPanel.add(bookingPanel, PANEL_BOOKING);
        cardsPanel.add(checkInPanel, PANEL_CHECKIN);
        cardsPanel.add(checkOutPanel, PANEL_CHECKOUT);
        cardsPanel.add(billingPanel, PANEL_BILLING);
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CONTENT_BG);

        // Navy Sidebar
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);
        root.add(cardsPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        // Brand Banner
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(28, 20, 24, 20));

        JLabel brandTitle = new JLabel("GRAND HORIZON");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("HOTEL MANAGEMENT");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandSub.setForeground(UITheme.SIDEBAR_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(brandSub);

        // Navigation Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        String[] menuItems = {
            PANEL_DASHBOARD,
            PANEL_ROOMS,
            PANEL_CUSTOMERS,
            PANEL_BOOKING,
            PANEL_CHECKIN,
            PANEL_CHECKOUT,
            PANEL_BILLING
        };

        for (String item : menuItems) {
            SidebarButton btn = new SidebarButton(item);
            btn.addActionListener(e -> showPanel(item));
            navButtons.put(item, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        // Bottom Exit Button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 20, 12));

        SidebarButton exitBtn = new SidebarButton("Exit System");
        exitBtn.setForeground(new Color(248, 113, 113));
        exitBtn.addActionListener(e -> confirmExit());
        bottomPanel.add(exitBtn, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    public void showPanel(String name) {
        this.currentPanelName = name;
        cardLayout.show(cardsPanel, name);

        // Update sidebar button states
        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }

        // Refresh data on panel entry
        switch (name) {
            case PANEL_DASHBOARD -> dashboardPanel.refreshData();
            case PANEL_ROOMS -> roomPanel.loadAllRooms();
            case PANEL_CUSTOMERS -> customerPanel.loadAllCustomers();
            case PANEL_BOOKING -> {
                bookingPanel.refreshCustomerDropdown();
                bookingPanel.refreshAvailableRooms();
                bookingPanel.loadAllReservations();
            }
            case PANEL_CHECKIN -> checkInPanel.loadConfirmedReservations();
            case PANEL_CHECKOUT -> checkOutPanel.loadCheckedInGuests();
            case PANEL_BILLING -> {
                billingPanel.refreshUnbilledReservations();
                billingPanel.loadAllBills();
            }
        }
    }

    public void showBillingForReservation(int resId) {
        showPanel(PANEL_BILLING);
        billingPanel.selectReservation(resId);
    }

    /**
     * Called whenever any module makes database modifications to keep all panels in sync.
     */
    public void notifyDataChanged() {
        dashboardPanel.refreshData();
        bookingPanel.refreshCustomerDropdown();
        bookingPanel.refreshAvailableRooms();
        billingPanel.refreshUnbilledReservations();
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to close the Hotel Room Booking System?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Custom styled navigation button for the dark navy sidebar.
     */
    private static class SidebarButton extends JButton {
        private boolean active = false;

        public SidebarButton(String text) {
            super(text);
            setFont(UITheme.FONT_BOLD);
            setForeground(UITheme.SIDEBAR_TEXT);
            setBackground(UITheme.SIDEBAR_BG);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(226, 40));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setBackground(UITheme.SIDEBAR_HOVER);
                        repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setBackground(UITheme.SIDEBAR_BG);
                        repaint();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(UITheme.SIDEBAR_ACTIVE);
                setForeground(Color.WHITE);
            } else {
                setBackground(UITheme.SIDEBAR_BG);
                setForeground(UITheme.SIDEBAR_TEXT);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
