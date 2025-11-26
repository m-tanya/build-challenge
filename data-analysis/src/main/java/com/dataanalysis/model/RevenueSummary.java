package com.dataanalysis.model;

import java.time.YearMonth;
import java.util.Map;

/**
 * Summary of revenue metrics and segmentations.
 */
public record RevenueSummary(
        double totalNetRevenue,
        long totalUnitsSold,
        double averageOrderValue,
        double averageSellingPrice,
        Map<String, Double> revenueByProduct,
        Map<String, Double> revenueByCategory,
        Map<String, Double> revenueByRegion,
        Map<String, Double> revenueByCountry,
        Map<String, Double> revenueByChannel,
        Map<String, Double> revenueByCustomerSegment,
        Map<String, Long> unitsByProduct,
        Map<String, Long> unitsByCategory,
        Map<String, Long> unitsByRegion,
        Map<String, Long> unitsByChannel,
        Map<String, Long> unitsByCustomerSegment,
        Map<String, Long> orderCountByProduct,
        Map<String, Long> orderCountByCategory,
        Map<String, Long> orderCountByRegion,
        Map<String, Long> orderCountByChannel,
        Map<String, Long> orderCountByCustomerSegment,
        Map<YearMonth, Double> revenueByMonth,
        Map<YearMonth, Long> unitsByMonth,
        Map<YearMonth, Double> momRevenueChange,
        Map<YearMonth, Double> momGrowthRate,
        Map<YearMonth, Double> rolling3MonthAverageRevenue,
        double totalDiscountAmount,
        double discountShareOfRevenue,
        double totalRefundImpact
) {
}

