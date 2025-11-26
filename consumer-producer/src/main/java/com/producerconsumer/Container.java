package com.producerconsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Container is a thread-safe storage for items.
 * Used as both source (for producers) and destination (for consumers).
 * 
 * @param <T> Type of items stored in the container
 */
public class Container<T> {
    private final List<T> items;

    /**
     * Creates a new empty container.
     */
    public Container() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds an item to the container.
     * This method is synchronized to ensure thread safety.
     * 
     * @param item Item to add
     */
    public synchronized void add(T item) {
        items.add(item);
    }

    /**
     * Gets an item at the specified index.
     * This method is synchronized to ensure thread safety.
     * 
     * @param index Index of item to retrieve
     * @return Item at the specified index
     */
    public synchronized T get(int index) {
        return items.get(index);
    }

    /**
     * Returns the number of items in the container.
     * This method is synchronized to ensure thread safety.
     * 
     * @return Number of items
     */
    public synchronized int size() {
        return items.size();
    }

    /**
     * Returns a defensive copy of all items in the container.
     * This prevents external modification of the internal list.
     * This method is synchronized to ensure thread safety.
     * 
     * @return Copy of all items
     */
    public synchronized List<T> getAll() {
        return new ArrayList<>(items);
    }

    /**
     * Removes and returns the first item in the container, or null if empty.
     * This is used by producers to drain items without exposing internal state.
     *
     * @return The removed item, or null if container is empty
     */
    public synchronized T removeFirst() {
        if (items.isEmpty()) {
            return null;
        }
        return items.remove(0);
    }

    /**
     * Checks if the container is empty.
     * This method is synchronized to ensure thread safety.
     * 
     * @return true if container is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }
}
