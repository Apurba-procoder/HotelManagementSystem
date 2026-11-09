package com.hotel.ui;

import com.hotel.model.Bill;
import com.hotel.model.Reservation;
import com.hotel.service.BillingService;
import com.hotel.service.ReservationService;
import com.hotel.util.BillPrinter;
import com.hotel.util.DateUtil;
import com.hotel.util.UITheme;
import com.hotel.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import java.util.List;

/**
 * Module 7: Billing and Payment Management
 * Bill generation, tax/discount calculation, payment tracking, invoice preview, and printing.
 */
public class BillingPanel extends JPanel {

    private final BillingService billingService;
    private final ReservationService reservationService;
    private final MainFrame mainFrame;

    private JTable billTable;
    private DefaultTableModel tableModel;

    // Bill Generation Fields
    private JComboBox<ReservationItem> cmbReservation;
    private JTextField txtNights;
    private JTextField txtRatePerNight;
    private JTextField txtAccommodationSubtotal;
    private JTextField txtTaxPercent;
    private JTextField txtDiscount;
    private JLabel lblGrandTotal;
    private JComboBox<String> cmbPaymentMethod;
    private JComboBox<String> cmbPaymentStatus;

    // Search & Filter
    private JTextField txtSearch;
    private JComboBox<String> cmbPaymentFilter;

    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.billingService = new BillingService();
        this.reservationService = new ReservationService();

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initUI();
        refreshUnbilledReservations();
        loadAllBills();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Billing & Payment Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Generate official hotel invoices, track Cash/Card/UPI transactions, and print guest receipts");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content: Left = Create Bill Card (400px), Right = Invoices History Table
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // LEFT: Invoice Generation Card
        JPanel billCard = UITheme.createCardPanel();
        billCard.setPreferredSize(new Dimension(410, 620));
        billCard.setLayout(new BorderLayout(0, 14));

        JLabel billTitle = new JLabel("Generate Customer Invoice");
        billTitle.setFont(UITheme.FONT_HEADER);
        billTitle.setForeground(UITheme.TEXT_PRIMARY);
        billCard.add(billTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        cmbReservation = new JComboBox<>();
        UITheme.styleComboBox(cmbReservation);
        cmbReservation.addActionListener(e -> onReservationSelected());

        txtNights = new JTextField();
        txtNights.setEditable(false);
        txtNights.setBackground(new Color(241, 245, 249));
        UITheme.styleTextField(txtNights);

        txtRatePerNight = new JTextField();
        txtRatePerNight.setEditable(false);
        txtRatePerNight.setBackground(new Color(241, 245, 249));
        UITheme.styleTextField(txtRatePerNight);

        txtAccommodationSubtotal = new JTextField();
        txtAccommodationSubtotal.setEditable(false);
        txtAccommodationSubtotal.setBackground(new Color(241, 245, 249));
        UITheme.styleTextField(txtAccommodationSubtotal);

        txtTaxPercent = new JTextField("12.0"); // Default 12% tax
        UITheme.styleTextField(txtTaxPercent);

        txtDiscount = new JTextField("0.0");
        UITheme.styleTextField(txtDiscount);

        // Real-time calculation listener
        DocumentListener calcListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calculateGrandTotal(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calculateGrandTotal(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calculateGrandTotal(); }
        };
        txtTaxPercent.getDocument().addDocumentListener(calcListener);
        txtDiscount.getDocument().addDocumentListener(calcListener);

        cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Card", "UPI"});
        UITheme.styleComboBox(cmbPaymentMethod);

        cmbPaymentStatus = new JComboBox<>(new String[]{"PAID", "UNPAID", "PARTIAL"});
        UITheme.styleComboBox(cmbPaymentStatus);

        lblGrandTotal = new JLabel("$0.00");
        lblGrandTotal.setFont(UITheme.FONT_METRIC);
        lblGrandTotal.setForeground(UITheme.PRIMARY_BLUE);

        addFormField(formFields, gbc, 0, "Select Reservation *:", cmbReservation);
        addFormField(formFields, gbc, 1, "Duration of Stay (Nights):", txtNights);
        addFormField(formFields, gbc, 2, "Room Price Per Night ($):", txtRatePerNight);
        addFormField(formFields, gbc, 3, "Accommodation Subtotal ($):", txtAccommodationSubtotal);
        addFormField(formFields, gbc, 4, "Applicable Tax Rate (%):", txtTaxPercent);
        addFormField(formFields, gbc, 5, "Discount Applied ($):", txtDiscount);
        addFormField(formFields, gbc, 6, "Payment Method *:", cmbPaymentMethod);
        addFormField(formFields, gbc, 7, "Payment Status *:", cmbPaymentStatus);

        JPanel totalBox = new JPanel(new BorderLayout(8, 0));
        totalBox.setBackground(new Color(241, 245, 249));
        totalBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        JLabel totalTitle = new JLabel("Grand Total Payable:");
        totalTitle.setFont(UITheme.FONT_BOLD);
        totalTitle.setForeground(UITheme.TEXT_PRIMARY);
        totalBox.add(totalTitle, BorderLayout.WEST);
        totalBox.add(lblGrandTotal, BorderLayout.EAST);

        gbc.gridy = 16;
        gbc.insets = new Insets(10, 4, 10, 4);
        formFields.add(totalBox, gbc);

        // Actions
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnGenerate = UITheme.createPrimaryButton("Generate Bill");
        btnGenerate.addActionListener(e -> handleGenerateBill());

        JButton btnReset = UITheme.createOutlineButton("Reset");
        btnReset.addActionListener(e -> resetForm());

        actionPanel.add(btnGenerate);
        actionPanel.add(btnReset);

        JScrollPane formScroll = new JScrollPane(formFields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);

        billCard.add(formScroll, BorderLayout.CENTER);
        billCard.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(billCard, BorderLayout.WEST);

        // RIGHT: Search Toolbar, History Table & Preview / Print buttons
        JPanel rightPanel = new JPanel(new BorderLayout(0, 16));
        rightPanel.setOpaque(false);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchCard.add(new JLabel("Payment Status:"));
        cmbPaymentFilter = new JComboBox<>(new String[]{"All", "PAID", "UNPAID", "PARTIAL"});
        UITheme.styleComboBox(cmbPaymentFilter);
        cmbPaymentFilter.addActionListener(e -> handleSearch());
        searchCard.add(cmbPaymentFilter);

        searchCard.add(new JLabel("Search Invoice:"));
        txtSearch = new JTextField(15);
        UITheme.styleTextField(txtSearch);
        searchCard.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> handleSearch());
        searchCard.add(btnSearch);

        JButton btnViewAll = UITheme.createOutlineButton("View All");
        btnViewAll.addActionListener(e -> loadAllBills());
        searchCard.add(btnViewAll);

        rightPanel.add(searchCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] cols = {"Bill #", "Res #", "Guest Name", "Phone", "Room", "Nights", "Total ($)", "Method", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        UITheme.styleTable(billTable);
        billTable.getColumnModel().getColumn(8).setCellRenderer(new UITheme.StatusBadgeRenderer());
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        billTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        billTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        billTable.getColumnModel().getColumn(5).setPreferredWidth(45);

        JScrollPane tableScroll = new JScrollPane(billTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);

        // Bottom Table Toolbar: View Invoice Preview, Print Bill, Update Payment
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton btnUpdatePay = UITheme.createOutlineButton("Mark as Paid");
        btnUpdatePay.addActionListener(e -> handleMarkAsPaid());

        JButton btnPreview = UITheme.createPrimaryButton("👁 View Invoice Preview");
        btnPreview.addActionListener(e -> handlePreviewInvoice());

        JButton btnPrint = UITheme.createSuccessButton("⎙ Print Invoice");
        btnPrint.addActionListener(e -> handlePrintInvoice());

        bottomBar.add(btnUpdatePay);
        bottomBar.add(btnPreview);
        bottomBar.add(btnPrint);

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

    public void refreshUnbilledReservations() {
        try {
            List<Reservation> all = reservationService.getAllReservations();
            cmbReservation.removeAllItems();
            for (Reservation r : all) {
                // Show reservations that do not have a bill yet, especially CHECKED_OUT or CHECKED_IN
                if (!billingService.hasBill(r.getReservationId())) {
                    cmbReservation.addItem(new ReservationItem(
                        r.getReservationId(),
                        r.getCustomerName(),
                        r.getRoomNumber(),
                        r.getNumberOfNights(),
                        r.getPricePerNight(),
                        r.getTotalEstimatedAmount(),
                        r.getReservationStatus()
                    ));
                }
            }
            onReservationSelected();
        } catch (SQLException e) {
            System.err.println("Failed to populate reservations: " + e.getMessage());
        }
    }

    public void selectReservation(int resId) {
        refreshUnbilledReservations();
        for (int i = 0; i < cmbReservation.getItemCount(); i++) {
            if (cmbReservation.getItemAt(i).resId() == resId) {
                cmbReservation.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onReservationSelected() {
        ReservationItem item = (ReservationItem) cmbReservation.getSelectedItem();
        if (item != null) {
            txtNights.setText(String.valueOf(item.nights()));
            txtRatePerNight.setText(String.format("%.2f", item.rate()));
            txtAccommodationSubtotal.setText(String.format("%.2f", item.nights() * item.rate()));
        } else {
            txtNights.setText("0");
            txtRatePerNight.setText("0.00");
            txtAccommodationSubtotal.setText("0.00");
        }
        calculateGrandTotal();
    }

    private void calculateGrandTotal() {
        try {
            double subtotal = 0.0;
            if (ValidationUtil.isNotEmpty(txtAccommodationSubtotal.getText())) {
                subtotal = Double.parseDouble(txtAccommodationSubtotal.getText().trim());
            }

            double taxPercent = 0.0;
            if (ValidationUtil.isNonNegativeNumber(txtTaxPercent.getText())) {
                taxPercent = Double.parseDouble(txtTaxPercent.getText().trim());
            }

            double discount = 0.0;
            if (ValidationUtil.isNonNegativeNumber(txtDiscount.getText())) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }

            double taxAmount = (subtotal * taxPercent) / 100.0;
            double grandTotal = Math.max(0.0, subtotal + taxAmount - discount);

            lblGrandTotal.setText("$" + String.format("%.2f", grandTotal));
        } catch (NumberFormatException e) {
            lblGrandTotal.setText("$0.00");
        }
    }

    public void loadAllBills() {
        try {
            List<Bill> list = billingService.getAllBills();
            displayBills(list);
            txtSearch.setText("");
            cmbPaymentFilter.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load invoices: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayBills(List<Bill> list) {
        tableModel.setRowCount(0);
        for (Bill b : list) {
            tableModel.addRow(new Object[]{
                b.getBillId(),
                b.getReservationId(),
                b.getCustomerName(),
                b.getCustomerPhone(),
                b.getRoomNumber(),
                b.getNumberOfNights(),
                "$" + String.format("%.2f", b.getGrandTotal()),
                b.getPaymentMethod(),
                b.getPaymentStatus(),
                DateUtil.formatDisplayDateTime(b.getBillDate())
            });
        }
    }

    private void handleGenerateBill() {
        ReservationItem item = (ReservationItem) cmbReservation.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an unbilled reservation.", "Reservation Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isNonNegativeNumber(txtTaxPercent.getText())) {
            JOptionPane.showMessageDialog(this, "Tax percent must be a valid non-negative number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isNonNegativeNumber(txtDiscount.getText())) {
            JOptionPane.showMessageDialog(this, "Discount must be a valid non-negative number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double tax = Double.parseDouble(txtTaxPercent.getText().trim());
        double discount = Double.parseDouble(txtDiscount.getText().trim());
        String method = (String) cmbPaymentMethod.getSelectedItem();
        String status = (String) cmbPaymentStatus.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Invoice Generation:\n"
            + "• Guest: " + item.guestName() + "\n"
            + "• Reservation: #" + item.resId() + " (Room " + item.room() + ")\n"
            + "• Total: " + lblGrandTotal.getText() + " (" + status + " via " + method + ")\n\n"
            + "Generate customer invoice?",
            "Confirm Billing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int billId = billingService.generateBill(item.resId(), tax, discount, method, status);
                JOptionPane.showMessageDialog(this, "Bill INV-" + String.format("%05d", billId) + " generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                loadAllBills();
                refreshUnbilledReservations();
                mainFrame.notifyDataChanged();

                // Open preview dialog right away
                Bill bill = billingService.getBillById(billId);
                showInvoiceDialog(bill);

            } catch (IllegalStateException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Billing Error", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePreviewInvoice() {
        Bill b = getSelectedBill();
        if (b != null) {
            showInvoiceDialog(b);
        }
    }

    private void handlePrintInvoice() {
        Bill b = getSelectedBill();
        if (b != null) {
            BillPrinter.printBill(b, this);
        }
    }

    private void handleMarkAsPaid() {
        Bill b = getSelectedBill();
        if (b != null) {
            if ("PAID".equalsIgnoreCase(b.getPaymentStatus())) {
                JOptionPane.showMessageDialog(this, "Bill INV-" + String.format("%05d", b.getBillId()) + " is already marked as PAID.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] methods = {"Cash", "Card", "UPI"};
            String chosenMethod = (String) JOptionPane.showInputDialog(this,
                "Select Payment Method for settlement:", "Settle Payment",
                JOptionPane.QUESTION_MESSAGE, null, methods, b.getPaymentMethod());

            if (chosenMethod != null) {
                try {
                    billingService.updatePayment(b.getBillId(), "PAID", chosenMethod);
                    JOptionPane.showMessageDialog(this, "Invoice #" + b.getBillId() + " marked as PAID successfully!", "Payment Settled", JOptionPane.INFORMATION_MESSAGE);
                    loadAllBills();
                    mainFrame.notifyDataChanged();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating payment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private Bill getSelectedBill() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int billId = (int) tableModel.getValueAt(row, 0);
        try {
            return billingService.getBillById(billId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void showInvoiceDialog(Bill bill) {
        if (bill == null) return;

        JDialog dlg = new JDialog(mainFrame, "Invoice Preview - INV-" + String.format("%05d", bill.getBillId()), true);
        dlg.setLayout(new BorderLayout(12, 12));
        dlg.setSize(620, 680);
        dlg.setLocationRelativeTo(this);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(BillPrinter.generateInvoiceHtml(bill));
        editorPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(editorPane);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnPrint = UITheme.createSuccessButton("⎙ Print Invoice");
        btnPrint.addActionListener(e -> {
            BillPrinter.printBill(bill, dlg);
        });

        JButton btnClose = UITheme.createOutlineButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);

        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        String status = (String) cmbPaymentFilter.getSelectedItem();
        try {
            List<Bill> list = billingService.searchBills(query, status);
            displayBills(list);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtTaxPercent.setText("12.0");
        txtDiscount.setText("0.0");
        cmbPaymentMethod.setSelectedIndex(0);
        cmbPaymentStatus.setSelectedIndex(0);
        refreshUnbilledReservations();
    }

    public record ReservationItem(int resId, String guestName, String room, int nights, double rate, double estTotal, String status) {
        @Override
        public String toString() {
            return "#" + resId + " - " + guestName + " (Room " + room + ", " + nights + " nts, " + status + ")";
        }
    }
}
