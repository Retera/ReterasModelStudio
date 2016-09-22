package net.wc3c.ode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.wc3c.slk.SLKFile;
import net.wc3c.util.CharInt;
import net.wc3c.util.Jass;
import net.wc3c.util.Jass.Parameter;
import net.wc3c.util.Jass.Type;
import net.wc3c.w3o.Property;
import net.wc3c.w3o.PropertyType;
import net.wc3c.w3o.W3TFile;
import net.wc3c.w3o.W3TFile.Item;

class ItemDataExtractor extends Extractor<W3TFile, Item> {
    
    private static final int                             FIELD_ID_DEFAULT_ITEM = CharInt.toInt("defi");
    private static final int                             FIELD_ID_CLASS        = CharInt.toInt("icla");
    private static final int                             COLUMN_ITEM_CLASS     = 4;
    
    private final Hashtable<String, Metadata<ItemField>> metadata              = new Hashtable<>();
    
    private class ItemField extends Field {
        
        public ItemField(final Integer id, final String name) {
            super(id, name);
        }
        
        @Override
        public StringBuilder generateLoadFunction() {
            final StringBuilder out = new StringBuilder();
            
            out.append(Jass.function("GetItemType" + getName(), getType().getJassType(), new Parameter(
                    Type.INTEGER,
                    "id")));
            out.append(Jass.ret(getType().loadFunctionName() + "(" + HASHTABLE_NAME + ",id," + getIndex() + ")"));
            out.append(Jass.endfunction());
            
            return out;
        }
        
        @Override
        public String generateSaveFunctionCall(final Item object, final Property<Item> property) {
            return generateSaveFunctionCall(
                    "'" + CharInt.toString(object.getId()) + "'",
                    Integer.toString(getIndex()),
                    property.getValue());
        }
        
    }
    
    private class ItemFieldRequest extends FieldRequest {
        private boolean            restrictToClasses;
        
        private final Set<String>  classes;
        private final Set<Integer> itemIdsToLoad;
        
        public ItemFieldRequest(final ItemField field, final ItemLoadRequest request) {
            super(field, request);
            
            restrictToClasses = request.isRestrictedToClasses();
            classes = request.getClasses();
            itemIdsToLoad = request.getItemIdsToLoad();
        }
        
        @Override
        public void absorb(final LoadRequest request) {
            super.absorb(request);
            
            final ItemLoadRequest req = (ItemLoadRequest) request;
            
            restrictToClasses = restrictToClasses && req.isRestrictedToClasses();
            classes.addAll(req.getClasses());
            itemIdsToLoad.addAll(req.getItemIdsToLoad());
        }
        
        @Override
        protected boolean canLoad(final Item object) {
            boolean load = false;
            
            if (isLoadDefaults()) {
                if (restrictToClasses) {
                    final Property<Item> property = object.getProperty(FIELD_ID_CLASS);
                    if (property != null && classes.contains(property.getValue())) {
                        load = true;
                    } else if (itemIdsToLoad.contains(object.getId())) {
                        load = true;
                    } else {
                        load = false;
                    }
                } else {
                    load = true;
                }
            }
            
            if (object.getPropertyEx(FIELD_ID_DEFAULT_ITEM) == null) {
                load = true;
            }
            
            return load;
        }
    }
    
    private class ItemLoadRequest extends LoadRequest {
        private boolean            restrictToClasses = false;
        
        private final Set<String>  classes           = new HashSet<String>();
        private final Set<Integer> itemIdsToLoad     = new HashSet<Integer>();
        
        public ItemLoadRequest(final String[] words) {
            super(words);
            
            for (final String word : words) {
                if (word.toLowerCase().startsWith("classes=")) {
                    restrictToClasses = true;
                    final String[] classes = word.substring(8).split(",");
                    for (final String cls : classes) {
                        this.classes.add(cls);
                    }
                } else if (word.toLowerCase().startsWith("rawcodes=")) {
                    final String[] rawcodes = word.substring(9).split(",");
                    for (final String rawcode : rawcodes) {
                        itemIdsToLoad.add(CharInt.toInt(rawcode));
                    }
                }
            }
        }
        
        public boolean isRestrictedToClasses() {
            return restrictToClasses;
        }
        
        public Set<String> getClasses() {
            return classes;
        }
        
        public Set<Integer> getItemIdsToLoad() {
            return itemIdsToLoad;
        }
    }
    
    public ItemDataExtractor(final File enclosingFolder, final W3TFile w3tFile) throws IOException {
        super(enclosingFolder, w3tFile);
    }
    
    @Override
    protected String getCommandString() {
        return "LoadItemData";
    }
    
    @Override
    protected String getLibraryNameString() {
        return "ExportedItemData";
    }
    
    @Override
    protected String getLibraryInitializerString() {
        return "InitExportedItemData";
    }
    
    @Override
    protected void loadMetadata() throws IOException {
        final SLKFile slk = new SLKFile(new File(getMetaPath(), "UnitMetaData.slk"));
        
        for (int row = 1; row < slk.getHeight(); row += 1) {
            final String fieldID = slk.getCell(0, row).toString();
            final String name = slk.getCell(1, row).toString();
            final String slkName = slk.getCell(2, row).toString();
            final String index = slk.getCell(3, row).toString();
            final String type = slk.getCell(7, row).toString();
            
            final ItemField f = (ItemField) getField(CharInt.toInt(fieldID));
            
            if (f != null) {
                removeFromInvalidFields(f);
                
                Metadata<ItemField> m = metadata.get(slkName);
                if (m == null) {
                    m = new Metadata<ItemField>();
                    metadata.put(slkName, m);
                }
                m.insertIntoFieldMapping(name, Integer.parseInt(index), f);
                
                if (type.equals("int")) {
                    f.setType(FieldType.INTEGER);
                } else if (type.equals("bool")) {
                    f.setType(FieldType.BOOLEAN);
                } else if (type.endsWith("real")) {
                    f.setType(FieldType.REAL);
                } else {
                    f.setType(FieldType.STRING);
                }
            }
        }
    }
    
    @Override
    protected void loadDefaultObjects() throws IOException {
        final SLKFile slk = new SLKFile(new File(getDataPath(), "ItemData.slk"));
        for (int i = 1; i < slk.getHeight(); i += 1) {
            final Object[] row = slk.getRow(i);
            
            final int id = CharInt.toInt(row[0].toString());
            final String cls = row[COLUMN_ITEM_CLASS].toString();
            
            if (getObject(id) == null) {
                final Item item = new Item(id);
                item.addProperty(new Property<Item>(FIELD_ID_DEFAULT_ITEM, PropertyType.BOOLEAN, true));
                item.addProperty(new Property<Item>(FIELD_ID_CLASS, PropertyType.STRING, cls));
                addObject(item);
            }
        }
    }
    
    @Override
    protected void loadProfile(final File file) throws IOException {
        final Metadata<ItemField> data = metadata.get("Profile");
        final BufferedReader br = new BufferedReader(new FileReader(file));
        boolean inBlock = false;
        Item item = null;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.trim();
            if (line.startsWith("//") || line.indexOf("=") == 0) {
                // ignore comments and invalid lines
                // invalid lines are lines where the first char is  '='
            } else if (line.startsWith("[")) {
                // new unit entry
                item = getObject(CharInt.toInt(line.substring(1, 5)));
                inBlock = item != null; // verify if its a valid entry
            } else if (inBlock) { // only proceed if the units valid, we dont want entries for invalid units
                final String[] lineParts = line.split("=", 2);
                
                // Column headings might refer to multiple fields (identified uniquely by their field IDs)
                // Example: Buttonpos refers to ubpx and ubpy, in that order. Values are separated by commas.
                final List<ItemField> fields = data.getFields(lineParts[0]);
                if (fields == null) {
                    continue;
                }
                
                for (final ItemField f : fields) {
                    item.addProperty(new Property<Item>(f.getId(), f.getType().getPropertyType(), data.extractValue(
                            lineParts[0],
                            f,
                            lineParts[1])));
                }
                
            }
        }
        br.close();
    }
    
    @Override
    protected void loadSLK(final File file) throws IOException {
        final SLKFile slk = new SLKFile(file);
        final Metadata<ItemField> data = metadata.get(file.getName().substring(0, file.getName().lastIndexOf('.')));
        
        if (data == null) {
            return;
        }
        
        // Decide which values need to be loaded from the SLKs,
        // as we are only interested in the ones the map requests us to load.
        //
        final ArrayList<List<ItemField>> columnFields = new ArrayList<List<ItemField>>(slk.getWidth());
        for (int i = 0; i < slk.getWidth(); i += 1) {
            columnFields.add(null);
        }
        
        final Object[] firstRow = slk.getRow(0);
        for (int i = 1; i < firstRow.length; i += 1) { // skip the first column (i=0)
            final Object cell = firstRow[i];
            if (cell != null) {
                columnFields.set(i, data.getFields(cell.toString()));
            }
        }
        
        // Now load those that were requested
        //
        for (int i = 1; i < slk.getHeight(); i += 1) {
            final Object[] row = slk.getRow(i);
            
            final Item item = getObject(CharInt.toInt(row[0].toString()));
            if (item == null) {
                continue;
            }
            
            for (int column = 1; column < slk.getWidth(); column += 1) {
                // Column headings might refer to multiple fields (identified uniquely by their field IDs)
                // Example: Buttonpos refers to ubpx and ubpy, in that order. Values are separated by commas.
                
                final List<ItemField> fields = columnFields.get(column);
                if (fields == null) {
                    continue;
                }
                
                final String columnHeading = slk.getCell(column, 0).toString();
                final Object value = row[column];
                
                for (final ItemField f : fields) {
                    if (value == null) {
                        item.addProperty(new Property<Item>(
                                f.getId(),
                                f.getType().getPropertyType(),
                                f.getType().getDefaultValue()));
                    } else {
                        item.addProperty(new Property<Item>(
                                f.getId(),
                                f.getType().getPropertyType(),
                                data.extractValue(columnHeading, f, value.toString())));
                    }
                }
            }
        }
    }
    
    @Override
    protected Field createField(final Integer id, final String name) {
        return new ItemField(id, name);
    }
    
    @Override
    protected LoadRequest createLoadRequest(final String loadRequestLine) {
        return new ItemLoadRequest(loadRequestLine.split("\\s+"));
    }
    
    @Override
    protected FieldRequest createFieldRequest(final Field field, final LoadRequest request) {
        return new ItemFieldRequest((ItemField) field, (ItemLoadRequest) request);
    }
    
    @Override
    protected void loadFields() {
        addRawcodeMapEntry("iabi", "Abilities");
        addRawcodeMapEntry("iarm", "ArmorType");
        addRawcodeMapEntry("iico", "Icon");
        addRawcodeMapEntry("ubpx", "ButtonPositionX");
        addRawcodeMapEntry("ubpy", "ButtonPositionY");
        addRawcodeMapEntry("icla", "Class");
        addRawcodeMapEntry("iclb", "ColorBlue");
        addRawcodeMapEntry("iclg", "ColorGreen");
        addRawcodeMapEntry("iclr", "ColorRed");
        addRawcodeMapEntry("icid", "CooldownGroup");
        addRawcodeMapEntry("ides", "Description");
        addRawcodeMapEntry("idrp", "DropsOnDeath");
        addRawcodeMapEntry("idro", "Droppable");
        addRawcodeMapEntry("ifil", "Model");
        addRawcodeMapEntry("igol", "GoldCost");
        addRawcodeMapEntry("uhot", "Hotkey");
        addRawcodeMapEntry("ihtp", "Health");
        addRawcodeMapEntry("iicd", "IgnoreCooldown");
        addRawcodeMapEntry("ilev", "Level");
        addRawcodeMapEntry("ilum", "LumberCost");
        addRawcodeMapEntry("imor", "IsTransformable");
        addRawcodeMapEntry("unam", "Name");
        addRawcodeMapEntry("ilvo", "UnclassifiedLevel");
        addRawcodeMapEntry("ipaw", "IsPawnable");
        addRawcodeMapEntry("iper", "IsPerishable");
        addRawcodeMapEntry("iprn", "IsRandomChoice");
        addRawcodeMapEntry("ipow", "IsPowerup");
        addRawcodeMapEntry("ipri", "Priority");
        addRawcodeMapEntry("ureq", "Requirements");
        addRawcodeMapEntry("urqa", "RequiredLevels");
        addRawcodeMapEntry("isca", "Scale");
        addRawcodeMapEntry("isel", "IsSellable");
        addRawcodeMapEntry("issc", "SelectionSize");
        addRawcodeMapEntry("isto", "MaximumStock");
        addRawcodeMapEntry("istr", "StockReplenishInterval");
        addRawcodeMapEntry("isst", "StartingStock");
        addRawcodeMapEntry("utip", "Tooltip");
        addRawcodeMapEntry("utub", "Ubertip");
        addRawcodeMapEntry("iusa", "IsUsable");
        addRawcodeMapEntry("iuse", "Uses");
    }
}
