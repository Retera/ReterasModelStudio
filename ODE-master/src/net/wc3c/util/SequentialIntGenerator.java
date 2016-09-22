package net.wc3c.util;

public class SequentialIntGenerator {
    private int index;
    private int count = 0;
    
    public SequentialIntGenerator(final int startingIndex) {
        index = startingIndex;
    }
    
    public SequentialIntGenerator() {
        this(0);
    }
    
    public int next() {
        final int result = index;
        index += 1;
        count += 1;
        return result;
    }
    
    public int getGeneratedCount() {
        return count;
    }
}
