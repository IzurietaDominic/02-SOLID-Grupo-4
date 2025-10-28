package main.java.com.twitter.hpack;

/**
 * Interfaz para nodos del árbol Huffman - permite diferentes implementaciones
 */
public interface HuffmanNode {
    /**
     * Procesa el siguiente bit y devuelve el siguiente nodo
     */
    HuffmanNode processBit(int bit);
    
    /**
     * Verifica si es un nodo terminal
     */
    boolean isTerminal();
    
    /**
     * Obtiene el símbolo (solo para nodos terminales)
     */
    int getSymbol();
    
    /**
     * Obtiene la profundidad del nodo en bits
     */
    int getDepth();
}
