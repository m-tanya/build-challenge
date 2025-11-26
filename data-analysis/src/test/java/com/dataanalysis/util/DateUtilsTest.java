package com.dataanalysis.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testParseDate() {
        LocalDate date = DateUtils.parseDate("2025-01-15");
        assertNotNull(date);
        assertEquals(2025, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testParseDateNull() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("   "));
    }

    @Test
    void testParseDateInvalid() {
        assertNull(DateUtils.parseDate("invalid"));
        assertNull(DateUtils.parseDate("2025-13-01"));
    }

    @Test
    void testFormatDate() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        assertEquals("2025-01-15", DateUtils.formatDate(date));
    }

    @Test
    void testFormatDateNull() {
        assertEquals("", DateUtils.formatDate(null));
    }

    @Test
    void testToYearMonth() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        YearMonth ym = DateUtils.toYearMonth(date);
        assertNotNull(ym);
        assertEquals(2025, ym.getYear());
        assertEquals(1, ym.getMonthValue());
    }

    @Test
    void testToYearMonthNull() {
        assertNull(DateUtils.toYearMonth(null));
    }
}

