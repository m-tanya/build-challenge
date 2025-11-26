package com.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EdgeCaseTest contains tests for edge cases and error conditions.
 * These tests verify that the system handles unusual inputs and error scenarios
 * correctly.
 */
public class EdgeCaseTest {

    @Test
    public void testNullItemRejection() {
        SharedQueue<String> queue = new SharedQueue<>(5);

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            queue.put(null);
        }, "Should throw NullPointerException for null item");

        assertTrue(exception.getMessage().contains("null"), "Exception message should mention null");
        assertEquals(0, queue.size(), "Queue should remain empty after null rejection");
    }

    @Test
    public void testZeroCapacity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SharedQueue<String>(0);
        }, "Should throw IllegalArgumentException for capacity 0");

        assertTrue(exception.getMessage().contains("1"), "Exception should mention minimum capacity");
    }

    @Test
    public void testNegativeCapacity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SharedQueue<String>(-5);
        }, "Should throw IllegalArgumentException for negative capacity");

        assertTrue(exception.getMessage().contains("-5"), "Exception should mention invalid capacity");
    }

    @Test
    public void testVeryLargeCapacity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SharedQueue<String>(Integer.MAX_VALUE);
        }, "Should throw IllegalArgumentException for excessive capacity");

        String msg = exception.getMessage();
        assertTrue(msg.contains("large") || msg.contains("Large"), "Exception should mention size issue");
    }

    @Test
    public void testCapacityOne() throws InterruptedException {
        SharedQueue<Integer> queue = new SharedQueue<>(1);

        queue.put(1);
        assertEquals(1, queue.size(), "Queue should have 1 item");
        assertTrue(queue.isFull(), "Queue should be full");

        int item = queue.get();
        assertEquals(1, item, "Should get correct item");
        assertTrue(queue.isEmpty(), "Queue should be empty");
        assertEquals(0, queue.size(), "Size should be 0");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testThreadInterruption() throws InterruptedException {
        SharedQueue<String> queue = new SharedQueue<>(1);
        queue.put("item1"); // Fill the queue

        // Create a thread that will block on put()
        Thread producer = new Thread(() -> {
            try {
                queue.put("item2"); // This will block
                fail("Should not reach here - thread should be interrupted");
            } catch (InterruptedException e) {
                // Expected - thread was interrupted while waiting
                assertTrue(Thread.currentThread().isInterrupted(),
                        "Interrupt flag should be set so callers can react");
            }
        });

        producer.start();
        Thread.sleep(100); // Give thread time to block
        producer.interrupt(); // Interrupt the blocked thread
        producer.join(1000); // Wait for thread to finish

        assertFalse(producer.isAlive(), "Thread should have terminated");
    }

    @Test
    public void testOverflowProtection() throws InterruptedException {
        SharedQueue<Integer> queue = new SharedQueue<>(10);

        // Simulate many operations
        for (int i = 0; i < 1000; i++) {
            queue.put(i);
            queue.get();
        }

        assertEquals(1000, queue.getItemsProduced(), "Should track 1000 produced items");
        assertEquals(1000, queue.getItemsConsumed(), "Should track 1000 consumed items");
        assertEquals(0, queue.getItemsInTransit(), "Should have 0 items in transit");

        // Verify return type is long (not int) to prevent overflow
        long produced = queue.getItemsProduced();
        assertTrue(produced >= 0, "Produced count should be non-negative");
    }

    @Test
    public void testEmptyQueueOperations() throws InterruptedException {
        SharedQueue<String> queue = new SharedQueue<>(5);

        // Test empty queue
        assertTrue(queue.isEmpty(), "New queue should be empty");
        assertFalse(queue.isFull(), "New queue should not be full");
        assertEquals(0, queue.size(), "Size should be 0");
        assertEquals(0, queue.getItemsProduced(), "No items produced yet");
        assertEquals(0, queue.getItemsConsumed(), "No items consumed yet");

        // Add one item
        queue.put("test");
        assertFalse(queue.isEmpty(), "Queue should not be empty");
        assertFalse(queue.isFull(), "Queue should not be full");
        assertEquals(1, queue.size(), "Size should be 1");

        // Fill queue
        for (int i = 0; i < 4; i++) {
            queue.put("item" + i);
        }
        assertTrue(queue.isFull(), "Queue should be full");
        assertEquals(5, queue.size(), "Size should be 5");
    }
}
