package main.java.com.twitter.hpack;

import java.io.IOException;
import java.io.InputStream;

public final class Decoder {
    private final StateMachineDecoder stateMachine;
    
    public Decoder(int maxHeaderSize, int maxHeaderTableSize) {
        StaticHeaderTable staticTable = new StaticTable();
        DynamicHeaderTable dynamicTable = new DynamicTable(maxHeaderTableSize);
        HeaderTableManager tableManager = new HeaderTableManager(staticTable, dynamicTable);
        HuffmanStringProcessor stringProcessor = new HuffmanStringProcessor(new StandardHuffmanDecoder(HpackUtil.HUFFMAN_CODES, HpackUtil.HUFFMAN_CODE_LENGTHS));
        HeaderValidator validator = new HeaderValidator(maxHeaderSize);
        
        this.stateMachine = new StateMachineDecoder(tableManager, stringProcessor, validator);
    }
    
    public Decoder(StaticHeaderTable staticTable, 
                  DynamicHeaderTable dynamicTable,
                  HuffmanDecoder huffmanDecoder,
                  int maxHeaderSize) {
        HeaderTableManager tableManager = new HeaderTableManager(staticTable, dynamicTable);
        HuffmanStringProcessor stringProcessor = new HuffmanStringProcessor(huffmanDecoder);
        HeaderValidator validator = new HeaderValidator(maxHeaderSize);
        
        this.stateMachine = new StateMachineDecoder(tableManager, stringProcessor, validator);
    }
    
    public void decode(InputStream in, HeaderListener headerListener) throws IOException {
        ByteSource byteSource = new InputStreamByteSource(in);
        stateMachine.decode(byteSource, headerListener);
    }
    
    public void decode(ByteSource in, HeaderListener headerListener) throws IOException {
        stateMachine.decode(in, headerListener);
    }
    
    public boolean endHeaderBlock() {
        return stateMachine.endHeaderBlock();
    }
    
    public void setMaxHeaderTableSize(int maxHeaderTableSize) {
        // Esta funcionalidad se maneja a través de DynamicTable
    }
    
    public int getMaxHeaderTableSize() {
        return 0; // Se obtendría del DynamicTable a través del StateMachineDecoder
    }
}