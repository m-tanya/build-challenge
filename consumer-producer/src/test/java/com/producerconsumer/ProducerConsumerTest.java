package com.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProducerConsumerTest contains integration tests for the full system.
 * These tests verify that Producers and Consumers work together correctly.
 */
public class ProducerConsumerTest {

    private static final Logger logger = Logger.getLogger(ProducerConsumerTest.class.getName());

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testSingleProducerSingleConsumer() throws InterruptedException {
        logger.info("Test 1: Single producer, single consumer");

        int numItems = 100;
        int capacity = 5;

        Container<WorkItem> source = createSource(numItems);
        SharedQueue<WorkItem> queue = new SharedQueue<>(capacity);
        Container<WorkItem> destination = new Container<>();

        Producer producer = new Producer("P1", source, queue, 0);
        Consumer consumer = new Consumer("C1", queue, destination, numItems, 0);

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        assertEquals(numItems, destination.size(), "Destination should have all items");
        assertEquals(0, queue.size(), "Queue should be empty");
        assertEquals(0, source.size(), "Source container should be empty");

        // Verify content integrity
        for (int i = 1; i <= numItems; i++) {
            WorkItem item = destination.get(i - 1);
            assertEquals(i, item.getId(), "Item ID should match");
            assertEquals("Data-" + i, item.getData(), "Item data should match");
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testMultipleProducersConsumers() throws InterruptedException {
        logger.info("Test 2: Multiple producers/consumers");

        int itemsPerProducer = 50;
        int numProducers = 2;
        int numConsumers = 2;
        int totalItems = itemsPerProducer * numProducers;

        SharedQueue<WorkItem> queue = new SharedQueue<>(10);
        Container<WorkItem> destination = new Container<>();

        Producer[] producers = new Producer[numProducers];
        Consumer[] consumers = new Consumer[numConsumers];

        // Create threads
        for (int i = 0; i < numProducers; i++) {
            // Give each producer its own source with a unique subset of items
            Container<WorkItem> producerSource = new Container<>();
            for (int j = 1; j <= itemsPerProducer; j++) {
                int id = (i * itemsPerProducer) + j;
                producerSource.add(new WorkItem(id, "Data-" + id));
            }
            producers[i] = new Producer("P" + i, producerSource, queue, 1);
        }

        for (int i = 0; i < numConsumers; i++) {
            consumers[i] = new Consumer("C" + i, queue, destination, totalItems / numConsumers, 1);
        }

        // Start threads
        for (Producer p : producers)
            p.start();
        for (Consumer c : consumers)
            c.start();

        // Wait for completion
        for (Producer p : producers)
            p.join();
        for (Consumer c : consumers)
            c.join();

        assertEquals(totalItems, destination.size(), "Destination should have all items");
        assertEquals(0, queue.size(), "Queue should be empty");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testLargeDataSetThroughput() throws InterruptedException {
        logger.info("Test 3: Large data set (1000 items)");

        int numItems = 1000;
        Container<WorkItem> source = createSource(numItems);
        SharedQueue<WorkItem> queue = new SharedQueue<>(50);
        Container<WorkItem> destination = new Container<>();

        Producer producer = new Producer("P1", source, queue, 0); // No delay
        Consumer consumer = new Consumer("C1", queue, destination, numItems, 0); // No delay

        long startTime = System.currentTimeMillis();

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        long duration = System.currentTimeMillis() - startTime;

        assertEquals(numItems, destination.size(), "Destination should have all items");
        assertEquals(0, source.size(), "Source container should be empty");

        double throughput = (double) numItems / duration * 1000;
        logger.info(String.format("Throughput: %.2f items/sec", throughput));

        assertTrue(duration < 2000, "Should complete 1000 items quickly (under 2s)");
    }

    private Container<WorkItem> createSource(int numItems) {
        Container<WorkItem> source = new Container<>();
        for (int i = 1; i <= numItems; i++) {
            source.add(new WorkItem(i, "Data-" + i));
        }
        return source;
    }
}
