package main.java.com.twitter.hpack;

/**
 * Interface para políticas de evicción de la tabla dinámica
 */
public interface EvictionPolicy {
    /**
     * Determina si se debe realizar evicción basado en el estado actual
     */
    boolean shouldEvict(int currentSize, int newEntrySize, int capacity);
    
    /**
     * Calcula cuántas entradas deben evictarse
     */
    int calculateEntriesToEvict(int currentSize, int newEntrySize, int capacity);
    
    /**
     * Obtiene el nombre de la política
     */
    String getPolicyName();
}