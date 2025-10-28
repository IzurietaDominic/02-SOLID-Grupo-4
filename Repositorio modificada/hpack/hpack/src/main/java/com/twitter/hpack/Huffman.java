package main.java.com.twitter.hpack;

public final class Huffman {
    public static final HuffmanDecoder DECODER = 
        new StandardHuffmanDecoder(HUFFMAN_CODES, HUFFMAN_CODE_LENGTHS);
    
    public static final HuffmanEncoder ENCODER = 
        new StandardHuffmanEncoder(HUFFMAN_CODES, HUFFMAN_CODE_LENGTHS);
    
    private Huffman() {
        // utility class
    }
}