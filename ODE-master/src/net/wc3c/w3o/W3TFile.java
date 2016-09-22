package net.wc3c.w3o;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.wc3c.util.BufferedDataChannel;
import net.wc3c.w3o.W3TFile.Item;
import net.wc3c.wts.WTSFile;

public class W3TFile extends W3OBase<Item> {
    public W3TFile(final Path source, final WTSFile trigStrs) throws IOException {
        super(source, trigStrs);
    }
    
    public W3TFile(final String sourcePath, final WTSFile trigStrs) throws IOException {
        super(sourcePath, trigStrs);
    }
    
    public W3TFile(final Path source) throws IOException {
        super(source);
    }
    
    public W3TFile(final String sourcePath) throws IOException {
        super(sourcePath);
    }
    
    public W3TFile(final WTSFile trigStrs) {
        super(trigStrs);
    }
    
    public W3TFile() {
        super();
    }
    
    /**
     * Returns a read-only view on the items contained within this W3T file.
     * 
     * @return a read-only view on all items in this file.
     */
    public Collection<Item> getItems() {
        return getEntries();
    }
    
    public void addItem(final Item item) {
        item.setContext(this);
        addEntry(item);
    }
    
    public Item getItem(final int itemId) {
        return getEntry(itemId);
    }
    
    @Override
    protected Item readEntry(final BufferedDataChannel dc) throws IOException {
        return new Item(dc, this);
    }
    
    public static class Item extends W3Object<W3TFile> {
        @Override
        protected Property<?> readProperty(final BufferedDataChannel dc) throws IOException {
            return new Property<Item>(dc, this);
        }
        
        @SuppressWarnings("unchecked")
        public Property<Item> getProperty(final int fieldId) {
            return (Property<Item>) super.getProperty(Property.generateKey(fieldId));
        }
        
        @SuppressWarnings("unchecked")
        public Property<Item> getPropertyEx(final int fieldId) {
            return (Property<Item>) super.getPropertyEx(Property.generateKey(fieldId));
        }
        
        public boolean hasProperty(final int fieldId) {
            return super.hasProperty(Property.generateKey(fieldId));
        }
        
        public void addProperty(final Property<Item> property) {
            if (hasProperty(property.getFieldId())) {
                return;
            }
            
            property.setContext(this);
            putProperty(property.generateKey(), property);
        }
        
        Item(final BufferedDataChannel dc, final W3TFile context) throws IOException {
            super(dc, context);
        }
        
        public Item(final int parentId, final int id) {
            super(parentId, id);
        }
        
        public Item(final int id) {
            super(id);
        }
    }
}
