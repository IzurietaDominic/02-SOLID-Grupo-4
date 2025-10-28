package main.java.com.twitter.hpack;

import java.io.IOException;

public class NumericEncoder {
    public void encodeInteger(ByteSink out, int mask, int n, int i) throws IOException {
        if (n < 0 || n > 8) {
            throw new IllegalArgumentException("N: " + n);
        }
        int nbits = 0xFF >>> (8 - n);
        if (i < nbits) {
            out.write(mask | i);
        } else {
            out.write(mask | nbits);
            int length = i - nbits;
            while (true) {
                if ((length & ~0x7F) == 0) {
                    out.write(length);
                    return;
                } else {
                    out.write((length & 0x7F) | 0x80);
                    length >>>= 7;
                }
            }
        }
    }
    
    public void encodeInteger(OutputStream out, int mask, int n, int i) throws IOException {
        encodeInteger(new OutputStreamByteSink(out), mask, n, i);
    }
}