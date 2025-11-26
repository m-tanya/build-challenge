package com.dataanalysis.analytics;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AggregationUtilsTest {

    @Test
    void testTopN() {
        Map<String, Double> data = Map.of(
                "A", 100.0,
                "B", 200.0,
                "C", 150.0,
                "D", 50.0
        );

        Map<String, Double> top2 = AggregationUtils.topN(data, 2);

        assertEquals(2, top2.size());
        assertTrue(top2.containsKey("B"));
        assertTrue(top2.containsKey("C"));
        assertEquals(200.0, top2.get("B"), 0.01);
        assertEquals(150.0, top2.get("C"), 0.01);
    }

    @Test
    void testTopNWithMoreThanAvailable() {
        Map<String, Double> data = Map.of("A", 100.0, "B", 200.0);

        Map<String, Double> top5 = AggregationUtils.topN(data, 5);

        assertEquals(2, top5.size());
    }

    @Test
    void testTopNEmpty() {
        Map<String, Double> empty = Map.of();
        Map<String, Double> result = AggregationUtils.topN(empty, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void testTopNOrdering() {
        Map<String, Double> data = Map.of(
                "A", 10.0,
                "B", 30.0,
                "C", 20.0
        );

        Map<String, Double> top = AggregationUtils.topN(data, 3);

        // Should be ordered by value descending
        Object[] keys = top.keySet().toArray();
        assertEquals("B", keys[0]);
        assertEquals("C", keys[1]);
        assertEquals("A", keys[2]);
    }
}

