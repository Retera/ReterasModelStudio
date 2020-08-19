package com.etheller.warsmash.parsers.mdlx;

import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class MdlxUnknownChunk implements MdlxChunk {
	public final short[] chunk;
	public final War3ID tag;

	public MdlxUnknownChunk(final BinaryReader reader, final long size, final War3ID tag) {
		System.err.println("Loading unknown chunk: " + tag);
		this.chunk = reader.readUInt8Array((int)size);
		this.tag = tag;
	}

	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeTag(tag.getValue());
		// Below: Byte.BYTES used because it's mean as a UInt8 array. This is
		// not using Short.BYTES, deliberately, despite using a short[] as the
		// type for the array. This is a Java problem that did not exist in the original
		// JavaScript implementation by Ghostwolf
		writer.writeUInt32(chunk.length * Byte.BYTES);
		writer.writeUInt8Array(chunk);
	}

	public long getByteLength(final int version) {
		return 8 + (this.chunk.length * Byte.BYTES);
	}
}
