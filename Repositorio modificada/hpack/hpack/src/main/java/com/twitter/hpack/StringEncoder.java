package main.java.com.twitter.hpack;

import java.io.IOException;

public class StringEncoder {
    private final boolean forceHuffmanOn;
    private final boolean forceHuffmanOff;
    private final HuffmanEncoder huffmanEncoder;
    
    public StringEncoder(boolean forceHuffmanOn, boolean forceHuffmanOff) {
        this.forceHuffmanOn = forceHuffmanOn;
        this.forceHuffmanOff = forceHuffmanOff;
        this.huffmanEncoder = new StandardHuffmanEncoder(HpackUtil.HUFFMAN_CODES, HpackUtil.HUFFMAN_CODE_LENGTHS);
    }
    
    public void encodeString(ByteSink out, byte[] string) throws IOException {
        int huffmanLength = huffmanEncoder.getEncodedLength(string);
        if ((huffmanLength < string.length && !forceHuffmanOff) || forceHuffmanOn) {
            NumericEncoder numericEncoder = new NumericEncoder();
            numericEncoder.encodeInteger(out, 0x80, 7, huffmanLength);
            huffmanEncoder.encode(new ByteSinkOutputStream(out), string);
        } else {
            NumericEncoder numericEncoder = new NumericEncoder();
            numericEncoder.encodeInteger(out, 0x00, 7, string.length);
            out.write(string);
        }
    }
    
    public void encodeString(OutputStream out, byte[] string) throws IOException {
        encodeString(new OutputStreamByteSink(out), string);
    }
    
    private static class ByteSinkOutputStream extends OutputStream {
        private final ByteSink byteSink;
        
        ByteSinkOutputStream(ByteSink byteSink) {
            this.byteSink = byteSink;
        }
        
        @Override
        public void write(int b) throws IOException {
            byteSink.write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            byteSink.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byte[] slice = new byte[len];
            System.arraycopy(b, off, slice, 0, len);
            byteSink.write(slice);
        }
    }
}