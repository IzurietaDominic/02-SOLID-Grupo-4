package main.java.com.twitter.hpack;

import java.io.IOException;

public class ULEB128Decoder {
    private static final IOException DECOMPRESSION_EXCEPTION = 
        new IOException("decompression failure");
    
    public int decode(ByteSource in) throws IOException {
        in.mark(5);
        int result = 0;
        int shift = 0;
        while (shift < 32) {
            if (in.available() == 0) {
                in.reset();
                return -1;
            }
            byte b = (byte) in.read();
            if (shift == 28 && (b & 0xF8) != 0) {
                break;
            }
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        in.reset();
        throw DECOMPRESSION_EXCEPTION;
    }
}