package com.dataanalysis.model;

import java.time.LocalDate;

/**
 * Immutable record representing a single line item from the sales CSV.
 */
public record SalesRecord(
        String transactionId,
        String invoiceId,
        LocalDate invoiceDate,
        LocalDate paymentDate,
        LocalDate dueDate,
        String customerId,
        String customerSegment,
        String productId,
        String productName,
        String productCategory,
        int quantity,
        double unitPrice,
        double discountAmount,
        double taxAmount,
        double costOfGoodsSold,
        String currency,
        String region,
        String country,
        String channel,
        boolean isRefund,
        boolean isSubscription,
        String subscriptionPlan,
        String paymentStatus,
        String taxJurisdiction
) {
    /**
     * Calculates the gross amount before discounts and refunds.
     * @return quantity * unitPrice
     */
    public double grossAmount() {
        return quantity * unitPrice;
    }

    /**
     * Calculates the net amount after discounts.
     * If this is a refund, the net amount is negative.
     * @return (grossAmount - discountAmount) * (isRefund ? -1 : 1)
     */
    public double netAmount() {
        double amount = grossAmount() - discountAmount;
        return isRefund ? -amount : amount;
    }

    /**
     * Calculates gross profit for this line item.
     * @return netAmount - (costOfGoodsSold * quantity)
     */
    public double grossProfit() {
        return netAmount() - (costOfGoodsSold * quantity);
    }

    /**
     * Checks if the invoice is paid.
     * @return true if paymentStatus is "PAID"
     */
    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

    /**
     * Checks if the invoice is open (not fully paid).
     * @return true if paymentStatus is not "PAID"
     */
    public boolean isOpen() {
        return !isPaid();
    }
}

