package com.dataanalysis.integration;

import com.dataanalysis.analytics.SalesAnalyticsService;
import com.dataanalysis.loader.CsvSalesLoader;
import com.dataanalysis.model.*;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndAnalysisTest {

    @Test
    void testEndToEndAnalysis() throws Exception {
        // Load test CSV
        URL resource = getClass().getClassLoader().getResource("test_sales.csv");
        if (resource == null) {
            // Try alternative path
            Path altPath = Paths.get("src/test/resources/test_sales.csv");
            if (altPath.toFile().exists()) {
                resource = altPath.toUri().toURL();
            }
        }
        assertNotNull(resource, "Test CSV file not found");
        Path csvPath = resource.getProtocol().equals("file") ? Paths.get(resource.toURI()) : Paths.get(resource.getPath());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvPath);

        assertEquals(4, records.size());

        // Run analytics
        SalesAnalyticsService analytics = new SalesAnalyticsService();
        RevenueSummary revenueSummary = analytics.calculateRevenueSummary(records);

        // Verify core metrics
        // INV-2001: 3*49.99 - 5 = 144.97
        // INV-2002: 1*99.99 = 99.99
        // INV-2003: 2*49.99 = 99.98
        // INV-2004: -(1*29.99 - 5) = -24.99 (refund)
        // Total: 144.97 + 99.99 + 99.98 - 24.99 = 319.95
        assertEquals(319.95, revenueSummary.totalNetRevenue(), 0.1);

        // Verify segmentation
        assertTrue(revenueSummary.revenueByProduct().containsKey("PROD-1"));
        assertTrue(revenueSummary.revenueByCategory().containsKey("Subscription"));
        assertTrue(revenueSummary.revenueByRegion().containsKey("CA"));

        // Verify time trends
        assertFalse(revenueSummary.revenueByMonth().isEmpty());

        // Verify discounts and refunds
        assertTrue(revenueSummary.totalDiscountAmount() >= 0);
        assertTrue(revenueSummary.totalRefundImpact() <= 0);

        // Test cash vs accrual
        Map<String, Map<java.time.YearMonth, Double>> cashVsAccrual = analytics.cashVsAccrualRevenue(records);
        assertTrue(cashVsAccrual.containsKey("accrual"));
        assertTrue(cashVsAccrual.containsKey("cash"));

        // Test aging
        LocalDate asOfDate = LocalDate.of(2025, 3, 1);
        AgingSummary aging = analytics.calculateAgingSummary(records, asOfDate);
        assertNotNull(aging);
        // INV-2002 is unpaid, so there should be outstanding amount
        assertTrue(aging.totalOutstandingAmount() > 0);

        // Test margins
        MarginSummary margin = analytics.calculateMarginSummary(records);
        assertNotNull(margin);
        assertTrue(margin.totalGrossProfit() != 0);
    }

    @Test
    void testInvoiceSummaries() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test_sales.csv");
        if (resource == null) {
            Path altPath = Paths.get("src/test/resources/test_sales.csv");
            if (altPath.toFile().exists()) {
                resource = altPath.toUri().toURL();
            }
        }
        assertNotNull(resource, "Test CSV file not found");
        Path csvPath = resource.getProtocol().equals("file") ? Paths.get(resource.toURI()) : Paths.get(resource.getPath());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvPath);

        SalesAnalyticsService analytics = new SalesAnalyticsService();
        Map<String, InvoiceSummary> invoices = analytics.calculateInvoiceSummaries(records);

        assertEquals(4, invoices.size());
        assertTrue(invoices.containsKey("INV-2001"));
        assertTrue(invoices.containsKey("INV-2002"));
        assertTrue(invoices.containsKey("INV-2003"));
        assertTrue(invoices.containsKey("INV-2004"));

        InvoiceSummary inv1 = invoices.get("INV-2001");
        assertEquals("PAID", inv1.paymentStatus());
        assertEquals(144.97, inv1.totalAmount(), 0.1);

        InvoiceSummary inv2 = invoices.get("INV-2002");
        assertEquals("UNPAID", inv2.paymentStatus());
        assertEquals(99.99, inv2.totalAmount(), 0.1);
    }

    @Test
    void testTopRankings() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test_sales.csv");
        if (resource == null) {
            Path altPath = Paths.get("src/test/resources/test_sales.csv");
            if (altPath.toFile().exists()) {
                resource = altPath.toUri().toURL();
            }
        }
        assertNotNull(resource, "Test CSV file not found");
        Path csvPath = resource.getProtocol().equals("file") ? Paths.get(resource.toURI()) : Paths.get(resource.getPath());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvPath);

        SalesAnalyticsService analytics = new SalesAnalyticsService();

        Map<String, Double> topProducts = analytics.topProductsByRevenue(records, 2);
        assertFalse(topProducts.isEmpty());
        assertTrue(topProducts.size() <= 2);

        Map<String, Double> topCustomers = analytics.topCustomersByRevenue(records, 2);
        assertFalse(topCustomers.isEmpty());
        assertTrue(topCustomers.size() <= 2);
    }
}

