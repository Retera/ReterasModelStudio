package de.wc3data.stream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlizzardDataOutputStream
        extends BufferedOutputStream {

    public BlizzardDataOutputStream(File file)
            throws FileNotFoundException {
        super(new FileOutputStream(file));
    }

    public BlizzardDataOutputStream(File file, boolean append)
            throws FileNotFoundException {
        super(new FileOutputStream(file, append));
    }

    static byte[] convertIntToByteArray(int i) {
        byte[] result = new byte[4];
        long l = i >= 0 ? i : 4294967296L + i;
        for (int j = 0; j < 4; j++) {
            result[j] = ((byte) (int) (l % 256L));
            l /= 256L;
        }
        return result;
    }

    static byte[] convertIntToNByteArray(int i, int numBytes) {
        byte[] result = new byte[numBytes];
        long l = i >= 0 ? i : 4294967296L + i;
        for (int j = 0; j < numBytes; j++) {
            result[j] = ((byte) (int) (l % 256L));
            l /= 256L;
        }
        return result;
    }

    public void writeChars(char[] toWrite)
            throws IOException {
        byte[] b = new byte[toWrite.length];
        for (int i = 0; i < toWrite.length; i++) {
            b[i] = ((byte) toWrite[i]);
        }
        write(b);
    }

    public void writeInt(int toWrite)
            throws IOException {
        write(convertIntToByteArray(toWrite));
    }

    public void writeNByteInt(int toWrite, int numBytes)
            throws IOException {
        write(convertIntToNByteArray(toWrite, numBytes));
    }

    public void writeBool(boolean toWrite)
            throws IOException {
        writeInt(toWrite ? 1 : 0);
    }

    public void writeByte(int toWrite)
            throws IOException {
        write(new byte[]{(byte) toWrite});
    }

    public void writeFloat(float toWrite)
            throws IOException {
        writeInt(Float.floatToIntBits(toWrite));
    }

    public void writeFourByteString(String toWrite)
            throws IOException {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            if ((toWrite != null) && (i < toWrite.length())) {
                result[i] = ((byte) toWrite.charAt(i));
            } else {
                result[i] = 0;
            }
        }
        write(result);
    }

    public void writeNByteString(String toWrite, int charCount)
            throws IOException {
        byte[] result = new byte[charCount];
        for (int i = 0; i < charCount; i++) {
            if ((toWrite != null) && (i < toWrite.length())) {
                result[i] = ((byte) toWrite.charAt(i));
            } else {
                result[i] = 0;
            }
        }
        write(result);
    }

    public void writeString(String toWrite)
            throws IOException {
        int length;
        if (toWrite == null) {
            length = 0;
        } else {
            length = toWrite.length();
        }
        byte[] result = new byte[length + 1];
        for (int i = 0; i < length; i++) {
            result[i] = ((byte) toWrite.charAt(i));
        }
        result[length] = 0;
        write(result);
    }
}
