package main.java.com.twitter.hpack;

import java.io.IOException;

public class DynamicTable implements DynamicHeaderTable {
    private final CircularQueue circularQueue;
    private final CapacityManager capacityManager;
    private EvictionPolicy evictionPolicy;
    
    public DynamicTable(int initialCapacity) {
        this(initialCapacity, new DefaultEvictionPolicy());
    }
    
    public DynamicTable(int initialCapacity, EvictionPolicy evictionPolicy) {
        this.circularQueue = new CircularQueue();
        this.capacityManager = new CapacityManager(initialCapacity);
        this.evictionPolicy = evictionPolicy;
        setCapacity(initialCapacity);
    }
    
    @Override
    public int length() {
        return circularQueue.length();
    }
    
    @Override
    public int size() {
        return circularQueue.size();
    }
    
    @Override
    public int capacity() {
        return capacityManager.getCapacity();
    }
    
    @Override
    public HeaderField getEntry(int index) throws IOException {
        if (index <= 0 || index > length()) {
            throw new IOException("illegal index value");
        }
        return circularQueue.getEntry(index);
    }
    
    @Override
    public int getIndex(byte[] name) {
        return circularQueue.findIndexByName(name);
    }
    
    @Override
    public int getIndex(byte[] name, byte[] value) {
        return circularQueue.findIndexByNameAndValue(name, value);
    }
    
    @Override
    public void add(HeaderField header) {
        int headerSize = header.size();
        if (headerSize > capacityManager.getCapacity()) {
            clear();
            return;
        }
        
        // Usar la política de evicción para determinar cuántas entradas evictar
        while (evictionPolicy.shouldEvict(size(), headerSize, capacityManager.getCapacity())) {
            int entriesToEvict = evictionPolicy.calculateEntriesToEvict(size(), headerSize, capacityManager.getCapacity());
            for (int i = 0; i < entriesToEvict && size() > 0; i++) {
                remove();
            }
        }
        
        circularQueue.add(header);
        capacityManager.updateSize(headerSize);
    }
    
    @Override
    public HeaderField remove() {
        HeaderField removed = circularQueue.remove();
        if (removed != null) {
            capacityManager.updateSize(-removed.size());
        }
        return removed;
    }
    
    @Override
    public void clear() {
        circularQueue.clear();
        capacityManager.resetSize();
    }
    
    @Override
    public void setCapacity(int capacity) {
        if (capacity < 0) {
            throw new IOException("invalid max dynamic table size");
        }
        
        capacityManager.setCapacity(capacity);
        
        // Evictar entradas si es necesario al reducir capacidad
        while (size() > capacity) {
            remove();
        }
        
        circularQueue.resize(capacityManager.calculateMaxEntries());
    }
    
    /**
     * Cambia la política de evicción en tiempo de ejecución
     */
    public void setEvictionPolicy(EvictionPolicy evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }
    
    /**
     * Obtiene la política de evicción actual
     */
    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }
}