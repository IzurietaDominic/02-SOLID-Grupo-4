package main.java.com.twitter.hpack;

import java.io.IOException;

public class HuffmanStringProcessor {
    private final HuffmanDecoder huffmanDecoder;
    
    public HuffmanStringProcessor(HuffmanDecoder huffmanDecoder) {
        this.huffmanDecoder = huffmanDecoder;
    }
    
    public byte[] readStringLiteral(ByteSource in, int length, boolean huffmanEncoded) throws IOException {
        byte[] buf = new byte[length];
        int bytesRead = in.read(buf);
        if (bytesRead != length) {
            throw new IOException("decompression failure");
        }
        
        if (huffmanEncoded) {
            return huffmanDecoder.decode(buf);
        } else {
            return buf;
        }
    }
}