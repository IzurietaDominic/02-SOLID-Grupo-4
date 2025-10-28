package main.java.com.twitter.hpack;

public interface DynamicHeaderTable extends HeaderTable {
    void add(HeaderField header) throws IOException;
    HeaderField remove();
    void clear();
    void setCapacity(int capacity) throws IOException;
}