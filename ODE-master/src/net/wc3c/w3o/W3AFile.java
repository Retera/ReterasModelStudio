package net.wc3c.w3o;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.w3o.W3AFile.Ability;
import net.wc3c.wts.WTSFile;

public class W3AFile extends W3OBase<Ability> {
    /**
     * Creates a new W3A file from the specified file and the specified WTS file.
     * 
     * @param source the W3A file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3A file.
     */
    public W3AFile(final Path source, final WTSFile trigStrs) throws IOException {
        super(source, trigStrs);
    }
    
    /**
     * Creates a new W3A file from the specified source and the specified WTS file.
     * 
     * @param sourcePath path to the W3A file.
     * @param trigStrs the WTS file.
     * @throws IOException in case there was a problem reading from the W3A file.
     */
    public W3AFile(final String sourcePath, final WTSFile trigStrs) throws IOException {
        super(sourcePath, trigStrs);
    }
    
    /**
     * Creates a new W3A file from the specified file.
     * 
     * @param source the W3A file.
     * @throws IOException in case there was a problem reading from the W3A file.
     */
    public W3AFile(final Path source) throws IOException {
        super(source);
    }
    
    /**
     * Creates a new W3A file from the specified source.
     * 
     * @param sourcePath path to the W3A file.
     * @throws IOException in case there was a problem reading from the W3A file.
     */
    public W3AFile(final String sourcePath) throws IOException {
        super(sourcePath);
    }
    
    /**
     * Creates a new W3A file with the specified WTS file backing it.
     * 
     * @param trigStrs the WTS file;
     */
    public W3AFile(final WTSFile trigStrs) {
        super(trigStrs);
    }
    
    /**
     * Creates a new W3A file.
     */
    public W3AFile() {
        super();
    }
    
    /**
     * Returns a read-only view on the abilities contained within this W3A file.
     * 
     * @return a read-only view on all abilities in this file.
     */
    public Collection<Ability> getAbilities() {
        return getEntries();
    }
    
    /**
     * Add a ability to this W3A file. Changes the context of the ability to this W3A file.
     * 
     * @param ability the ability to add.
     */
    public void addAbility(final Ability ability) {
        ability.setContext(this);
        addEntry(ability);
    }
    
    /**
     * Retrieves the ability identified by the specified ability ID from this W3A file.
     * 
     * @param abilityId the ability ID to look for.
     * @return the desired ability, if it exists, <code>null</code> otherwise.
     */
    public Ability getAbility(final int abilityId) {
        return getEntry(abilityId);
    }
    
    @Override
    protected Ability readEntry(final BufferedDataChannel dc) throws IOException {
        return new Ability(dc, this);
    }
    
    public static class Ability extends W3Object<W3AFile> {
        @Override
        protected Property<?> readProperty(final BufferedDataChannel dc) throws IOException {
            return new LevelProperty<Ability>(dc, this);
        }
        
        /**
         * Returns the property identified by the specified ID. If this ability does not have a matching property, its
         * parents properties are searched.
         * 
         * @param fieldId the property ID to look for.
         * @param dataPointer the data pointer of that property
         * @param level the level of that property
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public LevelProperty<Ability> getProperty(final int fieldId, final int dataPointer, final int level) {
            return (LevelProperty<Ability>) getProperty(LevelProperty.generateKey(fieldId, dataPointer, level));
        }
        
        /**
         * Returns the property identified by the specified ID. Does not search through the properties of the parent of
         * this ability, if no matching property can be found.
         * 
         * @param fieldId the property ID to look for.
         * @param dataPointer the data pointer of that property
         * @param level the level of that property
         * @return the property identified by the specified ID or <code>null</code> if no such property exists.
         */
        @SuppressWarnings("unchecked")
        public LevelProperty<Ability> getPropertyEx(final int fieldId, final int dataPointer, final int level) {
            return (LevelProperty<Ability>) getPropertyEx(LevelProperty.generateKey(fieldId, dataPointer, level));
        }
        
        /**
         * Checks if this ability has a property with the specified ID.
         * 
         * @param fieldId the ID to look for.
         * @param dataPointer the data pointer of that property
         * @param level the level of that property
         * @return <code>true</code> if this ability has a property with the specified ID, <code>false</code> otherwise.
         */
        public boolean hasProperty(final int fieldId, final int dataPointer, final int level) {
            return hasProperty(LevelProperty.generateKey(fieldId, dataPointer, level));
        }
        
        /**
         * Adds the specified property to this ability, if the ability does not already have a property with the same
         * ID. Also changes the context of the property to add to this ability.
         * 
         * @param property the property to add.
         */
        public void addProperty(final LevelProperty<Ability> property) {
            if (hasProperty(property.getFieldId(), property.getDataPointer(), property.getLevel())) {
                return;
            }
            
            property.setContext(this);
            putProperty(property.generateKey(), property);
        }
        
        Ability(final BufferedDataChannel dc, final W3AFile context) throws IOException {
            super(dc, context);
        }
        
        /**
         * Creates a new ability with the specified parent (identified by its ID) and the specified ID.
         * 
         * @param parentId the ID of the parent ability.
         * @param id the new ability's ID.
         */
        public Ability(final int parentId, final int id) {
            super(parentId, id);
        }
        
        /**
         * Creates a new standard WC3 ability.
         * 
         * @param id the new ability's ID.
         */
        public Ability(final int id) {
            super(id);
        }
    }
    
}
