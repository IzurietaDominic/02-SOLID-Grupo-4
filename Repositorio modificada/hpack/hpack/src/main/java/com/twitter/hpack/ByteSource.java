package com.twitter.hpack;

import java.io.IOException;

public interface ByteSource {
    int read() throws IOException;
    int read(byte[] b) throws IOException;
    int available() throws IOException;
    void mark(int readlimit);
    void reset() throws IOException;
    long skip(long n) throws IOException;
    boolean markSupported();
}