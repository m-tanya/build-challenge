package com.producerconsumer;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Consumer is a worker thread that retrieves items from a shared queue
 * and stores them in a destination container.
 * 
 * <p>
 * The consumer will block if the queue is empty, demonstrating the
 * blocking behavior of the producer-consumer pattern.
 * </p>
 * 
 * <p>
 * This class properly handles thread interruption and validates all inputs.
 * </p>
 */
public class Consumer extends Thread {
    private static final Logger logger = Logger.getLogger(Consumer.class.getName());

    private final String consumerName;
    private final SharedQueue<WorkItem> queue;
    private final Container<WorkItem> destination;
    private final int itemsToConsume;
    private final long delayMs;

    /**
     * Creates a new Consumer thread.
     * 
     * @param name           Name of this consumer (for logging, must not be null)
     * @param queue          Shared queue to get items from (must not be null)
     * @param destination    Container to store consumed items (must not be null)
     * @param itemsToConsume Number of items this consumer should consume (must be
     *                       non-negative)
     * @param delayMs        Delay in milliseconds between consuming items (must be
     *                       non-negative)
     * @throws NullPointerException     if name, queue, or destination is null
     * @throws IllegalArgumentException if itemsToConsume or delayMs is negative
     */
    public Consumer(String name, SharedQueue<WorkItem> queue,
            Container<WorkItem> destination, int itemsToConsume, long delayMs) {
        // Validate all our inputs upfront
        this.consumerName = Objects.requireNonNull(name, "Consumer name cannot be null");
        this.queue = Objects.requireNonNull(queue, "Shared queue cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination container cannot be null");

        // itemsToConsume can be zero (consumer does nothing) but not negative
        if (itemsToConsume < 0) {
            throw new IllegalArgumentException(
                    "Items to consume must be non-negative, but was: " + itemsToConsume);
        }
        this.itemsToConsume = itemsToConsume;

        // Same for delay - zero is fine, negative doesn't make sense
        if (delayMs < 0) {
            throw new IllegalArgumentException(
                    "Delay must be non-negative, but was: " + delayMs);
        }
        this.delayMs = delayMs;

        // Set thread name for easier debugging
        setName(consumerName);
    }

    /**
     * Main execution method for the consumer thread.
     * Retrieves items from queue and stores them in destination.
     * 
     * <p>
     * This method handles interruption gracefully by restoring the
     * interrupt status and exiting cleanly.
     * </p>
     */
    @Override
    public void run() {
        logger.info("[" + consumerName + "] Started");

        try {
            int consumed = 0;

            if (itemsToConsume == 0) {
                logger.info("[" + consumerName + "] No items to consume");
                return;
            }

            while (consumed < itemsToConsume) {
                WorkItem item = queue.get();

                if (item == null) {
                    logger.info("[" + consumerName + "] Queue closed, stopping consumption");
                    break;
                }

                destination.add(item);
                consumed++;

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            logger.info(String.format("[%s] Finished - consumed %d items",
                    consumerName, consumed));

        } catch (InterruptedException e) {
            // We were interrupted - probably shutting down
            // Important: restore the interrupt flag for any code above us
            logger.info("[" + consumerName + "] Interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Something went wrong that we didn't expect
            System.err.println("[" + consumerName + "] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
