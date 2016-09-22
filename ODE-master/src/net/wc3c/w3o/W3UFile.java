package net.wc3c.w3o;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.w3o.W3UFile.Unit;
import net.wc3c.wts.WTSFile;

public class W3UFile extends W3OBase<Unit> {
    /**
     * Creates a new W3U file from the specified file and the specified WTS file.
     * 
     * @param source the W3U file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3UFile(final Path source, final WTSFile trigStrs) throws IOException {
        super(source, trigStrs);
    }
    
    /**
     * Creates a new W3U file from the specified source and the specified WTS file.
     * 
     * @param sourcePath path to the W3U file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3UFile(final String sourcePath, final WTSFile trigStrs) throws IOException {
        super(sourcePath, trigStrs);
    }
    
    /**
     * Creates a new W3U file from the specified file.
     * 
     * @param source the W3U file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3UFile(final Path source) throws IOException {
        super(source);
    }
    
    /**
     * Creates a new W3U file from the specified source.
     * 
     * @param sourcePath path to the W3U file.
     * @throws IOException in case there was a problem reading from the W3O file.
     */
    public W3UFile(final String sourcePath) throws IOException {
        super(sourcePath);
    }
    
    /**
     * Creates a new W3U file with the specified WTS file backing it.
     * 
     * @param trigStrs the WTS file;
     */
    public W3UFile(final WTSFile trigStrs) {
        super(trigStrs);
    }
    
    /**
     * Creates a new W3U file.
     */
    public W3UFile() {
        super();
    }
    
    /**
     * Returns a read-only view on the units contained within this W3U file.
     * 
     * @return a read-only view on all units in this file.
     */
    public Collection<Unit> getUnits() {
        return getEntries();
    }
    
    /**
     * Add a unit to this W3U file. Changes the context of the unit to this W3U file.
     * 
     * @param unit the unit to add.
     */
    public void addUnit(final Unit unit) {
        unit.setContext(this);
        addEntry(unit);
    }
    
    /**
     * Retrieves the unit identified by the specified unit ID from this W3U file.
     * 
     * @param unitId the unit ID to look for.
     * @return the desired unit, if it exists, <code>null</code> otherwise.
     */
    public Unit getUnit(final int unitId) {
        return getEntry(unitId);
    }
    
    @Override
    protected Unit readEntry(final BufferedDataChannel dc) throws IOException {
        return new Unit(dc, this);
    }
    
    public static class Unit extends W3Object<W3UFile> {
        @Override
        protected Property<?> readProperty(final BufferedDataChannel dc) throws IOException {
            return new Property<Unit>(dc, this);
        }
        
        /**
         * Returns the property identified by the specified ID. If this unit does not have a matching property, its
         * parents properties are searched.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public Property<Unit> getProperty(final int fieldId) {
            return (Property<Unit>) getProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Returns the property identified by the specified ID. Does not search through the properties of the parent of
         * this unit, if no matching property can be found.
         * 
         * @param fieldId the property ID to look for.
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public Property<Unit> getPropertyEx(final int fieldId) {
            return (Property<Unit>) getPropertyEx(Property.generateKey(fieldId));
        }
        
        /**
         * Checks if this unit has a property with the specified ID.
         * 
         * @param fieldId the ID to look for.
         * @return <code>true</code> if this unit has a property with the specified ID, <code>false</code> otherwise.
         */
        public boolean hasProperty(final int fieldId) {
            return hasProperty(Property.generateKey(fieldId));
        }
        
        /**
         * Adds the specified property to this unit, if the unit does not already have a property with the same ID. Also
         * changes the context of the property to add to this unit.
         * 
         * @param property the property to add.
         */
        public void addProperty(final Property<Unit> property) {
            if (hasProperty(property.getFieldId())) {
                return;
            }
            
            property.setContext(this);
            putProperty(property.generateKey(), property);
        }
        
        Unit(final BufferedDataChannel dc, final W3UFile context) throws IOException {
            super(dc, context);
        }
        
        /**
         * Creates a new unit with the specified parent (identified by its ID) and the specified ID.
         * 
         * @param parentId the ID of the parent unit.
         * @param id the new unit's ID.
         */
        public Unit(final int parentId, final int id) {
            super(parentId, id);
        }
        
        /**
         * Creates a new standard WC3 unit.
         * 
         * @param id the new unit's ID.
         */
        public Unit(final int id) {
            super(id);
        }
    }
}
