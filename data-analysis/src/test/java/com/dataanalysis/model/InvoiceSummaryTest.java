package com.dataanalysis.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceSummaryTest {

    @Test
    void testOutstandingAmount() {
        InvoiceSummary paid = new InvoiceSummary(
                "INV-1", LocalDate.now(), LocalDate.now(), LocalDate.now(),
                "PAID", 1000.0, List.of()
        );

        assertEquals(0.0, paid.outstandingAmount(), 0.01);

        InvoiceSummary unpaid = new InvoiceSummary(
                "INV-2", LocalDate.now(), LocalDate.now(), null,
                "UNPAID", 1000.0, List.of()
        );

        assertEquals(1000.0, unpaid.outstandingAmount(), 0.01);
    }

    @Test
    void testDaysOverdue() {
        LocalDate dueDate = LocalDate.of(2025, 1, 1);
        LocalDate asOfDate = LocalDate.of(2025, 1, 31);

        InvoiceSummary overdue = new InvoiceSummary(
                "INV-1", LocalDate.now(), dueDate, null,
                "UNPAID", 1000.0, List.of()
        );

        assertEquals(30, overdue.daysOverdue(asOfDate));

        InvoiceSummary paid = new InvoiceSummary(
                "INV-2", LocalDate.now(), dueDate, LocalDate.now(),
                "PAID", 1000.0, List.of()
        );

        assertEquals(0, paid.daysOverdue(asOfDate));
    }

    @Test
    void testAgingBucket() {
        LocalDate dueDate = LocalDate.of(2025, 1, 1);
        LocalDate asOfDate = LocalDate.of(2025, 1, 15); // 14 days overdue

        InvoiceSummary current = new InvoiceSummary(
                "INV-1", LocalDate.now(), dueDate, null,
                "UNPAID", 1000.0, List.of()
        );

        assertEquals("0-30", current.agingBucket(asOfDate));

        asOfDate = LocalDate.of(2025, 2, 1); // 31 days overdue
        assertEquals("31-60", current.agingBucket(asOfDate));

        asOfDate = LocalDate.of(2025, 3, 1); // 59 days overdue
        assertEquals("31-60", current.agingBucket(asOfDate));

        asOfDate = LocalDate.of(2025, 3, 15); // 73 days overdue
        assertEquals("61-90", current.agingBucket(asOfDate));

        asOfDate = LocalDate.of(2025, 4, 15); // 104 days overdue
        assertEquals("90+", current.agingBucket(asOfDate));
    }
}

