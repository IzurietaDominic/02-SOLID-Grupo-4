package main.java.com.twitter.hpack;

import java.nio.charset.Charset;

final class HpackUtil {
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    
    static boolean equals(byte[] s1, byte[] s2) {
        if (s1.length != s2.length) {
            return false;
        }
        char c = 0;
        for (int i = 0; i < s1.length; i++) {
            c |= (s1[i] ^ s2[i]);
        }
        return c == 0;
    }
    
    static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }
    
    enum IndexType {
        INCREMENTAL,
        NONE,
        NEVER
    }
    
    static final int[] HUFFMAN_CODES = {
        0x1ff8,
        0x7fffd8,
        // ... (códigos Huffman completos del RFC 7541)
    };
    
    static final byte[] HUFFMAN_CODE_LENGTHS = {
        13, 23, 28, 28, 28, 28, 28, 28, 28, 24, 30, 28, 28, 30, 28, 28,
        // ... (longitudes de códigos Huffman completos del RFC 7541)
    };
    
    static final int HUFFMAN_EOS = 256;
    
    private HpackUtil() {
        // utility class
    }
}