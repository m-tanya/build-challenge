package com.dataanalysis.report;

import com.dataanalysis.model.*;
import com.dataanalysis.util.DateUtils;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Formats analytics results into human-readable console output.
 */
public class ConsoleReporter {

    /**
     * Prints a comprehensive sales analysis report.
     */
    public void printReport(RevenueSummary revenueSummary,
                           Map<String, Double> topProducts,
                           Map<String, Double> topCustomers,
                           Map<String, Double> topRegions,
                           Map<String, Map<YearMonth, Double>> cashVsAccrual,
                           AgingSummary agingSummary,
                           MarginSummary marginSummary) {
        printSalesOverview(revenueSummary);
        printRevenueBySegment(revenueSummary);
        printTimeBasedTrends(revenueSummary);
        printRankings(topProducts, topCustomers, topRegions);
        printDiscountsAndRefunds(revenueSummary);
        printCashVsAccrual(cashVsAccrual);
        printAgingAnalysis(agingSummary);
        printProfitabilityAnalysis(marginSummary);
    }

    private void printSalesOverview(RevenueSummary summary) {
        System.out.println("\n=== Sales Overview ===");
        System.out.printf("Total Net Revenue: $%,.2f%n", summary.totalNetRevenue());
        System.out.printf("Total Units Sold: %,d%n", summary.totalUnitsSold());
        System.out.printf("Average Order Value: $%,.2f%n", summary.averageOrderValue());
        System.out.printf("Average Selling Price per Unit: $%,.2f%n", summary.averageSellingPrice());
    }

    private void printRevenueBySegment(RevenueSummary summary) {
        System.out.println("\n=== Revenue by Segment and Region ===");
        
        System.out.println("\nRevenue by Product Category:");
        summary.revenueByCategory().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));

        System.out.println("\nRevenue by Region:");
        summary.revenueByRegion().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));

        System.out.println("\nRevenue by Channel:");
        summary.revenueByChannel().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));

        System.out.println("\nRevenue by Customer Segment:");
        summary.revenueByCustomerSegment().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));
    }

    private void printTimeBasedTrends(RevenueSummary summary) {
        System.out.println("\n=== Time-Based Trends ===");
        
        System.out.println("\nRevenue by Month:");
        summary.revenueByMonth().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));

        if (!summary.momRevenueChange().isEmpty()) {
            System.out.println("\nMonth-over-Month Revenue Change:");
            summary.momRevenueChange().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.printf("  %s: $%,.2f (%.2f%%)%n",
                            e.getKey(), e.getValue(), summary.momGrowthRate().getOrDefault(e.getKey(), 0.0)));
        }

        if (!summary.rolling3MonthAverageRevenue().isEmpty()) {
            System.out.println("\nRolling 3-Month Average Revenue:");
            summary.rolling3MonthAverageRevenue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));
        }
    }

    private void printRankings(Map<String, Double> topProducts,
                               Map<String, Double> topCustomers,
                               Map<String, Double> topRegions) {
        System.out.println("\n=== Top Rankings ===");
        
        if (!topProducts.isEmpty()) {
            System.out.println("\nTop Products by Revenue:");
            int rank = 1;
            for (Map.Entry<String, Double> entry : topProducts.entrySet()) {
                System.out.printf("  %d. %s: $%,.2f%n", rank++, entry.getKey(), entry.getValue());
            }
        }

        if (!topCustomers.isEmpty()) {
            System.out.println("\nTop Customers by Revenue:");
            int rank = 1;
            for (Map.Entry<String, Double> entry : topCustomers.entrySet()) {
                System.out.printf("  %d. %s: $%,.2f%n", rank++, entry.getKey(), entry.getValue());
            }
        }

        if (!topRegions.isEmpty()) {
            System.out.println("\nTop Regions by Revenue:");
            int rank = 1;
            for (Map.Entry<String, Double> entry : topRegions.entrySet()) {
                System.out.printf("  %d. %s: $%,.2f%n", rank++, entry.getKey(), entry.getValue());
            }
        }
    }

    private void printDiscountsAndRefunds(RevenueSummary summary) {
        System.out.println("\n=== Discounts and Refunds ===");
        System.out.printf("Total Discount Amount: $%,.2f%n", summary.totalDiscountAmount());
        System.out.printf("Discount Share of Revenue: %.2f%%%n", summary.discountShareOfRevenue());
        System.out.printf("Total Refund Impact: $%,.2f%n", summary.totalRefundImpact());
    }

    private void printCashVsAccrual(Map<String, Map<YearMonth, Double>> cashVsAccrual) {
        System.out.println("\n=== Cash vs Accrual Revenue ===");
        
        Map<YearMonth, Double> accrual = cashVsAccrual.get("accrual");
        Map<YearMonth, Double> cash = cashVsAccrual.get("cash");

        if (accrual != null && !accrual.isEmpty()) {
            System.out.println("\nAccrual Revenue (by Invoice Date):");
            accrual.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));
        }

        if (cash != null && !cash.isEmpty()) {
            System.out.println("\nCash Revenue (by Payment Date):");
            cash.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.printf("  %s: $%,.2f%n", e.getKey(), e.getValue()));
        }

        // Show differences
        if (accrual != null && cash != null) {
            System.out.println("\nDifference (Accrual - Cash):");
            accrual.keySet().stream()
                    .sorted()
                    .forEach(month -> {
                        double acc = accrual.getOrDefault(month, 0.0);
                        double cas = cash.getOrDefault(month, 0.0);
                        double diff = acc - cas;
                        System.out.printf("  %s: $%,.2f%n", month, diff);
                    });
        }
    }

    private void printAgingAnalysis(AgingSummary aging) {
        System.out.println("\n=== Open Invoices and Aging ===");
        System.out.printf("As of Date: %s%n", DateUtils.formatDate(aging.asOfDate()));
        System.out.printf("Total Outstanding Amount: $%,.2f%n", aging.totalOutstandingAmount());
        System.out.printf("Total Open Invoices: %,d%n", aging.totalOpenInvoices());

        System.out.println("\nOutstanding Amount by Aging Bucket:");
        for (String bucket : AgingSummary.AGING_BUCKETS) {
            double amount = aging.outstandingAmountByBucket().getOrDefault(bucket, 0.0);
            long count = aging.invoiceCountByBucket().getOrDefault(bucket, 0L);
            if (amount > 0 || count > 0) {
                System.out.printf("  %s days: $%,.2f (%d invoices)%n", bucket, amount, count);
            }
        }
    }

    private void printProfitabilityAnalysis(MarginSummary margin) {
        System.out.println("\n=== Profitability and Margins ===");
        System.out.printf("Total Gross Profit: $%,.2f%n", margin.totalGrossProfit());
        System.out.printf("Overall Gross Margin: %.2f%%%n", margin.overallGrossMarginPercent());

        System.out.println("\nGross Margin by Product Category:");
        margin.grossMarginPercentByCategory().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> {
                    double profit = margin.grossProfitByCategory().getOrDefault(e.getKey(), 0.0);
                    System.out.printf("  %s: %.2f%% (Profit: $%,.2f)%n",
                            e.getKey(), e.getValue(), profit);
                });

        System.out.println("\nGross Margin by Region:");
        margin.grossMarginPercentByRegion().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> {
                    double profit = margin.grossProfitByRegion().getOrDefault(e.getKey(), 0.0);
                    System.out.printf("  %s: %.2f%% (Profit: $%,.2f)%n",
                            e.getKey(), e.getValue(), profit);
                });

        System.out.println("\nProduct Margin Profiles:");
        margin.productMarginProfiles().values().stream()
                .sorted((a, b) -> Double.compare(b.revenue(), a.revenue()))
                .limit(10)
                .forEach(profile -> {
                    System.out.printf("  %s (%s): Revenue $%,.2f, Margin %.2f%%, Profile: %s%n",
                            profile.productName(), profile.productId(),
                            profile.revenue(), profile.grossMarginPercent(), profile.profile());
                });
    }
}

