package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class MdlxMaterial implements MdlxBlock, MdlxChunk {
	public static final War3ID LAYS = War3ID.fromString("LAYS");
	public int priorityPlane = 0;
	public int flags;
	/** 
	 * @since 900
	 */
	public String shader = "";
	public final List<MdlxLayer> layers = new ArrayList<>();

	/**
	 * Restricts us to only be able to parse models on one thread at a time, in
	 * return for high performance.
	 */
	private static final byte[] SHADER_BYTES_HEAP = new byte[80];

	@Override
	public void readMdx(final LittleEndianDataInputStream stream, final int version) throws IOException {
		ParseUtils.readUInt32(stream); // Don't care about the size

		this.priorityPlane = stream.readInt();// ParseUtils.readUInt32(stream);
		this.flags = stream.readInt();// ParseUtils.readUInt32(stream);

		if (version > 800) {
			this.shader = ParseUtils.readString(stream, SHADER_BYTES_HEAP);
		}

		stream.readInt(); // skip LAYS

		final long layerCount = ParseUtils.readUInt32(stream);
		for (int i = 0; i < layerCount; i++) {
			final MdlxLayer layer = new MdlxLayer();
			layer.readMdx(stream, version);
			this.layers.add(layer);
		}
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, getByteLength(version));
		stream.writeInt(this.priorityPlane); // was UInt32 in JS, but I *really* thought I used -1 in a past model
		stream.writeInt(this.flags); // UInt32 in JS

		if (version > 800) {
			final byte[] bytes = this.shader.getBytes(ParseUtils.UTF8);
			stream.write(bytes);
			for (int i = 0; i < (SHADER_BYTES_HEAP.length - bytes.length); i++) {
				stream.write((byte) 0);
			}
		}

		ParseUtils.writeWar3ID(stream, LAYS);
		ParseUtils.writeUInt32(stream, this.layers.size());

		for (final MdlxLayer layer : this.layers) {
			layer.writeMdx(stream, version);
		}
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_CONSTANT_COLOR:
				this.flags |= 0x1;
				break;
			case MdlUtils.TOKEN_SORT_PRIMS_NEAR_Z:
				this.flags |= 0x8;
				break;
			case MdlUtils.TOKEN_SORT_PRIMS_FAR_Z:
				this.flags |= 0x10;
				break;
			case MdlUtils.TOKEN_FULL_RESOLUTION:
				this.flags |= 0x20;
				break;
			case MdlUtils.TOKEN_PRIORITY_PLANE:
				this.priorityPlane = stream.readInt();
				break;
			case "Shader":
				this.shader = stream.read();
				break;
			case MdlUtils.TOKEN_LAYER: {
				final MdlxLayer layer = new MdlxLayer();
				layer.readMdl(stream, version);
				this.layers.add(layer);
				break;
			}
			default:
				throw new IllegalStateException("Unknown token in Material: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startBlock(MdlUtils.TOKEN_MATERIAL);

		if ((this.flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_CONSTANT_COLOR);
		}

		if ((this.flags & 0x8) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SORT_PRIMS_NEAR_Z);
		}

		if ((this.flags & 0x10) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SORT_PRIMS_FAR_Z);
		}

		if ((this.flags & 0x20) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_FULL_RESOLUTION);
		}

		if (this.priorityPlane != 0) {
			stream.writeAttrib(MdlUtils.TOKEN_PRIORITY_PLANE, this.priorityPlane);
		}

		if (version > 800) {
			stream.writeStringAttrib("Shader", shader);
		}
		
		for (final MdlxLayer layer : this.layers) {
			layer.writeMdl(stream, version);
		}

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = 20;

		if (version > 800) {
			size += 80;
		}
		
		for (final MdlxLayer layer : this.layers) {
			size += layer.getByteLength(version);
		}

		return size;
	}
}
