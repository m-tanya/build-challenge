package com.dataanalysis.integration;

import com.dataanalysis.analytics.SalesAnalyticsService;
import com.dataanalysis.generator.CsvDataGenerator;
import com.dataanalysis.loader.CsvSalesLoader;
import com.dataanalysis.model.SalesRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedCsvRoundTripTest {

    @Test
    void testGeneratedCsvRoundTrip(@TempDir Path tempDir) throws Exception {
        // Generate CSV
        Path csvFile = tempDir.resolve("generated_sales.csv");
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(100, csvFile);

        assertTrue(csvFile.toFile().exists());

        // Load CSV
        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        // Generator creates 1-5 line items per invoice, so 100 invoices can result in 100-500 records
        assertTrue(records.size() >= 100, "Expected at least 100 records, got " + records.size());
        assertTrue(records.size() <= 500, "Expected at most 500 records, got " + records.size());

        // Run analytics
        SalesAnalyticsService analytics = new SalesAnalyticsService();
        var revenueSummary = analytics.calculateRevenueSummary(records);

        // Basic invariants
        assertNotNull(revenueSummary);
        assertTrue(revenueSummary.totalUnitsSold() >= 0);
        
        // Check that subscription data is present if any subscription records exist
        boolean hasSubscriptions = records.stream().anyMatch(SalesRecord::isSubscription);
        if (hasSubscriptions) {
            assertFalse(revenueSummary.revenueByCategory().isEmpty());
        }

        // Test cash vs accrual
        var cashVsAccrual = analytics.cashVsAccrualRevenue(records);
        assertNotNull(cashVsAccrual);
        assertTrue(cashVsAccrual.containsKey("accrual"));
        assertTrue(cashVsAccrual.containsKey("cash"));

        // Test margins
        var marginSummary = analytics.calculateMarginSummary(records);
        assertNotNull(marginSummary);
        assertTrue(marginSummary.totalGrossProfit() != 0 || revenueSummary.totalNetRevenue() == 0);
    }

    @Test
    void testGeneratedCsvWithRefunds(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("sales_with_refunds.csv");
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(200, csvFile); // Larger dataset increases chance of refunds

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        boolean hasRefunds = records.stream().anyMatch(SalesRecord::isRefund);
        
        SalesAnalyticsService analytics = new SalesAnalyticsService();
        var revenueSummary = analytics.calculateRevenueSummary(records);

        if (hasRefunds) {
            assertTrue(revenueSummary.totalRefundImpact() <= 0);
        }

        // Total revenue should account for refunds
        double calculatedTotal = records.stream()
                .mapToDouble(SalesRecord::netAmount)
                .sum();
        assertEquals(calculatedTotal, revenueSummary.totalNetRevenue(), 0.01);
    }

    @Test
    void testGeneratedCsvInvoiceGrouping(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("sales.csv");
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(50, csvFile);

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        SalesAnalyticsService analytics = new SalesAnalyticsService();
        var invoices = analytics.calculateInvoiceSummaries(records);

        // Verify invoice grouping
        assertFalse(invoices.isEmpty());
        
        // Each invoice should have at least one line item
        invoices.values().forEach(inv -> {
            assertFalse(inv.lineItems().isEmpty());
            assertTrue(inv.totalAmount() != 0 || inv.lineItems().stream()
                    .anyMatch(r -> r.isRefund()));
        });
    }
}

