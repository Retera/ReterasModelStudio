package net.wc3c.w3o;

import java.io.IOException;

import net.wc3c.util.BufferedDataChannel;

public class VariationProperty<T extends W3Object<?>> extends Property<T> {
    private int variation;
    @SuppressWarnings("unused")
    private int dataPointer;
    
    /**
     * Returns the variation of this property.
     * 
     * @return the variation of this property.
     */
    public int getVariation() {
        return variation;
    }
    
    /**
     * Returns a unique identifier for this property. Wraps the ID, and the variation in a {@link Long}.
     * 
     * @return the ID, and the variation of this property wrapped in a {@link Long}.
     */
    @Override
    public Long generateKey() {
        return generateKey(this);
    }
    
    protected void setVariation(final int variation) {
        this.variation = variation;
    }
    
    protected void setDataPointer(final int dataPointer) {
        this.dataPointer = dataPointer;
    }
    
    @Override
    protected void readFrom(final BufferedDataChannel dc) throws IOException {
        setFieldId(dc.readIntBE());
        setType(PropertyType.fromInt(dc.readInt()));
        setVariation(dc.readInt());
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
    
    VariationProperty(final BufferedDataChannel dc, final T context) throws IOException {
        super(dc, context);
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param value
     */
    public VariationProperty(final int fieldId, final PropertyType type, final Object value) {
        super(fieldId, type, value);
        this.variation = 0;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param value
     * @param context
     */
    public VariationProperty(final int fieldId, final PropertyType type, final Object value, final T context) {
        super(fieldId, type, value, context);
        this.variation = 0;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param variation
     * @param value
     */
    public VariationProperty(final int fieldId, final PropertyType type, final int variation, final Object value) {
        super(fieldId, type, value);
        this.variation = variation;
    }
    
    /**
     * 
     * @param fieldId
     * @param type
     * @param variation
     * @param value
     * @param context
     */
    public VariationProperty(final int fieldId, final PropertyType type, final int variation, final Object value,
            final T context) {
        super(fieldId, type, value, context);
        this.variation = variation;
    }
    
    /**
     * 
     * @param property
     * @return
     */
    public static Long generateKey(final VariationProperty<?> property) {
        return generateKey(property.getFieldId(), property.getVariation());
    }
    
    /**
     * 
     * @param fieldId
     * @param variation
     * @return
     */
    public static Long generateKey(final int fieldId, final int variation) {
        long key = fieldId;
        key <<= 16;
        key |= (0x0000FFFF & variation);
        return key;
    }
}
