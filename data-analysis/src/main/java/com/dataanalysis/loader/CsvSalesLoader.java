package com.dataanalysis.loader;

import com.dataanalysis.model.SalesRecord;
import com.dataanalysis.util.DateUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Loads and parses sales data from CSV files.
 */
public class CsvSalesLoader {
    private static final Logger logger = Logger.getLogger(CsvSalesLoader.class.getName());

    /**
     * Loads sales records from a CSV file.
     * @param csvPath path to the CSV file
     * @return list of parsed SalesRecord objects
     * @throws IOException if file reading fails
     */
    public List<SalesRecord> load(Path csvPath) throws IOException {
        logger.info("Loading sales data from: " + csvPath);
        List<SalesRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.skip(1) // Skip header
                    .forEach(line -> {
                        try {
                            SalesRecord record = parseLine(line);
                            if (record != null) {
                                records.add(record);
                            } else {
                                errors.add("Failed to parse line: " + line);
                            }
                        } catch (Exception e) {
                            errors.add("Error parsing line: " + line + " - " + e.getMessage());
                        }
                    });
        }

        logger.info("Loaded " + records.size() + " valid records");
        if (!errors.isEmpty()) {
            logger.warning("Skipped " + errors.size() + " invalid rows");
            errors.forEach(error -> logger.warning(error));
        }

        return records;
    }

    /**
     * Parses a single CSV line into a SalesRecord.
     * @param line CSV line
     * @return SalesRecord or null if parsing fails
     */
    private SalesRecord parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = parseCsvLine(line);
        if (parts.length < 24) {
            return null;
        }

        try {
            return new SalesRecord(
                    parts[0].trim(),
                    parts[1].trim(),
                    DateUtils.parseDate(parts[2]),
                    DateUtils.parseDate(parts[3]),
                    DateUtils.parseDate(parts[4]),
                    parts[5].trim(),
                    parts[6].trim(),
                    parts[7].trim(),
                    parts[8].trim(),
                    parts[9].trim(),
                    parseInt(parts[10]),
                    parseDouble(parts[11]),
                    parseDouble(parts[12]),
                    parseDouble(parts[13]),
                    parseDouble(parts[14]),
                    parts[15].trim(),
                    parts[16].trim(),
                    parts[17].trim(),
                    parts[18].trim(),
                    parseBoolean(parts[19]),
                    parseBoolean(parts[20]),
                    parts[21].trim(),
                    parts[22].trim(),
                    parts[23].trim()
            );
        } catch (Exception e) {
            // Silently skip malformed lines - errors are collected in load() method
            return null;
        }
    }

    /**
     * Parses a CSV line handling quoted fields.
     * Simple implementation - splits on comma but respects quoted strings.
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    private int parseInt(String s) {
        try {
            return s == null || s.trim().isEmpty() ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(String s) {
        try {
            return s == null || s.trim().isEmpty() ? 0.0 : Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(String s) {
        if (s == null) {
            return false;
        }
        String trimmed = s.trim().toLowerCase();
        return "true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed);
    }
}

