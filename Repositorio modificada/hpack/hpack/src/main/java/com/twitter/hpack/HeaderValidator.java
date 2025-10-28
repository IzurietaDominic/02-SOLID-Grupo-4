package main.java.com.twitter.hpack;

public class HeaderValidator {
    private final int maxHeaderSize;
    private long headerSize;
    
    public HeaderValidator(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        this.headerSize = 0;
    }
    
    public void reset() {
        headerSize = 0;
    }
    
    public boolean exceedsMaxHeaderSize(long size) {
        if (size + headerSize <= maxHeaderSize) {
            return false;
        }
        headerSize = maxHeaderSize + 1;
        return true;
    }
    
    public void addHeaderSize(int nameLength, int valueLength) {
        long newSize = headerSize + nameLength + valueLength;
        if (newSize <= maxHeaderSize) {
            headerSize = newSize;
        } else {
            headerSize = maxHeaderSize + 1;
        }
    }
    
    public boolean isTruncated() {
        return headerSize > maxHeaderSize;
    }
    
    public long getHeaderSize() {
        return headerSize;
    }
}