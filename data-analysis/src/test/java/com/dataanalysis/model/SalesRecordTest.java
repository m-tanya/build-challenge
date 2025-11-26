package com.dataanalysis.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesRecordTest {

    @Test
    void testGrossAmount() {
        SalesRecord record = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, false, "", "PAID", "CA-STATE"
        );

        assertEquals(500.0, record.grossAmount(), 0.01);
    }

    @Test
    void testNetAmount() {
        SalesRecord record = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, false, "", "PAID", "CA-STATE"
        );

        assertEquals(490.0, record.netAmount(), 0.01);
    }

    @Test
    void testNetAmountWithRefund() {
        SalesRecord refund = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                true, false, "", "PAID", "CA-STATE"
        );

        assertEquals(-490.0, refund.netAmount(), 0.01);
    }

    @Test
    void testGrossProfit() {
        SalesRecord record = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, false, "", "PAID", "CA-STATE"
        );

        // netAmount = 490, COGS = 5 * 50 = 250, profit = 490 - 250 = 240
        assertEquals(240.0, record.grossProfit(), 0.01);
    }

    @Test
    void testIsPaid() {
        SalesRecord paid = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, false, "", "PAID", "CA-STATE"
        );

        assertTrue(paid.isPaid());
        assertFalse(paid.isOpen());
    }

    @Test
    void testIsOpen() {
        SalesRecord unpaid = new SalesRecord(
                "TXN-1", "INV-1", LocalDate.now(), null, LocalDate.now(),
                "CUST-1", "SMB", "PROD-1", "Product", "Category",
                5, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, false, "", "UNPAID", "CA-STATE"
        );

        assertFalse(unpaid.isPaid());
        assertTrue(unpaid.isOpen());
    }
}

