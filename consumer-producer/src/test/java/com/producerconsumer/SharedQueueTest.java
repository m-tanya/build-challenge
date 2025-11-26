package com.producerconsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SharedQueueTest contains unit tests for the SharedQueue class.
 * These tests verify the core functionality of the queue in isolation.
 */
public class SharedQueueTest {

    private SharedQueue<String> queue;

    @BeforeEach
    public void setUp() {
        queue = new SharedQueue<>(5);
    }

    @Test
    public void testBasicPutGet() throws InterruptedException {
        queue.put("item1");
        assertEquals(1, queue.size(), "Queue size should be 1 after put");
        assertFalse(queue.isEmpty(), "Queue should not be empty");

        String item = queue.get();
        assertEquals("item1", item, "Should retrieve correct item");
        assertEquals(0, queue.size(), "Queue size should be 0 after get");
        assertTrue(queue.isEmpty(), "Queue should be empty");
    }

    @Test
    public void testFifoOrdering() throws InterruptedException {
        queue.put("first");
        queue.put("second");
        queue.put("third");

        assertEquals("first", queue.get(), "Should retrieve first item first");
        assertEquals("second", queue.get(), "Should retrieve second item second");
        assertEquals("third", queue.get(), "Should retrieve third item third");
    }

    @Test
    public void testCapacity() throws InterruptedException {
        SharedQueue<String> smallQueue = new SharedQueue<>(2);

        smallQueue.put("1");
        smallQueue.put("2");

        assertTrue(smallQueue.isFull(), "Queue should be full at capacity");
        assertEquals(2, smallQueue.size(), "Size should equal capacity");
    }

    @Test
    public void testMultipleOperations() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            queue.put("item" + i);
            String item = queue.get();
            assertEquals("item" + i, item, "Should get item immediately");
        }
        assertTrue(queue.isEmpty(), "Queue should be empty after balanced ops");
    }

    @Test
    public void testEmptyQueueState() {
        assertTrue(queue.isEmpty(), "New queue should be empty");
        assertFalse(queue.isFull(), "New queue should not be full");
        assertEquals(0, queue.size(), "New queue size should be 0");
    }

    @Test
    public void testConsumersReleaseWhenProducersDone() throws InterruptedException {
        SharedQueue<String> localQueue = new SharedQueue<>(2);
        localQueue.registerProducer();

        Thread consumer = new Thread(() -> {
            try {
                assertNull(localQueue.get(), "Queue should return null after closing");
            } catch (InterruptedException e) {
                fail("Consumer should not be interrupted");
            }
        });

        consumer.start();
        Thread.sleep(50);
        localQueue.producerDone();
        consumer.join(1000);

        assertFalse(consumer.isAlive(), "Consumer thread should exit after queue closes");
    }
}
