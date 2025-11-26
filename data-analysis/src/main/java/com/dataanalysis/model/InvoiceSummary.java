package com.dataanalysis.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Summary of an invoice with its line items.
 */
public record InvoiceSummary(
        String invoiceId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        LocalDate paymentDate,
        String paymentStatus,
        double totalAmount,
        List<SalesRecord> lineItems
) {
    /**
     * Calculates the outstanding amount for this invoice.
     * @return totalAmount if not paid, 0 if paid
     */
    public double outstandingAmount() {
        return "PAID".equals(paymentStatus) ? 0.0 : totalAmount;
    }

    /**
     * Calculates days overdue from a reference date.
     * @param asOfDate the reference date
     * @return days overdue, or 0 if not overdue
     */
    public long daysOverdue(LocalDate asOfDate) {
        if ("PAID".equals(paymentStatus) || dueDate == null) {
            return 0;
        }
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(dueDate, asOfDate));
    }

    /**
     * Gets the aging bucket for this invoice.
     * @param asOfDate the reference date
     * @return aging bucket string (0-30, 31-60, 61-90, 90+)
     */
    public String agingBucket(LocalDate asOfDate) {
        long days = daysOverdue(asOfDate);
        if (days == 0) {
            return "Current";
        } else if (days <= 30) {
            return "0-30";
        } else if (days <= 60) {
            return "31-60";
        } else if (days <= 90) {
            return "61-90";
        } else {
            return "90+";
        }
    }
}

