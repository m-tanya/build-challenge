package com.dataanalysis.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic sales data CSV files for testing and demonstration.
 */
public class CsvDataGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Random RANDOM = new Random();

    private static final String[] CUSTOMER_SEGMENTS = {"SMB", "Freelancer", "Startup", "Enterprise"};
    private static final String[] PRODUCT_CATEGORIES = {"Subscription", "Hardware", "Software", "Service"};
    private static final String[] REGIONS = {"CA", "TX", "NY", "FL", "IL", "WA"};
    private static final String[] CHANNELS = {"Online", "Retail", "Partner"};
    private static final String[] SUBSCRIPTION_PLANS = {"Monthly", "Annual", "Quarterly"};
    private static final String[] PAYMENT_STATUSES = {"PAID", "PARTIAL", "UNPAID"};
    private static final String[] TAX_JURISDICTIONS = {"CA-STATE", "CA-LOCAL", "TX-STATE", "NY-STATE", "FL-STATE"};

    private static final String[] PRODUCTS = {
            "Bookkeeping Software", "Tax Preparation Tool", "Payroll System", "Invoice Generator",
            "Expense Tracker", "Financial Dashboard", "Accounting API", "Receipt Scanner",
            "Time Tracking", "Project Management", "CRM Integration", "Reporting Suite"
    };

    private final LocalDate startDate;
    private final LocalDate endDate;

    public CsvDataGenerator(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CsvDataGenerator() {
        this(LocalDate.now().minusMonths(24), LocalDate.now());
    }

    /**
     * Generates synthetic sales records and writes them to a CSV file.
     * @param count number of records to generate
     * @param outputPath path to output CSV file
     * @throws IOException if file writing fails
     */
    public void generate(int count, Path outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(getHeader());

        int invoiceCounter = 2000;
        int transactionCounter = 1000;
        LocalDate currentDate = startDate;

        for (int i = 0; i < count; i++) {
            String invoiceId = "INV-" + invoiceCounter;
            LocalDate invoiceDate = randomDateBetween(startDate, endDate);
            LocalDate dueDate = invoiceDate.plusDays(15 + RANDOM.nextInt(15)); // 15-30 days payment terms
            LocalDate paymentDate = null;
            String paymentStatus = PAYMENT_STATUSES[RANDOM.nextInt(PAYMENT_STATUSES.length)];

            // If paid, set payment date (sometimes late)
            if ("PAID".equals(paymentStatus)) {
                int daysAfterDue = RANDOM.nextInt(45) - 10; // -10 to 35 days after due date
                paymentDate = dueDate.plusDays(Math.max(0, daysAfterDue));
            } else if ("PARTIAL".equals(paymentStatus)) {
                // Partial payment might have a payment date
                if (RANDOM.nextBoolean()) {
                    paymentDate = dueDate.plusDays(RANDOM.nextInt(30));
                }
            }

            // Generate 1-5 line items per invoice
            int lineItems = 1 + RANDOM.nextInt(5);
            for (int j = 0; j < lineItems; j++) {
                String transactionId = "TXN-" + transactionCounter++;
                String line = generateLine(transactionId, invoiceId, invoiceDate, paymentDate, dueDate,
                        paymentStatus, transactionCounter);
                lines.add(line);
            }

            invoiceCounter++;
        }

        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, lines);
    }

    private String getHeader() {
        return "transaction_id,invoice_id,invoice_date,payment_date,due_date,customer_id,customer_segment," +
                "product_id,product_name,product_category,quantity,unit_price,discount_amount,tax_amount," +
                "cost_of_goods_sold,currency,region,country,channel,is_refund,is_subscription," +
                "subscription_plan,payment_status,tax_jurisdiction";
    }

    private String generateLine(String transactionId, String invoiceId, LocalDate invoiceDate,
                                 LocalDate paymentDate, LocalDate dueDate, String paymentStatus, int seed) {
        Random localRandom = new Random(seed);
        String customerId = "CUST-" + (100 + localRandom.nextInt(50));
        String customerSegment = CUSTOMER_SEGMENTS[localRandom.nextInt(CUSTOMER_SEGMENTS.length)];
        int productIndex = localRandom.nextInt(PRODUCTS.length);
        String productName = PRODUCTS[productIndex];
        String productId = "PROD-" + (productIndex + 1);
        String productCategory = PRODUCT_CATEGORIES[localRandom.nextInt(PRODUCT_CATEGORIES.length)];
        int quantity = 1 + localRandom.nextInt(10);
        double unitPrice = 19.99 + localRandom.nextDouble() * 200; // $19.99 to $219.99
        double discountAmount = localRandom.nextDouble() < 0.3 ? localRandom.nextDouble() * 20 : 0; // 30% chance of discount
        double taxAmount = (quantity * unitPrice - discountAmount) * 0.08; // 8% tax
        double costOfGoodsSold = unitPrice * (0.3 + localRandom.nextDouble() * 0.4); // 30-70% of price
        String currency = "USD";
        String region = REGIONS[localRandom.nextInt(REGIONS.length)];
        String country = "US";
        String channel = CHANNELS[localRandom.nextInt(CHANNELS.length)];
        boolean isRefund = localRandom.nextDouble() < 0.05; // 5% chance of refund
        boolean isSubscription = productCategory.equals("Subscription");
        String subscriptionPlan = isSubscription ? SUBSCRIPTION_PLANS[localRandom.nextInt(SUBSCRIPTION_PLANS.length)] : "";
        String taxJurisdiction = region + "-" + (localRandom.nextBoolean() ? "STATE" : "LOCAL");

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                transactionId, invoiceId, formatDate(invoiceDate), formatDate(paymentDate), formatDate(dueDate),
                customerId, customerSegment, productId, productName, productCategory, quantity, unitPrice,
                discountAmount, taxAmount, costOfGoodsSold, currency, region, country, channel,
                isRefund, isSubscription, subscriptionPlan, paymentStatus, taxJurisdiction);
    }

    private LocalDate randomDateBetween(LocalDate start, LocalDate end) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        long randomDays = RANDOM.nextInt((int) daysBetween);
        return start.plusDays(randomDays);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }
}

