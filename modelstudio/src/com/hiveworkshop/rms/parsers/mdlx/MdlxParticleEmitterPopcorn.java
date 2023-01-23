package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxParticleEmitterPopcorn extends MdlxGenericObject {
	public float lifeSpan = 0;
	public float emissionRate = 0;
	public float speed = 0;
	public float[] color = new float[]{1, 1, 1};
	public float alpha = 0;
	public int replaceableId = 0;
	public String path = "";
	public String animationVisiblityGuide = "";

	public MdlxParticleEmitterPopcorn() {
		super(0);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		lifeSpan = reader.readFloat32();
		emissionRate = reader.readFloat32();
		speed = reader.readFloat32();
		reader.readFloat32Array(color);
		alpha = reader.readFloat32();
		replaceableId = reader.readInt32();
		path = reader.read(260);
		animationVisiblityGuide = reader.read(260);

		readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeFloat32(lifeSpan);
		writer.writeFloat32(emissionRate);
		writer.writeFloat32(speed);
		writer.writeFloat32Array(color);
		writer.writeFloat32(alpha);
		writer.writeInt32(replaceableId);
		writer.writeWithNulls(path, 260);
		writer.writeWithNulls(animationVisiblityGuide, 260);

		writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
				case MdlUtils.TOKEN_SORT_PRIMS_FAR_Z -> flags |= 0x10000;
				case MdlUtils.TOKEN_UNSHADED -> flags |= 0x8000;
				case MdlUtils.TOKEN_UNFOGGED -> flags |= 0x40000;
				case MdlUtils.TOKEN_STATIC_LIFE_SPAN -> lifeSpan = stream.readFloat();
				case MdlUtils.TOKEN_LIFE_SPAN -> readTimeline(stream, AnimationMap.KPPL);
				case MdlUtils.TOKEN_STATIC_EMISSION_RATE -> emissionRate = stream.readFloat();
				case MdlUtils.TOKEN_EMISSION_RATE -> readTimeline(stream, AnimationMap.KPPE);
				case MdlUtils.TOKEN_STATIC_SPEED -> speed = stream.readFloat();
				case MdlUtils.TOKEN_SPEED -> readTimeline(stream, AnimationMap.KPPS);
				case MdlUtils.TOKEN_STATIC_COLOR -> stream.readColor(color);
				case MdlUtils.TOKEN_COLOR -> readTimeline(stream, AnimationMap.KPPC);
				case MdlUtils.TOKEN_STATIC_ALPHA -> alpha = stream.readFloat();
				case MdlUtils.TOKEN_ALPHA -> readTimeline(stream, AnimationMap.KPPA);
				case MdlUtils.TOKEN_VISIBILITY -> readTimeline(stream, AnimationMap.KPPV);
				case MdlUtils.TOKEN_REPLACEABLE_ID -> replaceableId = stream.readInt();
				case MdlUtils.TOKEN_PATH -> path = stream.read();
				case MdlUtils.TOKEN_ANIM_VISIBILITY_GUIDE -> animationVisiblityGuide = stream.read();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in MdlxParticleEmitterPopcorn " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_POPCORN_PARTICLE_EMITTER, name);
		writeGenericHeader(stream);

		if ((flags & 0x10000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SORT_PRIMS_FAR_Z);
		}

		if ((flags & 0x8000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNSHADED);
		}

		if ((flags & 0x40000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNFOGGED);
		}

		if (!writeTimeline(stream, AnimationMap.KPPL)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LIFE_SPAN, lifeSpan);
		}

		if (!writeTimeline(stream, AnimationMap.KPPE)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_EMISSION_RATE, emissionRate);
		}

		if (!writeTimeline(stream, AnimationMap.KPPS)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_SPEED, speed);
		}

		if (!writeTimeline(stream, AnimationMap.KPPC)) {
			stream.writeColor(MdlUtils.TOKEN_STATIC_COLOR, color);
		}

		if (!writeTimeline(stream, AnimationMap.KPPA)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ALPHA, alpha);
		}

		writeTimeline(stream, AnimationMap.KPPV);

		if (replaceableId != 0) {
			stream.writeAttrib(MdlUtils.TOKEN_REPLACEABLE_ID, replaceableId);
		}

		if (path.length() != 0) {
			stream.writeStringAttrib(MdlUtils.TOKEN_PATH, path);
		}

		if (animationVisiblityGuide.length() != 0) {
			stream.writeStringAttrib(MdlUtils.TOKEN_ANIM_VISIBILITY_GUIDE, animationVisiblityGuide);
		}

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 556 + super.getByteLength(version);
	}
}
