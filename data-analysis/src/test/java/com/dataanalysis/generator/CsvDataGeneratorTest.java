package com.dataanalysis.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataGeneratorTest {

    @Test
    void testGenerate(@TempDir Path tempDir) throws Exception {
        Path outputFile = tempDir.resolve("test_sales.csv");
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(100, outputFile);

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("transaction_id")); // Header
        assertTrue(lines.size() >= 101); // Header + at least 100 records (may be more due to multiple line items per invoice)
        assertTrue(lines.size() <= 501); // Header + at most 500 records (100 invoices * 5 max line items)
    }

    @Test
    void testGenerateWithCustomDateRange(@TempDir Path tempDir) throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        CsvDataGenerator generator = new CsvDataGenerator(start, end);

        Path outputFile = tempDir.resolve("test_sales.csv");
        generator.generate(50, outputFile);

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.size() >= 51); // Header + at least 50 records
        assertTrue(lines.size() <= 251); // Header + at most 250 records (50 invoices * 5 max line items)
    }

    @Test
    void testGeneratedDataStructure(@TempDir Path tempDir) throws Exception {
        Path outputFile = tempDir.resolve("test_sales.csv");
        CsvDataGenerator generator = new CsvDataGenerator();
        generator.generate(10, outputFile);

        List<String> lines = Files.readAllLines(outputFile);
        String header = lines.get(0);
        assertTrue(header.contains("transaction_id"));
        assertTrue(header.contains("invoice_id"));
        assertTrue(header.contains("invoice_date"));

        // Check a data line
        if (lines.size() > 1) {
            String[] fields = lines.get(1).split(",");
            assertTrue(fields.length >= 24); // Should have all required fields
            assertTrue(fields[0].startsWith("TXN-")); // Transaction ID format
            assertTrue(fields[1].startsWith("INV-")); // Invoice ID format
        }
    }
}

