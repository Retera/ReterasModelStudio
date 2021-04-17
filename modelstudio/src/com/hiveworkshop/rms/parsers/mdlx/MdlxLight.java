package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxLight extends MdlxGenericObject {
	public enum Type {
		OMNIDIRECTIONAL("Omnidirectional"),
		DIRECTIONAL("Directional"),
		AMBIENT("Ambient");

		String token;

		Type(final String token) {
			this.token = token;
		}

		public static Type fromId(final int id) {
			return values()[id];
		}

		@Override
		public String toString() {
			return token;
		}
	}

	public Type type = Type.OMNIDIRECTIONAL;
	public float[] attenuation = new float[2];
	public float[] color = new float[3];
	public float intensity = 0;
	public float[] ambientColor = new float[3];
	public float ambientIntensity = 0;

	public MdlxLight() {
		super(0x200);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		type = Type.fromId(reader.readInt32());
		reader.readFloat32Array(attenuation);
		reader.readFloat32Array(color);
		intensity = reader.readFloat32();
		reader.readFloat32Array(ambientColor);
		ambientIntensity = reader.readFloat32();

		readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeUInt32(type.ordinal());
		writer.writeFloat32Array(attenuation);
		writer.writeFloat32Array(color);
		writer.writeFloat32(intensity);
		writer.writeFloat32Array(ambientColor);
		writer.writeFloat32(ambientIntensity);

		writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
				case MdlUtils.TOKEN_OMNIDIRECTIONAL -> type = Type.OMNIDIRECTIONAL;
				case MdlUtils.TOKEN_DIRECTIONAL -> type = Type.DIRECTIONAL;
				case MdlUtils.TOKEN_AMBIENT -> type = Type.AMBIENT;
				case MdlUtils.TOKEN_STATIC_ATTENUATION_START -> attenuation[0] = stream.readFloat();
				case MdlUtils.TOKEN_ATTENUATION_START -> readTimeline(stream, AnimationMap.KLAS);
				case MdlUtils.TOKEN_STATIC_ATTENUATION_END -> attenuation[1] = stream.readFloat();
				case MdlUtils.TOKEN_ATTENUATION_END -> readTimeline(stream, AnimationMap.KLAE);
				case MdlUtils.TOKEN_STATIC_INTENSITY -> intensity = stream.readFloat();
				case MdlUtils.TOKEN_INTENSITY -> readTimeline(stream, AnimationMap.KLAI);
				case MdlUtils.TOKEN_STATIC_COLOR -> stream.readColor(color);
				case MdlUtils.TOKEN_COLOR -> readTimeline(stream, AnimationMap.KLAC);
				case MdlUtils.TOKEN_STATIC_AMB_INTENSITY -> ambientIntensity = stream.readFloat();
				case MdlUtils.TOKEN_AMB_INTENSITY -> readTimeline(stream, AnimationMap.KLBI);
				case MdlUtils.TOKEN_STATIC_AMB_COLOR -> stream.readColor(ambientColor);
				case MdlUtils.TOKEN_AMB_COLOR -> readTimeline(stream, AnimationMap.KLBC);
				case MdlUtils.TOKEN_VISIBILITY -> readTimeline(stream, AnimationMap.KLAV);
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Light: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_LIGHT, name);
		writeGenericHeader(stream);

		stream.writeFlag(type.toString());

		if (!writeTimeline(stream, AnimationMap.KLAS)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ATTENUATION_START, attenuation[0]);
		}

		if (!writeTimeline(stream, AnimationMap.KLAE)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ATTENUATION_END, attenuation[1]);
		}

		if (!writeTimeline(stream, AnimationMap.KLAI)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_INTENSITY, intensity);
		}

		if (!writeTimeline(stream, AnimationMap.KLAC)) {
			stream.writeColor(MdlUtils.TOKEN_STATIC_COLOR, color);
		}

		if (!writeTimeline(stream, AnimationMap.KLBI)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_AMB_INTENSITY, ambientIntensity);
		}

		if (!writeTimeline(stream, AnimationMap.KLBC)) {
			stream.writeColor(MdlUtils.TOKEN_STATIC_AMB_COLOR, ambientColor);
		}

		writeTimeline(stream, AnimationMap.KLAV);

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 48 + super.getByteLength(version);
	}
}
