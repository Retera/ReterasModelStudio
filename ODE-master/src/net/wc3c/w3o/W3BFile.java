package net.wc3c.w3o;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.w3o.W3BFile.Destructable;
import net.wc3c.wts.WTSFile;

public class W3BFile extends W3OBase<Destructable> {
    /**
     * Creates a new W3B file from the specified file and the specified WTS file.
     * 
     * @param source the W3B file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3BFile(final Path source, final WTSFile trigStrs) throws IOException {
        super(source, trigStrs);
    }
    
    /**
     * Creates a new W3B file from the specified source and the specified WTS file.
     * 
     * @param sourcePath path to the W3B file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3BFile(final String sourcePath, final WTSFile trigStrs) throws IOException {
        super(sourcePath, trigStrs);
    }
    
    /**
     * Creates a new W3B file from the specified file.
     * 
     * @param source the W3B file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3BFile(final Path source) throws IOException {
        super(source);
    }
    
    /**
     * Creates a new W3B file from the specified source.
     * 
     * @param sourcePath path to the W3B file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3BFile(final String sourcePath) throws IOException {
        super(sourcePath);
    }
    
    /**
     * Creates a new W3B file with the specified WTS file backing it.
     * 
     * @param trigStrs the WTS file;
     */
    public W3BFile(final WTSFile trigStrs) {
        super(trigStrs);
    }
    
    /**
     * Creates a new W3B file.
     */
    public W3BFile() {
        super();
    }
    
    /**
     * Returns a read-only view on the destructables contained within this W3B file.
     * 
     * @return a read-only view on all destructables in this file.
     */
    public Collection<Destructable> getDestructables() {
        return getEntries();
    }
    
    /**
     * Add a unit to this W3B file. Changes the context of the destructable to this W3B file.
     * 
     * @param destructable the destructable to add.
     */
    public void addDestructable(final Destructable destructable) {
        destructable.setContext(this);
        addEntry(destructable);
    }
    
    /**
     * Retrieves the destructable identified by the specified destructable ID from this W3B file.
     * 
     * @param destructableId the destructable ID to look for.
     * @return the desired destructable, if it exists, <code>null</code> otherwise.
     */
    public Destructable getDestructable(final int destructableId) {
        return getEntry(destructableId);
    }
    
    @Override
    protected Destructable readEntry(final BufferedDataChannel dc) throws IOException {
        return new Destructable(dc, this);
    }
    
    public static class Destructable extends W3Object<W3BFile> {
        @Override
        protected Property<?> readProperty(final BufferedDataChannel dc) throws IOException {
            return new Property<Destructable>(dc, this);
        }
        
        /**
         * Returns the property identified by the specified ID. If this destructable does not have a matching property,
         * its parents properties are searched.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public Property<Destructable> getProperty(final int fieldId) {
            return (Property<Destructable>) getProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Returns the property identified by the specified ID. Does not search through the properties of the parent of
         * this destructable, if no matching property can be found.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public Property<Destructable> getPropertyEx(final int fieldId) {
            return (Property<Destructable>) getPropertyEx(Property.generateKey(fieldId));
        }
        
        /**
         * Checks if this destructable has a property with the specified ID.
         * 
         * @param fieldId the ID to look for.
         * @return <code>true</code> if this destructable has a property with the specified ID, <code>false</code>
         *         otherwise.
         */
        public boolean hasProperty(final int fieldId) {
            return hasProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Adds the specified property to this destructable, if the destructable does not already have a property with
         * the same ID. Also changes the context of the property to add to this destructable.
         * 
         * @param property the property to add.
         */
        public void addProperty(final Property<Destructable> property) {
            if (hasProperty(property.getFieldId())) {
                return;
            }
            
            property.setContext(this);
            putProperty(property.generateKey(), property);
        }
        
        Destructable(final BufferedDataChannel dc, final W3BFile context) throws IOException {
            super(dc, context);
        }
        
        /**
         * Creates a new destructable with the specified parent (identified by its ID) and the specified ID.
         * 
         * @param parentId the ID of the parent destructable.
         * @param id the new destructable's ID.
         */
        public Destructable(final int parentId, final int id) {
            super(parentId, id);
        }
        
        /**
         * Creates a new standard WC3 destructable.
         * 
         * @param id the new destructable's ID.
         */
        public Destructable(final int id) {
            super(id);
        }
    }
}
