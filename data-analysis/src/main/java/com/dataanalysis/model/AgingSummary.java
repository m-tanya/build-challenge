package com.dataanalysis.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * Summary of invoice aging analysis.
 */
public record AgingSummary(
        LocalDate asOfDate,
        double totalOutstandingAmount,
        long totalOpenInvoices,
        Map<String, Double> outstandingAmountByBucket,
        Map<String, Long> invoiceCountByBucket
) {
    /**
     * Aging buckets: Current, 0-30, 31-60, 61-90, 90+
     */
    public static final String[] AGING_BUCKETS = {"Current", "0-30", "31-60", "61-90", "90+"};
}

