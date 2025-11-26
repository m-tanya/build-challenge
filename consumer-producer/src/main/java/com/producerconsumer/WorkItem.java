package com.producerconsumer;

/**
 * WorkItem represents a unit of work to be transferred from producer to consumer.
 * This class is immutable to ensure thread safety.
 */
public class WorkItem {
    private final int id;
    private final String data;
    private final long timestamp;
    
    /**
     * Creates a new WorkItem with the given id and data.
     * Timestamp is automatically set to current time.
     * 
     * @param id Unique identifier for this work item
     * @param data Data payload
     */
    public WorkItem(int id, String data) {
        this.id = id;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public String getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "WorkItem{id=" + id + ", data='" + data + "'}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkItem other = (WorkItem) obj;
        return id == other.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
