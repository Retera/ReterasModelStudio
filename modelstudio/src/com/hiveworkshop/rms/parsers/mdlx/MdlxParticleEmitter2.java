package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxParticleEmitter2 extends MdlxGenericObject {
	public enum FilterMode {
		BLEND("Blend"),
		ADDITIVE("Additive"),
		MODULATE("Modulate"),
		MODULATE2X("Modulate2x"),
		ALPHAKEY("AlphaKey");

		String token;

		FilterMode(final String token) {
			this.token = token;
		}
		
		public static FilterMode fromId(final int id) {
			return values()[id];
		}

		public static int nameToId(final String name) {
			for (final FilterMode mode : values()) {
				if (mode.token.equals(name)) {
					return mode.ordinal();
				}
			}
			return -1;
		}

		@Override
		public String toString() {
			return token;
		}
	}

	public enum HeadOrTail {
		HEAD("Head"),
		TAIL("Tail"),
		BOTH("Both");

		String token;

		HeadOrTail(final String token) {
			this.token = token;
		}

		public static HeadOrTail fromId(final int id) {
			return values()[id];
		}

		public static int nameToId(final String name) {
			for (final HeadOrTail mode : values()) {
				if (mode.token.equals(name)) {
					return mode.ordinal();
				}
			}

			return -1;
		}
		
		@Override
		public String toString() {
			return token;
		}
	}

	public float speed = 0;
	public float variation = 0;
	public float latitude = 0;
	public float longitude = 0;
	public float gravity = 0;
	public float zSource = 0;
	public float lifeSpan = 0;
	public float emissionRate = 0;
	public float length = 0;
	public float width = 0;
	public FilterMode filterMode = FilterMode.BLEND;
	public long rows = 0;
	public long columns = 0;
	public HeadOrTail headOrTail = HeadOrTail.HEAD;
	public float tailLength = 0;
	public float timeMiddle = 0;
	public final float[][] segmentColors = new float[3][3];
	public short[] segmentAlphas = new short[3]; // unsigned byte[]
	public float[] segmentScaling = new float[3];
	public long[][] headIntervals = new long[2][3];
	public long[][] tailIntervals = new long[2][3];
	public int textureId = -1;
	public long squirt = 0;
	public int priorityPlane = 0;
	public long replaceableId = 0;

	public long emitterSize;
	public long emitterType;
	public byte[] geometryMdl = new byte[104];
	public byte[] recursionMdl = new byte[104];
	public float twinkleFPS;           // default is 10.0
	public float twinkleOnOff;         // boolean, twinkle applies additional scaling to make a shrink and grow effect
	public float twinkleScaleMin;      // twinkle is not applied if twinkleScaleMax - twinkleScaleMin == 0.0
	public float twinkleScaleMax;
	public float ivelScale;            // instant velocity scale, multiplier for each particle's intial velocity
	public float tumblexMin;           // tumble adds a randomised rotation to each particle
	public float tumblexMax;
	public float tumbleyMin;
	public float tumbleyMax;
	public float tumblezMin;
	public float tumblezMax;
	public float drag;                 // decreases particle velocity over time
	public float spin;
	public float[] windVector = new float[3];
	public float windTime;
	public float followSpeed1;
	public float followScale1;
	public float followSpeed2;
	public float followScale2;
	public int numSplines;
	public float[] splineData;

	public MdlxParticleEmitter2() {
		super(0x1000);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		int position = reader.position();
		long size = reader.readUInt32();

		super.readMdx(reader, version);

		int posAfterEmitterSize;
		if(version == 1300) {
			emitterSize = reader.readUInt32();
			posAfterEmitterSize = reader.position();
//			size = emitterSize;
			emitterType = reader.readUInt32();
		} else {
			posAfterEmitterSize = 0;
		}
		speed = reader.readFloat32();
		variation = reader.readFloat32();
		latitude = reader.readFloat32();
		if(version == 1300) {
			longitude = reader.readFloat32();
		}
		gravity = reader.readFloat32();
		if(version == 1300) {
			zSource = reader.readFloat32();
		}
		lifeSpan = reader.readFloat32();
		emissionRate = reader.readFloat32();
		length = reader.readFloat32();
		width = reader.readFloat32();
		if(version != 1300) {
			filterMode = FilterMode.fromId(reader.readInt32());
		}
		rows = reader.readUInt32();
		columns = reader.readUInt32();
		headOrTail = HeadOrTail.fromId(reader.readInt32());
		tailLength = reader.readFloat32();
		timeMiddle = reader.readFloat32();
		reader.readFloat32Array(segmentColors[0]);
		reader.readFloat32Array(segmentColors[1]);
		reader.readFloat32Array(segmentColors[2]);
		reader.readUInt8Array(segmentAlphas);
		reader.readFloat32Array(segmentScaling);
		reader.readUInt32Array(headIntervals[0]);
		reader.readUInt32Array(headIntervals[1]);
		reader.readUInt32Array(tailIntervals[0]);
		reader.readUInt32Array(tailIntervals[1]);
		if(version == 1300) {
			filterMode = FilterMode.fromId(reader.readInt32());
		}
		textureId = reader.readInt32();
		if(version != 1300) {
			squirt = reader.readUInt32();
		}
		priorityPlane = reader.readInt32();
		replaceableId = reader.readUInt32();

		if(version == 1300) {
			reader.readInt8Array(geometryMdl);
			reader.readInt8Array(recursionMdl);
			twinkleFPS = reader.readFloat32();
			twinkleOnOff= reader.readFloat32();
			twinkleScaleMin= reader.readFloat32();
			twinkleScaleMax= reader.readFloat32();
			ivelScale = reader.readFloat32();
			tumblexMin = reader.readFloat32();
			tumblexMax = reader.readFloat32();
			tumbleyMin = reader.readFloat32();
			tumbleyMax = reader.readFloat32();
			tumblezMin = reader.readFloat32();
			tumblezMax = reader.readFloat32();
			drag = reader.readFloat32();
			spin = reader.readFloat32();
			reader.readFloat32Array(windVector);
			windTime = reader.readFloat32();
			followSpeed1 = reader.readFloat32();
			followScale1 = reader.readFloat32();
			followSpeed2 = reader.readFloat32();
			followScale2 = reader.readFloat32();
			numSplines = reader.readInt32();
			splineData = new float[numSplines * 3];
			reader.readFloat32Array(splineData);
			squirt = reader.readUInt32();

			while(reader.position() - posAfterEmitterSize < emitterSize) {
				reader.readFloat32();
			}
		}

		readTimelines(reader, size - (reader.position() - position), version);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeFloat32(speed);
		writer.writeFloat32(variation);
		writer.writeFloat32(latitude);
		writer.writeFloat32(gravity);
		writer.writeFloat32(lifeSpan);
		writer.writeFloat32(emissionRate);
		writer.writeFloat32(length);
		writer.writeFloat32(width);
		writer.writeInt32(filterMode.ordinal());
		writer.writeUInt32(rows);
		writer.writeUInt32(columns);
		writer.writeInt32(headOrTail.ordinal());
		writer.writeFloat32(tailLength);
		writer.writeFloat32(timeMiddle);
		writer.writeFloat32Array(segmentColors[0]);
		writer.writeFloat32Array(segmentColors[1]);
		writer.writeFloat32Array(segmentColors[2]);
		writer.writeUInt8Array(segmentAlphas);
		writer.writeFloat32Array(segmentScaling);
		writer.writeUInt32Array(headIntervals[0]);
		writer.writeUInt32Array(headIntervals[1]);
		writer.writeUInt32Array(tailIntervals[0]);
		writer.writeUInt32Array(tailIntervals[1]);
		writer.writeInt32(textureId);
		writer.writeUInt32(squirt);
		writer.writeInt32(priorityPlane);
		writer.writeUInt32(replaceableId);

		writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
			case MdlUtils.TOKEN_SORT_PRIMS_FAR_Z:
				flags |= 0x10000;
				break;
			case MdlUtils.TOKEN_UNSHADED:
				flags |= 0x8000;
				break;
			case MdlUtils.TOKEN_LINE_EMITTER:
				flags |= 0x20000;
				break;
			case MdlUtils.TOKEN_UNFOGGED:
				flags |= 0x40000;
				break;
			case MdlUtils.TOKEN_MODEL_SPACE:
				flags |= 0x80000;
				break;
			case MdlUtils.TOKEN_XY_QUAD:
				flags |= 0x100000;
				break;
			case MdlUtils.TOKEN_STATIC_SPEED:
				speed = stream.readFloat();
				break;
			case MdlUtils.TOKEN_SPEED:
				readTimeline(stream, AnimationMap.KP2S);
				break;
			case MdlUtils.TOKEN_STATIC_VARIATION:
				variation = stream.readFloat();
				break;
			case MdlUtils.TOKEN_VARIATION:
				readTimeline(stream, AnimationMap.KP2R);
				break;
			case MdlUtils.TOKEN_STATIC_LATITUDE:
				latitude = stream.readFloat();
				break;
			case MdlUtils.TOKEN_LATITUDE:
				readTimeline(stream, AnimationMap.KP2L);
				break;
			case MdlUtils.TOKEN_STATIC_GRAVITY:
				gravity = stream.readFloat();
				break;
			case MdlUtils.TOKEN_GRAVITY:
				readTimeline(stream, AnimationMap.KP2G);
				break;
			case MdlUtils.TOKEN_VISIBILITY:
				readTimeline(stream, AnimationMap.KP2V);
				break;
			case MdlUtils.TOKEN_SQUIRT:
				squirt = 1;
				break;
			case MdlUtils.TOKEN_LIFE_SPAN:
				lifeSpan = stream.readFloat();
				break;
			case MdlUtils.TOKEN_STATIC_EMISSION_RATE:
				emissionRate = stream.readFloat();
				break;
			case MdlUtils.TOKEN_EMISSION_RATE:
				readTimeline(stream, AnimationMap.KP2E);
				break;
			case MdlUtils.TOKEN_STATIC_WIDTH:
				width = stream.readFloat();
				break;
			case MdlUtils.TOKEN_WIDTH:
				readTimeline(stream, AnimationMap.KP2W);
				break;
			case MdlUtils.TOKEN_STATIC_LENGTH:
				length = stream.readFloat();
				break;
			case MdlUtils.TOKEN_LENGTH:
				readTimeline(stream, AnimationMap.KP2N);
				break;
			case MdlUtils.TOKEN_BLEND:
				filterMode = FilterMode.BLEND;
				break;
			case MdlUtils.TOKEN_ADDITIVE:
				filterMode = FilterMode.ADDITIVE;
				break;
			case MdlUtils.TOKEN_MODULATE:
				filterMode = FilterMode.MODULATE;
				break;
			case MdlUtils.TOKEN_MODULATE2X:
				filterMode = FilterMode.MODULATE2X;
				break;
			case MdlUtils.TOKEN_ALPHAKEY:
				filterMode = FilterMode.ALPHAKEY;
				break;
			case MdlUtils.TOKEN_ROWS:
				rows = stream.readUInt32();
				break;
			case MdlUtils.TOKEN_COLUMNS:
				columns = stream.readUInt32();
				break;
			case MdlUtils.TOKEN_HEAD:
				headOrTail = HeadOrTail.HEAD;
				break;
			case MdlUtils.TOKEN_TAIL:
				headOrTail = HeadOrTail.TAIL;
				break;
			case MdlUtils.TOKEN_BOTH:
				headOrTail = HeadOrTail.BOTH;
				break;
			case MdlUtils.TOKEN_TAIL_LENGTH:
				tailLength = stream.readFloat();
				break;
			case MdlUtils.TOKEN_TIME:
				timeMiddle = stream.readFloat();
				break;
			case MdlUtils.TOKEN_SEGMENT_COLOR:
				stream.read(); // {

				for (int i = 0; i < 3; i++) {
					stream.read(); // Color
					stream.readColor(segmentColors[i]);
				}

				stream.read(); // }
				break;
			case MdlUtils.TOKEN_ALPHA:
				stream.readUInt8Array(segmentAlphas);
				break;
			case MdlUtils.TOKEN_PARTICLE_SCALING:
				stream.readFloatArray(segmentScaling);
				break;
			case MdlUtils.TOKEN_LIFE_SPAN_UV_ANIM:
				stream.readIntArray(headIntervals[0]);
				break;
			case MdlUtils.TOKEN_DECAY_UV_ANIM:
				stream.readIntArray(headIntervals[1]);
				break;
			case MdlUtils.TOKEN_TAIL_UV_ANIM:
				stream.readIntArray(tailIntervals[0]);
				break;
			case MdlUtils.TOKEN_TAIL_DECAY_UV_ANIM:
				stream.readIntArray(tailIntervals[1]);
				break;
			case MdlUtils.TOKEN_TEXTURE_ID:
				textureId = stream.readInt();
				break;
			case MdlUtils.TOKEN_REPLACEABLE_ID:
				replaceableId = stream.readUInt32();
				break;
			case MdlUtils.TOKEN_PRIORITY_PLANE:
				priorityPlane = stream.readInt();
				break;
			default:
				throw new RuntimeException("Unknown token in ParticleEmitter2 " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_PARTICLE_EMITTER2, name);
		writeGenericHeader(stream);

		if ((flags & 0x10000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SORT_PRIMS_FAR_Z);
		}

		if ((flags & 0x8000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNSHADED);
		}

		if ((flags & 0x20000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_LINE_EMITTER);
		}

		if ((flags & 0x40000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNFOGGED);
		}

		if ((flags & 0x80000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_MODEL_SPACE);
		}

		if ((flags & 0x100000) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_XY_QUAD);
		}

		if (!writeTimeline(stream, AnimationMap.KP2S)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_SPEED, speed);
		}

		if (!writeTimeline(stream, AnimationMap.KP2R)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_VARIATION, variation);
		}

		if (!writeTimeline(stream, AnimationMap.KP2L)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LATITUDE, latitude);
		}

		if (!writeTimeline(stream, AnimationMap.KP2G)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_GRAVITY, gravity);
		}

		writeTimeline(stream, AnimationMap.KP2V);

		if (squirt != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SQUIRT);
		}

		stream.writeFloatAttrib(MdlUtils.TOKEN_LIFE_SPAN, lifeSpan);

		if (!writeTimeline(stream, AnimationMap.KP2E)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_EMISSION_RATE, emissionRate);
		}

		if (!writeTimeline(stream, AnimationMap.KP2W)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_WIDTH, width);
		}

		if (!writeTimeline(stream, AnimationMap.KP2N)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_LENGTH, length);
		}

		stream.writeFlag(filterMode.toString());
		stream.writeAttribUInt32(MdlUtils.TOKEN_ROWS, rows);
		stream.writeAttribUInt32(MdlUtils.TOKEN_COLUMNS, columns);
		stream.writeFlag(headOrTail.toString());
		stream.writeFloatAttrib(MdlUtils.TOKEN_TAIL_LENGTH, tailLength);
		stream.writeFloatAttrib(MdlUtils.TOKEN_TIME, timeMiddle);

		stream.startBlock(MdlUtils.TOKEN_SEGMENT_COLOR);
		stream.writeColor(MdlUtils.TOKEN_COLOR, segmentColors[0]);
		stream.writeColor(MdlUtils.TOKEN_COLOR, segmentColors[1]);
		stream.writeColor(MdlUtils.TOKEN_COLOR, segmentColors[2]);
		stream.endBlockComma();

		stream.writeArrayAttrib(MdlUtils.TOKEN_ALPHA, segmentAlphas);
		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_PARTICLE_SCALING, segmentScaling);
		stream.writeArrayAttrib(MdlUtils.TOKEN_LIFE_SPAN_UV_ANIM, headIntervals[0]);
		stream.writeArrayAttrib(MdlUtils.TOKEN_DECAY_UV_ANIM, headIntervals[1]);
		stream.writeArrayAttrib(MdlUtils.TOKEN_TAIL_UV_ANIM, tailIntervals[0]);
		stream.writeArrayAttrib(MdlUtils.TOKEN_TAIL_DECAY_UV_ANIM, tailIntervals[1]);
		stream.writeAttrib(MdlUtils.TOKEN_TEXTURE_ID, textureId);

		if (replaceableId != 0) {
			stream.writeAttribUInt32(MdlUtils.TOKEN_REPLACEABLE_ID, replaceableId);
		}

		if (priorityPlane != 0) {
			stream.writeAttrib(MdlUtils.TOKEN_PRIORITY_PLANE, priorityPlane);
		}

		writeGenericTimelines(stream);

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		int extra = 0;
		if(version == 1300) {
			extra = 12 + 296 + (numSplines * 12);
		}
		return 175 + extra + super.getByteLength(version);
	}
}
