package net.wc3c.w3o;

import java.io.IOException;

import net.wc3c.util.BufferedDataChannel;

public class Property<T extends W3Object<?>> implements W3OContext<T> {
    private int          fieldId;
    private PropertyType type;
    
    private Object       value;
    @SuppressWarnings("unused")
    private int          end;
    
    private T            context;
    
    /**
     * Returns the current context of this property. Usually the unit this property belongs to.
     * 
     * @return the context of this property.
     */
    @Override
    public T getContext() {
        return this.context;
    }
    
    /**
     * Changes the context of this property. Should be a valid {@link T} object.
     * 
     * @param context the new context of this property.
     */
    @Override
    public void setContext(final T context) {
        this.context = context;
    }
    
    /**
     * Returns the ID of this property.
     * 
     * @return the ID of this property.
     */
    public int getFieldId() {
        return this.fieldId;
    }
    
    /**
     * Returns the type of data this property contains.
     * 
     * @return type of this property's value.
     */
    public PropertyType getType() {
        return this.type;
    }
    
    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public Object getValue() {
        return this.value;
    }
    
    /**
     * Returns a unique identifier for this property. Wraps the ID of this property in a {@link Long}.
     * 
     * @return the ID of this property as a {@link Long}.
     */
    public Long generateKey() {
        return generateKey(this);
    }
    
    protected void setFieldId(final int fieldId) {
        this.fieldId = fieldId;
    }
    
    protected void setType(final PropertyType type) {
        this.type = type;
    }
    
    protected void setValue(final Object value) {
        this.value = value;
    }
    
    protected void setEnd(final int end) {
        this.end = end;
    }
    
    protected void readFrom(final BufferedDataChannel dc) throws IOException {
        setFieldId(dc.readIntBE());
        setType(PropertyType.fromInt(dc.readInt()));
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
    
    Property(final BufferedDataChannel dc, final T context) throws IOException {
        this.context = context;
        readFrom(dc);
    }
    
    /**
     * Creates a new property with the specified attributes.
     * 
     * @param fieldId the ID of this property.
     * @param type type information for the value.
     * @param value value of this property.
     */
    public Property(final int fieldId, final PropertyType type, final Object value) {
        this.fieldId = fieldId;
        this.type = type;
        this.value = value;
    }
    
    /**
     * Creates a new property with the specified attributes.
     * 
     * @param fieldId the ID of this property.
     * @param type type information for the value.
     * @param value value of this property.
     * @param context context for this property.
     */
    public Property(final int fieldId, final PropertyType type, final Object value, final T context) {
        this.fieldId = fieldId;
        this.type = type;
        this.value = value;
        this.context = context;
    }
    
    /**
     * Generates a unique key for the specified property. Wraps the property's ID in a {@link Long}.
     * 
     * @param property the property for which a key should be generated.
     * @return the property's ID wrapped in a {@link Long}.
     */
    public static Long generateKey(final Property<?> property) {
        return generateKey(property.getFieldId());
    }
    
    /**
     * Generates a key from the specified ID. Wraps the ID in a {@link Long}.
     * 
     * @param fieldId ID of a property.
     * @return the ID wrapped in a {@link Long}.
     */
    public static Long generateKey(final int fieldId) {
        return (long) fieldId;
    }
}
