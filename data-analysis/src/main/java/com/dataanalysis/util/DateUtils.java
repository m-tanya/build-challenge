package com.dataanalysis.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date operations.
 */
public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parses a date string in yyyy-MM-dd format.
     * @param dateStr the date string
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a LocalDate to yyyy-MM-dd string.
     * @param date the date
     * @return formatted string or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    /**
     * Extracts YearMonth from a LocalDate.
     * @param date the date
     * @return YearMonth or null if date is null
     */
    public static YearMonth toYearMonth(LocalDate date) {
        return date == null ? null : YearMonth.from(date);
    }
}

