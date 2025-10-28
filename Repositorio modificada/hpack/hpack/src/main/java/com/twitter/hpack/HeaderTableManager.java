package main.java.com.twitter.hpack;

import java.io.IOException;

public class HeaderTableManager {
    private final StaticHeaderTable staticTable;
    private final DynamicHeaderTable dynamicTable;
    
    public HeaderTableManager(StaticHeaderTable staticTable, DynamicHeaderTable dynamicTable) {
        this.staticTable = staticTable;
        this.dynamicTable = dynamicTable;
    }
    
    public HeaderField getEntry(int index) throws IOException {
        if (index <= staticTable.length()) {
            return staticTable.getEntry(index);
        } else if (index - staticTable.length() <= dynamicTable.length()) {
            return dynamicTable.getEntry(index - staticTable.length());
        } else {
            throw new IOException("illegal index value");
        }
    }
    
    public void readName(int index, byte[] nameBuffer) throws IOException {
        if (index <= staticTable.length()) {
            HeaderField headerField = staticTable.getEntry(index);
            System.arraycopy(headerField.name, 0, nameBuffer, 0, headerField.name.length);
        } else if (index - staticTable.length() <= dynamicTable.length()) {
            HeaderField headerField = dynamicTable.getEntry(index - staticTable.length());
            System.arraycopy(headerField.name, 0, nameBuffer, 0, headerField.name.length);
        } else {
            throw new IOException("illegal index value");
        }
    }
    
    public void indexHeader(int index, HeaderListener headerListener) throws IOException {
        if (index <= staticTable.length()) {
            HeaderField headerField = staticTable.getEntry(index);
            addHeader(headerListener, headerField.name, headerField.value, false);
        } else if (index - staticTable.length() <= dynamicTable.length()) {
            HeaderField headerField = dynamicTable.getEntry(index - staticTable.length());
            addHeader(headerListener, headerField.name, headerField.value, false);
        } else {
            throw new IOException("illegal index value");
        }
    }
    
    private void addHeader(HeaderListener headerListener, byte[] name, byte[] value, boolean sensitive) {
        if (name.length == 0) {
            throw new AssertionError("name is empty");
        }
        headerListener.addHeader(name, value, sensitive);
    }
    
    public StaticHeaderTable getStaticTable() {
        return staticTable;
    }
    
    public DynamicHeaderTable getDynamicTable() {
        return dynamicTable;
    }
}