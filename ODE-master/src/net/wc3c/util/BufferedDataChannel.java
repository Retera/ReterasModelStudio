package net.wc3c.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author Deaod <deaod@deaod.de>
 * 
 */
public final class BufferedDataChannel {
    private static final int  DEFAULT_BUFFER_SIZE = 0x1000;
    
    private final ByteChannel channel;
    private final ByteBuffer  buffer;
    private final ByteOrder   order;
    
    public BufferedDataChannel(final ByteChannel channel) throws IOException {
        this(channel, DEFAULT_BUFFER_SIZE);
    }
    
    public BufferedDataChannel(final ByteChannel channel, final int bufferSize) throws IOException {
        this(channel, bufferSize, ByteOrder.nativeOrder());
    }
    
    public BufferedDataChannel(final ByteChannel channel, final ByteOrder order) throws IOException {
        this(channel, DEFAULT_BUFFER_SIZE, order);
    }
    
    public BufferedDataChannel(final ByteChannel channel, final int bufferSize, final ByteOrder order)
            throws IOException {
        this.channel = channel;
        buffer = ByteBuffer.allocate(bufferSize);
        this.order = order;
        
        buffer.clear();
        this.channel.read(buffer);
        buffer.flip();
    }
    
    /**
     * Returns the default byte order with which to read from the array. After constructing an instance of DataReader,
     * the default byte order is the native one.
     * 
     * @return The default byte order with which to read from the array.
     * @see #readShort()
     * @see #readInt()
     * @see #readLong()
     * @see #readFloat()
     * @see #readDouble()
     * @see ByteOrder
     */
    public final ByteOrder getByteOrder() {
        return order;
    }
    
    //
    
    /**
     * Reads an 8-bit integer from the input stream. Blocks until the next byte is available.
     * 
     * @return The 8-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final byte readByte() throws IOException {
        if (buffer.hasRemaining()) {
            return buffer.get();
        } else {
            buffer.clear();
            if (channel.read(buffer) == -1) {
                throw new IOException("Unexpected end of channel");
            }
            buffer.flip();
            
            return readByte();
        }
    }
    
    //
    
    /**
     * Reads a 16-bit integer from the input stream with Little Endian byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 16-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final short readShortLE() throws IOException {
        short result = 0;
        result |= (readByte() << 0) & 0x00FF;
        result |= (readByte() << 8) & 0xFF00;
        return result;
    }
    
    /**
     * Reads a 16-bit integer from the input stream with Big Endian byte order. Blocks until enough bytes have been read
     * from the input stream.
     * 
     * @return The 16-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final short readShortBE() throws IOException {
        short result = 0;
        result |= (readByte() << 8) & 0xFF00;
        result |= (readByte() << 0) & 0x00FF;
        return result;
    }
    
    /**
     * Reads a 16-bit integer from the input stream with the default byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 16-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     * @see #getByteOrder()
     * @see #setByteOrder(ByteOrder)
     */
    public final short readShort() throws IOException {
        if (order == ByteOrder.BIG_ENDIAN) {
            return readShortBE();
        } else {
            return readShortLE();
        }
    }
    
    //
    
    /**
     * Reads a 32-bit integer from the input stream with Little Endian byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 32-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final int readIntLE() throws IOException {
        int result = 0;
        result |= (readShortLE() << 0) & 0x0000FFFF;
        result |= (readShortLE() << 16) & 0xFFFF0000;
        return result;
    }
    
    /**
     * Reads a 32-bit integer from the input stream with Big Endian byte order. Blocks until enough bytes have been read
     * from the input stream.
     * 
     * @return The 32-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final int readIntBE() throws IOException {
        int result = 0;
        result |= (readShortBE() << 16) & 0xFFFF0000;
        result |= (readShortBE() << 0) & 0x0000FFFF;
        return result;
    }
    
    /**
     * Reads a 32-bit integer from the input stream with the default byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 32-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     * @see #getByteOrder()
     * @see #setByteOrder(ByteOrder)
     */
    public final int readInt() throws IOException {
        if (order == ByteOrder.BIG_ENDIAN) {
            return readIntBE();
        } else {
            return readIntLE();
        }
    }
    
    //
    
    /**
     * Reads a 64-bit integer from the input stream with Little Endian byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 64-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final long readLongLE() throws IOException {
        long result = 0;
        result |= ((long) readIntLE() << 0L) & 0x00000000FFFFFFFFL;
        result |= ((long) readIntLE() << 32L) & 0xFFFFFFFF00000000L;
        return result;
    }
    
    /**
     * Reads a 64-bit integer from the input stream with Big Endian byte order. Blocks until enough bytes have been read
     * from the input stream.
     * 
     * @return The 64-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     */
    public final long readLongBE() throws IOException {
        long result = 0;
        result |= ((long) readIntBE() << 32L) & 0xFFFFFFFF00000000L;
        result |= ((long) readIntBE() << 0L) & 0x00000000FFFFFFFFL;
        return result;
    }
    
    /**
     * Reads a 64-bit integer from the input stream with the default byte order. Blocks until enough bytes have been
     * read from the input stream.
     * 
     * @return The 64-bit integer value read from the input stream.
     * @throws IOException if the underlying input stream throws an exception or the end of the input stream is reached
     *             unexpectedly.
     * @see #getByteOrder()
     * @see #setByteOrder(ByteOrder)
     */
    public final long readLong() throws IOException {
        if (order == ByteOrder.BIG_ENDIAN) {
            return readLongBE();
        } else {
            return readLongLE();
        }
    }
    
    //
    
    public final float readFloatLE() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }
    
    public final float readFloatBE() throws IOException {
        return Float.intBitsToFloat(readIntBE());
    }
    
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    //
    
    public final double readDoubleLE() throws IOException {
        return Double.longBitsToDouble(readLongLE());
    }
    
    public final double readDoubleBE() throws IOException {
        return Double.longBitsToDouble(readLongBE());
    }
    
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    
    //
    
    public final int read(final byte[] destination, final int offset, final int length) throws IOException {
        if (buffer.remaining() > 0) {
            final int actual = Math.min(length, buffer.remaining());
            buffer.get(destination, offset, actual);
            return actual;
        } else {
            buffer.clear();
            channel.read(buffer);
            buffer.flip();
            
            return read(destination, offset, length);
        }
    }
    
    public final int read(final byte[] destination) throws IOException {
        return read(destination, 0, destination.length);
    }
    
    //
    
    public final String readUTF8String() throws IOException {
        byte[] rawData = new byte[1024];
        int index = 0;
        for (byte cur = readByte(); cur > 0; cur = readByte()) {
            if (index >= rawData.length) {
                final byte[] newRawData = new byte[rawData.length * 2];
                System.arraycopy(rawData, 0, newRawData, 0, rawData.length);
                rawData = newRawData;
            }
            rawData[index] = cur;
            
            index += 1;
        }
        
        return new String(rawData, 0, index, StandardCharsets.UTF_8);
    }
    
    public final String readUTF8String(final int length) throws IOException {
        final byte[] rawData = new byte[length];
        for (int index = 0; index < length; index += 1) {
            rawData[index] = readByte();
        }
        
        return new String(rawData, StandardCharsets.UTF_8);
    }
}
