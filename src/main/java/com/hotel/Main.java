package com.hotel;

import com.hotel.database.DatabaseInitializer;
import com.hotel.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application launcher for Hotel Room Booking Management System.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize modern Look and Feel with graceful fallbacks
        initLookAndFeel();

        // 2. Initialize SQLite Database schemas and seed initial data
        DatabaseInitializer.initialize();

        // 3. Launch GUI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Fatal error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void initLookAndFeel() {
        try {
            // First attempt: FlatLaf Light
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            // Fallback 1: Nimbus Look and Feel
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        return;
                    }
                }
                // Fallback 2: System Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Notice: Using default Swing look and feel: " + ex.getMessage());
            }
        }
    }
}
