package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MdlxUnknownChunk implements MdlxChunk {
	public short[] chunk;
	public final War3ID tag;

	public MdlxUnknownChunk(final War3ID tag) {
		System.err.println("Loading unknown chunk: " + tag + " (" + Integer.toHexString(tag.getValue()) + ")");
		this.tag = tag;
	}

	public MdlxUnknownChunk(final long size, final War3ID tag) {
		System.err.println("Loading unknown chunk: " + tag + " (" + Integer.toHexString(tag.getValue()) + ")");
		chunk = new short[(int) size];
		this.tag = tag;
	}

	public MdlxUnknownChunk(final BinaryReader reader, final long size, final War3ID tag) {
		System.err.println("Loading unknown chunk: " + tag + " (" + Integer.toHexString(tag.getValue()) + ")");
		chunk = reader.readUInt8Array((int)size);
		this.tag = tag;
	}

	public void readMdx(final BinaryReader reader, final int version) {
		reader.readUInt8Array(chunk);
	}

	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeTag(tag.getValue());
		// Below: Byte.BYTES used because it's mean as a UInt8 array.
		// This is not using Short.BYTES, deliberately, despite using a short[] as the type for the array.
		// This is a Java problem that did not exist in the original JavaScript implementation by Ghostwolf
		writer.writeUInt32(chunk.length * Byte.BYTES);
		writer.writeUInt8Array(chunk);
	}

	public void readMdl(final MdlTokenInputStream stream, final int version) {
		ArrayList<Short> shorts = new ArrayList<>();
		for (final String token : stream.readBlock()) {
			byte[] bytes = token.getBytes(StandardCharsets.UTF_8);
			for (int i = 0; i < bytes.length/4; i++) {
				short temp = 0;
				temp += bytes[i*4 + 0] << 0;
				temp += bytes[i*4 + 1] << 1;
				temp += bytes[i*4 + 2] << 2;
				temp += bytes[i*4 + 3] << 3;
				shorts.add(temp);
			}
		}

		chunk = new short[shorts.size()];
		for (int i = 0; i < chunk.length; i++) {
			chunk[i] = shorts.get(i);
		}
	}

	public void writeMdl(final MdlTokenOutputStream stream, final int version) {


	}

	@Override
	public long getByteLength(final int version) {
		return 8 + (chunk.length * Byte.BYTES);
	}
}
