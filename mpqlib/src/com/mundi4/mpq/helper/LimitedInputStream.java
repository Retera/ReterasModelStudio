package com.mundi4.mpq.helper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream {

    private final byte[] singleByteBuf = new byte[1];

    private int pos;
    private final int limit;

    public LimitedInputStream(InputStream in, int limit) {
	super(in);
	this.limit = limit;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if (off < 0 || len < 0 || len > b.length - off) {
	    throw new IndexOutOfBoundsException();
	}

	if (pos >= limit) {
	    return -1;
	}
	if (pos + len > limit) {
	    len = limit - pos;
	}
	if (len <= 0) {
	    return 0;
	}

	int n = in.read(b, off, len);
	pos += n;
	return n;
    }

    @Override
    public int read() throws IOException {
	int n = read(singleByteBuf, 0, 1);
	return (n == -1) ? -1 : singleByteBuf[0] & 0xff;
    }

    @Override
    public long skip(long n) throws IOException {
	if (pos + n > limit) {
	    n = limit - pos;
	}
	int skipped = (int) Math.min(Integer.MAX_VALUE, n);
	skipped = (int) in.skip(skipped);
	pos += skipped;
	return skipped;
    }

    @Override
    public int available() {
	return limit - pos;
    }

    @Override
    public boolean markSupported() {
	return false;
    }

}
