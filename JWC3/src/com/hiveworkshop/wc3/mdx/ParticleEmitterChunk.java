package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

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
			if (MdxUtils.checkOptionalId(in, ParticleEmitterVisibility.key)) {
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
		
		public ParticleEmitter() {
			
		}
		public ParticleEmitter(com.hiveworkshop.wc3.mdl.ParticleEmitter mdlEmitter) {
			node = new Node(mdlEmitter);
			node.flags |= 0x1000;
			emissionRate = (float)mdlEmitter.getEmissionRate();
			gravity = (float)mdlEmitter.getGravity();
			longitude = (float)mdlEmitter.getLongitude();
			latitude = (float)mdlEmitter.getLatitude();
			spawnModelFileName = mdlEmitter.getPath();
			lifeSpan = (float)mdlEmitter.getLifeSpan();
			initialVelocity = (float)mdlEmitter.getInitVelocity();
			for( AnimFlag af: mdlEmitter.getAnimFlags() ) {
				if( af.getName().equals("Visibility") ) {
					particleEmitterVisibility = new ParticleEmitterVisibility();
					particleEmitterVisibility.globalSequenceId = af.getGlobalSeqId();
					particleEmitterVisibility.interpolationType = af.getInterpType();
					particleEmitterVisibility.scalingTrack = new ParticleEmitterVisibility.ScalingTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						ParticleEmitterVisibility.ScalingTrack mdxEntry = particleEmitterVisibility.new ScalingTrack();
						particleEmitterVisibility.scalingTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number)mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((Number)mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number)mdlEntry.outTan).floatValue();
						}
					}
				} else {
					System.err.println("discarded flag " + af.getName());
				}
			}
		}
	}
}
