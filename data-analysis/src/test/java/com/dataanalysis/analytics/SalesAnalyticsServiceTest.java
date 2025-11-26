package com.dataanalysis.analytics;

import com.dataanalysis.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyticsServiceTest {

    private SalesAnalyticsService service;
    private List<SalesRecord> testRecords;

    @BeforeEach
    void setUp() {
        service = new SalesAnalyticsService();
        testRecords = createTestRecords();
    }

    private List<SalesRecord> createTestRecords() {
        List<SalesRecord> records = new ArrayList<>();
        LocalDate baseDate = LocalDate.of(2025, 1, 15);

        // Invoice 1: Paid, Product A, Category Subscription
        records.add(new SalesRecord(
                "TXN-1", "INV-1", baseDate, baseDate.plusDays(10), baseDate.plusDays(15),
                "CUST-1", "SMB", "PROD-A", "Product A", "Subscription",
                2, 100.0, 10.0, 5.0, 50.0,
                "USD", "CA", "US", "Online",
                false, true, "Monthly", "PAID", "CA-STATE"
        ));

        // Invoice 2: Unpaid, Product B, Category Software
        records.add(new SalesRecord(
                "TXN-2", "INV-2", baseDate.plusDays(5), null, baseDate.plusDays(20),
                "CUST-2", "Startup", "PROD-B", "Product B", "Software",
                1, 200.0, 0.0, 8.0, 100.0,
                "USD", "TX", "US", "Retail",
                false, false, "", "UNPAID", "TX-STATE"
        ));

        // Invoice 3: Paid, Product A again, different month
        records.add(new SalesRecord(
                "TXN-3", "INV-3", baseDate.plusMonths(1), baseDate.plusMonths(1).plusDays(10),
                baseDate.plusMonths(1).plusDays(15),
                "CUST-1", "SMB", "PROD-A", "Product A", "Subscription",
                1, 100.0, 0.0, 4.0, 50.0,
                "USD", "CA", "US", "Online",
                false, true, "Monthly", "PAID", "CA-STATE"
        ));

        // Refund record
        records.add(new SalesRecord(
                "TXN-4", "INV-4", baseDate, baseDate.plusDays(5), baseDate.plusDays(15),
                "CUST-3", "Freelancer", "PROD-C", "Product C", "Service",
                1, 50.0, 0.0, 2.0, 20.0,
                "USD", "NY", "US", "Partner",
                true, false, "", "PAID", "NY-STATE"
        ));

        return records;
    }

    @Test
    void testCalculateRevenueSummary() {
        RevenueSummary summary = service.calculateRevenueSummary(testRecords);

        // Total net revenue: (2*100-10) + (1*200) + (1*100) + (-1*50) = 190 + 200 + 100 - 50 = 440
        assertEquals(440.0, summary.totalNetRevenue(), 0.01);

        // Total units: 2 + 1 + 1 - 1 = 3
        assertEquals(3, summary.totalUnitsSold());

        assertTrue(summary.averageOrderValue() > 0);
        assertTrue(summary.averageSellingPrice() > 0);
    }

    @Test
    void testSegmentation() {
        RevenueSummary summary = service.calculateRevenueSummary(testRecords);

        // Revenue by product
        assertTrue(summary.revenueByProduct().containsKey("PROD-A"));
        assertTrue(summary.revenueByProduct().containsKey("PROD-B"));
        assertTrue(summary.revenueByProduct().containsKey("PROD-C"));

        // Revenue by category
        assertTrue(summary.revenueByCategory().containsKey("Subscription"));
        assertTrue(summary.revenueByCategory().containsKey("Software"));
        assertTrue(summary.revenueByCategory().containsKey("Service"));

        // Revenue by region
        assertTrue(summary.revenueByRegion().containsKey("CA"));
        assertTrue(summary.revenueByRegion().containsKey("TX"));
        assertTrue(summary.revenueByRegion().containsKey("NY"));
    }

    @Test
    void testTimeBasedTrends() {
        RevenueSummary summary = service.calculateRevenueSummary(testRecords);

        assertFalse(summary.revenueByMonth().isEmpty());
        assertTrue(summary.revenueByMonth().containsKey(YearMonth.of(2025, 1)));
        assertTrue(summary.revenueByMonth().containsKey(YearMonth.of(2025, 2)));
    }

    @Test
    void testTopProductsByRevenue() {
        Map<String, Double> topProducts = service.topProductsByRevenue(testRecords, 2);

        assertFalse(topProducts.isEmpty());
        assertTrue(topProducts.size() <= 2);
    }

    @Test
    void testTopCustomersByRevenue() {
        Map<String, Double> topCustomers = service.topCustomersByRevenue(testRecords, 2);

        assertFalse(topCustomers.isEmpty());
        assertTrue(topCustomers.size() <= 2);
        assertTrue(topCustomers.containsKey("CUST-1"));
    }

    @Test
    void testTopRegionsByRevenue() {
        Map<String, Double> topRegions = service.topRegionsByRevenue(testRecords, 2);

        assertFalse(topRegions.isEmpty());
        assertTrue(topRegions.size() <= 2);
    }

    @Test
    void testCashVsAccrualRevenue() {
        Map<String, Map<YearMonth, Double>> cashVsAccrual = service.cashVsAccrualRevenue(testRecords);

        assertTrue(cashVsAccrual.containsKey("accrual"));
        assertTrue(cashVsAccrual.containsKey("cash"));

        Map<YearMonth, Double> accrual = cashVsAccrual.get("accrual");
        Map<YearMonth, Double> cash = cashVsAccrual.get("cash");

        assertFalse(accrual.isEmpty());
        // Cash should only include paid invoices
        assertTrue(cash.values().stream().allMatch(v -> v != null));
    }

    @Test
    void testCalculateInvoiceSummaries() {
        Map<String, InvoiceSummary> invoices = service.calculateInvoiceSummaries(testRecords);

        assertEquals(4, invoices.size());
        assertTrue(invoices.containsKey("INV-1"));
        assertTrue(invoices.containsKey("INV-2"));
        assertTrue(invoices.containsKey("INV-3"));
        assertTrue(invoices.containsKey("INV-4"));

        InvoiceSummary inv1 = invoices.get("INV-1");
        assertEquals(190.0, inv1.totalAmount(), 0.01); // 2*100 - 10
        assertEquals("PAID", inv1.paymentStatus());
    }

    @Test
    void testCalculateAgingSummary() {
        LocalDate asOfDate = LocalDate.of(2025, 2, 1);
        AgingSummary aging = service.calculateAgingSummary(testRecords, asOfDate);

        assertNotNull(aging);
        assertEquals(asOfDate, aging.asOfDate());
        assertTrue(aging.totalOutstandingAmount() > 0); // INV-2 is unpaid
        assertTrue(aging.totalOpenInvoices() > 0);
    }

    @Test
    void testAgingBuckets() {
        LocalDate asOfDate = LocalDate.of(2025, 2, 20); // After INV-2 due date
        AgingSummary aging = service.calculateAgingSummary(testRecords, asOfDate);

        assertFalse(aging.outstandingAmountByBucket().isEmpty());
        assertFalse(aging.invoiceCountByBucket().isEmpty());
    }

    @Test
    void testCalculateMarginSummary() {
        MarginSummary margin = service.calculateMarginSummary(testRecords);

        assertNotNull(margin);
        assertTrue(margin.totalGrossProfit() != 0);
        assertTrue(margin.overallGrossMarginPercent() != 0);

        assertFalse(margin.grossProfitByProduct().isEmpty());
        assertFalse(margin.grossMarginPercentByProduct().isEmpty());
        assertFalse(margin.grossProfitByCategory().isEmpty());
        assertFalse(margin.grossMarginPercentByCategory().isEmpty());
    }

    @Test
    void testDiscountsAndRefunds() {
        RevenueSummary summary = service.calculateRevenueSummary(testRecords);

        assertTrue(summary.totalDiscountAmount() >= 0);
        assertTrue(summary.discountShareOfRevenue() >= 0);
        assertTrue(summary.totalRefundImpact() <= 0); // Refunds are negative
    }

    @Test
    void testEmptyRecords() {
        List<SalesRecord> empty = Collections.emptyList();
        RevenueSummary summary = service.calculateRevenueSummary(empty);

        assertEquals(0.0, summary.totalNetRevenue(), 0.01);
        assertEquals(0, summary.totalUnitsSold());
        assertTrue(summary.revenueByProduct().isEmpty());
    }

    @Test
    void testRefundHandling() {
        // Verify refunds are properly handled in calculations
        RevenueSummary summary = service.calculateRevenueSummary(testRecords);

        // Check that refund impact is negative
        assertTrue(summary.totalRefundImpact() <= 0);

        // Check that refunds affect total revenue correctly
        double totalWithoutRefund = testRecords.stream()
                .filter(r -> !r.isRefund())
                .mapToDouble(SalesRecord::netAmount)
                .sum();
        double refundAmount = testRecords.stream()
                .filter(SalesRecord::isRefund)
                .mapToDouble(SalesRecord::netAmount)
                .sum();

        assertEquals(totalWithoutRefund + refundAmount, summary.totalNetRevenue(), 0.01);
    }
}

