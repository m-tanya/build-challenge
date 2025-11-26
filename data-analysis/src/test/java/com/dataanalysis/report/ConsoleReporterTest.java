package com.dataanalysis.report;

import com.dataanalysis.model.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleReporterTest {

    @Test
    void testPrintReport() {
        ConsoleReporter reporter = new ConsoleReporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            RevenueSummary revenueSummary = createTestRevenueSummary();
            Map<String, Double> topProducts = Map.of("PROD-A", 1000.0, "PROD-B", 500.0);
            Map<String, Double> topCustomers = Map.of("CUST-1", 2000.0);
            Map<String, Double> topRegions = Map.of("CA", 1500.0);
            Map<String, Map<YearMonth, Double>> cashVsAccrual = createTestCashVsAccrual();
            AgingSummary agingSummary = createTestAgingSummary();
            MarginSummary marginSummary = createTestMarginSummary();

            reporter.printReport(revenueSummary, topProducts, topCustomers, topRegions,
                    cashVsAccrual, agingSummary, marginSummary);

            String output = outputStream.toString();
            assertTrue(output.contains("Sales Overview"));
            assertTrue(output.contains("Revenue by Segment"));
            assertTrue(output.contains("Cash vs Accrual"));
            assertTrue(output.contains("Open Invoices"));
            assertTrue(output.contains("Profitability"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testPrintReportWithEmptyData() {
        ConsoleReporter reporter = new ConsoleReporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            RevenueSummary emptyRevenue = new RevenueSummary(
                    0.0, 0, 0.0, 0.0,
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    0.0, 0.0, 0.0
            );

            reporter.printReport(emptyRevenue, Map.of(), Map.of(), Map.of(),
                    Map.of("accrual", Map.of(), "cash", Map.of()),
                    new AgingSummary(LocalDate.now(), 0.0, 0, Map.of(), Map.of()),
                    new MarginSummary(0.0, 0.0, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()));

            String output = outputStream.toString();
            // Should not crash even with empty data
            assertNotNull(output);
        } finally {
            System.setOut(originalOut);
        }
    }

    private RevenueSummary createTestRevenueSummary() {
        return new RevenueSummary(
                10000.0, 100, 500.0, 100.0,
                Map.of("PROD-A", 5000.0, "PROD-B", 3000.0),
                Map.of("Subscription", 6000.0, "Software", 4000.0),
                Map.of("CA", 5000.0, "TX", 3000.0),
                Map.of("US", 10000.0),
                Map.of("Online", 7000.0, "Retail", 3000.0),
                Map.of("SMB", 6000.0, "Startup", 4000.0),
                Map.of("PROD-A", 50L, "PROD-B", 30L),
                Map.of("Subscription", 60L, "Software", 40L),
                Map.of("CA", 50L, "TX", 30L),
                Map.of("Online", 70L, "Retail", 30L),
                Map.of("SMB", 60L, "Startup", 40L),
                Map.of("PROD-A", 10L, "PROD-B", 5L),
                Map.of("Subscription", 12L, "Software", 8L),
                Map.of("CA", 10L, "TX", 5L),
                Map.of("Online", 14L, "Retail", 6L),
                Map.of("SMB", 12L, "Startup", 8L),
                Map.of(YearMonth.of(2025, 1), 5000.0, YearMonth.of(2025, 2), 5000.0),
                Map.of(YearMonth.of(2025, 1), 50L, YearMonth.of(2025, 2), 50L),
                Map.of(YearMonth.of(2025, 2), 0.0),
                Map.of(YearMonth.of(2025, 2), 0.0),
                Map.of(YearMonth.of(2025, 2), 5000.0),
                500.0, 5.0, -100.0
        );
    }

    private Map<String, Map<YearMonth, Double>> createTestCashVsAccrual() {
        Map<YearMonth, Double> accrual = Map.of(
                YearMonth.of(2025, 1), 5000.0,
                YearMonth.of(2025, 2), 5000.0
        );
        Map<YearMonth, Double> cash = Map.of(
                YearMonth.of(2025, 1), 4500.0,
                YearMonth.of(2025, 2), 5500.0
        );
        return Map.of("accrual", accrual, "cash", cash);
    }

    private AgingSummary createTestAgingSummary() {
        return new AgingSummary(
                LocalDate.of(2025, 2, 1),
                2000.0,
                5,
                Map.of("0-30", 1000.0, "31-60", 800.0, "61-90", 200.0),
                Map.of("0-30", 3L, "31-60", 1L, "61-90", 1L)
        );
    }

    private MarginSummary createTestMarginSummary() {
        return new MarginSummary(
                6000.0,
                60.0,
                Map.of("PROD-A", 3000.0, "PROD-B", 2000.0),
                Map.of("PROD-A", 60.0, "PROD-B", 66.67),
                Map.of("Subscription", 3600.0, "Software", 2400.0),
                Map.of("Subscription", 60.0, "Software", 60.0),
                Map.of("CA", 3000.0, "TX", 2000.0),
                Map.of("CA", 60.0, "TX", 66.67),
                Map.of("PROD-A", new MarginSummary.ProductMarginProfile(
                        "PROD-A", "Product A", 5000.0, 3000.0, 60.0, "Balanced"
                ))
        );
    }
}

