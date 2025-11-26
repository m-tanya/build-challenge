package com.producerconsumer;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * ProducerConsumerDemo demonstrates the producer-consumer pattern
 * with thread synchronization.
 * 
 * This application creates a producer thread that reads items from a source
 * container and puts them into a shared queue, and a consumer thread that
 * retrieves items from the queue and stores them in a destination container.
 * 
 * The shared queue has bounded capacity, causing producers to block when full
 * and consumers to block when empty, demonstrating proper thread
 * synchronization.
 */
public class ProducerConsumerDemo {

    // Configure logger format
    static {
        Logger rootLogger = Logger.getLogger("");
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n",
                        dateFormat.format(new Date(record.getMillis())),
                        record.getLevel(),
                        record.getMessage());
            }
        });
        rootLogger.addHandler(handler);
    }

    public static void main(String[] args) {
        // Configuration
        int numItems = 20;
        int queueCapacity = 5;
        long producerDelay = 50; // milliseconds
        long consumerDelay = 100; // milliseconds (slower consumer)

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Producer-Consumer Pattern Demo      ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.println("Configuration:");
        System.out.println("  Items to transfer: " + numItems);
        System.out.println("  Queue capacity: " + queueCapacity);
        System.out.println("  Producer delay: " + producerDelay + "ms");
        System.out.println("  Consumer delay: " + consumerDelay + "ms");
        System.out.println("  (Consumer is slower - expect queue to fill up)\n");

        // Setup components
        Container<WorkItem> source = createSource(numItems);
        SharedQueue<WorkItem> queue = new SharedQueue<>(queueCapacity);
        Container<WorkItem> destination = new Container<>();

        System.out.println("Starting producer and consumer threads...\n");

        // Create threads
        Producer producer = new Producer("Producer-1", source, queue, producerDelay);
        Consumer consumer = new Consumer("Consumer-1", queue, destination,
                numItems, consumerDelay);

        // Start timing
        long startTime = System.currentTimeMillis();

        // Start threads
        producer.start();
        consumer.start();

        // Wait for both threads to complete
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted");
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - startTime;

        // Print statistics
        queue.printStats();

        // Verify results
        System.out.println("\n=== Results ===");
        System.out.println("Time elapsed: " + duration + "ms");
        System.out.println("Source size: " + source.size());
        System.out.println("Destination size: " + destination.size());
        System.out.println("Queue size: " + queue.size());

        // Verification
        boolean success = destination.size() == numItems && queue.size() == 0;
        System.out.println("\nVerification: " + (success ? "✓ SUCCESS" : "✗ FAILED"));

        if (success) {
            System.out.println("All " + numItems + " items successfully transferred!");
        } else {
            System.out.println("ERROR: Expected " + numItems + " items in destination, " +
                    "got " + destination.size());
        }
    }

    /**
     * Creates a source container with the specified number of work items.
     * 
     * @param numItems Number of items to create
     * @return Container filled with work items
     */
    private static Container<WorkItem> createSource(int numItems) {
        Container<WorkItem> source = new Container<>();
        for (int i = 1; i <= numItems; i++) {
            source.add(new WorkItem(i, "Data-" + i));
        }
        return source;
    }
}
