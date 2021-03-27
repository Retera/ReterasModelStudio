package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxRibbonEmitter extends MdlxGenericObject {
	public float heightAbove = 0;
	public float heightBelow = 0;
	public float alpha = 0;
	public float[] color = new float[3];
	public float lifeSpan = 0;
	public long textureSlot = 0;
	public long emissionRate = 0;
	public long rows = 0;
	public long columns = 0;
	public int materialId = 0;
	public float gravity = 0;

	public MdlxRibbonEmitter() {
		super(0x4000);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		heightAbove = reader.readFloat32();
		heightBelow = reader.readFloat32();
		alpha = reader.readFloat32();
		reader.readFloat32Array(color);
		lifeSpan = reader.readFloat32();
		textureSlot = reader.readUInt32();
		emissionRate = reader.readUInt32();
		rows = reader.readUInt32();
		columns = reader.readUInt32();
		materialId = reader.readInt32();
		gravity = reader.readFloat32();

		readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeFloat32(heightAbove);
		writer.writeFloat32(heightBelow);
		writer.writeFloat32(alpha);
		writer.writeFloat32Array(color);
		writer.writeFloat32(lifeSpan);
		writer.writeUInt32(textureSlot);
		writer.writeUInt32(emissionRate);
		writer.writeUInt32(rows);
		writer.writeUInt32(columns);
		writer.writeInt32(materialId);
		writer.writeFloat32(gravity);

		writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
				case MdlUtils.TOKEN_STATIC_HEIGHT_ABOVE -> heightAbove = stream.readFloat();
				case MdlUtils.TOKEN_HEIGHT_ABOVE -> readTimeline(stream, AnimationMap.KRHA);
				case MdlUtils.TOKEN_STATIC_HEIGHT_BELOW -> heightBelow = stream.readFloat();
				case MdlUtils.TOKEN_HEIGHT_BELOW -> readTimeline(stream, AnimationMap.KRHB);
				case MdlUtils.TOKEN_STATIC_ALPHA -> alpha = stream.readFloat();
				case MdlUtils.TOKEN_ALPHA -> readTimeline(stream, AnimationMap.KRAL);
				case MdlUtils.TOKEN_STATIC_COLOR -> stream.readColor(color);
				case MdlUtils.TOKEN_COLOR -> readTimeline(stream, AnimationMap.KRCO);
				case MdlUtils.TOKEN_STATIC_TEXTURE_SLOT -> textureSlot = stream.readUInt32();
				case MdlUtils.TOKEN_TEXTURE_SLOT -> readTimeline(stream, AnimationMap.KRTX);
				case MdlUtils.TOKEN_VISIBILITY -> readTimeline(stream, AnimationMap.KRVS);
				case MdlUtils.TOKEN_EMISSION_RATE -> emissionRate = stream.readUInt32();
				case MdlUtils.TOKEN_LIFE_SPAN -> lifeSpan = stream.readFloat();
				case MdlUtils.TOKEN_GRAVITY -> gravity = stream.readFloat();
				case MdlUtils.TOKEN_ROWS -> rows = stream.readUInt32();
				case MdlUtils.TOKEN_COLUMNS -> columns = stream.readUInt32();
				case MdlUtils.TOKEN_MATERIAL_ID -> materialId = stream.readInt();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in RibbonEmitter " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_RIBBON_EMITTER, name);
		writeGenericHeader(stream);

		if (!writeTimeline(stream, AnimationMap.KRHA)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_HEIGHT_ABOVE, heightAbove);
		}

		if (!writeTimeline(stream, AnimationMap.KRHB)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_HEIGHT_BELOW, heightBelow);
		}

		if (!writeTimeline(stream, AnimationMap.KRAL)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ALPHA, alpha);
		}

		if (!writeTimeline(stream, AnimationMap.KRCO)) {
			stream.writeColor(MdlUtils.TOKEN_STATIC_COLOR, color);
		}

		if (!writeTimeline(stream, AnimationMap.KRTX)) {
			stream.writeAttribUInt32(MdlUtils.TOKEN_STATIC_TEXTURE_SLOT, textureSlot);
		}

		writeTimeline(stream, AnimationMap.KRVS);

		stream.writeAttribUInt32(MdlUtils.TOKEN_EMISSION_RATE, emissionRate);
		stream.writeFloatAttrib(MdlUtils.TOKEN_LIFE_SPAN, lifeSpan);

		if (gravity != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_GRAVITY, gravity);
		}

		stream.writeAttribUInt32(MdlUtils.TOKEN_ROWS, rows);
		stream.writeAttribUInt32(MdlUtils.TOKEN_COLUMNS, columns);
		stream.writeAttrib(MdlUtils.TOKEN_MATERIAL_ID, materialId);

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 56 + super.getByteLength(version);
	}
}
