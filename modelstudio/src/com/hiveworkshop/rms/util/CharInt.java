package com.hiveworkshop.rms.util;

import java.nio.charset.StandardCharsets;

public final class CharInt {
    private CharInt() {
    }
    
    /**
     * Convert a four character string into an integer.<br>
     * Do not use characters outside the ASCII character set.
     * 
     * @param id the string to convert
     * @return integer representation of the string.
     */
    public static int toInt(final String id) {
        final byte[] bytes = id.getBytes(StandardCharsets.US_ASCII);
        int result = 0;
        if (bytes.length >= 4) {
            result |= (bytes[3] << 0) & 0x000000FF;
            result |= (bytes[2] << 8) & 0x0000FF00;
            result |= (bytes[1] << 16) & 0x00FF0000;
            result |= (bytes[0] << 24) & 0xFF000000;
        } else {
            for (int i = 0; i < bytes.length; i++) {
                result |= (bytes[i] << (24 - (i << 3)));
            }
        }
        
        return result;
    }
    
    /**
     * Convert an integer into a four character string.
     * 
     * @param id the integer to convert
     * @return four character string representing the integer.
     */
    public static String toString(final int id) {

        return String.valueOf((char) ((id >> 24) & 0xFF)) +
                (char) ((id >> 16) & 0xFF) +
                (char) ((id >> 8) & 0xFF) +
                (char) ((id >> 0) & 0xFF);
    }
}
