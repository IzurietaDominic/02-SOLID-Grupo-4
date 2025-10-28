package main.java.com.twitter.hpack;

import java.io.IOException;

public class StateMachineDecoder {
    private final HeaderTableManager tableManager;
    private final HuffmanStringProcessor stringProcessor;
    private final HeaderValidator validator;
    private final ULEB128Decoder uleb128Decoder;
    
    private State state;
    private HpackUtil.IndexType indexType;
    private int index;
    private boolean huffmanEncoded;
    private int skipLength;
    private int nameLength;
    private int valueLength;
    private byte[] name;
    
    private enum State {
        READ_HEADER_REPRESENTATION,
        READ_MAX_DYNAMIC_TABLE_SIZE,
        READ_INDEXED_HEADER,
        READ_INDEXED_HEADER_NAME,
        READ_LITERAL_HEADER_NAME_LENGTH_PREFIX,
        READ_LITERAL_HEADER_NAME_LENGTH,
        READ_LITERAL_HEADER_NAME,
        SKIP_LITERAL_HEADER_NAME,
        READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX,
        READ_LITERAL_HEADER_VALUE_LENGTH,
        READ_LITERAL_HEADER_VALUE,
        SKIP_LITERAL_HEADER_VALUE
    }
    
    public StateMachineDecoder(HeaderTableManager tableManager, 
                             HuffmanStringProcessor stringProcessor,
                             HeaderValidator validator) {
        this.tableManager = tableManager;
        this.stringProcessor = stringProcessor;
        this.validator = validator;
        this.uleb128Decoder = new ULEB128Decoder();
        reset();
    }
    
    public void reset() {
        validator.reset();
        state = State.READ_HEADER_REPRESENTATION;
        indexType = HpackUtil.IndexType.NONE;
        name = new byte[0];
    }
    
    public void decode(ByteSource in, HeaderListener headerListener) throws IOException {
        while (in.available() > 0) {
            switch(state) {
                case READ_HEADER_REPRESENTATION:
                    readHeaderRepresentation(in, headerListener);
                    break;
                case READ_MAX_DYNAMIC_TABLE_SIZE:
                    readMaxDynamicTableSize(in);
                    break;
                case READ_INDEXED_HEADER:
                    readIndexedHeader(in, headerListener);
                    break;
                case READ_INDEXED_HEADER_NAME:
                    readIndexedHeaderName(in);
                    break;
                case READ_LITERAL_HEADER_NAME_LENGTH_PREFIX:
                    readLiteralHeaderNameLengthPrefix(in);
                    break;
                case READ_LITERAL_HEADER_NAME_LENGTH:
                    readLiteralHeaderNameLength(in);
                    break;
                case READ_LITERAL_HEADER_NAME:
                    readLiteralHeaderName(in);
                    break;
                case SKIP_LITERAL_HEADER_NAME:
                    skipLiteralHeaderName(in);
                    break;
                case READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX:
                    readLiteralHeaderValueLengthPrefix(in, headerListener);
                    break;
                case READ_LITERAL_HEADER_VALUE_LENGTH:
                    readLiteralHeaderValueLength(in);
                    break;
                case READ_LITERAL_HEADER_VALUE:
                    readLiteralHeaderValue(in, headerListener);
                    break;
                case SKIP_LITERAL_HEADER_VALUE:
                    skipLiteralHeaderValue(in);
                    break;
                default:
                    throw new IllegalStateException("should not reach here");
            }
        }
    }
    
    private void readHeaderRepresentation(ByteSource in, HeaderListener headerListener) throws IOException {
        byte b = (byte) in.read();
        
        if (b < 0) {
            // Indexed Header Field
            index = b & 0x7F;
            if (index == 0) {
                throw new IOException("illegal index value");
            } else if (index == 0x7F) {
                state = State.READ_INDEXED_HEADER;
            } else {
                tableManager.indexHeader(index, headerListener);
                state = State.READ_HEADER_REPRESENTATION;
            }
        } else if ((b & 0x40) == 0x40) {
            // Literal Header Field with Incremental Indexing
            indexType = HpackUtil.IndexType.INCREMENTAL;
            index = b & 0x3F;
            processLiteralHeader(index);
        } else if ((b & 0x20) == 0x20) {
            // Dynamic Table Size Update
            index = b & 0x1F;
            if (index == 0x1F) {
                state = State.READ_MAX_DYNAMIC_TABLE_SIZE;
            } else {
                setDynamicTableSize(index);
                state = State.READ_HEADER_REPRESENTATION;
            }
        } else {
            // Literal Header Field without Indexing / never Indexed
            indexType = ((b & 0x10) == 0x10) ? HpackUtil.IndexType.NEVER : HpackUtil.IndexType.NONE;
            index = b & 0x0F;
            processLiteralHeader(index);
        }
    }
    
    private void processLiteralHeader(int index) throws IOException {
        if (index == 0) {
            state = State.READ_LITERAL_HEADER_NAME_LENGTH_PREFIX;
        } else if (index == 0x0F || index == 0x3F) {
            state = State.READ_INDEXED_HEADER_NAME;
        } else {
            readName(index);
            state = State.READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX;
        }
    }
    
    private void readName(int index) throws IOException {
        byte[] nameBuffer = new byte[1024]; // Tamaño suficiente para nombres de header
        tableManager.readName(index, nameBuffer);
        // En una implementación real, necesitaríamos manejar el tamaño dinámicamente
        this.name = nameBuffer;
    }
    
    private void readMaxDynamicTableSize(ByteSource in) throws IOException {
        int maxSize = uleb128Decoder.decode(in);
        if (maxSize == -1) return;
        
        if (maxSize > Integer.MAX_VALUE - index) {
            throw new IOException("decompression failure");
        }
        
        setDynamicTableSize(index + maxSize);
        state = State.READ_HEADER_REPRESENTATION;
    }
    
    private void setDynamicTableSize(int size) throws IOException {
        tableManager.getDynamicTable().setCapacity(size);
    }
    
    private void readIndexedHeader(ByteSource in, HeaderListener headerListener) throws IOException {
        int headerIndex = uleb128Decoder.decode(in);
        if (headerIndex == -1) return;
        
        if (headerIndex > Integer.MAX_VALUE - index) {
            throw new IOException("decompression failure");
        }
        
        tableManager.indexHeader(index + headerIndex, headerListener);
        state = State.READ_HEADER_REPRESENTATION;
    }
    
    private void readIndexedHeaderName(ByteSource in) throws IOException {
        int nameIndex = uleb128Decoder.decode(in);
        if (nameIndex == -1) return;
        
        if (nameIndex > Integer.MAX_VALUE - index) {
            throw new IOException("decompression failure");
        }
        
        readName(index + nameIndex);
        state = State.READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX;
    }
    
    private void readLiteralHeaderNameLengthPrefix(ByteSource in) throws IOException {
        byte b = (byte) in.read();
        huffmanEncoded = (b & 0x80) == 0x80;
        index = b & 0x7F;
        if (index == 0x7f) {
            state = State.READ_LITERAL_HEADER_NAME_LENGTH;
        } else {
            nameLength = index;
            processNameLength();
        }
    }
    
    private void readLiteralHeaderNameLength(ByteSource in) throws IOException {
        nameLength = uleb128Decoder.decode(in);
        if (nameLength == -1) return;
        
        if (nameLength > Integer.MAX_VALUE - index) {
            throw new IOException("decompression failure");
        }
        nameLength += index;
        processNameLength();
    }
    
    private void processNameLength() throws IOException {
        if (nameLength == 0) {
            throw new IOException("decompression failure");
        }
        
        if (validator.exceedsMaxHeaderSize(nameLength)) {
            if (indexType == HpackUtil.IndexType.NONE) {
                skipLength = nameLength;
                state = State.SKIP_LITERAL_HEADER_NAME;
            } else {
                tableManager.getDynamicTable().clear();
                skipLength = nameLength;
                state = State.SKIP_LITERAL_HEADER_NAME;
            }
        } else {
            state = State.READ_LITERAL_HEADER_NAME;
        }
    }
    
    private void readLiteralHeaderName(ByteSource in) throws IOException {
        if (in.available() < nameLength) return;
        
        name = stringProcessor.readStringLiteral(in, nameLength, huffmanEncoded);
        state = State.READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX;
    }
    
    private void skipLiteralHeaderName(ByteSource in) throws IOException {
        skipLength -= in.skip(skipLength);
        if (skipLength == 0) {
            state = State.READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX;
        }
    }
    
    private void readLiteralHeaderValueLengthPrefix(ByteSource in, HeaderListener headerListener) throws IOException {
        byte b = (byte) in.read();
        huffmanEncoded = (b & 0x80) == 0x80;
        index = b & 0x7F;
        if (index == 0x7f) {
            state = State.READ_LITERAL_HEADER_VALUE_LENGTH;
        } else {
            valueLength = index;
            processValueLength(headerListener);
        }
    }
    
    private void readLiteralHeaderValueLength(ByteSource in) throws IOException {
        valueLength = uleb128Decoder.decode(in);
        if (valueLength == -1) return;
        
        if (valueLength > Integer.MAX_VALUE - index) {
            throw new IOException("decompression failure");
        }
        valueLength += index;
        processValueLength(null);
    }
    
    private void processValueLength(HeaderListener headerListener) throws IOException {
        long newHeaderSize = (long) name.length + (long) valueLength;
        if (validator.exceedsMaxHeaderSize(newHeaderSize)) {
            if (indexType == HpackUtil.IndexType.NONE) {
                state = State.SKIP_LITERAL_HEADER_VALUE;
            } else {
                tableManager.getDynamicTable().clear();
                state = State.SKIP_LITERAL_HEADER_VALUE;
            }
        } else if (valueLength == 0 && headerListener != null) {
            insertHeader(headerListener, name, new byte[0]);
            state = State.READ_HEADER_REPRESENTATION;
        } else {
            state = State.READ_LITERAL_HEADER_VALUE;
        }
    }
    
    private void readLiteralHeaderValue(ByteSource in, HeaderListener headerListener) throws IOException {
        if (in.available() < valueLength) return;
        
        byte[] value = stringProcessor.readStringLiteral(in, valueLength, huffmanEncoded);
        insertHeader(headerListener, name, value);
        state = State.READ_HEADER_REPRESENTATION;
    }
    
    private void skipLiteralHeaderValue(ByteSource in) throws IOException {
        valueLength -= in.skip(valueLength);
        if (valueLength == 0) {
            state = State.READ_HEADER_REPRESENTATION;
        }
    }
    
    private void insertHeader(HeaderListener headerListener, byte[] name, byte[] value) {
        validator.addHeaderSize(name.length, value.length);
        headerListener.addHeader(name, value, indexType == HpackUtil.IndexType.NEVER);
        
        if (indexType == HpackUtil.IndexType.INCREMENTAL) {
            try {
                tableManager.getDynamicTable().add(new HeaderField(name, value));
            } catch (IOException e) {
                // Log the error but continue processing
            }
        }
    }
    
    public boolean endHeaderBlock() {
        boolean truncated = validator.isTruncated();
        reset();
        return truncated;
    }
}