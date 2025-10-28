package main.java.com.twitter.hpack;

public interface HeaderListener {
    void addHeader(byte[] name, byte[] value, boolean sensitive);
}