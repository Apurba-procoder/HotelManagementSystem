package com.hotel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for handling dates, timestamps, formatting and night calculations.
 */
public class DateUtil {

    public static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateUtil() {}

    public static String formatDbDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DB_DATE_FORMATTER);
    }

    public static LocalDate parseDbDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DB_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatDisplayDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    public static String formatDisplayDate(String dbDateStr) {
        LocalDate date = parseDbDate(dbDateStr);
        return date != null ? formatDisplayDate(date) : (dbDateStr != null ? dbDateStr : "-");
    }

    public static String formatDbDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DB_DATETIME_FORMATTER);
    }

    public static LocalDateTime parseDbDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DB_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatDisplayDateTime(String dbDateTimeStr) {
        LocalDateTime dt = parseDbDateTime(dbDateTimeStr);
        return dt != null ? dt.format(DISPLAY_DATETIME_FORMATTER) : (dbDateTimeStr != null ? dbDateTimeStr : "-");
    }

    public static int calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return 0;
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return (int) Math.max(days, 0);
    }

    public static int calculateNights(String checkInStr, String checkOutStr) {
        LocalDate cin = parseDbDate(checkInStr);
        LocalDate cout = parseDbDate(checkOutStr);
        return calculateNights(cin, cout);
    }

    public static String getCurrentDbDate() {
        return LocalDate.now().format(DB_DATE_FORMATTER);
    }

    public static String getCurrentDbDateTime() {
        return LocalDateTime.now().format(DB_DATETIME_FORMATTER);
    }
}
