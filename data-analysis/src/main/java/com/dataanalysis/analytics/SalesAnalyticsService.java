package com.dataanalysis.analytics;

import com.dataanalysis.model.*;
import com.dataanalysis.util.DateUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for performing sales analytics using Java Streams and lambda expressions.
 */
public class SalesAnalyticsService {

    /**
     * Calculates comprehensive revenue summary.
     */
    public RevenueSummary calculateRevenueSummary(List<SalesRecord> records) {
        double totalNetRevenue = records.stream()
                .mapToDouble(SalesRecord::netAmount)
                .sum();

        long totalUnitsSold = records.stream()
                .mapToLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                .sum();

        // Average order value: revenue per invoice, then average
        Map<String, Double> revenueByInvoice = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::invoiceId,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));
        double averageOrderValue = revenueByInvoice.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double averageSellingPrice = totalUnitsSold != 0 ? totalNetRevenue / totalUnitsSold : 0.0;

        // Segmentation by product
        Map<String, Double> revenueByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> revenueByCategory = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productCategory,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> revenueByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> revenueByCountry = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::country,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> revenueByChannel = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::channel,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> revenueByCustomerSegment = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::customerSegment,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        // Units by segment
        Map<String, Long> unitsByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        Map<String, Long> unitsByCategory = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productCategory,
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        Map<String, Long> unitsByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        Map<String, Long> unitsByChannel = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::channel,
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        Map<String, Long> unitsByCustomerSegment = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::customerSegment,
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        // Order counts by segment
        Map<String, Long> orderCountByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.counting()
                ));

        Map<String, Long> orderCountByCategory = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productCategory,
                        Collectors.counting()
                ));

        Map<String, Long> orderCountByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.counting()
                ));

        Map<String, Long> orderCountByChannel = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::channel,
                        Collectors.counting()
                ));

        Map<String, Long> orderCountByCustomerSegment = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::customerSegment,
                        Collectors.counting()
                ));

        // Time-based trends
        Map<YearMonth, Double> revenueByMonth = records.stream()
                .filter(r -> r.invoiceDate() != null)
                .collect(Collectors.groupingBy(
                        r -> DateUtils.toYearMonth(r.invoiceDate()),
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<YearMonth, Long> unitsByMonth = records.stream()
                .filter(r -> r.invoiceDate() != null)
                .collect(Collectors.groupingBy(
                        r -> DateUtils.toYearMonth(r.invoiceDate()),
                        Collectors.summingLong(r -> r.isRefund() ? -r.quantity() : r.quantity())
                ));

        // Month-over-month changes
        Map<YearMonth, Double> momRevenueChange = new LinkedHashMap<>();
        Map<YearMonth, Double> momGrowthRate = new LinkedHashMap<>();
        List<YearMonth> sortedMonths = revenueByMonth.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < sortedMonths.size(); i++) {
            YearMonth current = sortedMonths.get(i);
            YearMonth previous = sortedMonths.get(i - 1);
            double currentRevenue = revenueByMonth.get(current);
            double previousRevenue = revenueByMonth.get(previous);
            double change = currentRevenue - previousRevenue;
            double growthRate = previousRevenue != 0 ? (change / previousRevenue) * 100 : 0.0;
            momRevenueChange.put(current, change);
            momGrowthRate.put(current, growthRate);
        }

        // Rolling 3-month average
        Map<YearMonth, Double> rolling3MonthAverageRevenue = new LinkedHashMap<>();
        for (int i = 2; i < sortedMonths.size(); i++) {
            YearMonth current = sortedMonths.get(i);
            double sum = revenueByMonth.get(sortedMonths.get(i - 2)) +
                    revenueByMonth.get(sortedMonths.get(i - 1)) +
                    revenueByMonth.get(current);
            rolling3MonthAverageRevenue.put(current, sum / 3.0);
        }

        // Discounts and refunds
        double totalDiscountAmount = records.stream()
                .mapToDouble(SalesRecord::discountAmount)
                .sum();

        double discountShareOfRevenue = totalNetRevenue != 0 ?
                (totalDiscountAmount / (totalNetRevenue + totalDiscountAmount)) * 100 : 0.0;

        double totalRefundImpact = records.stream()
                .filter(SalesRecord::isRefund)
                .mapToDouble(SalesRecord::netAmount)
                .sum();

        return new RevenueSummary(
                totalNetRevenue, totalUnitsSold, averageOrderValue, averageSellingPrice,
                revenueByProduct, revenueByCategory, revenueByRegion, revenueByCountry,
                revenueByChannel, revenueByCustomerSegment,
                unitsByProduct, unitsByCategory, unitsByRegion, unitsByChannel, unitsByCustomerSegment,
                orderCountByProduct, orderCountByCategory, orderCountByRegion, orderCountByChannel,
                orderCountByCustomerSegment,
                revenueByMonth, unitsByMonth, momRevenueChange, momGrowthRate, rolling3MonthAverageRevenue,
                totalDiscountAmount, discountShareOfRevenue, totalRefundImpact
        );
    }

    /**
     * Gets top N products by revenue.
     */
    public Map<String, Double> topProductsByRevenue(List<SalesRecord> records, int topN) {
        Map<String, Double> revenueByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));
        return AggregationUtils.topN(revenueByProduct, topN);
    }

    /**
     * Gets top N customers by revenue.
     */
    public Map<String, Double> topCustomersByRevenue(List<SalesRecord> records, int topN) {
        Map<String, Double> revenueByCustomer = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::customerId,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));
        return AggregationUtils.topN(revenueByCustomer, topN);
    }

    /**
     * Gets top N regions by revenue.
     */
    public Map<String, Double> topRegionsByRevenue(List<SalesRecord> records, int topN) {
        Map<String, Double> revenueByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));
        return AggregationUtils.topN(revenueByRegion, topN);
    }

    /**
     * Calculates cash vs accrual revenue view.
     * @return map with "accrual" and "cash" keys, each containing a map of YearMonth to revenue
     */
    public Map<String, Map<YearMonth, Double>> cashVsAccrualRevenue(List<SalesRecord> records) {
        // Accrual view: group by invoice_date
        Map<YearMonth, Double> accrualRevenue = records.stream()
                .filter(r -> r.invoiceDate() != null)
                .collect(Collectors.groupingBy(
                        r -> DateUtils.toYearMonth(r.invoiceDate()),
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        // Cash view: group by payment_date for paid invoices
        Map<YearMonth, Double> cashRevenue = records.stream()
                .filter(r -> r.isPaid() && r.paymentDate() != null)
                .collect(Collectors.groupingBy(
                        r -> DateUtils.toYearMonth(r.paymentDate()),
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Map<YearMonth, Double>> result = new LinkedHashMap<>();
        result.put("accrual", accrualRevenue);
        result.put("cash", cashRevenue);
        return result;
    }

    /**
     * Calculates invoice summaries grouped by invoice ID.
     */
    public Map<String, InvoiceSummary> calculateInvoiceSummaries(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(SalesRecord::invoiceId))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<SalesRecord> lineItems = entry.getValue();
                            SalesRecord first = lineItems.get(0);
                            double totalAmount = lineItems.stream()
                                    .mapToDouble(SalesRecord::netAmount)
                                    .sum();
                            return new InvoiceSummary(
                                    first.invoiceId(),
                                    first.invoiceDate(),
                                    first.dueDate(),
                                    first.paymentDate(),
                                    first.paymentStatus(),
                                    totalAmount,
                                    lineItems
                            );
                        }
                ));
    }

    /**
     * Calculates aging summary for open invoices.
     */
    public AgingSummary calculateAgingSummary(List<SalesRecord> records, LocalDate asOfDate) {
        Map<String, InvoiceSummary> invoices = calculateInvoiceSummaries(records);

        // Filter open invoices
        List<InvoiceSummary> openInvoices = invoices.values().stream()
                .filter(inv -> inv.outstandingAmount() > 0)
                .collect(Collectors.toList());

        double totalOutstandingAmount = openInvoices.stream()
                .mapToDouble(InvoiceSummary::outstandingAmount)
                .sum();

        // Group by aging bucket
        Map<String, Double> outstandingAmountByBucket = openInvoices.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.agingBucket(asOfDate),
                        Collectors.summingDouble(InvoiceSummary::outstandingAmount)
                ));

        Map<String, Long> invoiceCountByBucket = openInvoices.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.agingBucket(asOfDate),
                        Collectors.counting()
                ));

        return new AgingSummary(
                asOfDate,
                totalOutstandingAmount,
                openInvoices.size(),
                outstandingAmountByBucket,
                invoiceCountByBucket
        );
    }

    /**
     * Calculates profitability and margin summary.
     */
    public MarginSummary calculateMarginSummary(List<SalesRecord> records) {
        double totalGrossProfit = records.stream()
                .mapToDouble(SalesRecord::grossProfit)
                .sum();

        double totalNetRevenue = records.stream()
                .mapToDouble(SalesRecord::netAmount)
                .sum();

        double overallGrossMarginPercent = totalNetRevenue != 0 ?
                (totalGrossProfit / totalNetRevenue) * 100 : 0.0;

        // Gross profit by product
        Map<String, Double> grossProfitByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.summingDouble(SalesRecord::grossProfit)
                ));

        Map<String, Double> revenueByProduct = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productId,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> grossMarginPercentByProduct = new LinkedHashMap<>();
        for (String productId : grossProfitByProduct.keySet()) {
            double profit = grossProfitByProduct.get(productId);
            double revenue = revenueByProduct.get(productId);
            double marginPercent = revenue != 0 ? (profit / revenue) * 100 : 0.0;
            grossMarginPercentByProduct.put(productId, marginPercent);
        }

        // Gross profit by category
        Map<String, Double> grossProfitByCategory = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productCategory,
                        Collectors.summingDouble(SalesRecord::grossProfit)
                ));

        Map<String, Double> revenueByCategory = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::productCategory,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> grossMarginPercentByCategory = new LinkedHashMap<>();
        for (String category : grossProfitByCategory.keySet()) {
            double profit = grossProfitByCategory.get(category);
            double revenue = revenueByCategory.get(category);
            double marginPercent = revenue != 0 ? (profit / revenue) * 100 : 0.0;
            grossMarginPercentByCategory.put(category, marginPercent);
        }

        // Gross profit by region
        Map<String, Double> grossProfitByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingDouble(SalesRecord::grossProfit)
                ));

        Map<String, Double> revenueByRegion = records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingDouble(SalesRecord::netAmount)
                ));

        Map<String, Double> grossMarginPercentByRegion = new LinkedHashMap<>();
        for (String region : grossProfitByRegion.keySet()) {
            double profit = grossProfitByRegion.get(region);
            double revenue = revenueByRegion.get(region);
            double marginPercent = revenue != 0 ? (profit / revenue) * 100 : 0.0;
            grossMarginPercentByRegion.put(region, marginPercent);
        }

        // Product margin profiles
        Map<String, MarginSummary.ProductMarginProfile> productMarginProfiles = new LinkedHashMap<>();
        
        // Get top and bottom products by revenue and margin for classification
        // Use top 40% by revenue, bottom 40% by margin for "High Revenue Low Margin"
        // Use bottom 40% by revenue, top 40% by margin for "High Margin Low Revenue"
        int topN = Math.max(1, (int)(revenueByProduct.size() * 0.4)); // Top 40%
        int bottomN = Math.max(1, (int)(revenueByProduct.size() * 0.4)); // Bottom 40%
        
        Map<String, Double> topProductsByRev = AggregationUtils.topN(revenueByProduct, topN);
        Map<String, Double> bottomProductsByRev = revenueByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(bottomN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        Map<String, Double> topProductsByMargin = grossMarginPercentByProduct.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<String, Double> bottomProductsByMargin = grossMarginPercentByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(bottomN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        for (String productId : grossProfitByProduct.keySet()) {
            double profit = grossProfitByProduct.get(productId);
            double revenue = revenueByProduct.get(productId);
            double marginPercent = revenue != 0 ? (profit / revenue) * 100 : 0.0;

            // Find product name
            String productName = records.stream()
                    .filter(r -> r.productId().equals(productId))
                    .findFirst()
                    .map(SalesRecord::productName)
                    .orElse("Unknown");

            // Classify profile: High Revenue Low Margin = top by revenue AND bottom by margin
            // High Margin Low Revenue = bottom by revenue AND top by margin
            String profile;
            boolean isHighRevenue = topProductsByRev.containsKey(productId);
            boolean isLowRevenue = bottomProductsByRev.containsKey(productId);
            boolean isHighMargin = topProductsByMargin.containsKey(productId);
            boolean isLowMargin = bottomProductsByMargin.containsKey(productId);
            
            if (isHighRevenue && isLowMargin) {
                profile = "High Revenue Low Margin";
            } else if (isLowRevenue && isHighMargin) {
                profile = "High Margin Low Revenue";
            } else {
                profile = "Balanced";
            }

            productMarginProfiles.put(productId, new MarginSummary.ProductMarginProfile(
                    productId, productName, revenue, profit, marginPercent, profile
            ));
        }

        return new MarginSummary(
                totalGrossProfit,
                overallGrossMarginPercent,
                grossProfitByProduct,
                grossMarginPercentByProduct,
                grossProfitByCategory,
                grossMarginPercentByCategory,
                grossProfitByRegion,
                grossMarginPercentByRegion,
                productMarginProfiles
        );
    }
}

