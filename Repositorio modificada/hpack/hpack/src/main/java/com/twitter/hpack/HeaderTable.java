package main.java.com.twitter.hpack;

import java.io.IOException;

public interface HeaderTable {
    HeaderField getEntry(int index) throws IOException;
    int getIndex(byte[] name);
    int getIndex(byte[] name, byte[] value);
    int length();
    int size();
    int capacity();
}