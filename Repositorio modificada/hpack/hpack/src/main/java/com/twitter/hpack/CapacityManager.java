package main.java.com.twitter.hpack;

import static com.twitter.hpack.HeaderField.HEADER_ENTRY_OVERHEAD;

final class CapacityManager {
    private int capacity;
    private int currentSize;
    
    CapacityManager(int initialCapacity) {
        this.capacity = initialCapacity;
        this.currentSize = 0;
    }
    
    int getCapacity() {
        return capacity;
    }
    
    void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
    }
    
    int getCurrentSize() {
        return currentSize;
    }
    
    void updateSize(int delta) {
        currentSize += delta;
        // Asegurar que el tamaño no sea negativo
        if (currentSize < 0) {
            currentSize = 0;
        }
    }
    
    void resetSize() {
        currentSize = 0;
    }
    
    int calculateMaxEntries() {
        if (capacity == 0) {
            return 0;
        }
        
        int maxEntries = capacity / HEADER_ENTRY_OVERHEAD;
        if (capacity % HEADER_ENTRY_OVERHEAD != 0) {
            maxEntries++;
        }
        return Math.max(1, maxEntries); // Mínimo 1 entrada
    }
    
    int getAvailableCapacity() {
        return capacity - currentSize;
    }
    
    boolean hasCapacityFor(int entrySize) {
        return entrySize <= getAvailableCapacity();
    }
}