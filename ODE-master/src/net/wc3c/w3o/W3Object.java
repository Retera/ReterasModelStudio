package net.wc3c.w3o;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import net.wc3c.util.BufferedDataChannel;

public abstract class W3Object<T extends W3OBase<?>> implements W3OContext<T> {
    private int                          parentId;
    private int                          id;
    private final Map<Long, Property<?>> properties = new Hashtable<Long, Property<?>>();
    
    private T                            context;
    
    /**
     * Returns the current context of this object, usually the file its contained in.
     * 
     * @return the context of this object.
     */
    @Override
    public T getContext() {
        return this.context;
    }
    
    /**
     * Changes the context of this object. Should be a valid {@link T} object.
     * 
     * @param context the new context of this object.
     */
    @Override
    public void setContext(final T context) {
        this.context = context;
    }
    
    protected void setParentId(final int id) {
        this.parentId = id;
    }
    
    protected void setId(final int id) {
        this.id = id;
    }
    
    /**
     * Returns the ID of the parent object. If this object is a standard WC3 object, this objects ID will be returned.
     * 
     * @return the ID of the parent object.
     */
    public int getParentId() {
        return this.parentId;
    }
    
    /**
     * Returns the ID of this object.
     * 
     * @return the ID of this object.
     */
    public int getId() {
        if (this.id != 0) {
            return this.id;
        }
        return this.parentId;
    }
    
    /**
     * Returns whether or not this object is a standard object of WC3.
     * 
     * @return <code>true</code> if this is a standard object, <code>false</code> otherwise.
     */
    protected boolean isStandardObject() {
        return this.id == 0;
    }
    
    protected Property<?> putProperty(final Long key, final Property<?> property) {
        return this.properties.put(key, property);
    }
    
    public Property<?> getProperty(final Long key) {
        if (hasProperty(key)) {
            return this.properties.get(key);
        } else if (isStandardObject() == false) {
            final W3Object<?> object = getContext().getEntry(getParentId());
            if (object != null) {
                return object.getProperty(key);
            }
        }
        
        return null;
    }
    
    public Property<?> getPropertyEx(final Long key) {
        return this.properties.get(key);
    }
    
    public boolean hasProperty(final Long key) {
        return this.properties.containsKey(key);
    }
    
    protected abstract Property<?> readProperty(BufferedDataChannel dc) throws IOException;
    
    void readFrom(final BufferedDataChannel dc) throws IOException {
        setParentId(dc.readIntBE());
        setId(dc.readIntBE());
        final int numProperties = dc.readInt();
        for (int i = 0; i < numProperties; i += 1) {
            final Property<?> mod = readProperty(dc);
            
            putProperty(mod.generateKey(), mod);
        }
    }
    
    protected W3Object() {
        
    }
    
    protected W3Object(final BufferedDataChannel dc, final T context) throws IOException {
        setContext(context);
        readFrom(dc);
    }
    
    /**
     * Creates a new object with the specified parent (identified by its ID) and the specified ID.
     * 
     * @param parentId the ID of the parent object.
     * @param id the new object's ID.
     */
    public W3Object(final int parentId, final int id) {
        this.parentId = parentId;
        this.id = id;
    }
    
    /**
     * Creates a new standard WC3 object.
     * 
     * @param id the new object's ID.
     */
    public W3Object(final int id) {
        this.parentId = id;
        this.id = 0;
    }
}
