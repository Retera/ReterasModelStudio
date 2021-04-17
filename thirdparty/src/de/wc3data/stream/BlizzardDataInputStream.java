package de.wc3data.stream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlizzardDataInputStream
        extends BufferedInputStream {

    private long offset = 0L;

    public BlizzardDataInputStream(final InputStream in) {
        super(in);
    }

    private static int convertToInt(final byte[] b) {
        return b[0] & 0xFF
                | (b[1] & 0xFF) << 8
                | (b[2] & 0xFF) << 16
                | (b[3] & 0xFF) << 24;
    }

    private static int convertNToInt(final byte[] b) {
        int result = 0;
        for (int i = 0; i < b.length; i++) {
            result |= (b[i] & 0xFF) << i * 8;
        }
        return result;
    }

    private static short convertToShort(final byte[] b) {
        int result = 0;

        for (int i = 0; i < b.length; i++) {
            result |= (b[i] & 0xFF) << i * 8;
        }
        return (short) result;
        // return (short)(b[0] << 8 | b[1] & 0xFF);
    }

    /*public long skip(long count)
     throws IOException
     {
     System.out.println("Skipping: " + count);
     throw new Error("BDIS: skip is buggy!");
     }*/
    public long getOffset() {
        return this.offset;
    }

    public byte readByte()
            throws IOException {
        final byte[] b = new byte[1];
        this.offset += 1L;
        read(b);
        return b[0];
    }

    @Override
	public int read(final byte[] b)
            throws IOException {
        this.offset += b.length;
        return super.read(b);
    }

    public void skipUntilZeroInt()
            throws IOException {
        int numZeroBytes = 0;
        for (;;) {
            this.offset += 1L;
            int readByte;
            if ((readByte = read()) == 0) {
                numZeroBytes++;
                if (numZeroBytes != 4) {
                }
            } else {
                if (readByte == -1) {
                    return;
                }
                numZeroBytes = 0;
            }
        }
    }

    public short readShort()
            throws IOException {
        final byte[] b = new byte[2];
        this.offset += 2L;
        read(b, 0, 2);
        return convertToShort(b);
    }

    public int readInt()
            throws IOException {
        final byte[] b = new byte[4];
        this.offset += 4L;
        read(b, 0, 4);
        return convertToInt(b);
    }

    public int readNByteInt(final int numBytes)
            throws IOException {
        final byte[] b = new byte[numBytes];
        this.offset += numBytes;
        read(b, 0, numBytes);
        return convertNToInt(b);
    }

    public boolean readBool()
            throws IOException {
        return readInt() == 1;
    }

    public float readFloat()
            throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public char readChar()
            throws IOException {
        this.offset += 1L;
        return (char) read();
    }

    public char[] readChars(final int charCount)
            throws IOException {
        final char[] c = new char[charCount];
        for (int i = 0; i < charCount; i++) {
            c[i] = readChar();
        }
        return c;
    }

    public String readCharsAsString(final int charCount)
            throws IOException {
        final char[] c = readChars(charCount);
        for (int i = 0; i < charCount; i++) {
            if (c[i] == 0) {
                return String.valueOf(c, 0, i);
            }
        }
        return String.valueOf(c);
    }

    public String readCharsAsStringCheckNull(final int charCount)
            throws IOException {
        final char[] result = readChars(charCount);
        boolean isNull = true;
        for (int i = 0; i < charCount; i++) {
            if (result[i] != 0) {
                isNull = false;
                break;
            }
        }
        if (isNull) {
            return null;
        }
        return String.valueOf(result);
    }

    public String readExportSig()
            throws IOException {
        final char[] c = readChars(4);
        for (int i = 0; i < 4; i++) {
            if (c[i] == 65535) {
                return null;
            }
        }
        return String.valueOf(c);
    }

    public String readString()
            throws IOException {
        final StringBuilder sb = new StringBuilder(16);
        int curVal = read();
        while (curVal != 0) {
            if (curVal == -1) {
                return null;
            }
            sb.append((char) curVal);
            curVal = read();
            this.offset += 1L;
        }
        this.offset += 1L;
        return sb.toString();
    }

    @Override
	public void close()
            throws IOException {
        this.in.close();
    }
}