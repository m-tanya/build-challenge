package com.producerconsumer;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Producer is a worker thread that reads items from a source container
 * and puts them into a shared queue.
 * 
 * <p>
 * The producer will block if the queue is full, demonstrating the
 * blocking behavior of the producer-consumer pattern.
 * </p>
 * 
 * <p>
 * This class properly handles thread interruption and validates all inputs.
 * </p>
 */
public class Producer extends Thread {
    private static final Logger logger = Logger.getLogger(Producer.class.getName());

    private final String producerName;
    private final Container<WorkItem> source;
    private final SharedQueue<WorkItem> queue;
    private final long delayMs;

    /**
     * Creates a new Producer thread.
     * 
     * @param name    Name of this producer (for logging, must not be null)
     * @param source  Container to read items from (must not be null)
     * @param queue   Shared queue to put items into (must not be null)
     * @param delayMs Delay in milliseconds between producing items (must be
     *                non-negative)
     * @throws NullPointerException     if name, source, or queue is null
     * @throws IllegalArgumentException if delayMs is negative
     */
    public Producer(String name, Container<WorkItem> source,
            SharedQueue<WorkItem> queue, long delayMs) {
        // Make sure all our dependencies are valid
        // Using Objects.requireNonNull gives us clear error messages if something's
        // wrong
        this.producerName = Objects.requireNonNull(name, "Producer name cannot be null");
        this.source = Objects.requireNonNull(source, "Source container cannot be null");
        this.queue = Objects.requireNonNull(queue, "Shared queue cannot be null");
        this.queue.registerProducer();

        // Delay can be zero (no delay) but not negative
        if (delayMs < 0) {
            throw new IllegalArgumentException(
                    "Delay must be non-negative, but was: " + delayMs);
        }
        this.delayMs = delayMs;

        // Set the thread name - makes debugging much easier
        setName(producerName);
    }

    /**
     * Main execution method for the producer thread.
     * Reads all items from source and puts them into the queue.
     * 
     * <p>
     * This method handles interruption gracefully by restoring the
     * interrupt status and exiting cleanly.
     * </p>
     */
    @Override
    public void run() {
        logger.info("[" + producerName + "] Started");

        try {
            int producedCount = 0;

            while (true) {
                WorkItem item = source.removeFirst();
                if (item == null) {
                    break;
                }

                queue.put(item);
                producedCount++;

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            logger.info(String.format("[%s] Finished - produced %d items",
                    producerName, producedCount));

        } catch (InterruptedException e) {
            // Someone interrupted us (probably during shutdown)
            // Restore the interrupt flag so calling code knows we were interrupted
            logger.info("[" + producerName + "] Interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Something unexpected went wrong
            System.err.println("[" + producerName + "] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            queue.producerDone();
        }
    }
}
