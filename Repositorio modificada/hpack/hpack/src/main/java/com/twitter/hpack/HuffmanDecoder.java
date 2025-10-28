package main.java.com.twitter.hpack;

import java.io.IOException;

public interface HuffmanDecoder {
    byte[] decode(byte[] data) throws IOException;
}