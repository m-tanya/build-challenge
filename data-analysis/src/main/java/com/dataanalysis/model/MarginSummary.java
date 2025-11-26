package com.dataanalysis.model;

import java.util.Map;

/**
 * Summary of profitability and margin analysis.
 */
public record MarginSummary(
        double totalGrossProfit,
        double overallGrossMarginPercent,
        Map<String, Double> grossProfitByProduct,
        Map<String, Double> grossMarginPercentByProduct,
        Map<String, Double> grossProfitByCategory,
        Map<String, Double> grossMarginPercentByCategory,
        Map<String, Double> grossProfitByRegion,
        Map<String, Double> grossMarginPercentByRegion,
        Map<String, ProductMarginProfile> productMarginProfiles
) {
    /**
     * Profile of a product showing revenue vs margin characteristics.
     */
    public record ProductMarginProfile(
            String productId,
            String productName,
            double revenue,
            double grossProfit,
            double grossMarginPercent,
            String profile // "High Revenue Low Margin", "High Margin Low Revenue", "Balanced"
    ) {}
}

