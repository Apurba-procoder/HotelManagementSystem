package com.hotel.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Hotel Management UI Theme, design tokens, and component styling helpers.
 */
public class UITheme {

    // Palette Colors
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);       // Dark Navy #0F172A
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);    // Slate 800
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);   // Accent Blue #2563EB
    public static final Color SIDEBAR_TEXT = new Color(241, 245, 249);
    public static final Color SIDEBAR_MUTED = new Color(148, 163, 184);

    public static final Color CONTENT_BG = new Color(248, 250, 252);    // Clean slate #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);  // Soft border #E2E8F0

    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);    // #2563EB
    public static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216);
    public static final Color SUCCESS_GREEN = new Color(22, 163, 74);   // #16A34A
    public static final Color SUCCESS_GREEN_HOVER = new Color(21, 128, 61);
    public static final Color WARNING_AMBER = new Color(217, 119, 6);   // #D97706
    public static final Color DANGER_RED = new Color(220, 38, 38);      // #DC2626
    public static final Color DANGER_RED_HOVER = new Color(185, 28, 28);
    public static final Color SECONDARY_GRAY = new Color(100, 116, 139);

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    public static final Color TABLE_SELECTION_BG = new Color(224, 242, 254);
    public static final Color TABLE_SELECTION_FG = new Color(15, 23, 42);

    // Standard Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 26);

    private UITheme() {}

    /**
     * Creates a styled card panel with rounded border and white background.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return card;
    }

    /**
     * Creates a primary accent button with smooth hover effect.
     */
    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY_BLUE, PRIMARY_BLUE_HOVER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS_GREEN, SUCCESS_GREEN_HOVER, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER_RED, DANGER_RED_HOVER, Color.WHITE);
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static JButton createButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });
        return btn;
    }

    /**
     * Styles text field with clean padding and border.
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
    }

    /**
     * Styles combo box.
     */
    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(FONT_REGULAR);
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
    }

    /**
     * Styles standard JTable with modern headers, row height, and alternating row colors.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TABLE_SELECTION_FG);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        // Default cell renderer with left padding and status pill support
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
    }

    /**
     * Status Badge Table Cell Renderer for reservations, bills, and room statuses.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_BOLD);

            if (value != null) {
                String val = value.toString().toUpperCase();
                switch (val) {
                    case "CONFIRMED", "ACTIVE" -> {
                        label.setBackground(new Color(224, 242, 254));
                        label.setForeground(new Color(2, 132, 199));
                    }
                    case "CHECKED_IN" -> {
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(180, 83, 9));
                    }
                    case "CHECKED_OUT", "PAID" -> {
                        label.setBackground(new Color(220, 252, 231));
                        label.setForeground(new Color(22, 101, 52));
                    }
                    case "CANCELLED", "UNPAID", "MAINTENANCE" -> {
                        label.setBackground(new Color(254, 226, 226));
                        label.setForeground(new Color(185, 28, 28));
                    }
                    case "PARTIAL" -> {
                        label.setBackground(new Color(254, 249, 195));
                        label.setForeground(new Color(161, 98, 7));
                    }
                    default -> {
                        label.setBackground(new Color(241, 245, 249));
                        label.setForeground(TEXT_SECONDARY);
                    }
                }
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4, 8, 4, 8),
                    BorderFactory.createLineBorder(label.getBackground(), 1, true)
                ));
            }
            if (isSelected) {
                label.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1, true));
            }
            return label;
        }
    }
}
