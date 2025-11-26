package com.dataanalysis.analytics;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility methods for common aggregation operations.
 */
public class AggregationUtils {
    /**
     * Sorts a map by values in descending order and returns top N entries.
     * @param map the map to sort
     * @param topN number of top entries to return
     * @return sorted map with top N entries
     */
    public static <K, V extends Comparable<V>> Map<K, V> topN(Map<K, V> map, int topN) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}

