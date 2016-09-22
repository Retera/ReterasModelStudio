package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class ParticleEmitterChunk {
	public ParticleEmitter[] particleEmitter = new ParticleEmitter[0];

	public static final String key = "PREM";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "PREM");
		int chunkSize = in.readInt();
		List<ParticleEmitter> particleEmitterList = new ArrayList();
		int particleEmitterCounter = chunkSize;
		while (particleEmitterCounter > 0) {
			ParticleEmitter tempparticleEmitter = new ParticleEmitter();
			particleEmitterList.add(tempparticleEmitter);
			tempparticleEmitter.load(in);
			particleEmitterCounter -= tempparticleEmitter.getSize();
		}
		particleEmitter = particleEmitterList
				.toArray(new ParticleEmitter[particleEmitterList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfParticleEmitters = particleEmitter.length;
		out.writeNByteString("PREM", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < particleEmitter.length; i++) {
			particleEmitter[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < particleEmitter.length; i++) {
			a += particleEmitter[i].getSize();
		}

		return a;
	}

	public class ParticleEmitter {
		public Node node = new Node();
		public float emissionRate;
		public float gravity;
		public float longitude;
		public float latitude;
		public String spawnModelFileName = "";
		public int unknownNull;
		public float lifeSpan;
		public float initialVelocity;
		public ParticleEmitterVisibility particleEmitterVisibility;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			emissionRate = in.readFloat();
			gravity = in.readFloat();
			longitude = in.readFloat();
			latitude = in.readFloat();
			spawnModelFileName = in.readCharsAsString(256);
			unknownNull = in.readInt();
			lifeSpan = in.readFloat();
			initialVelocity = in.readFloat();
			if (MdxUtils.checkOptionalId(in, particleEmitterVisibility.key)) {
				particleEmitterVisibility = new ParticleEmitterVisibility();
				particleEmitterVisibility.load(in);
			}

		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeFloat(emissionRate);
			out.writeFloat(gravity);
			out.writeFloat(longitude);
			out.writeFloat(latitude);
			out.writeNByteString(spawnModelFileName, 256);
			out.writeInt(unknownNull);
			out.writeFloat(lifeSpan);
			out.writeFloat(initialVelocity);
			if (particleEmitterVisibility != null) {
				particleEmitterVisibility.save(out);
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
			a += 256;
			a += 4;
			a += 4;
			a += 4;
			if (particleEmitterVisibility != null) {
				a += particleEmitterVisibility.getSize();
			}

			return a;
		}
	}
}
