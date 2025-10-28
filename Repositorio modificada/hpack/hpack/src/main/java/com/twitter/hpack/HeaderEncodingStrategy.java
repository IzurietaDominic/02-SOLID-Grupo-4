package main.java.com.twitter.hpack;

import java.io.IOException;

/**
 * Estrategia de encoding de headers HPACK
 * Implementa las diferentes representaciones del RFC 7541
 */
public class HeaderEncodingStrategy {
    private final DynamicTableManager tableManager;
    private final NumericEncoder numericEncoder;
    private final StringEncoder stringEncoder;
    private final boolean useIndexing;
    private final StaticHeaderTable staticTable;
    
    public HeaderEncodingStrategy(DynamicTableManager tableManager,
                                 NumericEncoder numericEncoder,
                                 StringEncoder stringEncoder,
                                 boolean useIndexing) {
        this.tableManager = tableManager;
        this.numericEncoder = numericEncoder;
        this.stringEncoder = stringEncoder;
        this.useIndexing = useIndexing;
        this.staticTable = new StaticTable(); // Podría inyectarse para mayor flexibilidad
    }
    
    public HeaderEncodingStrategy(DynamicTableManager tableManager,
                                 NumericEncoder numericEncoder,
                                 StringEncoder stringEncoder,
                                 boolean useIndexing,
                                 StaticHeaderTable staticTable) {
        this.tableManager = tableManager;
        this.numericEncoder = numericEncoder;
        this.stringEncoder = stringEncoder;
        this.useIndexing = useIndexing;
        this.staticTable = staticTable;
    }
    
    /**
     * Codifica un header según las reglas HPACK
     */
    public void encodeHeader(ByteSink out, byte[] name, byte[] value, boolean sensitive) throws IOException {
        if (sensitive) {
            encodeSensitiveHeader(out, name, value);
            return;
        }
        
        if (tableManager.getCapacity() == 0) {
            encodeHeaderWithStaticTableOnly(out, name, value);
            return;
        }
        
        int headerSize = HeaderField.sizeOf(name, value);
        if (headerSize > tableManager.getCapacity()) {
            encodeLiteralHeader(out, name, value, HpackUtil.IndexType.NONE);
            return;
        }
        
        // Buscar en tabla dinámica
        Integer index = tableManager.findHeaderIndex(name, value);
        if (index != null) {
            encodeIndexedHeader(out, index);
            return;
        }
        
        // Buscar en tabla estática
        Integer staticIndex = staticTable.getIndex(name, value);
        if (staticIndex != -1) {
            encodeIndexedHeader(out, staticIndex);
            return;
        }
        
        // Encoding literal
        encodeLiteralHeaderWithIndexing(out, name, value);
    }
    
    /**
     * Codifica header sensible (never indexed)
     */
    private void encodeSensitiveHeader(ByteSink out, byte[] name, byte[] value) throws IOException {
        Integer nameIndex = findBestNameIndex(name);
        encodeLiteralHeader(out, name, value, HpackUtil.IndexType.NEVER, nameIndex);
    }
    
    /**
     * Codifica header cuando solo se usa tabla estática
     */
    private void encodeHeaderWithStaticTableOnly(ByteSink out, byte[] name, byte[] value) throws IOException {
        int staticIndex = staticTable.getIndex(name, value);
        if (staticIndex != -1) {
            encodeIndexedHeader(out, staticIndex);
        } else {
            Integer nameIndex = staticTable.getIndex(name);
            encodeLiteralHeader(out, name, value, HpackUtil.IndexType.NONE, 
                              nameIndex != -1 ? nameIndex : null);
        }
    }
    
    /**
     * Codifica header literal con indexing opcional
     */
    private void encodeLiteralHeaderWithIndexing(ByteSink out, byte[] name, byte[] value) throws IOException {
        Integer nameIndex = findBestNameIndex(name);
        
        if (useIndexing) {
            tableManager.ensureCapacity(HeaderField.sizeOf(name, value));
        }
        
        HpackUtil.IndexType indexType = useIndexing ? 
            HpackUtil.IndexType.INCREMENTAL : HpackUtil.IndexType.NONE;
            
        encodeLiteralHeader(out, name, value, indexType, nameIndex);
        
        if (useIndexing) {
            tableManager.addHeader(name, value);
        }
    }
    
    /**
     * Codifica header indexado
     */
    private void encodeIndexedHeader(ByteSink out, int index) throws IOException {
        numericEncoder.encodeInteger(out, 0x80, 7, index);
    }
    
    /**
     * Codifica header literal
     */
    private void encodeLiteralHeader(ByteSink out, byte[] name, byte[] value, 
                                   HpackUtil.IndexType indexType) throws IOException {
        encodeLiteralHeader(out, name, value, indexType, null);
    }
    
    /**
     * Codifica header literal con índice de nombre opcional
     */
    private void encodeLiteralHeader(ByteSink out, byte[] name, byte[] value,
                                   HpackUtil.IndexType indexType, Integer nameIndex) throws IOException {
        int mask = getMaskForIndexType(indexType);
        int prefixBits = getPrefixBitsForIndexType(indexType);
        
        int actualNameIndex = nameIndex != null ? nameIndex : 0;
        numericEncoder.encodeInteger(out, mask, prefixBits, actualNameIndex);
        
        if (nameIndex == null) {
            stringEncoder.encodeString(out, name);
        }
        
        stringEncoder.encodeString(out, value);
    }
    
    /**
     * Encuentra el mejor índice para el nombre
     */
    private Integer findBestNameIndex(byte[] name) {
        // Primero buscar en tabla estática
        int staticIndex = staticTable.getIndex(name);
        if (staticIndex != -1) {
            return staticIndex;
        }
        
        // Luego buscar en tabla dinámica
        Integer dynamicIndex = tableManager.findNameIndex(name);
        if (dynamicIndex != null) {
            return dynamicIndex + staticTable.length();
        }
        
        return null;
    }
    
    /**
     * Obtiene máscara según el tipo de índice
     */
    private int getMaskForIndexType(HpackUtil.IndexType indexType) {
        switch (indexType) {
            case INCREMENTAL: return 0x40;
            case NONE: return 0x00;
            case NEVER: return 0x10;
            default: throw new IllegalStateException("Unknown index type: " + indexType);
        }
    }
    
    /**
     * Obtiene bits de prefijo según el tipo de índice
     */
    private int getPrefixBitsForIndexType(HpackUtil.IndexType indexType) {
        switch (indexType) {
            case INCREMENTAL: return 6;
            case NONE: return 4;
            case NEVER: return 4;
            default: throw new IllegalStateException("Unknown index type: " + indexType);
        }
    }
    
    /**
     * Codifica header usando OutputStream (para compatibilidad)
     */
    public void encodeHeader(OutputStream out, byte[] name, byte[] value, boolean sensitive) throws IOException {
        encodeHeader(new OutputStreamByteSink(out), name, value, sensitive);
    }
    
    // Métodos de acceso para testing
    public DynamicTableManager getTableManager() {
        return tableManager;
    }
    
    public NumericEncoder getNumericEncoder() {
        return numericEncoder;
    }
    
    public StringEncoder getStringEncoder() {
        return stringEncoder;
    }
    
    public boolean isUseIndexing() {
        return useIndexing;
    }
}