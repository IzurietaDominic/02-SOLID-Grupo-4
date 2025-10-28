package main.java.com.twitter.hpack;

import static com.twitter.hpack.HeaderField.HEADER_ENTRY_OVERHEAD;

final class CircularQueue {
    private HeaderField[] headerFields;
    private int head;
    private int tail;
    private int size;
    private int length;
    
    CircularQueue() {
        headerFields = new HeaderField[0];
        head = tail = size = length = 0;
    }
    
    int length() {
        return length;
    }
    
    int size() {
        return size;
    }
    
    HeaderField getEntry(int index) {
        if (index <= 0 || index > length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        int i = head - index;
        if (i < 0) {
            return headerFields[i + headerFields.length];
        } else {
            return headerFields[i];
        }
    }
    
    int findIndexByName(byte[] name) {
        if (length == 0 || name == null) {
            return -1;
        }
        
        for (int i = 1; i <= length; i++) {
            HeaderField entry = getEntry(i);
            if (HpackUtil.equals(name, entry.name)) {
                return i;
            }
        }
        return -1;
    }
    
    int findIndexByNameAndValue(byte[] name, byte[] value) {
        if (length == 0 || name == null || value == null) {
            return -1;
        }
        
        for (int i = 1; i <= length; i++) {
            HeaderField entry = getEntry(i);
            if (HpackUtil.equals(name, entry.name) && HpackUtil.equals(value, entry.value)) {
                return i;
            }
        }
        return -1;
    }
    
    void add(HeaderField header) {
        if (headerFields.length == 0) {
            // Inicializar con capacidad mínima
            resize(8);
        }
        
        // Verificar si necesitamos redimensionar
        if (length >= headerFields.length) {
            resize(headerFields.length * 2);
        }
        
        headerFields[head] = header;
        head = (head + 1) % headerFields.length;
        size += header.size();
        length++;
        
        // Ajustar tail si estamos llenos
        if (length > headerFields.length) {
            tail = (tail + 1) % headerFields.length;
            length = headerFields.length;
        }
    }
    
    HeaderField remove() {
        if (length == 0) {
            return null;
        }
        
        HeaderField removed = headerFields[tail];
        if (removed == null) {
            return null;
        }
        
        size -= removed.size();
        headerFields[tail] = null;
        tail = (tail + 1) % headerFields.length;
        length--;
        
        return removed;
    }
    
    void clear() {
        for (int i = 0; i < headerFields.length; i++) {
            headerFields[i] = null;
        }
        head = 0;
        tail = 0;
        size = 0;
        length = 0;
    }
    
    void resize(int newCapacity) {
        if (newCapacity <= 0) {
            newCapacity = 8; // Capacidad mínima
        }
        
        if (headerFields != null && headerFields.length == newCapacity) {
            return;
        }
        
        HeaderField[] tmp = new HeaderField[newCapacity];
        int len = Math.min(length, newCapacity);
        
        // Copiar elementos preservando el orden
        for (int i = 0; i < len; i++) {
            int sourceIndex = (tail + i) % headerFields.length;
            tmp[i] = headerFields[sourceIndex];
        }
        
        this.headerFields = tmp;
        this.tail = 0;
        this.head = len % newCapacity;
        this.length = len;
        
        // Recalcular tamaño
        this.size = 0;
        for (int i = 0; i < len; i++) {
            if (tmp[i] != null) {
                this.size += tmp[i].size();
            }
        }
    }
    
    /**
     * Obtiene todas las entradas en orden (más reciente primero)
     */
    HeaderField[] getEntries() {
        HeaderField[] entries = new HeaderField[length];
        for (int i = 0; i < length; i++) {
            entries[i] = getEntry(i + 1);
        }
        return entries;
    }
    
    /**
     * Verifica si la cola está vacía
     */
    boolean isEmpty() {
        return length == 0;
    }
    
    /**
     * Verifica si la cola está llena
     */
    boolean isFull() {
        return length >= headerFields.length;
    }
}