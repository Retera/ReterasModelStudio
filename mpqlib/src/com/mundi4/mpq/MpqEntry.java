package com.mundi4.mpq;

public class MpqEntry implements MpqConstants, Cloneable, Comparable<MpqEntry> {

    long position;
    long compressedSize;
    long size;
    int flags;
    String name;

    public MpqEntry(String name) {
	if (name == null) {
	    throw new NullPointerException();
	}
	this.name = name;
    }

    MpqEntry(long position, long compressedSize, long size, int flags) {
	this.position = position;
	this.compressedSize = compressedSize;
	this.size = size;
	this.flags = flags;
    }

    public String getName() {
	return name;
    }

    public long getFilePosition() {
	return position;
    }

    public long getCompressedSize() {
	return compressedSize;
    }

    public long getSize() {
	return size;
    }

    public int getFlags() {
	return flags;
    }

    public boolean isCompressed() {
	return (COMPRESSED & flags) == COMPRESSED;
    }

    public boolean isSingleUnit() {
	return (SINGLE_UNIT & flags) == SINGLE_UNIT;
    }

    public boolean isDummyFile() {
	return (DUMMY_FILE & flags) == DUMMY_FILE;
    }

    public boolean isEncrypted() {
	return (ENCRYPTED & flags) == ENCRYPTED;
    }

    public boolean hasExtra() {
	return (EXISTS & flags) == EXISTS;
    }

    @Override
    public int hashCode() {
	if (name != null) {
	    return name.hashCode();
	}
	return super.hashCode();
    }
    
    @Override
    public int compareTo(MpqEntry o) {
	if (name == null && o.name == null) {
	    return 0;
	} else if (name == null) {
	    return -1;
	} else if (o.name == null) {
	    return 1;
	}
	return name.compareTo(o.name);
    }

}
