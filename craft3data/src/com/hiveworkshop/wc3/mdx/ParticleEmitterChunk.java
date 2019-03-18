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

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "PREM");
		final int chunkSize = in.readInt();
		final List<ParticleEmitter> particleEmitterList = new ArrayList();
		int particleEmitterCounter = chunkSize;
		while (particleEmitterCounter > 0) {
			final ParticleEmitter tempparticleEmitter = new ParticleEmitter();
			particleEmitterList.add(tempparticleEmitter);
			tempparticleEmitter.load(in);
			particleEmitterCounter -= tempparticleEmitter.getSize();
		}
		particleEmitter = particleEmitterList.toArray(new ParticleEmitter[particleEmitterList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfParticleEmitters = particleEmitter.length;
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
		public ParticleEmitterEmissionRate particleEmitterEmissionRate;
		public ParticleEmitterGravity particleEmitterGravity;
		public ParticleEmitterLongitude particleEmitterLongitude;
		public ParticleEmitterLatitude particleEmitterLatitude;
		public ParticleEmitterLifeSpan particleEmitterLifeSpan;
		public ParticleEmitterSpeed particleEmitterSpeed;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
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
			for (int i = 0; i < 7; i++) {
				if (MdxUtils.checkOptionalId(in, ParticleEmitterEmissionRate.key)) {
					particleEmitterEmissionRate = new ParticleEmitterEmissionRate();
					particleEmitterEmissionRate.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterGravity.key)) {
					particleEmitterGravity = new ParticleEmitterGravity();
					particleEmitterGravity.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterLongitude.key)) {
					particleEmitterLongitude = new ParticleEmitterLongitude();
					particleEmitterLongitude.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterLatitude.key)) {
					particleEmitterLatitude = new ParticleEmitterLatitude();
					particleEmitterLatitude.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterLifeSpan.key)) {
					particleEmitterLifeSpan = new ParticleEmitterLifeSpan();
					particleEmitterLifeSpan.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterSpeed.key)) {
					System.err.println("LOADED SPEED");
					System.exit(0);
					particleEmitterSpeed = new ParticleEmitterSpeed();
					particleEmitterSpeed.load(in);
				} else if (MdxUtils.checkOptionalId(in, ParticleEmitterVisibility.key)) {
					particleEmitterVisibility = new ParticleEmitterVisibility();
					particleEmitterVisibility.load(in);
				}
			}

		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
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
			if (particleEmitterEmissionRate != null) {
				particleEmitterEmissionRate.save(out);
			}
			if (particleEmitterGravity != null) {
				particleEmitterGravity.save(out);
			}
			if (particleEmitterLongitude != null) {
				particleEmitterLongitude.save(out);
			}
			if (particleEmitterLatitude != null) {
				particleEmitterLatitude.save(out);
			}
			if (particleEmitterLifeSpan != null) {
				particleEmitterLifeSpan.save(out);
			}
			if (particleEmitterSpeed != null) {
				particleEmitterSpeed.save(out);
			}
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
			if (particleEmitterEmissionRate != null) {
				a += particleEmitterEmissionRate.getSize();
			}
			if (particleEmitterGravity != null) {
				a += particleEmitterGravity.getSize();
			}
			if (particleEmitterLongitude != null) {
				a += particleEmitterLongitude.getSize();
			}
			if (particleEmitterLatitude != null) {
				a += particleEmitterLatitude.getSize();
			}
			if (particleEmitterLifeSpan != null) {
				a += particleEmitterLifeSpan.getSize();
			}
			if (particleEmitterSpeed != null) {
				a += particleEmitterSpeed.getSize();
			}
			if (particleEmitterVisibility != null) {
				a += particleEmitterVisibility.getSize();
			}

			return a;
		}

		public ParticleEmitter() {

		}

		public ParticleEmitter(final com.hiveworkshop.wc3.mdl.ParticleEmitter mdlEmitter) {
			node = new Node(mdlEmitter);
			node.flags |= 0x1000;
			emissionRate = (float) mdlEmitter.getEmissionRate();
			gravity = (float) mdlEmitter.getGravity();
			longitude = (float) mdlEmitter.getLongitude();
			latitude = (float) mdlEmitter.getLatitude();
			spawnModelFileName = mdlEmitter.getPath();
			lifeSpan = (float) mdlEmitter.getLifeSpan();
			initialVelocity = (float) mdlEmitter.getInitVelocity();
			if (mdlEmitter.isMDLEmitter()) {
				node.flags |= Node.NodeFlag.EMITTER_USES_MDL.getValue();
			} else {
				node.flags |= Node.NodeFlag.EMITTER_USES_TGA.getValue();
			}
			for (final AnimFlag af : mdlEmitter.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					particleEmitterVisibility = new ParticleEmitterVisibility();
					particleEmitterVisibility.globalSequenceId = af.getGlobalSeqId();
					particleEmitterVisibility.interpolationType = af.getInterpType();
					particleEmitterVisibility.scalingTrack = new ParticleEmitterVisibility.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterVisibility.ScalingTrack mdxEntry = particleEmitterVisibility.new ScalingTrack();
						particleEmitterVisibility.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("EmissionRate")) {
					particleEmitterEmissionRate = new ParticleEmitterEmissionRate();
					particleEmitterEmissionRate.globalSequenceId = af.getGlobalSeqId();
					particleEmitterEmissionRate.interpolationType = af.getInterpType();
					particleEmitterEmissionRate.scalingTrack = new ParticleEmitterEmissionRate.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterEmissionRate.ScalingTrack mdxEntry = particleEmitterEmissionRate.new ScalingTrack();
						particleEmitterEmissionRate.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.emissionRate = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Gravity")) {
					particleEmitterGravity = new ParticleEmitterGravity();
					particleEmitterGravity.globalSequenceId = af.getGlobalSeqId();
					particleEmitterGravity.interpolationType = af.getInterpType();
					particleEmitterGravity.scalingTrack = new ParticleEmitterGravity.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterGravity.ScalingTrack mdxEntry = particleEmitterGravity.new ScalingTrack();
						particleEmitterGravity.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.gravity = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Longitude")) {
					particleEmitterLongitude = new ParticleEmitterLongitude();
					particleEmitterLongitude.globalSequenceId = af.getGlobalSeqId();
					particleEmitterLongitude.interpolationType = af.getInterpType();
					particleEmitterLongitude.scalingTrack = new ParticleEmitterLongitude.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterLongitude.ScalingTrack mdxEntry = particleEmitterLongitude.new ScalingTrack();
						particleEmitterLongitude.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.longitude = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Latitude")) {
					particleEmitterLatitude = new ParticleEmitterLatitude();
					particleEmitterLatitude.globalSequenceId = af.getGlobalSeqId();
					particleEmitterLatitude.interpolationType = af.getInterpType();
					particleEmitterLatitude.scalingTrack = new ParticleEmitterLatitude.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterLatitude.ScalingTrack mdxEntry = particleEmitterLatitude.new ScalingTrack();
						particleEmitterLatitude.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.latitude = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("LifeSpan")) {
					particleEmitterLifeSpan = new ParticleEmitterLifeSpan();
					particleEmitterLifeSpan.globalSequenceId = af.getGlobalSeqId();
					particleEmitterLifeSpan.interpolationType = af.getInterpType();
					particleEmitterLifeSpan.scalingTrack = new ParticleEmitterLifeSpan.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterLifeSpan.ScalingTrack mdxEntry = particleEmitterLifeSpan.new ScalingTrack();
						particleEmitterLifeSpan.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.lifeSpan = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Speed")
						/* ghostwolf named it speed in his code, I think it's a bug */ || af.getName()
								.equals("InitVelocity")) {
					particleEmitterSpeed = new ParticleEmitterSpeed();
					particleEmitterSpeed.globalSequenceId = af.getGlobalSeqId();
					particleEmitterSpeed.interpolationType = af.getInterpType();
					particleEmitterSpeed.scalingTrack = new ParticleEmitterSpeed.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final ParticleEmitterSpeed.ScalingTrack mdxEntry = particleEmitterSpeed.new ScalingTrack();
						particleEmitterSpeed.scalingTrack[i] = mdxEntry;
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
