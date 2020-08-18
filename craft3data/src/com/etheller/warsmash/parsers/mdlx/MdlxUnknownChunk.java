package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class MdlxUnknownChunk implements MdlxChunk {
	public final short[] chunk;
	public final War3ID tag;

	public MdlxUnknownChunk(final BinaryReader reader, final long size, final War3ID tag)
			throws IOException {
		System.err.println("Loading unknown chunk: " + tag);
		this.chunk = reader.readUInt8Array((int)size);
		this.tag = tag;
	}

	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeWar3ID(stream, this.tag);
		// Below: Byte.BYTES used because it's mean as a UInt8 array. This is
		// not using Short.BYTES, deliberately, despite using a short[] as the
		// type for the array. This is a Java problem that did not exist in the original
		// JavaScript implementation by Ghostwolf
		ParseUtils.writeUInt32(stream, this.chunk.length * Byte.BYTES);
		ParseUtils.writeUInt8Array(stream, this.chunk);
	}

	@Override
	public long getByteLength(final int version) {
		return 8 + (this.chunk.length * Byte.BYTES);
	}
}
