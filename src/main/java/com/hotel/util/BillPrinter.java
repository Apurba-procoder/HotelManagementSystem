package com.hotel.util;

import com.hotel.model.Bill;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Utility for rendering, formatting, and printing customer invoices using standard Java Printing API.
 */
public class BillPrinter implements Printable {

    public static final String HOTEL_NAME = "Grand Horizon Hotel & Suites";
    public static final String HOTEL_ADDRESS = "100 Oceanview Boulevard, Palm Beach, FL 33480";
    public static final String HOTEL_CONTACT = "Phone: +1 (800) 555-ROOM | Email: reservations@grandhorizon.com";

    private final Bill bill;

    public BillPrinter(Bill bill) {
        this.bill = bill;
    }

    /**
     * Generates a beautifully styled HTML invoice for preview and printing.
     */
    public static String generateInvoiceHtml(Bill bill) {
        if (bill == null) return "<html><body><h3>No Bill Selected</h3></body></html>";

        String statusColor = "PAID".equalsIgnoreCase(bill.getPaymentStatus()) ? "#16a34a" :
                             "PARTIAL".equalsIgnoreCase(bill.getPaymentStatus()) ? "#d97706" : "#dc2626";

        return """
            <html>
            <head>
            <style>
                body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    margin: 25px;
                    color: #1e293b;
                    background-color: #ffffff;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #2563eb;
                    padding-bottom: 12px;
                    margin-bottom: 20px;
                }
                .hotel-title {
                    font-size: 24px;
                    font-weight: bold;
                    color: #1e3a8a;
                    margin: 0;
                }
                .hotel-sub {
                    font-size: 11px;
                    color: #64748b;
                    margin-top: 4px;
                }
                .invoice-meta {
                    width: 100%%;
                    margin-bottom: 18px;
                }
                .meta-col {
                    vertical-align: top;
                    font-size: 12px;
                }
                .badge {
                    display: inline-block;
                    padding: 4px 10px;
                    border-radius: 4px;
                    font-weight: bold;
                    color: #ffffff;
                    background-color: %s;
                }
                table.details-table {
                    width: 100%%;
                    border-collapse: collapse;
                    margin-top: 15px;
                    font-size: 12px;
                }
                table.details-table th {
                    background-color: #f1f5f9;
                    color: #334155;
                    text-align: left;
                    padding: 8px 10px;
                    border-bottom: 1px solid #cbd5e1;
                }
                table.details-table td {
                    padding: 8px 10px;
                    border-bottom: 1px solid #e2e8f0;
                }
                .calc-section {
                    margin-top: 20px;
                    width: 100%%;
                }
                .calc-table {
                    width: 280px;
                    float: right;
                    border-collapse: collapse;
                    font-size: 12px;
                }
                .calc-table td {
                    padding: 5px 8px;
                }
                .calc-total {
                    font-size: 14px;
                    font-weight: bold;
                    color: #1e3a8a;
                    border-top: 2px solid #cbd5e1;
                    padding-top: 8px;
                }
                .footer {
                    margin-top: 45px;
                    text-align: center;
                    font-size: 11px;
                    color: #94a3b8;
                    border-top: 1px dashed #cbd5e1;
                    padding-top: 12px;
                }
            </style>
            </head>
            <body>
                <div class="header">
                    <div class="hotel-title">%s</div>
                    <div class="hotel-sub">%s</div>
                    <div class="hotel-sub">%s</div>
                </div>

                <table class="invoice-meta">
                    <tr>
                        <td class="meta-col" style="width: 50%%;">
                            <strong>BILLED TO:</strong><br>
                            <strong>Guest Name:</strong> %s<br>
                            <strong>Phone:</strong> %s<br>
                            <strong>Reservation ID:</strong> #%d<br>
                            <strong>Room Booked:</strong> Room %s (%s)
                        </td>
                        <td class="meta-col" style="width: 50%%; text-align: right;">
                            <strong>INVOICE NO:</strong> INV-%05d<br>
                            <strong>Invoice Date:</strong> %s<br>
                            <strong>Payment Method:</strong> %s<br>
                            <strong>Payment Status:</strong> <span class="badge">%s</span>
                        </td>
                    </tr>
                </table>

                <table class="details-table">
                    <thead>
                        <tr>
                            <th>Description</th>
                            <th>Stay Period</th>
                            <th style="text-align: center;">Nights</th>
                            <th style="text-align: right;">Rate / Night</th>
                            <th style="text-align: right;">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Accommodation: Room %s (%s)</td>
                            <td>%s to %s</td>
                            <td style="text-align: center;">%d</td>
                            <td style="text-align: right;">$%.2f</td>
                            <td style="text-align: right;">$%.2f</td>
                        </tr>
                    </tbody>
                </table>

                <table style="width: 100%%; margin-top: 15px;">
                    <tr>
                        <td style="vertical-align: top; font-size: 11px; color: #64748b;">
                            <strong>Terms & Notes:</strong><br>
                            Thank you for staying at %s.<br>
                            Standard check-out time is 11:00 AM.<br>
                            For inquiries, please keep your invoice number handy.
                        </td>
                        <td style="width: 300px; vertical-align: top;">
                            <table style="width: 100%%; font-size: 12px;">
                                <tr>
                                    <td>Accommodation Subtotal:</td>
                                    <td style="text-align: right;"><strong>$%.2f</strong></td>
                                </tr>
                                <tr>
                                    <td>Taxes & Fees:</td>
                                    <td style="text-align: right;">+$%.2f</td>
                                </tr>
                                <tr>
                                    <td>Special Discount:</td>
                                    <td style="text-align: right; color: #16a34a;">-$%.2f</td>
                                </tr>
                                <tr style="border-top: 2px solid #2563eb;">
                                    <td style="font-size: 15px; font-weight: bold; color: #1e3a8a; padding-top: 8px;">Grand Total:</td>
                                    <td style="font-size: 15px; font-weight: bold; color: #1e3a8a; text-align: right; padding-top: 8px;">$%.2f</td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>

                <div class="footer">
                    *** Thank you for your business! Have a safe and pleasant journey! ***
                </div>
            </body>
            </html>
        """.formatted(
            statusColor,
            HOTEL_NAME,
            HOTEL_ADDRESS,
            HOTEL_CONTACT,
            safe(bill.getCustomerName()),
            safe(bill.getCustomerPhone()),
            bill.getReservationId(),
            safe(bill.getRoomNumber()),
            safe(bill.getRoomType()),
            bill.getBillId(),
            DateUtil.formatDisplayDateTime(bill.getBillDate()),
            bill.getPaymentMethod(),
            bill.getPaymentStatus(),
            safe(bill.getRoomNumber()),
            safe(bill.getRoomType()),
            DateUtil.formatDisplayDate(bill.getCheckInDate()),
            DateUtil.formatDisplayDate(bill.getCheckOutDate()),
            bill.getNumberOfNights(),
            bill.getPricePerNight(),
            bill.getAccommodationCharge(),
            HOTEL_NAME,
            bill.getAccommodationCharge(),
            bill.getTaxAmount(),
            bill.getDiscount(),
            bill.getGrandTotal()
        );
    }

    private static String safe(String s) {
        return s != null ? s : "-";
    }

    /**
     * Prints the bill using Java's native PrinterJob.
     */
    public static void printBill(Bill bill, Component parent) {
        if (bill == null) {
            JOptionPane.showMessageDialog(parent, "No bill selected to print.", "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Invoice_INV-" + String.format("%05d", bill.getBillId()));
        job.setPrintable(new BillPrinter(bill));

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parent, "Invoice sent to printer successfully!", "Print Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(parent, "Printing failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = 40;
        int x = 40;
        int width = (int) pageFormat.getImageableWidth() - 80;

        // Header
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString(HOTEL_NAME, x, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString(HOTEL_ADDRESS, x, y);
        y += 14;
        g2d.drawString(HOTEL_CONTACT, x, y);
        y += 15;

        // Divider
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        // Meta
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("INVOICE: INV-" + String.format("%05d", bill.getBillId()), x, y);
        g2d.drawString("Date: " + DateUtil.formatDisplayDateTime(bill.getBillDate()), x + 280, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString("Customer: " + safe(bill.getCustomerName()) + " (Phone: " + safe(bill.getCustomerPhone()) + ")", x, y);
        y += 16;
        g2d.drawString("Reservation ID: #" + bill.getReservationId() + " | Room: " + safe(bill.getRoomNumber()) + " (" + safe(bill.getRoomType()) + ")", x, y);
        y += 16;
        g2d.drawString("Stay: " + DateUtil.formatDisplayDate(bill.getCheckInDate()) + " to " + DateUtil.formatDisplayDate(bill.getCheckOutDate()) +
                " (" + bill.getNumberOfNights() + " nights)", x, y);
        y += 20;

        // Charges
        g2d.drawLine(x, y, x + width, y);
        y += 18;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.drawString("Item Description", x, y);
        g2d.drawString("Rate", x + 240, y);
        g2d.drawString("Nights", x + 310, y);
        g2d.drawString("Amount", x + 380, y);
        y += 8;
        g2d.drawLine(x, y, x + width, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString("Room Accommodation (" + safe(bill.getRoomType()) + ")", x, y);
        g2d.drawString("$" + String.format("%.2f", bill.getPricePerNight()), x + 240, y);
        g2d.drawString(String.valueOf(bill.getNumberOfNights()), x + 320, y);
        g2d.drawString("$" + String.format("%.2f", bill.getAccommodationCharge()), x + 380, y);
        y += 25;

        // Totals
        g2d.drawLine(x + 220, y, x + width, y);
        y += 18;
        g2d.drawString("Subtotal:", x + 240, y);
        g2d.drawString("$" + String.format("%.2f", bill.getAccommodationCharge()), x + 380, y);
        y += 16;
        g2d.drawString("Taxes & Fees:", x + 240, y);
        g2d.drawString("+$" + String.format("%.2f", bill.getTaxAmount()), x + 380, y);
        y += 16;
        g2d.drawString("Discount:", x + 240, y);
        g2d.drawString("-$" + String.format("%.2f", bill.getDiscount()), x + 380, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.drawString("Grand Total:", x + 240, y);
        g2d.drawString("$" + String.format("%.2f", bill.getGrandTotal()), x + 380, y);
        y += 22;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString("Payment Method: " + bill.getPaymentMethod() + " | Payment Status: " + bill.getPaymentStatus(), x, y);
        y += 30;

        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2d.drawString("Thank you for choosing " + HOTEL_NAME + "!", x + 80, y);

        return PAGE_EXISTS;
    }
}
