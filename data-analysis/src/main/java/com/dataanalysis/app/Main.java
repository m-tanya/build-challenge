package com.dataanalysis.app;

import com.dataanalysis.analytics.SalesAnalyticsService;
import com.dataanalysis.generator.CsvDataGenerator;
import com.dataanalysis.loader.CsvSalesLoader;
import com.dataanalysis.model.AgingSummary;
import com.dataanalysis.model.MarginSummary;
import com.dataanalysis.model.RevenueSummary;
import com.dataanalysis.report.ConsoleReporter;
import com.dataanalysis.util.DateUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the sales data analysis application.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];

        try {
            if ("generate".equals(command)) {
                handleGenerate(args);
            } else if ("analyze".equals(command)) {
                handleAnalyze(args);
            } else {
                logger.severe("Unknown command: " + command);
                printUsage();
                System.exit(1);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  Generate CSV data:");
        System.out.println("    java -cp out com.dataanalysis.app.Main generate <count> <outputPath>");
        System.out.println("    Example: generate 5000 data/sales.csv");
        System.out.println();
        System.out.println("  Analyze sales data:");
        System.out.println("    java -cp out com.dataanalysis.app.Main analyze [inputPath] [--asOfDate=YYYY-MM-DD]");
        System.out.println("    Example: analyze data/sales.csv --asOfDate=2025-01-31");
        System.out.println("    Example: analyze --asOfDate=2025-01-31  (uses default: data/sales.csv)");
        System.out.println("    Example: analyze  (uses default: data/sales.csv)");
    }

    private static void handleGenerate(String[] args) throws Exception {
        if (args.length < 3) {
            logger.severe("Error: generate command requires <count> and <outputPath>");
            printUsage();
            System.exit(1);
        }

        int count = Integer.parseInt(args[1]);
        Path outputPath = Paths.get(args[2]);

        logger.info("Generating " + count + " sales records to " + outputPath);
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(count, outputPath);
        logger.info("Generation complete!");
    }

    private static void handleAnalyze(String[] args) throws Exception {
        Path inputPath = null;
        LocalDate asOfDate = LocalDate.now();

        // Parse arguments - CSV path can be first argument or after --asOfDate
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--asOfDate=")) {
                String dateStr = args[i].substring("--asOfDate=".length());
                asOfDate = DateUtils.parseDate(dateStr);
                if (asOfDate == null) {
                    logger.severe("Error: Invalid date format. Use YYYY-MM-DD");
                    System.exit(1);
                }
            } else if (!args[i].startsWith("--")) {
                // This is the CSV path (first non-option argument)
                if (inputPath == null) {
                    inputPath = Paths.get(args[i]);
                }
            }
        }

        // Use default CSV path if not provided
        if (inputPath == null) {
            inputPath = Paths.get("data/sales.csv");
            logger.info("No CSV path provided, using default: " + inputPath);
        }

        logger.info("Loading sales data from: " + inputPath);
        CsvSalesLoader loader = new CsvSalesLoader();
        List<com.dataanalysis.model.SalesRecord> records = loader.load(inputPath);

        if (records.isEmpty()) {
            logger.severe("Error: No valid records found in CSV file");
            System.exit(1);
        }

        logger.info("Performing analytics...");
        SalesAnalyticsService analytics = new SalesAnalyticsService();

        // Calculate all analytics
        RevenueSummary revenueSummary = analytics.calculateRevenueSummary(records);
        Map<String, Double> topProducts = analytics.topProductsByRevenue(records, 5);
        Map<String, Double> topCustomers = analytics.topCustomersByRevenue(records, 5);
        Map<String, Double> topRegions = analytics.topRegionsByRevenue(records, 10);
        Map<String, Map<java.time.YearMonth, Double>> cashVsAccrual = analytics.cashVsAccrualRevenue(records);
        AgingSummary agingSummary = analytics.calculateAgingSummary(records, asOfDate);
        MarginSummary marginSummary = analytics.calculateMarginSummary(records);

        // Print report
        ConsoleReporter reporter = new ConsoleReporter();
        reporter.printReport(revenueSummary, topProducts, topCustomers, topRegions,
                cashVsAccrual, agingSummary, marginSummary);
    }
}

