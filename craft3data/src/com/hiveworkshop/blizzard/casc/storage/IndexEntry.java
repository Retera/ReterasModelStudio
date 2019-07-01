package com.hiveworkshop.blizzard.casc.storage;

import com.hiveworkshop.blizzard.casc.Key;

public class IndexEntry {
	/**
	 * Index encoding key.
	 */
	private Key key;
	
	/**
	 * Logical offset of storage container.
	 */
	private long dataOffset;
	
	/**
	 * Size of storage container.
	 */
	private long fileSize;
	
	public IndexEntry(byte[] key, long dataOffset, long fileSize) {
		this.key = new Key(key);
		this.dataOffset = dataOffset;
		this.fileSize = fileSize;
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("IndexEntry{key=");
		builder.append(key);
		builder.append(", dataOffset=");
		builder.append(dataOffset);
		builder.append(", fileSize=");
		builder.append(fileSize);
		builder.append("}");

		return builder.toString();
	}

	public long getDataOffset() {
		return dataOffset;
	}

	public long getFileSize() {
		return fileSize;
	}
	
	public String getKeyString() {
		return key.toString();
	}
	
	public Key getKey() {
		return key;
	}

	public int compareKey(final Key otherKey) {
		return otherKey.compareTo(key);
	}
}
