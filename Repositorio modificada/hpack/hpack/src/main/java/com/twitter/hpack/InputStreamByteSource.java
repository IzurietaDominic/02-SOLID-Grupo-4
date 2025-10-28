package main.java.com.twitter.hpack;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamByteSource implements ByteSource {
    private final InputStream inputStream;
    
    public InputStreamByteSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }
    
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }
    
    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }
    
    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }
    
    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }
    
    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}