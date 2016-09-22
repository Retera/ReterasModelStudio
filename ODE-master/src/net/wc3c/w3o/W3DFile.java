package net.wc3c.w3o;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.w3o.W3DFile.Doodad;
import net.wc3c.wts.WTSFile;

public class W3DFile extends W3OBase<Doodad> {
    /**
     * Creates a new W3D file from the specified file and the specified WTS file.
     * 
     * @param source the W3D file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3D file.
     */
    public W3DFile(final Path source, final WTSFile trigStrs) throws IOException {
        super(source, trigStrs);
    }
    
    /**
     * Creates a new W3D file from the specified source and the specified WTS file.
     * 
     * @param sourcePath path to the W3D file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3D file.
     */
    public W3DFile(final String sourcePath, final WTSFile trigStrs) throws IOException {
        super(sourcePath, trigStrs);
    }
    
    /**
     * Creates a new W3D file from the specified file.
     * 
     * @param source the W3D file.
     * @throws IOException in case there was a problem reading from the W3D file.
     */
    public W3DFile(final Path source) throws IOException {
        super(source);
    }
    
    /**
     * Creates a new W3D file from the specified source.
     * 
     * @param sourcePath path to the W3D file.
     * @throws IOException in case there was a problem reading from the W3D file.
     */
    public W3DFile(final String sourcePath) throws IOException {
        super(sourcePath);
    }
    
    /**
     * Creates a new W3D file with the specified WTS file backing it.
     * 
     * @param trigStrs the WTS file;
     */
    public W3DFile(final WTSFile trigStrs) {
        super(trigStrs);
    }
    
    /**
     * Creates a new W3D file.
     */
    public W3DFile() {
        super();
    }
    
    /**
     * Returns a read-only view on the doodads contained within this W3D file.
     * 
     * @return a read-only view on all doodads in this file.
     */
    public Collection<Doodad> getDoodads() {
        return getEntries();
    }
    
    /**
     * Add a doodad to this W3D file. Changes the context of the doodad to this W3D file.
     * 
     * @param doodad the doodad to add.
     */
    public void addDoodad(final Doodad doodad) {
        doodad.setContext(this);
        addEntry(doodad);
    }
    
    /**
     * Retrieves the doodad identified by the specified doodad ID from this W3D file.
     * 
     * @param doodadId the doodad ID to look for.
     * @return the desired doodad, if it exists, <code>null</code> otherwise.
     */
    public Doodad getDoodad(final int doodadId) {
        return getEntry(doodadId);
    }
    
    @Override
    protected Doodad readEntry(final BufferedDataChannel dc) throws IOException {
        return new Doodad(dc, this);
    }
    
    public static class Doodad extends W3Object<W3DFile> {
        @Override
        protected Property<?> readProperty(final BufferedDataChannel dc) throws IOException {
            return new VariationProperty<Doodad>(dc, this);
        }
        
        /**
         * Returns the property identified by the specified ID. If this doodad does not have a matching property, its
         * parents properties are searched.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public VariationProperty<Doodad> getProperty(final int fieldId) {
            return (VariationProperty<Doodad>) getProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Returns the property identified by the specified ID. Does not search through the properties of the parent of
         * this doodad, if no matching property can be found.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public VariationProperty<Doodad> getPropertyEx(final int fieldId) {
            return (VariationProperty<Doodad>) getPropertyEx(Property.generateKey(fieldId));
        }
        
        /**
         * Checks if this doodad has a property with the specified ID.
         * 
         * @param fieldId the ID to look for.
         * @return <code>true</code> if this doodad has a property with the specified ID, <code>false</code> otherwise.
         */
        public boolean hasProperty(final int fieldId) {
            return hasProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Adds the specified property to this doodad, if the doodad does not already have a property with the same ID.
         * Also changes the context of the property to add to this doodad.
         * 
         * @param property the property to add.
         */
        public void addProperty(final VariationProperty<Doodad> property) {
            if (hasProperty(property.getFieldId())) {
                return;
            }
            
            property.setContext(this);
            putProperty(property.generateKey(), property);
        }
        
        Doodad(final BufferedDataChannel dc, final W3DFile context) throws IOException {
            super(dc, context);
        }
        
        /**
         * Creates a new doodad with the specified parent (identified by its ID) and the specified ID.
         * 
         * @param parentId the ID of the parent doodad.
         * @param id the new doodad's ID.
         */
        public Doodad(final int parentId, final int id) {
            super(parentId, id);
        }
        
        /**
         * Creates a new standard WC3 doodad.
         * 
         * @param id the new doodad's ID.
         */
        public Doodad(final int id) {
            super(id);
        }
    }
    
}
