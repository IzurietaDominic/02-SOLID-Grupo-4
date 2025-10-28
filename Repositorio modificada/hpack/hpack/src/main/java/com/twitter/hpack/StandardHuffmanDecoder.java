package main.java.com.twitter.hpack;

/*
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StandardHuffmanDecoder implements HuffmanDecoder {
    private static final IOException EOS_DECODED = new IOException("EOS Decoded");
    private static final IOException INVALID_PADDING = new IOException("Invalid Padding");
    
    private final Node root;
    
    public StandardHuffmanDecoder(int[] codes, byte[] lengths) {
        if (codes.length != 257 || codes.length != lengths.length) {
            throw new IllegalArgumentException("invalid Huffman coding");
        }
        root = buildTree(codes, lengths);
    }
    
    @Override
    public byte[] decode(byte[] buf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Node node = root;
        int current = 0;
        int bits = 0;
        for (int i = 0; i < buf.length; i++) {
            int b = buf[i] & 0xFF;
            current = (current << 8) | b;
            bits += 8;
            while (bits >= 8) {
                int c = (current >>> (bits - 8)) & 0xFF;
                node = node.children[c];
                bits -= node.bits;
                if (node.isTerminal()) {
                    if (node.symbol == HpackUtil.HUFFMAN_EOS) {
                        throw EOS_DECODED;
                    }
                    baos.write(node.symbol);
                    node = root;
                }
            }
        }
        
        while (bits > 0) {
            int c = (current << (8 - bits)) & 0xFF;
            node = node.children[c];
            if (node.isTerminal() && node.bits <= bits) {
                bits -= node.bits;
                baos.write(node.symbol);
                node = root;
            } else {
                break;
            }
        }
        
        int mask = (1 << bits) - 1;
        if ((current & mask) != mask) {
            throw INVALID_PADDING;
        }
        
        return baos.toByteArray();
    }
    
    private static final class Node {
        private final int symbol;
        private final int bits;
        private final Node[] children;
        
        private Node() {
            symbol = 0;
            bits = 8;
            children = new Node[256];
        }
        
        private Node(int symbol, int bits) {
            assert(bits > 0 && bits <= 8);
            this.symbol = symbol;
            this.bits = bits;
            children = null;
        }
        
        private boolean isTerminal() {
            return children == null;
        }
    }
    
    private static Node buildTree(int[] codes, byte[] lengths) {
        Node root = new Node();
        for (int i = 0; i < codes.length; i++) {
            insert(root, i, codes[i], lengths[i]);
        }
        return root;
    }
    
    private static void insert(Node root, int symbol, int code, byte length) {
        Node current = root;
        while (length > 8) {
            if (current.isTerminal()) {
                throw new IllegalStateException("invalid Huffman code: prefix not unique");
            }
            length -= 8;
            int i = (code >>> length) & 0xFF;
            if (current.children[i] == null) {
                current.children[i] = new Node();
            }
            current = current.children[i];
        }
        
        Node terminal = new Node(symbol, length);
        int shift = 8 - length;
        int start = (code << shift) & 0xFF;
        int end = 1 << shift;
        for (int i = start; i < start + end; i++) {
            current.children[i] = terminal;
        }
    }
} 
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StandardHuffmanDecoder implements HuffmanDecoder {
    private static final IOException EOS_DECODED = new IOException("EOS Decoded");
    private static final IOException INVALID_PADDING = new IOException("Invalid Padding");
    
    private final HuffmanNode root;
    
    public StandardHuffmanDecoder(int[] codes, byte[] lengths) {
        if (codes.length != 257 || codes.length != lengths.length) {
            throw new IllegalArgumentException("invalid Huffman coding");
        }
        this.root = buildTree(codes, lengths);
    }
    
    @Override
    public byte[] decode(byte[] buf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        HuffmanNode currentNode = root;
        int current = 0;
        int bits = 0;
        
        for (int i = 0; i < buf.length; i++) {
            int b = buf[i] & 0xFF;
            current = (current << 8) | b;
            bits += 8;
            
            while (bits >= 8) {
                int c = (current >>> (bits - 8)) & 0xFF;
                currentNode = ((StandardHuffmanNode) currentNode).getChild(c);
                bits -= currentNode.getDepth();
                
                if (currentNode.isTerminal()) {
                    if (currentNode.getSymbol() == HpackUtil.HUFFMAN_EOS) {
                        throw EOS_DECODED;
                    }
                    baos.write(currentNode.getSymbol());
                    currentNode = root;
                }
            }
        }
        
        // Procesar bits restantes
        while (bits > 0) {
            int c = (current << (8 - bits)) & 0xFF;
            HuffmanNode nextNode = ((StandardHuffmanNode) currentNode).getChild(c);
            
            if (nextNode.isTerminal() && nextNode.getDepth() <= bits) {
                bits -= nextNode.getDepth();
                baos.write(nextNode.getSymbol());
                currentNode = root;
            } else {
                break;
            }
        }
        
        // Validar padding
        int mask = (1 << bits) - 1;
        if ((current & mask) != mask) {
            throw INVALID_PADDING;
        }
        
        return baos.toByteArray();
    }
    
    private HuffmanNode buildTree(int[] codes, byte[] lengths) {
        StandardHuffmanNode root = new StandardHuffmanNode();
        
        for (int i = 0; i < codes.length; i++) {
            insert(root, i, codes[i], lengths[i]);
        }
        
        return root;
    }
    
    private void insert(StandardHuffmanNode root, int symbol, int code, byte length) {
        StandardHuffmanNode current = root;
        
        // Navegar por los bits más significativos
        while (length > 8) {
            length -= 8;
            int index = (code >>> length) & 0xFF;
            
            if (current.getChild(index) == null) {
                current.setChild(index, new StandardHuffmanNode());
            }
            
            HuffmanNode next = current.getChild(index);
            if (next instanceof StandardHuffmanNode) {
                current = (StandardHuffmanNode) next;
            } else {
                throw new IllegalStateException("invalid Huffman code: prefix not unique");
            }
        }
        
        // Crear nodo terminal para los bits restantes
        StandardHuffmanNode terminal = new StandardHuffmanNode(symbol, length);
        int shift = 8 - length;
        int start = (code << shift) & 0xFF;
        int end = 1 << shift;
        
        for (int i = start; i < start + end; i++) {
            current.setChild(i, terminal);
        }
    }
    
    /**
     * Obtiene la raíz del árbol (para testing)
     */
    public HuffmanNode getRoot() {
        return root;
    }
}






