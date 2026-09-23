package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParseLog;
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
	// MDX 1200+
	public float shadowIntensity = 0.4f;
	// MDX 1300+ (Forsaken Kingdom); the defaults are the values of the stock 3.0 data
	public int shadowCasting = 0;
	public float shadowCastingStart = 0;
	public float shadowCastingEnd = 0;
	// MDX 1600+
	public float quadraticFalloff = 0.0005f;
	public float linearFalloff = 0;
	public float damping = 0.00001f;

	public MdlxLight() {
		super(0x200);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		type = Type.fromId(reader.readInt32());
		if (MdlxVersion.hasLightShadowCasting(version)) {
			shadowCasting = reader.readInt32();
		}
		reader.readFloat32Array(attenuation);
		reader.readFloat32Array(color);
		intensity = reader.readFloat32();
		reader.readFloat32Array(ambientColor);
		ambientIntensity = reader.readFloat32();
		if (MdlxVersion.hasLightShadowIntensity(version)) {
			shadowIntensity = reader.readFloat32();
		}
		if (MdlxVersion.hasLightShadowCasting(version)) {
			shadowCastingStart = reader.readFloat32();
			shadowCastingEnd = reader.readFloat32();
		}
		if (MdlxVersion.hasLightFalloff(version)) {
			quadraticFalloff = reader.readFloat32();
			linearFalloff = reader.readFloat32();
			damping = reader.readFloat32();
		}

		readTimelines(reader, size - (reader.position() - position));
		// unknown trailing data (e.g. a track from a newer game version): skip it instead of desynchronising
		reader.position((int) (position + size));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeUInt32(type.ordinal());
		if (MdlxVersion.hasLightShadowCasting(version)) {
			writer.writeInt32(shadowCasting);
		}
		writer.writeFloat32Array(attenuation);
		writer.writeFloat32Array(color);
		writer.writeFloat32(intensity);
		writer.writeFloat32Array(ambientColor);
		writer.writeFloat32(ambientIntensity);
		if (MdlxVersion.hasLightShadowIntensity(version)) {
			writer.writeFloat32(shadowIntensity);
		}
		if (MdlxVersion.hasLightShadowCasting(version)) {
			writer.writeFloat32(shadowCastingStart);
			writer.writeFloat32(shadowCastingEnd);
		}
		if (MdlxVersion.hasLightFalloff(version)) {
			writer.writeFloat32(quadraticFalloff);
			writer.writeFloat32(linearFalloff);
			writer.writeFloat32(damping);
		}

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
				case MdlUtils.TOKEN_STATIC_SHADOW_INTENSITY -> shadowIntensity = stream.readFloat();
				case MdlUtils.TOKEN_SHADOW_CASTING -> shadowCasting = 1;
				case MdlUtils.TOKEN_STATIC_SHADOW_CASTING_START -> shadowCastingStart = stream.readFloat();
				case MdlUtils.TOKEN_SHADOW_CASTING_START -> readTimeline(stream, AnimationMap.KLSS);
				case MdlUtils.TOKEN_STATIC_SHADOW_CASTING_END -> shadowCastingEnd = stream.readFloat();
				case MdlUtils.TOKEN_SHADOW_CASTING_END -> readTimeline(stream, AnimationMap.KLSE);
				case MdlUtils.TOKEN_STATIC_QUADRATIC_FALLOFF -> quadraticFalloff = stream.readFloat();
				case MdlUtils.TOKEN_QUADRATIC_FALLOFF -> readTimeline(stream, AnimationMap.KLQF);
				case MdlUtils.TOKEN_STATIC_LINEAR_FALLOFF -> linearFalloff = stream.readFloat();
				case MdlUtils.TOKEN_LINEAR_FALLOFF -> readTimeline(stream, AnimationMap.KLLF);
				case MdlUtils.TOKEN_STATIC_DAMPING -> damping = stream.readFloat();
				case MdlUtils.TOKEN_DAMPING -> readTimeline(stream, AnimationMap.KLDA);
				default -> MdlxParseLog.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Light: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_LIGHT, name);
		writeGenericHeader(stream);

		stream.writeFlag(type.toString());
		if (MdlxVersion.hasLightShadowCasting(version) && shadowCasting != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SHADOW_CASTING);
		}

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

		if (MdlxVersion.hasLightShadowIntensity(version)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_SHADOW_INTENSITY, shadowIntensity);
		}
		if (MdlxVersion.hasLightShadowCasting(version)) {
			if (!writeTimeline(stream, AnimationMap.KLSS)) {
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_SHADOW_CASTING_START, shadowCastingStart);
			}
			if (!writeTimeline(stream, AnimationMap.KLSE)) {
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_SHADOW_CASTING_END, shadowCastingEnd);
			}
		}
		if (MdlxVersion.hasLightFalloff(version)) {
			if (!writeTimeline(stream, AnimationMap.KLQF)) {
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_QUADRATIC_FALLOFF, quadraticFalloff);
			}
			if (!writeTimeline(stream, AnimationMap.KLLF)) {
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LINEAR_FALLOFF, linearFalloff);
			}
			if (!writeTimeline(stream, AnimationMap.KLDA)) {
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_DAMPING, damping);
			}
		}

		writeTimeline(stream, AnimationMap.KLAV);

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = 48;
		if (MdlxVersion.hasLightShadowIntensity(version)) {
			size += 4;
		}
		if (MdlxVersion.hasLightShadowCasting(version)) {
			size += 4 + 8;
		}
		if (MdlxVersion.hasLightFalloff(version)) {
			size += 12;
		}
		return size + super.getByteLength(version);
	}
}
