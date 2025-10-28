package main.java.com.twitter.hpack;

/**
 * Entrada en la tabla dinámica con información adicional para gestión
 * Usa composición en lugar de herencia para respetar LSP
 */
public class HeaderEntry {
    private final HeaderField headerField;
    private HeaderEntry before;
    private HeaderEntry after;
    private HeaderEntry next;
    private final int hash;
    private final int index;
    
    public HeaderEntry(int hash, byte[] name, byte[] value, int index, HeaderEntry next) {
        this.headerField = new HeaderField(name, value);
        this.hash = hash;
        this.index = index;
        this.next = next;
        this.before = this.after = this; // Inicialmente apunta a sí mismo
    }
    
    public HeaderField getHeaderField() {
        return headerField;
    }
    
    public byte[] getName() {
        return headerField.name;
    }
    
    public byte[] getValue() {
        return headerField.value;
    }
    
    public int size() {
        return headerField.size();
    }
    
    public int getHash() {
        return hash;
    }
    
    public int getIndex() {
        return index;
    }
    
    public HeaderEntry getBefore() {
        return before;
    }
    
    public HeaderEntry getAfter() {
        return after;
    }
    
    public HeaderEntry getNext() {
        return next;
    }
    
    public void setBefore(HeaderEntry before) {
        this.before = before;
    }
    
    public void setAfter(HeaderEntry after) {
        this.after = after;
    }
    
    public void setNext(HeaderEntry next) {
        this.next = next;
    }
    
    /**
     * Remueve esta entrada de la lista vinculada
     */
    public void remove() {
        before.after = after;
        after.before = before;
        before = null;
        after = null;
        next = null;
    }
    
    /**
     * Inserta esta entrada antes de la entrada especificada
     */
    public void addBefore(HeaderEntry existingEntry) {
        after = existingEntry;
        before = existingEntry.before;
        before.after = this;
        after.before = this;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HeaderEntry)) return false;
        
        HeaderEntry other = (HeaderEntry) obj;
        return headerField.equals(other.headerField) &&
               hash == other.hash &&
               index == other.index;
    }
    
    @Override
    public int hashCode() {
        return 31 * headerField.hashCode() + hash;
    }
    
    @Override
    public String toString() {
        return "HeaderEntry{index=" + index + ", " + headerField.toString() + "}";
    }
}
