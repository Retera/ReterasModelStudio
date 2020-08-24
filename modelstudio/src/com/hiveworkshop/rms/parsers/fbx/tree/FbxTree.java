package com.hiveworkshop.rms.parsers.fbx.tree;

import com.hiveworkshop.rms.util.BinaryReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.InflaterInputStream;

public class FbxTree {
    static private final String FBX_BINARY_MAGIC = "Kaydara FBX Binary  \0";
    static private final String FBX_32BITS_SENTINAL = "\0".repeat(13);
    static private final String FBX_64BITS_SENTINAL = "\0".repeat(25);
    
    public int version = 0;
    public FbxTreeNode rootNode = new FbxTreeNode();

    public FbxTree() {

    }

    public FbxTree(final ByteBuffer buffer) throws IOException {
        if (buffer != null) {
            load(buffer);
        }
    }

    public void load(final ByteBuffer buffer) throws IOException {
        final BinaryReader reader = new BinaryReader(buffer);

        if (!reader.readBytes(FBX_BINARY_MAGIC.length()).equals(FBX_BINARY_MAGIC)) {
            throw new RuntimeException("Bad FBX Binary magic");
        }
        
        reader.move(1); // Always 0x1A.
        reader.move(1); // Always 0x00.
        
        version = reader.readInt32();

        final boolean is64bits = version > 7400;

        FbxTreeNode node;

        while ((node = readNode(reader, is64bits)) != null) {
            rootNode.children.add(node);
        }
    }

    static private FbxTreeNode readNode(final BinaryReader reader, final boolean is64bits) throws IOException {
        final long endOffset;
        
        if (is64bits) {
            endOffset = reader.readInt64();
        } else {
            endOffset = reader.readInt32();
        }

        if (endOffset == 0) {
            return null;
        }
        
        final long propCount;
        final long propLength; // Not sure what this is used for.

        if (is64bits) {
            propCount = reader.readInt64();
            propLength = reader.readInt64();
        } else {
            propCount = reader.readInt32();
            propLength = reader.readInt32();
        }

        final short nameLen = reader.readUInt8();
        
        final FbxTreeNode node = new FbxTreeNode();

        node.name = reader.readBytes(nameLen);

        for (int i = 0; i < propCount; i++) {
            readProperty(node, reader);
        }

        if (reader.position() < endOffset) {
            final String sentinal;

            if (is64bits) {
                sentinal = FBX_64BITS_SENTINAL;
            } else {
                sentinal = FBX_32BITS_SENTINAL;
            }

            while (reader.position() < endOffset - sentinal.length()) {
                node.children.add(readNode(reader, is64bits));
            }

            if (!reader.readBytes(sentinal.length()).equals(sentinal)) {
                throw new RuntimeException("Failed to read the nested block NULL entry");
            }
        }

        if (reader.position() != endOffset) {
            throw new RuntimeException("Scope length not reached, something went wrong");
        }

        return node;
    }

    static private void readProperty(final FbxTreeNode node, final BinaryReader reader) throws IOException {
        final FbxTreeProperty<?> property;
        final char type = (char)(reader.readInt8() & 0xFF);

        switch(type) {
            case 'Y':
                property = new FbxTreeProperty<>(type, Short.valueOf(reader.readInt16())); // short
                break;
            case 'C':
                property = new FbxTreeProperty<>(type, Byte.valueOf(reader.readInt8())); // bool
                break;
            case 'I':
                property = new FbxTreeProperty<>(type, Integer.valueOf(reader.readInt32())); // int
                break;
            case 'F':
                property = new FbxTreeProperty<>(type, Float.valueOf(reader.readFloat32())); // float
                break;
            case 'D':
                property = new FbxTreeProperty<>(type, Double.valueOf(reader.readFloat64())); // double
                break;
            case 'L':
                property = new FbxTreeProperty<>(type, Long.valueOf(reader.readInt64())); // long
                break;
            case 'R':
                property = new FbxTreeProperty<>(type, reader.readBytes(reader.readInt32())); // binary
                break;
            case 'S':
                property = new FbxTreeProperty<>(type, reader.readBytes(reader.readInt32())); // string
                break;
            case 'f':
                property = new FbxTreeProperty<>(type, readFloatArray(reader)); // float[]
                break;
            case 'i':
                property = new FbxTreeProperty<>(type, readIntArray(reader)); // int[]
                break;
            case 'd':
                property = new FbxTreeProperty<>(type, readDoubleArray(reader)); // double[]
                break;
            case 'l':
                property = new FbxTreeProperty<>(type, readLongArray(reader)); // long[]
                break;
            case 'b':
                property = new FbxTreeProperty<>(type, readArray(reader)); // byte[]
                break;
            case 'c':
                property = new FbxTreeProperty<>(type, readArray(reader)); // byte[]
                break;
            default:
                throw new RuntimeException("Unknown property type: " + type);
        }

        node.properties.add(property);
    }

    static private ByteBuffer readArray(final BinaryReader reader) throws IOException {
        final int length = reader.readInt32();
        final int encoding = reader.readInt32();
        final int compLen = reader.readInt32();
        byte[] bytes = reader.readInt8Array(compLen);

        if (encoding == 1) {
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            final InflaterInputStream inflateStream = new InflaterInputStream(byteStream);

            bytes = inflateStream.readAllBytes();
        }

        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    static private int[] readIntArray(final BinaryReader reader) throws IOException {
        final ByteBuffer buffer = readArray(reader);
        final int[] array = new int[buffer.capacity() / 4];
        
        buffer.asIntBuffer().get(array);

        return array;
    }

    static private float[] readFloatArray(final BinaryReader reader) throws IOException {
        final ByteBuffer buffer = readArray(reader);
        final float[] array = new float[buffer.capacity() / 4];
        
        buffer.asFloatBuffer().get(array);

        return array;
    }

    static private long[] readLongArray(final BinaryReader reader) throws IOException {
        final ByteBuffer buffer = readArray(reader);
        final long[] array = new long[buffer.capacity() / 8];
        
        buffer.asLongBuffer().get(array);

        return array;
    }

    static private double[] readDoubleArray(final BinaryReader reader) throws IOException {
        final ByteBuffer buffer = readArray(reader);
        final double[] array = new double[buffer.capacity() / 8];
        
        buffer.asDoubleBuffer().get(array);

        return array;
    }
}
