package net.wc3c.ode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * 
 * @author bagarils
 * 
 * @param <F>
 */
class Metadata<F extends Extractor<?, ?>.Field> {
    private final Hashtable<String, List<F>> fieldMapping = new Hashtable<String, List<F>>();
    
    /**
     * Associates the given field with the given property name
     * 
     * @param name the name to associate the field with
     * @param index the index of the field for the given name
     * @param field the field in question
     */
    public void insertIntoFieldMapping(final String name, final int index, final F field) {
        List<F> list = this.fieldMapping.get(name);
        
        if (list == null) {
            list = new ArrayList<F>();
            this.fieldMapping.put(name, list);
        }
        
        if (index < 0) {
            list.add(field);
        } else {
            for (int i = list.size(); i <= index; i += 1) {
                list.add(null);
            }
            list.set(index, field);
        }
    }
    
    /**
     * Returns an unmodifiable list of fields that share a property name
     * 
     * @param property the name of the property you want a list of fields of
     * @return a list of fields sharing the property name, or <code>null</code> if no fields share that property name
     */
    public List<F> getFields(final String property) {
        final List<F> fields = this.fieldMapping.get(property);
        
        if (fields != null) {
            return Collections.unmodifiableList(fields);
        } else {
            return null;
        }
    }
    
    /**
     * Extracts a value for the specified field from the raw value of the specified property.
     * 
     * @param property name of the property the field is stored under
     * @param field the field to extract a value for
     * @param rawValue the raw value to extract data from
     * @return the requested value of the field
     */
    public Object extractValue(final String property, final F field, final String rawValue) {
        final List<F> list = this.fieldMapping.get(property);
        assert list != null;
        String result;
        
        if (list.size() > 1) {
            final int index = list.indexOf(field);
            assert index > -1;
            final String[] values = rawValue.split(",");
            
            if (index >= values.length) {
                return field.getType().getDefaultValue();
            } else {
                result = values[index];
            }
        } else {
            result = rawValue;
        }
        
        switch (field.getType()) {
            case BOOLEAN:
                if (result.equals("true") || result.equals("1")) {
                    return true;
                } else {
                    return false;
                }
                
            case INTEGER:
                try {
                    return Integer.valueOf(result);
                } catch (final NumberFormatException e) {
                    return field.getType().getDefaultValue();
                }
                
            case REAL:
                try {
                    return Float.valueOf(result);
                } catch (final NumberFormatException e) {
                    return field.getType().getDefaultValue();
                }
                
            case STRING:
            default:
                return result;
                
        }
    }
}
