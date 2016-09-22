package com.mundi4.mpq.helper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.mundi4.mpq.StormBuffer;

public class DecryptorInputStream extends FilterInputStream {

    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    private static final int SKIP_BUFFER_SIZE = 512;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;

    private static int defaultBufferSize = 512;

    /**
     * The internal buffer array where the data is stored. When necessary, it
     * may be replaced by another array of a different size.
     */
    protected volatile byte buf[];

    /**
     * Atomic updater to provide compareAndSet for buf. This is necessary
     * because closes can be asynchronous. We use nullness of buf[] as primary
     * indicator that this stream is closed. (The "in" field is also nulled out
     * on close.)
     */
    private static final AtomicReferenceFieldUpdater<DecryptorInputStream, byte[]> bufUpdater = AtomicReferenceFieldUpdater
	    .newUpdater(DecryptorInputStream.class, byte[].class, "buf");

    /**
     * The index one greater than the index of the last valid byte in the
     * buffer. This value is always in the range <code>0</code> through
     * <code>buf.length</code>; elements <code>buf[0]</code> through
     * <code>buf[count-1]
     * </code>contain buffered input data obtained from the underlying input
     * stream.
     */
    protected int count;

    /**
     * The current position in the buffer. This is the index of the next
     * character to be read from the <code>buf</code> array.
     * <p>
     * This value is always in the range <code>0</code> through
     * <code>count</code>. If it is less than <code>count</code>, then
     * <code>buf[pos]</code> is the next byte to be supplied as input; if it is
     * equal to <code>count</code>, then the next <code>read</code> or
     * <code>skip</code> operation will require more bytes to be read from the
     * contained input stream.
     */
    protected int pos;

    private int seed1;

    private int seed2 = 0xeeeeeeee;

    public DecryptorInputStream(InputStream in, int seed) {
	this(in, seed, defaultBufferSize);
    }

    public DecryptorInputStream(InputStream in, int seed, int size) {
	super(in);
	if (size <= 0) {
	    throw new IllegalArgumentException("Buffer size <= 0");
	}
	buf = new byte[size];
	seed1 = seed;
    }

    private InputStream getInIfOpen() throws IOException {
	InputStream input = in;
	if (input == null)
	    throw new IOException("Stream closed");
	return input;
    }

    private byte[] getBufIfOpen() throws IOException {
	byte[] buffer = buf;
	if (buffer == null)
	    throw new IOException("Stream closed");
	return buffer;
    }

    private void fill() throws IOException {
	byte[] buffer = getBufIfOpen();
	pos = 0;
	count = pos;
	int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
	if (n > 0) {
	    count = n;
	    decrypt();
	}
    }

    private void decrypt() {
	for (int i = 0; i < count - 3; i += 4) {
	    seed2 += StormBuffer.get(0x400 + (seed1 & 0xff));
	    // seed2 += MpqUtils.STORM_BUFFER[0x400 + (seed1 & 0xff)];
	    int result = MpqUtils.toInt(buf, i);
	    result ^= seed1 + seed2;

	    seed1 = ((~seed1 << 21) + 0x11111111) | (seed1 >>> 11);
	    seed2 = result + seed2 + (seed2 << 5) + 3;

	    buf[i + 0] = ((byte) (result & 0xff));
	    buf[i + 1] = ((byte) ((result >> 8) & 0xff));
	    buf[i + 2] = ((byte) ((result >> 16) & 0xff));
	    buf[i + 3] = ((byte) ((result >> 24) & 0xff));
	}
    }

    public synchronized int read() throws IOException {
	if (pos >= count) {
	    fill();
	    if (pos >= count)
		return -1;
	}
	return getBufIfOpen()[pos++] & 0xff;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
	int avail = count - pos;
	if (avail <= 0) {
	    fill();
	    avail = count - pos;
	    if (avail <= 0)
		return -1;
	}
	int cnt = (avail < len) ? avail : len;
	System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
	pos += cnt;
	return cnt;
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
	getBufIfOpen(); // Check for closed stream
	if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	int n = 0;
	for (;;) {
	    int nread = read1(b, off + n, len - n);
	    if (nread <= 0)
		return (n == 0) ? nread : n;
	    n += nread;
	    if (n >= len)
		return n;
	    // if not closed but no bytes available, return
	    InputStream input = in;
	    if (input != null && input.available() <= 0)
		return n;
	}
    }

    public synchronized long skip(long n) throws IOException {
	getBufIfOpen(); // Check for closed stream
	if (n <= 0) {
	    return 0;
	}
	long remaining = n;
	int nr;
	if (skipBuffer == null)
	    skipBuffer = new byte[SKIP_BUFFER_SIZE];

	byte[] localSkipBuffer = skipBuffer;

	while (remaining > 0) {
	    nr = read(localSkipBuffer, 0, (int) Math.min(SKIP_BUFFER_SIZE,
		    remaining));
	    if (nr < 0) {
		break;
	    }
	    remaining -= nr;
	}

	return n - remaining;
    }

    public synchronized int available() throws IOException {
	return getInIfOpen().available() + (count - pos);
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
	return false;
    }

    public void close() throws IOException {
	byte[] buffer;
	while ((buffer = buf) != null) {
	    if (bufUpdater.compareAndSet(this, buffer, null)) {
		InputStream input = in;
		in = null;
		if (input != null)
		    input.close();
		return;
	    }
	    // Else retry in case a new buf was CASed in fill()
	}
    }
}
