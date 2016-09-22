/**
 * 
 */
package net.wc3c.slk;

/**
 * Indicates an exception when parsing an SLK file or data stream.
 * 
 * @author Deaod
 * 
 */
public class SLKParseException extends RuntimeException {
    
    SLKParseException(final String string) {
        super(string);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 5956365208389996615L;
    
}
