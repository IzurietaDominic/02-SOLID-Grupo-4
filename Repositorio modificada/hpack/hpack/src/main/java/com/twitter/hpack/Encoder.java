package main.java.com.twitter.hpack;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class Encoder {
    private final HeaderEncodingStrategy encodingStrategy;
    private final DynamicTableManager tableManager;
    private final NumericEncoder numericEncoder;
    private final StringEncoder stringEncoder;
    
    private final boolean useIndexing;
    private final boolean forceHuffmanOn;
    private final boolean forceHuffmanOff;
    
    public Encoder(int maxHeaderTableSize) {
        this(maxHeaderTableSize, true, false, false);
    }
    
    public Encoder(int maxHeaderTableSize, boolean useIndexing, boolean forceHuffmanOn, boolean forceHuffmanOff) {
        this.useIndexing = useIndexing;
        this.forceHuffmanOn = forceHuffmanOn;
        this.forceHuffmanOff = forceHuffmanOff;
        
        this.tableManager = new DynamicTableManager(maxHeaderTableSize);
        this.numericEncoder = new NumericEncoder();
        this.stringEncoder = new StringEncoder(forceHuffmanOn, forceHuffmanOff);
        this.encodingStrategy = new HeaderEncodingStrategy(tableManager, numericEncoder, stringEncoder, useIndexing);
    }
    
    public void encodeHeader(OutputStream out, byte[] name, byte[] value, boolean sensitive) throws IOException {
        ByteSink byteSink = new OutputStreamByteSink(out);
        encodingStrategy.encodeHeader(byteSink, name, value, sensitive);
    }
    
    public void encodeHeader(ByteSink out, byte[] name, byte[] value, boolean sensitive) throws IOException {
        encodingStrategy.encodeHeader(out, name, value, sensitive);
    }
    
    public void setMaxHeaderTableSize(OutputStream out, int maxHeaderTableSize) throws IOException {
        tableManager.setCapacity(maxHeaderTableSize);
        numericEncoder.encodeInteger(out, 0x20, 5, maxHeaderTableSize);
    }
    
    public int getMaxHeaderTableSize() {
        return tableManager.getCapacity();
    }
}