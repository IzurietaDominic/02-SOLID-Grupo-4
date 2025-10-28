package main.java.com.twitter.hpack;

import java.io.IOException;
import java.io.OutputStream;

public class StandardHuffmanEncoder implements HuffmanEncoder {
    private final int[] codes;
    private final byte[] lengths;
    
    public StandardHuffmanEncoder(int[] codes, byte[] lengths) {
        this.codes = codes;
        this.lengths = lengths;
    }
    
    @Override
    public void encode(OutputStream out, byte[] data) throws IOException {
        encode(out, data, 0, data.length);
    }
    
    @Override
    public void encode(OutputStream out, byte[] data, int off, int len) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        } else if (data == null) {
            throw new NullPointerException("data");
        } else if (off < 0 || len < 0 || (off + len) < 0 || off > data.length || (off + len) > data.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        
        long current = 0;
        int n = 0;
        
        for (int i = 0; i < len; i++) {
            int b = data[off + i] & 0xFF;
            int code = codes[b];
            int nbits = lengths[b];
            
            current <<= nbits;
            current |= code;
            n += nbits;
            
            while (n >= 8) {
                n -= 8;
                out.write(((int)(current >> n)));
            }
        }
        
        if (n > 0) {
            current <<= (8 - n);
            current |= (0xFF >>> n);
            out.write((int)current);
        }
    }
    
    @Override
    public int getEncodedLength(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        long len = 0;
        for (byte b : data) {
            len += lengths[b & 0xFF];
        }
        return (int)((len + 7) >> 3);
    }
}