package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxSequence implements MdlxBlock {
	public String name = "";
	public long[] interval = new long[2];
	public float moveSpeed = 0;
	public int flags = 0;
	public float rarity = 0;
	public long syncPoint = 0;
	public MdlxExtent extent = new MdlxExtent();
	
	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		name = reader.read(80);
		reader.readUInt32Array(interval);
		moveSpeed = reader.readFloat32();
		flags = reader.readInt32();
		rarity = reader.readFloat32();
		syncPoint = reader.readUInt32();
		extent.readMdx(reader);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeWithNulls(name, 80);
		writer.writeUInt32Array(interval);
		writer.writeFloat32(moveSpeed);
		writer.writeUInt32(flags);
		writer.writeFloat32(rarity);
		writer.writeUInt32(syncPoint);
		extent.writeMdx(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		name = stream.read();

		for (final String token : stream.readBlock()) {
			switch (token) {
				case MdlUtils.TOKEN_INTERVAL -> stream.readIntArray(interval);
				case MdlUtils.TOKEN_NONLOOPING -> flags = 1;
				case MdlUtils.TOKEN_MOVESPEED -> moveSpeed = stream.readFloat();
				case MdlUtils.TOKEN_RARITY -> rarity = stream.readFloat();
				case MdlUtils.TOKEN_MINIMUM_EXTENT -> stream.readFloatArray(extent.min);
				case MdlUtils.TOKEN_MAXIMUM_EXTENT -> stream.readFloatArray(extent.max);
				case MdlUtils.TOKEN_BOUNDSRADIUS -> extent.boundsRadius = stream.readFloat();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Sequence \"" + name + "\": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_ANIM, name);
		stream.writeArrayAttrib(MdlUtils.TOKEN_INTERVAL, interval);

		if (flags == 1) {
			stream.writeFlag(MdlUtils.TOKEN_NONLOOPING);
		}

		if (moveSpeed != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_MOVESPEED, moveSpeed);
		}

		if (rarity != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_RARITY, rarity);
		}

		extent.writeMdl(stream);
		stream.endBlock();
	}
}
