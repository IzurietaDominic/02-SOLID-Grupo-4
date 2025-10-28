package main.java.com.twitter.hpack;

import static com.twitter.hpack.HpackUtil.ISO_8859_1;
import static com.twitter.hpack.HpackUtil.requireNonNull;

class HeaderField implements Comparable<HeaderField> {
    static final int HEADER_ENTRY_OVERHEAD = 32;
    
    static int sizeOf(byte[] name, byte[] value) {
        return name.length + value.length + HEADER_ENTRY_OVERHEAD;
    }
    
    final byte[] name;
    final byte[] value;
    
    HeaderField(String name, String value) {
        this(name.getBytes(ISO_8859_1), value.getBytes(ISO_8859_1));
    }
    
    HeaderField(byte[] name, byte[] value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }
    
    int size() {
        return name.length + value.length + HEADER_ENTRY_OVERHEAD;
    }
    
    @Override
    public int compareTo(HeaderField anotherHeaderField) {
        int ret = compareTo(name, anotherHeaderField.name);
        if (ret == 0) {
            ret = compareTo(value, anotherHeaderField.value);
        }
        return ret;
    }
    
    private int compareTo(byte[] s1, byte[] s2) {
        int len1 = s1.length;
        int len2 = s2.length;
        int lim = Math.min(len1, len2);
        
        int k = 0;
        while (k < lim) {
            byte b1 = s1[k];
            byte b2 = s2[k];
            if (b1 != b2) {
                return b1 - b2;
            }
            k++;
        }
        return len1 - len2;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HeaderField)) {
            return false;
        }
        HeaderField other = (HeaderField) obj;
        boolean nameEquals = HpackUtil.equals(name, other.name);
        boolean valueEquals = HpackUtil.equals(value, other.value);
        return nameEquals && valueEquals;
    }
    
    @Override
    public String toString() {
        String nameString = new String(name);
        String valueString = new String(value);
        return nameString + ": " + valueString;
    }
}