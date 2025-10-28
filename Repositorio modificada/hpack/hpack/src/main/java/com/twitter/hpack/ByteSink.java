package main.java.com.twitter.hpack;

import java.io.IOException;

public interface ByteSink {
    void write(int b) throws IOException;
    void write(byte[] b) throws IOException;
    void write(byte[] b, int off, int len) throws IOException;
    void flush() throws IOException;
}