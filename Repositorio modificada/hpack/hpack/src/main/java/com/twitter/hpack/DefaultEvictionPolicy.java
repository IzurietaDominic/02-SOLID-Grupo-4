package main.java.com.twitter.hpack;

/**
 * Política de evicción por defecto: FIFO (First-In-First-Out)
 * Implementa la política estándar del RFC 7541
 */
public class DefaultEvictionPolicy implements EvictionPolicy {
    
    @Override
    public boolean shouldEvict(int currentSize, int newEntrySize, int capacity) {
        // Se requiere evicción si la nueva entrada excede la capacidad disponible
        return currentSize + newEntrySize > capacity;
    }
    
    @Override
    public int calculateEntriesToEvict(int currentSize, int newEntrySize, int capacity) {
        if (!shouldEvict(currentSize, newEntrySize, capacity)) {
            return 0;
        }
        
        // En FIFO, se evicta hasta que haya espacio suficiente
        // En la práctica, se evicta una entrada a la vez
        return 1;
    }
    
    @Override
    public String getPolicyName() {
        return "FIFO";
    }
}