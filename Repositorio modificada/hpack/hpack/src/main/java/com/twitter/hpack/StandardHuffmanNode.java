package main.java.com.twitter.hpack;

/**
 * Implementación estándar de nodo Huffman para HPACK
 * Mantiene compatibilidad con el RFC 7541
 */
public class StandardHuffmanNode implements HuffmanNode {
    private final int symbol;
    private final int bits;
    private final HuffmanNode[] children;
    
    /**
     * Constructor para nodo interno
     */
    public StandardHuffmanNode() {
        this.symbol = 0;
        this.bits = 8;
        this.children = new HuffmanNode[256];
    }
    
    /**
     * Constructor para nodo terminal
     */
    public StandardHuffmanNode(int symbol, int bits) {
        if (bits <= 0 || bits > 8) {
            throw new IllegalArgumentException("Bits must be between 1 and 8");
        }
        this.symbol = symbol;
        this.bits = bits;
        this.children = null;
    }
    
    @Override
    public HuffmanNode processBit(int bit) {
        if (isTerminal()) {
            throw new IllegalStateException("Cannot process bit on terminal node");
        }
        if (bit < 0 || bit > 255) {
            throw new IllegalArgumentException("Bit must be between 0 and 255");
        }
        return children[bit];
    }
    
    @Override
    public boolean isTerminal() {
        return children == null;
    }
    
    @Override
    public int getSymbol() {
        if (!isTerminal()) {
            throw new IllegalStateException("Internal nodes do not have symbols");
        }
        return symbol;
    }
    
    @Override
    public int getDepth() {
        return bits;
    }
    
    /**
     * Establece un hijo para este nodo interno
     */
    public void setChild(int index, HuffmanNode node) {
        if (isTerminal()) {
            throw new IllegalStateException("Cannot set child on terminal node");
        }
        if (index < 0 || index >= children.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        children[index] = node;
    }
    
    /**
     * Obtiene un hijo (para testing)
     */
    public HuffmanNode getChild(int index) {
        if (isTerminal()) {
            throw new IllegalStateException("Terminal nodes do not have children");
        }
        return children[index];
    }
}