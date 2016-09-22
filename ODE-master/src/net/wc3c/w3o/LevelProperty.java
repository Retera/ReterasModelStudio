package net.wc3c.w3o;

import java.io.IOException;

import net.wc3c.util.BufferedDataChannel;

public class LevelProperty<T extends W3Object<?>> extends Property<T> {
    private int level;
    private int dataPointer;
    
    /**
     * Returns the level of this property.
     * 
     * @return the level of this property.
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Returns the data pointer of this property.
     * 
     * @return the data pointer of this property.
     */
    public int getDataPointer() {
        return dataPointer;
    }
    
    /**
     * Returns a unique identifier for this property. Wraps the ID, the level, and the data pointer in a {@link Long}.
     * 
     * @return the ID, the level, and the data pointer of this property wrapped in a {@link Long}.
     */
    @Override
    public Long generateKey() {
        return generateKey(this);
    }
    
    protected void setLevel(final int level) {
        this.level = level;
    }
    
    protected void setDataPointer(final int dataPointer) {
        this.dataPointer = dataPointer;
    }
    
    @Override
    protected void readFrom(final BufferedDataChannel dc) throws IOException {
        setFieldId(dc.readIntBE());
        setType(PropertyType.fromInt(dc.readInt()));
        setLevel(dc.readInt());
        setDataPointer(dc.readInt());
        switch (getType()) {
            case INTEGER:
                setValue(new Integer(dc.readInt()));
                break;
            
            case REAL:
            case UNREAL:
                setValue(new Float(dc.readFloat()));
                break;
            
            case BOOLEAN:
                setValue(new Boolean(dc.readInt() != 0));
                break;
            
            case STRING:
            default: {
                final String value = dc.readUTF8String();
                
                if (getContext().getContext().getTriggerStrings() != null && value.startsWith("TRIGSTR_")) {
                    setValue(getContext().getContext().getTriggerStrings().get(Integer.parseInt(value.substring(8))));
                } else {
                    setValue(value);
                }
                
                break;
            }
        }
        setEnd(dc.readInt());
    }
    
    LevelProperty(final BufferedDataChannel dc, final T context) throws IOException {
        super(dc, context);
        this.level = 0;
        this.dataPointer = 0;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param value
     */
    public LevelProperty(final int fieldId, final PropertyType type, final Object value) {
        super(fieldId, type, value);
        this.level = 0;
        this.dataPointer = 0;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param value
     * @param context
     */
    public LevelProperty(final int fieldId, final PropertyType type, final Object value, final T context) {
        super(fieldId, type, value, context);
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param level
     * @param dataPointer
     * @param value
     */
    public LevelProperty(final int fieldId, final PropertyType type, final int level, final int dataPointer,
            final Object value) {
        super(fieldId, type, value);
        this.level = level;
        this.dataPointer = dataPointer;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param level
     * @param dataPointer
     * @param value
     * @param context
     */
    public LevelProperty(final int fieldId, final PropertyType type, final int level, final int dataPointer,
            final Object value, final T context) {
        super(fieldId, type, value, context);
        this.level = level;
        this.dataPointer = dataPointer;
    }
    
    /**
     * 
     * @param property
     * @return
     */
    public static Long generateKey(final LevelProperty<?> property) {
        return generateKey(property.getFieldId(), property.getDataPointer(), property.getLevel());
    }
    
    /**
     * 
     * @param fieldId
     * @param dataPointer
     * @param level
     * @return
     */
    public static Long generateKey(final int fieldId, final int dataPointer, final int level) {
        long key = fieldId;
        key <<= 32;
        key |= (0xFFFF0000 & dataPointer);
        key |= (0x0000FFFF & level);
        return key;
    }
}
