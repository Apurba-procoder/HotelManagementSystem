package com.hotel.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for form inputs, numbers, and date validation.
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\-\\s()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private ValidationUtil() {}

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        String cleanPhone = phone.trim();
        return PHONE_PATTERN.matcher(cleanPhone).matches() && cleanPhone.replaceAll("[^0-9]", "").length() >= 7;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email can be optional
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isStrictValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isPositiveNumber(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            double v = Double.parseDouble(value.trim());
            return v > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeNumber(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            double v = Double.parseDouble(value.trim());
            return v >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            int v = Integer.parseInt(value.trim());
            return v > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        return checkOut.isAfter(checkIn);
    }
}
