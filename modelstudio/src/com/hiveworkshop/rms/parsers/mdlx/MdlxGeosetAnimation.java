package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

import java.util.Iterator;

public class MdlxGeosetAnimation extends MdlxAnimatedObject {
	public float alpha = 1;
	public int flags = 0;
	public float[] color = { 1, 1, 1 };
	public int geosetId = -1;

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final long size = reader.readUInt32();

		alpha = reader.readFloat32();
		flags = reader.readInt32();
		reader.readFloat32Array(color);
		geosetId = reader.readInt32();

		readTimelines(reader, size - 28);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));
		writer.writeFloat32(alpha);
		writer.writeInt32(flags);
		writer.writeFloat32Array(color);
		writer.writeInt32(geosetId);

		writeTimelines(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		final Iterator<String> blockIterator = readAnimatedBlock(stream);
		while (blockIterator.hasNext()) {
			final String token = blockIterator.next();
			switch (token) {
				case MdlUtils.TOKEN_DROP_SHADOW -> flags |= 0x1;
				case MdlUtils.TOKEN_STATIC_ALPHA -> alpha = stream.readFloat();
				case MdlUtils.TOKEN_ALPHA -> readTimeline(stream, AnimationMap.KGAO);
				case MdlUtils.TOKEN_STATIC_COLOR -> {
					flags |= 0x2;
					stream.readColor(color);
				}
				case MdlUtils.TOKEN_COLOR -> {
					flags |= 0x2;
					readTimeline(stream, AnimationMap.KGAC);
				}
				case MdlUtils.TOKEN_GEOSETID -> geosetId = stream.readInt();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in GeosetAnimation: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_GEOSETANIM);

		if ((flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_DROP_SHADOW);
		}

		if (!writeTimeline(stream, AnimationMap.KGAO)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ALPHA, alpha);
		}

		if ((flags & 0x2) != 0) {
			if (!writeTimeline(stream, AnimationMap.KGAC)
					&& ((color[0] != 0) || (color[1] != 0) || (color[2] != 0))) {
				stream.writeColor(MdlUtils.TOKEN_STATIC_COLOR, color);
			}
		}

		if (geosetId != -1) { // TODO Retera added -1 check here, why wasn't it there before in JS???
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETID, geosetId);
		}

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 28 + super.getByteLength(version);
	}
}
