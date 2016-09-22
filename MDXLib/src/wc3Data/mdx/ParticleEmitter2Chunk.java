package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class ParticleEmitter2Chunk {
	public ParticleEmitter2[] particleEmitter2 = new ParticleEmitter2[0];

	public static final String key = "PRE2";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "PRE2");
		int chunkSize = in.readInt();
		List<ParticleEmitter2> particleEmitter2List = new ArrayList();
		int particleEmitter2Counter = chunkSize;
		while (particleEmitter2Counter > 0) {
			ParticleEmitter2 tempparticleEmitter2 = new ParticleEmitter2();
			particleEmitter2List.add(tempparticleEmitter2);
			tempparticleEmitter2.load(in);
			particleEmitter2Counter -= tempparticleEmitter2.getSize();
		}
		particleEmitter2 = particleEmitter2List
				.toArray(new ParticleEmitter2[particleEmitter2List.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfParticleEmitters2 = particleEmitter2.length;
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

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
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
				if (MdxUtils
						.checkOptionalId(in, particleEmitter2Visibility.key)) {
					particleEmitter2Visibility = new ParticleEmitter2Visibility();
					particleEmitter2Visibility.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						particleEmitter2EmissionRate.key)) {
					particleEmitter2EmissionRate = new ParticleEmitter2EmissionRate();
					particleEmitter2EmissionRate.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						particleEmitter2Width.key)) {
					particleEmitter2Width = new ParticleEmitter2Width();
					particleEmitter2Width.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						particleEmitter2Length.key)) {
					particleEmitter2Length = new ParticleEmitter2Length();
					particleEmitter2Length.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						particleEmitter2Speed.key)) {
					particleEmitter2Speed = new ParticleEmitter2Speed();
					particleEmitter2Speed.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						particleEmitter2Latitude.key)) {
					particleEmitter2Latitude = new ParticleEmitter2Latitude();
					particleEmitter2Latitude.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
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
	}
}
