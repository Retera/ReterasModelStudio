package com.hiveworkshop.rms.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryWriter {
    public ByteBuffer buffer;

    public BinaryWriter(final int capacity) {
        buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
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

    public void write(final String value) {
        writeInt8Array(value.getBytes());
    }

    public void writeWithNulls(final String value, final int length) {
        final byte[] bytes = value.getBytes();
        final int nulls = length - bytes.length;

        writeInt8Array(bytes);

        if (nulls > 0) {
            for (int i = 0; i < nulls; i++) {
                writeInt8((byte)0);
            }
        }  
    }

    public void writeInt8(final byte value) {
        buffer.put(value);
    }

    public void writeInt16(final short value) {
        buffer.putShort(value);
    }

    public void writeInt32(final int value) {
        buffer.putInt(value);
    }

    public void writeInt64(final long value) {
        buffer.putLong(value);
    }

    public void writeUInt8(final short value) {
        buffer.put((byte)value);
    }

    public void writeUInt16(final int value) {
        buffer.putShort((short)value);
    }

    public void writeUInt32(final long value) {
        buffer.putInt((int)value);
    }

    public void writeFloat32(final float value) {
        buffer.putFloat(value);
    }

    public void writeFloat64(final double value) {
        buffer.putDouble(value);
    }

    public void writeInt8Array(final byte[] values) {
        for (final byte value : values) {
            writeInt8(value);
        }
    }

    public void writeInt16Array(final short[] values) {
        for (final short value : values) {
            writeInt16(value);
        }
    }

    public void writeInt32Array(final int[] values) {
        for (final int value : values) {
            writeInt32(value);
        }
    }

    public void writeInt64Array(final long[] values) {
        for (final long value : values) {
            writeInt64(value);
        }
    }

    public void writeUInt8Array(final short[] values) {
        for (final short value : values) {
            writeUInt8(value);
        }
    }

    public void writeUInt16Array(final int[] values) {
        for (final int value : values) {
            writeUInt16(value);
        }
    }

    public void writeUInt32Array(final long[] values) {
        for (final long value : values) {
            writeUInt32(value);
        }
    }

    public void writeFloat32Array(final float[] values) {
        for (final float value : values) {
            writeFloat32(value);
        }
    }

    public void writeFloat64Array(final double[] values) {
        for (final double value : values) {
            writeFloat64(value);
        }
    }

    public void writeTag(final int tag) {
        writeInt32(Integer.reverseBytes(tag));
    }
}
