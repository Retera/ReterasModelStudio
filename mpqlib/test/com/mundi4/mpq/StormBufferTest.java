package com.mundi4.mpq;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StormBufferTest {

    private static final int BUFFER_SIZE = 0x500;
    private int[] stormBuffer;

    @Before
    public void setUp() throws IOException {
        stormBuffer = new int[BUFFER_SIZE];
        InputStream is = getClass().getResourceAsStream("StormBuffer.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                assertTrue(i < BUFFER_SIZE);
                assertTrue(line.length() > 0);
                long val = Long.parseLong(line, 16);
                int ival = (int) (val & 0xffffffff);
                stormBuffer[i++] = ival;
            }
            assertEquals(BUFFER_SIZE, i);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testGet() {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            assertEquals(stormBuffer[i], StormBuffer.get(i));
        }
    }

}
