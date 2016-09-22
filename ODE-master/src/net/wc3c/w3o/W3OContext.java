package net.wc3c.w3o;

interface W3OContext<T> {
    /**
     * Returns the context of this object.
     * 
     * @return the current context of this object.
     */
    T getContext();
    
    /**
     * Changes the context of this object to the one specified.
     * 
     * @param context the new context.
     */
    void setContext(T context);
}
