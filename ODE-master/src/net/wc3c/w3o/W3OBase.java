package net.wc3c.w3o;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.wts.WTSFile;

public abstract class W3OBase<T extends W3Object<?>> {
    private final Path            source;
    private final WTSFile         triggerStrings;
    private final Map<Integer, T> entries = new Hashtable<Integer, T>();
    
    /**
     * Creates a new W3O file from the specified source and the specified WTS file.
     * 
     * @param source the W3O file
     * @param triggerStrings the WTS file
     * @throws IOException in case there was a problem reading from the W3O file
     */
    public W3OBase(final Path source, final WTSFile triggerStrings) throws IOException {
        this.source = source;
        this.triggerStrings = triggerStrings;
        
        parse();
    }
    
    /**
     * Creates a new W3O file from the specified source.
     * 
     * @param source the W3O file
     * @throws IOException in case there was a problem reading from the W3O file
     */
    public W3OBase(final Path source) throws IOException {
        this(source, null);
    }
    
    /**
     * Creates a new W3O file from the specified source and the specified WTS file.
     * 
     * @param sourcePath path to the W3O file
     * @param triggerStrings the WTS file
     * @throws IOException in case there was a problem reading from the W3O file
     */
    public W3OBase(final String sourcePath, final WTSFile triggerStrings) throws IOException {
        this(Paths.get(sourcePath), triggerStrings);
    }
    
    /**
     * Creates a new W3O file from the specified source.
     * 
     * @param sourcePath path to the W3O file
     * @throws IOException in case there was a problem reading from the W3O file
     */
    public W3OBase(final String sourcePath) throws IOException {
        this(Paths.get(sourcePath), null);
    }
    
    /**
     * Creates a new W3O file with the specified WTS file backing it.
     * 
     * @param triggerStrings the WTS file
     */
    public W3OBase(final WTSFile triggerStrings) {
        source = null;
        this.triggerStrings = triggerStrings;
    }
    
    /**
     * Creates a new W3O file
     */
    public W3OBase() {
        this((WTSFile) null);
    }
    
    /**
     * Returns the WTS file.
     * 
     * @return the WTS file.
     */
    public WTSFile getTriggerStrings() {
        return this.triggerStrings;
    }
    
    /**
     * 
     * @return
     */
    public Collection<T> getEntries() {
        return Collections.unmodifiableCollection(this.entries.values());
    }
    
    public void addEntry(final T entry) {
        this.entries.put(entry.getId(), entry);
    }
    
    public T getEntry(final int id) {
        return this.entries.get(id);
    }
    
    protected abstract T readEntry(BufferedDataChannel dc) throws IOException;
    
    private void parseTable(final BufferedDataChannel dc) throws IOException {
        final int numEntries = dc.readInt();
        
        for (int i = 0; i < numEntries; i += 1) {
            final T entry = readEntry(dc);
            
            this.entries.put(entry.getId(), entry);
        }
    }
    
    private void parse() throws IOException {
        final BufferedDataChannel dc = new BufferedDataChannel(FileChannel.open(this.source), ByteOrder.LITTLE_ENDIAN);
        
        @SuppressWarnings("unused")
        final int version = dc.readInt();
        
        parseTable(dc);
        parseTable(dc);
    }
}
