package com.hiveworkshop.rms.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryReader {
    ByteBuffer buffer;

    public BinaryReader(final ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);

        this.buffer = buffer;
    }

    public int remaining() {
        return buffer.remaining();
    }

    public int position() {
        return buffer.position();
    }

    public void position(final int newPosition) {
        buffer.position(newPosition);
    }

    public void move(final int offset) {
        buffer.position(buffer.position() + offset);
    }

    public String read(final int count) {
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < count; i++) {
            byte b = buffer.get();

            if (b != 0) {
                value.append((char) (b & 0xFF));
            }
        }

        return value.toString();
    }

    public String readBytes(final int count) {
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < count; i++) {
            value.append((char) (buffer.get() & 0xFF));
        }

        return value.toString();
    }

    public String readUntilNull() {
        StringBuilder value = new StringBuilder();
        byte b = buffer.get();

        while (b != 0) {
            value.append((char) (b & 0xFF));

            b = buffer.get();
        }

        return value.toString();
    }

    public byte readInt8() {
        return buffer.get();
    }

    public short readInt16() {
        return buffer.getShort();
    }

    public int readInt32() {
        return buffer.getInt();
    }

    public long readInt64() {
        return buffer.getLong();
    }

    public short readUInt8() {
        return (short)Byte.toUnsignedInt(buffer.get());
    }

    public int readUInt16() {
        return Short.toUnsignedInt(buffer.getShort());
    }

    public long readUInt32() {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    public float readFloat32() {
        return buffer.getFloat();
    }

    public double readFloat64() {
        return buffer.getDouble();
    }

    public byte[] readInt8Array(final byte[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readInt8();
        }

        return out;
    }

    public byte[] readInt8Array(final int count) {
        return readInt8Array(new byte[count]);
    }

    public short[] readInt16Array(final short[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readInt16();
        }

        return out;
    }

    public short[] readInt16Array(final int count) {
        return readInt16Array(new short[count]);
    }

    public int[] readInt32Array(final int[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readInt32();
        }

        return out;
    }

    public int[] readInt32Array(final int count) {
        return readInt32Array(new int[count]);
    }

    public long[] readInt64Array(final long[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readInt64();
        }

        return out;
    }

    public long[] readInt64Array(final int count) {
        return readInt64Array(new long[count]);
    }

    public short[] readUInt8Array(final short[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readUInt8();
        }

        return out;
    }

    public short[] readUInt8Array(final int count) {
        return readUInt8Array(new short[count]);
    }

    public int[] readUInt16Array(final int[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readUInt16();
        }

        return out;
    }

    public int[] readUInt16Array(final int count) {
        return readUInt16Array(new int[count]);
    }

    public long[] readUInt32Array(final long[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readUInt32();
        }

        return out;
    }

    public long[] readUInt32Array(final int count) {
        return readUInt32Array(new long[count]);
    }

    public float[] readFloat32Array(final float[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readFloat32();
        }
        return out;
    }

    public float[] readInvFloat32Array(final float[] out) {
        for (int i = out.length-1; i >= 0; i--) {
            out[i] = readFloat32();
        }
        return out;
    }

    public float[] readFloat32Array(final int count) {
        return readFloat32Array(new float[count]);
    }

    public double[] readFloat64Array(final double[] out) {
        for (int i = 0, l = out.length; i < l; i++) {
            out[i] = readFloat64();
        }

        return out;
    }

    public double[] readFloat64Array(final int count) {
        return readFloat64Array(new double[count]);
    }

    public int readTag() {
        return Integer.reverseBytes(readInt32());
    }
}
