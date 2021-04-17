package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

import java.util.Iterator;

public class MdlxParticleEmitter extends MdlxGenericObject {
	public float emissionRate = 0;
	public float gravity = 0;
	public float longitude = 0;
	public float latitude = 0;
	public String path = "";
	public float lifeSpan = 0;
	public float speed = 0;

	public MdlxParticleEmitter() {
		super(0x1000);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		emissionRate = reader.readFloat32();
		gravity = reader.readFloat32();
		longitude = reader.readFloat32();
		latitude = reader.readFloat32();
		path = reader.read(260);
		lifeSpan = reader.readFloat32();
		speed = reader.readFloat32();

		readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeFloat32(emissionRate);
		writer.writeFloat32(gravity);
		writer.writeFloat32(longitude);
		writer.writeFloat32(latitude);
		writer.writeWithNulls(path, 260);
		writer.writeFloat32(lifeSpan);
		writer.writeFloat32(speed);

		writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
				case MdlUtils.TOKEN_EMITTER_USES_MDL -> flags |= 0x8000;
				case MdlUtils.TOKEN_EMITTER_USES_TGA -> flags |= 0x10000;
				case MdlUtils.TOKEN_STATIC_EMISSION_RATE -> emissionRate = stream.readFloat();
				case MdlUtils.TOKEN_EMISSION_RATE -> readTimeline(stream, AnimationMap.KPEE);
				case MdlUtils.TOKEN_STATIC_GRAVITY -> gravity = stream.readFloat();
				case MdlUtils.TOKEN_GRAVITY -> readTimeline(stream, AnimationMap.KPEG);
				case MdlUtils.TOKEN_STATIC_LONGITUDE -> longitude = stream.readFloat();
				case MdlUtils.TOKEN_LONGITUDE -> readTimeline(stream, AnimationMap.KPLN);
				case MdlUtils.TOKEN_STATIC_LATITUDE -> latitude = stream.readFloat();
				case MdlUtils.TOKEN_LATITUDE -> readTimeline(stream, AnimationMap.KPLT);
				case MdlUtils.TOKEN_VISIBILITY -> readTimeline(stream, AnimationMap.KPEV);
				case MdlUtils.TOKEN_PARTICLE -> {
					final Iterator<String> iterator = readAnimatedBlock(stream);
					while (iterator.hasNext()) {
						final String subToken = iterator.next();
						switch (subToken) {
							case MdlUtils.TOKEN_STATIC_LIFE_SPAN -> lifeSpan = stream.readFloat();
							case MdlUtils.TOKEN_LIFE_SPAN -> readTimeline(stream, AnimationMap.KPEL);
							case MdlUtils.TOKEN_STATIC_INIT_VELOCITY -> speed = stream.readFloat();
							case MdlUtils.TOKEN_INIT_VELOCITY -> readTimeline(stream, AnimationMap.KPES);
							case MdlUtils.TOKEN_PATH -> path = stream.read();
							default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in ParticleEmitter " + name + "'s Particle: " + subToken);
						}
					}
				}
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in ParticleEmitter " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_PARTICLE_EMITTER, name);
		writeGenericHeader(stream);

		if ((flags & 0x8000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_EMITTER_USES_MDL);
		}

		if ((flags & 0x10000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_EMITTER_USES_TGA);
		}

		if (!writeTimeline(stream, AnimationMap.KPEE)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_EMISSION_RATE, emissionRate);
		}

		if (!writeTimeline(stream, AnimationMap.KPEG)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_GRAVITY, gravity);
		}

		if (!writeTimeline(stream, AnimationMap.KPLN)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LONGITUDE, longitude);
		}

		if (!writeTimeline(stream, AnimationMap.KPLT)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LATITUDE, latitude);
		}

		writeTimeline(stream, AnimationMap.KPEV);

		stream.startBlock(MdlUtils.TOKEN_PARTICLE);

		if (!writeTimeline(stream, AnimationMap.KPEL)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LIFE_SPAN, lifeSpan);
		}

		if (!writeTimeline(stream, AnimationMap.KPES)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_INIT_VELOCITY, speed);
		}

		if (((flags & 0x8000) != 0) || ((flags & 0x10000) != 0)) {
			stream.writeAttrib(MdlUtils.TOKEN_PATH, path);
		}

		stream.endBlock();

		writeGenericTimelines(stream);

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 288 + super.getByteLength(version);
	}
}
