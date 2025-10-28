package main.java.com.twitter.hpack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticTable implements StaticHeaderTable {
    private static final String EMPTY = "";
    
    private final List<HeaderField> staticTable;
    private final Map<String, Integer> staticIndexByName;
    
    public StaticTable() {
        this(createDefaultHeaders());
    }
    
    public StaticTable(List<HeaderField> headers) {
        this.staticTable = List.copyOf(headers);
        this.staticIndexByName = createMap(headers);
    }
    
    private static List<HeaderField> createDefaultHeaders() {
        return Arrays.asList(
            new HeaderField(":authority", EMPTY),
            new HeaderField(":method", "GET"),
            new HeaderField(":method", "POST"),
            new HeaderField(":path", "/"),
            new HeaderField(":path", "/index.html"),
            new HeaderField(":scheme", "http"),
            new HeaderField(":scheme", "https"),
            new HeaderField(":status", "200"),
            new HeaderField(":status", "204"),
            new HeaderField(":status", "206"),
            new HeaderField(":status", "304"),
            new HeaderField(":status", "400"),
            new HeaderField(":status", "404"),
            new HeaderField(":status", "500"),
            new HeaderField("accept-charset", EMPTY),
            new HeaderField("accept-encoding", "gzip, deflate"),
            new HeaderField("accept-language", EMPTY),
            new HeaderField("accept-ranges", EMPTY),
            new HeaderField("accept", EMPTY),
            new HeaderField("access-control-allow-origin", EMPTY),
            new HeaderField("age", EMPTY),
            new HeaderField("allow", EMPTY),
            new HeaderField("authorization", EMPTY),
            new HeaderField("cache-control", EMPTY),
            new HeaderField("content-disposition", EMPTY),
            new HeaderField("content-encoding", EMPTY),
            new HeaderField("content-language", EMPTY),
            new HeaderField("content-length", EMPTY),
            new HeaderField("content-location", EMPTY),
            new HeaderField("content-range", EMPTY),
            new HeaderField("content-type", EMPTY),
            new HeaderField("cookie", EMPTY),
            new HeaderField("date", EMPTY),
            new HeaderField("etag", EMPTY),
            new HeaderField("expect", EMPTY),
            new HeaderField("expires", EMPTY),
            new HeaderField("from", EMPTY),
            new HeaderField("host", EMPTY),
            new HeaderField("if-match", EMPTY),
            new HeaderField("if-modified-since", EMPTY),
            new HeaderField("if-none-match", EMPTY),
            new HeaderField("if-range", EMPTY),
            new HeaderField("if-unmodified-since", EMPTY),
            new HeaderField("last-modified", EMPTY),
            new HeaderField("link", EMPTY),
            new HeaderField("location", EMPTY),
            new HeaderField("max-forwards", EMPTY),
            new HeaderField("proxy-authenticate", EMPTY),
            new HeaderField("proxy-authorization", EMPTY),
            new HeaderField("range", EMPTY),
            new HeaderField("referer", EMPTY),
            new HeaderField("refresh", EMPTY),
            new HeaderField("retry-after", EMPTY),
            new HeaderField("server", EMPTY),
            new HeaderField("set-cookie", EMPTY),
            new HeaderField("strict-transport-security", EMPTY),
            new HeaderField("transfer-encoding", EMPTY),
            new HeaderField("user-agent", EMPTY),
            new HeaderField("vary", EMPTY),
            new HeaderField("via", EMPTY),
            new HeaderField("www-authenticate", EMPTY)
        );
    }
    
    @Override
    public HeaderField getEntry(int index) {
        if (index <= 0 || index > staticTable.size()) {
            throw new IndexOutOfBoundsException("Invalid static table index: " + index);
        }
        return staticTable.get(index - 1);
    }
    
    @Override
    public int getIndex(byte[] name) {
        String nameString = new String(name, HpackUtil.ISO_8859_1);
        Integer index = staticIndexByName.get(nameString);
        return index != null ? index : -1;
    }
    
    @Override
    public int getIndex(byte[] name, byte[] value) {
        int index = getIndex(name);
        if (index == -1) {
            return -1;
        }
        
        while (index <= staticTable.size()) {
            HeaderField entry = getEntry(index);
            if (!HpackUtil.equals(name, entry.name)) {
                break;
            }
            if (HpackUtil.equals(value, entry.value)) {
                return index;
            }
            index++;
        }
        
        return -1;
    }
    
    @Override
    public int length() {
        return staticTable.size();
    }
    
    @Override
    public int size() {
        return staticTable.stream().mapToInt(HeaderField::size).sum();
    }
    
    @Override
    public int capacity() {
        return Integer.MAX_VALUE;
    }
    
    private Map<String, Integer> createMap(List<HeaderField> headers) {
        int length = headers.size();
        HashMap<String, Integer> ret = new HashMap<String, Integer>(length);
        for (int index = length; index > 0; index--) {
            HeaderField entry = headers.get(index - 1);
            String name = new String(entry.name, 0, entry.name.length, HpackUtil.ISO_8859_1);
            ret.putIfAbsent(name, index);
        }
        return ret;
    }
}
