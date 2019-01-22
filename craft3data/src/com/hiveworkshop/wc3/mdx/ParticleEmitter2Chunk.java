package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class ParticleEmitter2Chunk {
	public ParticleEmitter2[] particleEmitter2 = new ParticleEmitter2[0];

	public static final String key = "PRE2";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "PRE2");
		final int chunkSize = in.readInt();
		final List<ParticleEmitter2> particleEmitter2List = new ArrayList();
		int particleEmitter2Counter = chunkSize;
		while (particleEmitter2Counter > 0) {
			final ParticleEmitter2 tempparticleEmitter2 = new ParticleEmitter2();
			particleEmitter2List.add(tempparticleEmitter2);
			tempparticleEmitter2.load(in);
			particleEmitter2Counter -= tempparticleEmitter2.getSize();
		}
		particleEmitter2 = particleEmitter2List.toArray(new ParticleEmitter2[particleEmitter2List.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfParticleEmitters2 = particleEmitter2.length;
		out.writeNByteString("PRE2", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < particleEmitter2.length; i++) {
			particleEmitter2[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < particleEmitter2.length; i++) {
			a += particleEmitter2[i].getSize();
		}

		return a;
	}

	public class ParticleEmitter2 {
		public Node node = new Node();
		public float speed;
		public float variation;
		public float latitude;
		public float gravity;
		public float lifespan;
		public float emissionRate;
		public float length;
		public float width;
		public int filterMode;
		public int rows;
		public int columns;
		public int headOrTail;
		public float tailLength;
		public float time;
		public float[] segmentColor = new float[9];
		public byte[] segmentAlpha = new byte[3];
		public float[] segmentScaling = new float[3];
		public int headIntervalStart;
		public int headIntervalEnd;
		public int headIntervalRepeat;
		public int headDecayIntervalStart;
		public int headDecayIntervalEnd;
		public int headDecayIntervalRepeat;
		public int tailIntervalStart;
		public int tailIntervalEnd;
		public int tailIntervalRepeat;
		public int tailDecayIntervalStart;
		public int tailDecayIntervalEnd;
		public int tailDecayIntervalRepeat;
		public int textureId;
		public int squirt;
		public int priorityPlane;
		public int replaceableId;
		public ParticleEmitter2Visibility particleEmitter2Visibility;
		public ParticleEmitter2EmissionRate particleEmitter2EmissionRate;
		public ParticleEmitter2Width particleEmitter2Width;
		public ParticleEmitter2Length particleEmitter2Length;
		public ParticleEmitter2Speed particleEmitter2Speed;
		public ParticleEmitter2Latitude particleEmitter2Latitude;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			speed = in.readFloat();
			variation = in.readFloat();
			latitude = in.readFloat();
			gravity = in.readFloat();
			lifespan = in.readFloat();
			emissionRate = in.readFloat();
			length = in.readFloat();
			width = in.readFloat();
			filterMode = in.readInt();
			rows = in.readInt();
			columns = in.readInt();
			headOrTail = in.readInt();
			tailLength = in.readFloat();
			time = in.readFloat();
			segmentColor = MdxUtils.loadFloatArray(in, 9);
			segmentAlpha = MdxUtils.loadByteArray(in, 3);
			segmentScaling = MdxUtils.loadFloatArray(in, 3);
			headIntervalStart = in.readInt();
			headIntervalEnd = in.readInt();
			headIntervalRepeat = in.readInt();
			headDecayIntervalStart = in.readInt();
			headDecayIntervalEnd = in.readInt();
			headDecayIntervalRepeat = in.readInt();
			tailIntervalStart = in.readInt();
			tailIntervalEnd = in.readInt();
			tailIntervalRepeat = in.readInt();
			tailDecayIntervalStart = in.readInt();
			tailDecayIntervalEnd = in.readInt();
			tailDecayIntervalRepeat = in.readInt();
			textureId = in.readInt();
			squirt = in.readInt();
			priorityPlane = in.readInt();
			replaceableId = in.readInt();
			for (int i = 0; i < 6; i++) {
				if (MdxUtils.checkOptionalId(in, ParticleEmitter2Visibility.key)) {
					particleEmitter2Visibility = new ParticleEmitter2Visibility();
					particleEmitter2Visibility.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2EmissionRate.key)) {
					particleEmitter2EmissionRate = new ParticleEmitter2EmissionRate();
					particleEmitter2EmissionRate.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Width.key)) {
					particleEmitter2Width = new ParticleEmitter2Width();
					particleEmitter2Width.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Length.key)) {
					particleEmitter2Length = new ParticleEmitter2Length();
					particleEmitter2Length.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Speed.key)) {
					particleEmitter2Speed = new ParticleEmitter2Speed();
					particleEmitter2Speed.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Latitude.key)) {
					particleEmitter2Latitude = new ParticleEmitter2Latitude();
					particleEmitter2Latitude.load(in);
				}

			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeFloat(speed);
			out.writeFloat(variation);
			out.writeFloat(latitude);
			out.writeFloat(gravity);
			out.writeFloat(lifespan);
			out.writeFloat(emissionRate);
			out.writeFloat(length);
			out.writeFloat(width);
			out.writeInt(filterMode);
			out.writeInt(rows);
			out.writeInt(columns);
			out.writeInt(headOrTail);
			out.writeFloat(tailLength);
			out.writeFloat(time);
			if (segmentColor.length % 9 != 0) {
				throw new IllegalArgumentException(
						"The array segmentColor needs either the length 9 or a multiple of this number. (got "
								+ segmentColor.length + ")");
			}
			MdxUtils.saveFloatArray(out, segmentColor);
			if (segmentAlpha.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array segmentAlpha needs either the length 3 or a multiple of this number. (got "
								+ segmentAlpha.length + ")");
			}
			MdxUtils.saveByteArray(out, segmentAlpha);
			if (segmentScaling.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array segmentScaling needs either the length 3 or a multiple of this number. (got "
								+ segmentScaling.length + ")");
			}
			MdxUtils.saveFloatArray(out, segmentScaling);
			out.writeInt(headIntervalStart);
			out.writeInt(headIntervalEnd);
			out.writeInt(headIntervalRepeat);
			out.writeInt(headDecayIntervalStart);
			out.writeInt(headDecayIntervalEnd);
			out.writeInt(headDecayIntervalRepeat);
			out.writeInt(tailIntervalStart);
			out.writeInt(tailIntervalEnd);
			out.writeInt(tailIntervalRepeat);
			out.writeInt(tailDecayIntervalStart);
			out.writeInt(tailDecayIntervalEnd);
			out.writeInt(tailDecayIntervalRepeat);
			out.writeInt(textureId);
			out.writeInt(squirt);
			out.writeInt(priorityPlane);
			out.writeInt(replaceableId);
			if (particleEmitter2Visibility != null) {
				particleEmitter2Visibility.save(out);
			}
			if (particleEmitter2EmissionRate != null) {
				particleEmitter2EmissionRate.save(out);
			}
			if (particleEmitter2Width != null) {
				particleEmitter2Width.save(out);
			}
			if (particleEmitter2Length != null) {
				particleEmitter2Length.save(out);
			}
			if (particleEmitter2Speed != null) {
				particleEmitter2Speed.save(out);
			}
			if (particleEmitter2Latitude != null) {
				particleEmitter2Latitude.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += node.getSize();
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 36;
			a += 3;
			a += 12;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			if (particleEmitter2Visibility != null) {
				a += particleEmitter2Visibility.getSize();
			}
			if (particleEmitter2EmissionRate != null) {
				a += particleEmitter2EmissionRate.getSize();
			}
			if (particleEmitter2Width != null) {
				a += particleEmitter2Width.getSize();
			}
			if (particleEmitter2Length != null) {
				a += particleEmitter2Length.getSize();
			}
			if (particleEmitter2Speed != null) {
				a += particleEmitter2Speed.getSize();
			}
			if (particleEmitter2Latitude != null) {
				a += particleEmitter2Latitude.getSize();
			}

			return a;
		}

		public ParticleEmitter2() {

		}

		public ParticleEmitter2(final com.hiveworkshop.wc3.mdl.ParticleEmitter2 mdlEmitter) {
			node = new Node(mdlEmitter);
			node.flags |= 0x1000;
			speed = (float) mdlEmitter.getSpeed();
			variation = (float) mdlEmitter.getVariation();
			latitude = (float) mdlEmitter.getLatitude();
			gravity = (float) mdlEmitter.getGravity();
			lifespan = (float) mdlEmitter.getLifeSpan();
			emissionRate = (float) mdlEmitter.getEmissionRate();
			length = (float) mdlEmitter.getLength();
			width = (float) mdlEmitter.getWidth();

			// for( ParticleEmitter2FilterMode nodeFlag: ParticleEmitter2FilterMode.values() ) {
			// if( mdlEmitter.getFlags().contains(nodeFlag.getMdlText()) )
			// filterMode = nodeFlag.ordinal();
			// }
			// BLEND("Blend"),
			// ADDITIVE("Additive"),
			// MODULATE("Modulate"),
			// MODULATE2X("Modulate2x"),
			// ADDALPHA("AlphaKey");

			rows = mdlEmitter.getRows();
			columns = mdlEmitter.getColumns();

			for (final String flag : mdlEmitter.getFlags()) {
				switch (flag) {
				case "Head":
					headOrTail = 0;
					break;
				case "Tail":
					headOrTail = 1;
					break;
				case "Both":
					headOrTail = 2;
					break;
				case "Blend":
					filterMode = 0;
					break;
				case "Additive":
					filterMode = 1;
					break;
				case "Modulate":
					filterMode = 2;
					break;
				case "Modulate2x":
					filterMode = 3;
					break;
				case "AlphaKey":
					filterMode = 4;
					break;
				case "Squirt":
					squirt = 1;
					break;
				default:
					break;
				// do nothing for the other flags, there will be many
				}
			}
			tailLength = (float) mdlEmitter.getTailLength();
			time = (float) mdlEmitter.getTime();
			segmentColor = new float[mdlEmitter.getSegmentColorCount() * 3];
			int index = 0;
			for (int i = 0; i < mdlEmitter.getSegmentColorCount(); i++) {
				final Vertex color = mdlEmitter.getSegmentColor(i);
				segmentColor[index++] = (float) color.getZ();
				segmentColor[index++] = (float) color.getY();
				segmentColor[index++] = (float) color.getX();
			}
			segmentAlpha = new byte[] { (byte) mdlEmitter.getAlpha().getX(), (byte) mdlEmitter.getAlpha().getY(),
					(byte) mdlEmitter.getAlpha().getZ() };
			segmentScaling = mdlEmitter.getParticleScaling().toFloatArray();

			headIntervalStart = (int) mdlEmitter.getLifeSpanUVAnim().getX();
			headIntervalEnd = (int) mdlEmitter.getLifeSpanUVAnim().getY();
			headIntervalRepeat = (int) mdlEmitter.getLifeSpanUVAnim().getZ();

			headDecayIntervalStart = (int) mdlEmitter.getDecayUVAnim().getX();
			headDecayIntervalEnd = (int) mdlEmitter.getDecayUVAnim().getY();
			headDecayIntervalRepeat = (int) mdlEmitter.getDecayUVAnim().getZ();

			tailIntervalStart = (int) mdlEmitter.getTailUVAnim().getX();
			tailIntervalEnd = (int) mdlEmitter.getTailUVAnim().getY();
			tailIntervalRepeat = (int) mdlEmitter.getTailUVAnim().getZ();

			tailDecayIntervalStart = (int) mdlEmitter.getTailDecayUVAnim().getX();
			tailDecayIntervalEnd = (int) mdlEmitter.getTailDecayUVAnim().getY();
			tailDecayIntervalRepeat = (int) mdlEmitter.getTailDecayUVAnim().getZ();

			textureId = mdlEmitter.getTextureID();
			priorityPlane = mdlEmitter.getPriorityPlane();
			replaceableId = mdlEmitter.getReplaceableId();

			for (final AnimFlag af : mdlEmitter.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					particleEmitter2Visibility = new ParticleEmitter2Visibility();
					particleEmitter2Visibility.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2Visibility.interpolationType = af.getInterpType();
					particleEmitter2Visibility.scalingTrack = new ParticleEmitter2Visibility.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2Visibility.ScalingTrack mdxEntry = particleEmitter2Visibility.new ScalingTrack();
						particleEmitter2Visibility.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("EmissionRate")) {
					particleEmitter2EmissionRate = new ParticleEmitter2EmissionRate();
					particleEmitter2EmissionRate.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2EmissionRate.interpolationType = af.getInterpType();
					particleEmitter2EmissionRate.scalingTrack = new ParticleEmitter2EmissionRate.ScalingTrack[af
							.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2EmissionRate.ScalingTrack mdxEntry = particleEmitter2EmissionRate.new ScalingTrack();
						particleEmitter2EmissionRate.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.emissionRate = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Latitude")) {
					particleEmitter2Latitude = new ParticleEmitter2Latitude();
					particleEmitter2Latitude.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2Latitude.interpolationType = af.getInterpType();
					particleEmitter2Latitude.scalingTrack = new ParticleEmitter2Latitude.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2Latitude.ScalingTrack mdxEntry = particleEmitter2Latitude.new ScalingTrack();
						particleEmitter2Latitude.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.speed = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Length")) {
					particleEmitter2Length = new ParticleEmitter2Length();
					particleEmitter2Length.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2Length.interpolationType = af.getInterpType();
					particleEmitter2Length.scalingTrack = new ParticleEmitter2Length.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2Length.ScalingTrack mdxEntry = particleEmitter2Length.new ScalingTrack();
						particleEmitter2Length.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.length = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Width")) {
					particleEmitter2Width = new ParticleEmitter2Width();
					particleEmitter2Width.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2Width.interpolationType = af.getInterpType();
					particleEmitter2Width.scalingTrack = new ParticleEmitter2Width.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2Width.ScalingTrack mdxEntry = particleEmitter2Width.new ScalingTrack();
						particleEmitter2Width.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.width = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Speed")) {
					particleEmitter2Speed = new ParticleEmitter2Speed();
					particleEmitter2Speed.globalSequenceId = af.getGlobalSeqId();
					particleEmitter2Speed.interpolationType = af.getInterpType();
					particleEmitter2Speed.scalingTrack = new ParticleEmitter2Speed.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitter2Speed.ScalingTrack mdxEntry = particleEmitter2Speed.new ScalingTrack();
						particleEmitter2Speed.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.speed = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
		}
	}
}
