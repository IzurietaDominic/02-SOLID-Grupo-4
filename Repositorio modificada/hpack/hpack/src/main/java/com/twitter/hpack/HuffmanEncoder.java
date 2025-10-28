package main.java.com.twitter.hpack;

import java.io.IOException;
import java.io.OutputStream;

public interface HuffmanEncoder {
    void encode(OutputStream out, byte[] data) throws IOException;
    void encode(OutputStream out, byte[] data, int off, int len) throws IOException;
    int getEncodedLength(byte[] data);
}