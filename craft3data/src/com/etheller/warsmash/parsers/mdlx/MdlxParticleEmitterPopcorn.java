package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

public class MdlxParticleEmitterPopcorn extends MdlxGenericObject {
	public float lifeSpan = 0;
	public float emissionRate = 0;
	public float speed = 0;
	public float[] color = new float[] { 1, 1, 1 };
	public float alpha = 0;
	public int replaceableId = 0;
	public String path = "";
	public String animationVisiblityGuide = "";

	/**
	 * Restricts us to only be able to parse models on one thread at a time, in
	 * return for high performance.
	 */
	private static final byte[] BYTES_HEAP = new byte[260];

	public MdlxParticleEmitterPopcorn() {
		super(0);
	}

	@Override
	public void readMdx(final LittleEndianDataInputStream stream, final int version) throws IOException {
		final long size = ParseUtils.readUInt32(stream);

		super.readMdx(stream, version);

		lifeSpan = stream.readFloat();
		emissionRate = stream.readFloat();
		speed = stream.readFloat();
		ParseUtils.readFloatArray(stream, color);
		alpha = stream.readFloat();
		replaceableId = stream.readInt();
		path = ParseUtils.readString(stream, BYTES_HEAP);
		animationVisiblityGuide = ParseUtils.readString(stream, BYTES_HEAP);

		readTimelines(stream, size - this.getByteLength(version));
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, getByteLength(version));

		super.writeMdx(stream, version);

		stream.writeFloat(lifeSpan);
		stream.writeFloat(emissionRate);
		stream.writeFloat(speed);
		ParseUtils.writeFloatArray(stream, color);
		stream.writeFloat(alpha);
		stream.writeInt(replaceableId);
		ParseUtils.writeString(stream, path, 260);
		ParseUtils.writeString(stream, animationVisiblityGuide, 260);

		writeNonGenericAnimationChunks(stream);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
			case "SortPrimsFarZ":
				flags |= 0x10000;
				break;
			case "Unshaded":
				flags |= 0x8000;
				break;
			case "Unfogged":
				flags |= 0x40000;
				break;
			case "static LifeSpan":
				lifeSpan = stream.readFloat();
				break;
			case "LifeSpan":
				readTimeline(stream, AnimationMap.KPPL);
				break;
			case "static EmissionRate":
				emissionRate = stream.readFloat();
				break;
			case "EmissionRate":
				readTimeline(stream, AnimationMap.KPPE);
				break;
			case "static Speed":
				speed = stream.readFloat();
				break;
			case "Speed":
				readTimeline(stream, AnimationMap.KPPS);
				break;
			case "static Color":
				stream.readColor(color);
				break;
			case "Color":
				readTimeline(stream, AnimationMap.KPPC);
				break;
			case "static Alpha":
				alpha = stream.readFloat();
				break;
			case "Alpha":
				readTimeline(stream, AnimationMap.KPPA);
				break;
			case "Visibility":
				readTimeline(stream, AnimationMap.KPPV);
				break;
			case "ReplaceableId":
				replaceableId = stream.readInt();
				break;
			case "Path":
				path = stream.read();
				break;
			case "AnimVisibilityGuide":
				animationVisiblityGuide = stream.read();
				break;
			default:
				throw new IllegalStateException("Unknown token in MdlxParticleEmitterPopcorn " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startObjectBlock(MdlUtils.TOKEN_PARTICLE_EMITTER2, this.name);
		writeGenericHeader(stream);

		if ((flags & 0x10000) != 0) {
			stream.writeFlag("SortPrimsFarZ");
		}
	
		if ((flags & 0x8000) != 0) {
			stream.writeFlag("Unshaded");
		}
	
		if ((flags & 0x40000) != 0) {
			stream.writeFlag("Unfogged");
		}
	
		if (!writeTimeline(stream, AnimationMap.KPPL)) {
			stream.writeFloatAttrib("static LifeSpan", lifeSpan);
		}
	
		if (!writeTimeline(stream, AnimationMap.KPPE)) {
			stream.writeFloatAttrib("static EmissionRate", emissionRate);
		}
	
		if (!writeTimeline(stream, AnimationMap.KPPS)) {
			stream.writeFloatAttrib("static Speed", speed);
		}
	
		if (!writeTimeline(stream, AnimationMap.KPPC)) {
			stream.writeFloatArrayAttrib("static Color", color);
		}
	
		if (!writeTimeline(stream, AnimationMap.KPPA)) {
			stream.writeFloatAttrib("static Alpha", alpha);
		}
	
		writeTimeline(stream, AnimationMap.KPPV);
	
		if (replaceableId != 0) {
			stream.writeAttrib("ReplaceableId", replaceableId);
		}
	
		if (path.length() != 0) {
			stream.writeStringAttrib("Path", path);
		}
	
		if (animationVisiblityGuide.length() != 0) {
			stream.writeStringAttrib("AnimVisibilityGuide", animationVisiblityGuide);
		}

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 175 + super.getByteLength(version);
	}
}
