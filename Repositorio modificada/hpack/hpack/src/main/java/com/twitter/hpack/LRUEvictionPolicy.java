package main.java.com.twitter.hpack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Política de evicción LRU (Least Recently Used) - Ejemplo de extensión
 * No es parte del RFC 7541 pero demuestra la extensibilidad
 */
public class LRUEvictionPolicy implements EvictionPolicy {
    private final LinkedHashMap<Integer, Long> accessOrder;
    private long accessCounter;
    
    public LRUEvictionPolicy() {
        this.accessOrder = new LinkedHashMap<>(16, 0.75f, true);
        this.accessCounter = 0;
    }
    
    public void recordAccess(int entryIndex) {
        accessOrder.put(entryIndex, ++accessCounter);
    }
    
    @Override
    public boolean shouldEvict(int currentSize, int newEntrySize, int capacity) {
        return currentSize + newEntrySize > capacity;
    }
    
    @Override
    public int calculateEntriesToEvict(int currentSize, int newEntrySize, int capacity) {
        if (!shouldEvict(currentSize, newEntrySize, capacity)) {
            return 0;
        }
        
        // En LRU, podríamos evictar múltiples entradas de una vez
        // basado en el patrón de acceso
        int spaceNeeded = currentSize + newEntrySize - capacity;
        if (spaceNeeded > 0) {
            // Calcular cuántas entradas aproximadamente necesitamos evictar
            // Esto es una simplificación - en implementación real se calcularía basado en tamaños reales
            return Math.max(1, (spaceNeeded / 1024) + 1); // Asumiendo tamaño promedio de 1KB
        }
        return 1;
    }
    
    @Override
    public String getPolicyName() {
        return "LRU";
    }
    
    /**
     * Obtiene la entrada menos recientemente usada
     */
    public Integer getLRUEntry() {
        return accessOrder.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}