package main.java.com.twitter.hpack;

import java.io.IOException;
import java.util.Arrays;

public class DynamicTableManager {
    private static final int BUCKET_SIZE = 17;
    
    private final HeaderEntry[] headerFields;
    private final HeaderEntry head;
    private int size;
    private int capacity;
    
    private final StaticHeaderTable staticTable;
    
    public DynamicTableManager(int maxHeaderTableSize) {
        this.staticTable = new StaticTable();
        this.headerFields = new HeaderEntry[BUCKET_SIZE];
        // Crear nodo cabeza de la lista vinculada
        this.head = new HeaderEntry(-1, new byte[0], new byte[0], Integer.MAX_VALUE, null);
        this.capacity = maxHeaderTableSize;
        // Inicializar lista circular
        head.setBefore(head);
        head.setAfter(head);
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
        ensureCapacity(0);
    }
    
    public Integer findHeaderIndex(byte[] name, byte[] value) {
        if (size == 0) return null;
        
        int h = hash(name);
        int i = index(h);
        for (HeaderEntry e = headerFields[i]; e != null; e = e.getNext()) {
            if (e.getHash() == h && 
                HpackUtil.equals(name, e.getName()) && 
                HpackUtil.equals(value, e.getValue())) {
                return getIndex(e.getIndex()) + staticTable.length();
            }
        }
        return null;
    }
    
    public Integer findNameIndex(byte[] name) {
        int index = findDynamicNameIndex(name);
        return index != -1 ? index + staticTable.length() : null;
    }
    
    private int findDynamicNameIndex(byte[] name) {
        if (size == 0) return -1;
        
        int h = hash(name);
        int i = index(h);
        int foundIndex = -1;
        for (HeaderEntry e = headerFields[i]; e != null; e = e.getNext()) {
            if (e.getHash() == h && HpackUtil.equals(name, e.getName())) {
                foundIndex = e.getIndex();
                break;
            }
        }
        return getIndex(foundIndex);
    }
    
    public void addHeader(byte[] name, byte[] value) {
        int headerSize = HeaderField.sizeOf(name, value);
        if (headerSize > capacity) {
            clear();
            return;
        }
        
        ensureCapacity(headerSize);
        
        // Copiar arrays para evitar modificaciones externas
        name = Arrays.copyOf(name, name.length);
        value = Arrays.copyOf(value, value.length);
        
        int h = hash(name);
        int i = index(h);
        HeaderEntry old = headerFields[i];
        
        // Crear nueva entrada
        HeaderEntry newEntry = new HeaderEntry(h, name, value, head.getBefore().getIndex() - 1, old);
        headerFields[i] = newEntry;
        
        // Insertar en lista vinculada
        newEntry.addBefore(head);
        size += headerSize;
    }
    
    public void ensureCapacity(int headerSize) {
        while (size + headerSize > capacity) {
            if (size == 0) break;
            removeOldest();
        }
    }
    
    private void removeOldest() {
        if (size == 0) return;
        
        HeaderEntry eldest = head.getAfter();
        if (eldest == head) return; // Lista vacía
        
        int h = eldest.getHash();
        int i = index(h);
        HeaderEntry prev = headerFields[i];
        HeaderEntry current = prev;
        
        // Remover de la tabla hash
        while (current != null) {
            HeaderEntry next = current.getNext();
            if (current == eldest) {
                if (prev == eldest) {
                    headerFields[i] = next;
                } else {
                    prev.setNext(next);
                }
                // Remover de lista vinculada
                eldest.remove();
                size -= eldest.size();
                return;
            }
            prev = current;
            current = next;
        }
    }
    
    private void clear() {
        Arrays.fill(headerFields, null);
        head.setBefore(head);
        head.setAfter(head);
        this.size = 0;
    }
    
    private int getIndex(int index) {
        if (index == -1) return index;
        return index - head.getBefore().getIndex() + 1;
    }
    
    private static int hash(byte[] name) {
        int h = 0;
        for (int i = 0; i < name.length; i++) {
            h = 31 * h + name[i];
        }
        if (h > 0) return h;
        else if (h == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        else return -h;
    }
    
    private static int index(int h) {
        return h % BUCKET_SIZE;
    }
    
    // Métodos para testing
    int getCurrentSize() {
        return size;
    }
    
    int getEntryCount() {
        int count = 0;
        HeaderEntry current = head.getAfter();
        while (current != head) {
            count++;
            current = current.getAfter();
        }
        return count;
    }
}