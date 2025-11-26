package com.producerconsumer;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * SharedQueue is a bounded, blocking queue that coordinates producer and
 * consumer threads.
 * This implementation uses manual synchronization with wait() and notifyAll().
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Bounded capacity (prevents unbounded memory growth)</li>
 * <li>Blocking operations (producers wait when full, consumers wait when
 * empty)</li>
 * <li>Thread-safe (all operations are synchronized)</li>
 * <li>FIFO ordering (first in, first out)</li>
 * <li>Null-safe (rejects null items)</li>
 * <li>Interrupt-aware (properly handles thread interruption)</li>
 * </ul>
 * 
 * <p>
 * Thread Safety:
 * </p>
 * This class is thread-safe. All public methods are synchronized to ensure
 * mutual exclusion and memory visibility.
 * 
 * @param <T> Type of items stored in the queue (must not be null)
 */
public class SharedQueue<T> {
    private static final Logger logger = Logger.getLogger(SharedQueue.class.getName());

    private final Queue<T> queue;
    private final int capacity;

    // Use AtomicLong to prevent overflow with large numbers of items
    private final AtomicLong itemsProduced = new AtomicLong(0);
    private final AtomicLong itemsConsumed = new AtomicLong(0);
    private int activeProducers = 0;
    private boolean closed = false;

    /**
     * Creates a new SharedQueue with the specified capacity.
     * 
     * @param capacity Maximum number of items the queue can hold
     * @throws IllegalArgumentException if capacity is less than 1 or greater than
     *                                  Integer.MAX_VALUE
     */
    public SharedQueue(int capacity) {
        // Basic validation: capacity must be at least 1
        // (A queue with capacity 0 doesn't make sense!)
        if (capacity < 1) {
            throw new IllegalArgumentException(
                    "Queue capacity must be at least 1, but was: " + capacity);
        }

        // Also check for unreasonably large values
        // Some JVMs reserve space for array headers, so we leave a small buffer
        if (capacity > Integer.MAX_VALUE - 8) {
            throw new IllegalArgumentException(
                    "Queue capacity too large: " + capacity);
        }

        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }

    /**
     * Adds an item to the queue.
     * If the queue is full, this method blocks until space becomes available.
     * 
     * <p>
     * This method uses a while loop (not if) to handle spurious wakeups correctly.
     * After being notified, the thread re-checks the condition before proceeding.
     * </p>
     * 
     * <p>
     * If the thread is interrupted while waiting, the interrupt status is restored
     * and an InterruptedException is thrown.
     * </p>
     * 
     * @param item Item to add to the queue (must not be null)
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws NullPointerException if item is null
     */
    public void put(T item) throws InterruptedException {
        Objects.requireNonNull(item, "Cannot put null item into queue");

        String logMessage = null;
        synchronized (this) {
            while (queue.size() >= capacity) {
                waitSafely();
            }

            if (closed) {
                throw new IllegalStateException("Cannot put items into a closed queue");
            }

            queue.add(item);
            itemsProduced.incrementAndGet();
            logMessage = buildLogMessage("Produced item");
            notifyAll();
        }

        if (logMessage != null) {
            logger.info(logMessage);
        }
    }

    /**
     * Retrieves and removes an item from the queue.
     * If the queue is empty, this method blocks until an item becomes available.
     * 
     * <p>
     * This method uses a while loop (not if) to handle spurious wakeups correctly.
     * After being notified, the thread re-checks the condition before proceeding.
     * </p>
     * 
     * <p>
     * If the thread is interrupted while waiting, the interrupt status is restored
     * and an InterruptedException is thrown.
     * </p>
     * 
     * @return Item removed from the queue (never null)
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public T get() throws InterruptedException {
        T item;
        String logMessage = null;

        synchronized (this) {
            while (queue.isEmpty() && !closed) {
                waitSafely();
            }

            if (queue.isEmpty() && closed) {
                return null;
            }

            item = queue.remove();
            itemsConsumed.incrementAndGet();
            logMessage = buildLogMessage("Consumed item");
            notifyAll();
        }

        if (logMessage != null) {
            logger.info(logMessage);
        }
        return item;
    }

    /**
     * Returns the current number of items in the queue.
     * This is an approximate value in concurrent scenarios.
     * 
     * @return Current queue size (0 to capacity)
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Checks if the queue is empty.
     * This is an approximate value in concurrent scenarios.
     * 
     * @return true if queue is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Checks if the queue is full.
     * This is an approximate value in concurrent scenarios.
     * 
     * @return true if queue is at capacity, false otherwise
     */
    public synchronized boolean isFull() {
        return queue.size() >= capacity;
    }

    /**
     * Returns the maximum capacity of the queue.
     * 
     * @return Queue capacity (always positive)
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the total number of items that have been produced.
     * Uses AtomicLong to prevent overflow with very large numbers.
     * 
     * @return Total items produced (0 to Long.MAX_VALUE)
     */
    public long getItemsProduced() {
        return itemsProduced.get();
    }

    /**
     * Returns the total number of items that have been consumed.
     * Uses AtomicLong to prevent overflow with very large numbers.
     * 
     * @return Total items consumed (0 to Long.MAX_VALUE)
     */
    public long getItemsConsumed() {
        return itemsConsumed.get();
    }

    /**
     * Returns the number of items currently in transit (produced but not yet
     * consumed).
     * 
     * @return Items in transit (should equal current queue size in steady state)
     */
    public synchronized long getItemsInTransit() {
        return itemsProduced.get() - itemsConsumed.get();
    }

    /**
     * Prints statistics about queue usage.
     * Thread-safe method that provides a consistent snapshot of queue state.
     */
    public synchronized void printStats() {
        logger.info("\n=== Queue Statistics ===");
        logger.info("Capacity: " + capacity);
        logger.info("Items produced: " + itemsProduced.get());
        logger.info("Items consumed: " + itemsConsumed.get());
        logger.info("Current size: " + queue.size());
        logger.info("Items in transit: " + getItemsInTransit());
        logger.info("Closed: " + closed);
    }

    /**
     * Returns a string representation of this queue.
     * Useful for debugging.
     * 
     * @return String representation including size and capacity
     */
    @Override
    public synchronized String toString() {
        return "SharedQueue{" +
                "size=" + queue.size() +
                ", capacity=" + capacity +
                ", produced=" + itemsProduced.get() +
                ", consumed=" + itemsConsumed.get() +
                ", closed=" + closed +
                '}';
    }

    /**
     * Registers a producer so the queue knows when all producers have finished.
     */
    public synchronized void registerProducer() {
        if (closed) {
            throw new IllegalStateException("Queue already closed");
        }
        activeProducers++;
    }

    /**
     * Marks a producer as completed. When the last producer finishes, the queue is
     * closed and blocked consumers are released.
     */
    public synchronized void producerDone() {
        if (activeProducers == 0) {
            return;
        }

        activeProducers--;
        if (activeProducers == 0) {
            closed = true;
            notifyAll();
        }
    }

    private void waitSafely() throws InterruptedException {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private String buildLogMessage(String prefix) {
        return String.format("[%s] %s. Queue size: %d/%d",
                Thread.currentThread().getName(), prefix, queue.size(), capacity);
    }
}
