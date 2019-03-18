package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.MdlxUtils;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class RibbonEmitterChunk {
	public RibbonEmitter[] ribbonEmitter = new RibbonEmitter[0];

	public static final String key = "RIBB";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "RIBB");
		final int chunkSize = in.readInt();
		final List<RibbonEmitter> ribbonEmitterList = new ArrayList();
		int ribbonEmitterCounter = chunkSize;
		while (ribbonEmitterCounter > 0) {
			final RibbonEmitter tempribbonEmitter = new RibbonEmitter();
			ribbonEmitterList.add(tempribbonEmitter);
			tempribbonEmitter.load(in);
			ribbonEmitterCounter -= tempribbonEmitter.getSize();
		}
		ribbonEmitter = ribbonEmitterList.toArray(new RibbonEmitter[ribbonEmitterList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfRibbonEmitters = ribbonEmitter.length;
		out.writeNByteString("RIBB", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < ribbonEmitter.length; i++) {
			ribbonEmitter[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < ribbonEmitter.length; i++) {
			a += ribbonEmitter[i].getSize();
		}

		return a;
	}

	public class RibbonEmitter {
		public Node node = new Node();
		public float heightAbove;
		public float heightBelow;
		public float alpha;
		public float[] color = new float[3];
		public float lifeSpan;
		public int textureSlot;
		public int emissionRate;
		public int rows;
		public int columns;
		public int materialId;
		public float gravity;
		public RibbonEmitterVisibility ribbonEmitterVisibility;
		public RibbonEmitterHeightAbove ribbonEmitterHeightAbove;
		public RibbonEmitterHeightBelow ribbonEmitterHeightBelow;
		public RibbonEmitterAlpha ribbonEmitterAlpha;
		public RibbonEmitterTextureSlot ribbonEmitterTextureSlot;
		public RibbonEmitterColor ribbonEmitterColor;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			heightAbove = in.readFloat();
			heightBelow = in.readFloat();
			alpha = in.readFloat();
			color = MdxUtils.loadFloatArray(in, 3);
			lifeSpan = in.readFloat();
			textureSlot = in.readInt();
			emissionRate = in.readInt();
			rows = in.readInt();
			columns = in.readInt();
			materialId = in.readInt();
			gravity = in.readFloat();
			for (int i = 0; i < 6; i++) {
				if (MdxUtils.checkOptionalId(in, RibbonEmitterVisibility.key)) {
					ribbonEmitterVisibility = new RibbonEmitterVisibility();
					ribbonEmitterVisibility.load(in);
				} else if (MdxUtils.checkOptionalId(in, RibbonEmitterHeightAbove.key)) {
					ribbonEmitterHeightAbove = new RibbonEmitterHeightAbove();
					ribbonEmitterHeightAbove.load(in);
				} else if (MdxUtils.checkOptionalId(in, RibbonEmitterHeightBelow.key)) {
					ribbonEmitterHeightBelow = new RibbonEmitterHeightBelow();
					ribbonEmitterHeightBelow.load(in);
				} else if (MdxUtils.checkOptionalId(in, RibbonEmitterAlpha.key)) {
					ribbonEmitterAlpha = new RibbonEmitterAlpha();
					ribbonEmitterAlpha.load(in);
				} else if (MdxUtils.checkOptionalId(in, RibbonEmitterTextureSlot.key)) {
					ribbonEmitterTextureSlot = new RibbonEmitterTextureSlot();
					ribbonEmitterTextureSlot.load(in);
				} else if (MdxUtils.checkOptionalId(in, RibbonEmitterColor.key)) {
					ribbonEmitterColor = new RibbonEmitterColor();
					ribbonEmitterColor.load(in);
				}
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeFloat(heightAbove);
			out.writeFloat(heightBelow);
			out.writeFloat(alpha);
			if ((color.length % 3) != 0) {
				throw new IllegalArgumentException(
						"The array color needs either the length 3 or a multiple of this number. (got " + color.length
								+ ")");
			}
			MdxUtils.saveFloatArray(out, color);
			out.writeFloat(lifeSpan);
			out.writeInt(textureSlot);
			out.writeInt(emissionRate);
			out.writeInt(rows);
			out.writeInt(columns);
			out.writeInt(materialId);
			out.writeFloat(gravity);
			if (ribbonEmitterVisibility != null) {
				ribbonEmitterVisibility.save(out);
			}
			if (ribbonEmitterHeightAbove != null) {
				ribbonEmitterHeightAbove.save(out);
			}
			if (ribbonEmitterHeightBelow != null) {
				ribbonEmitterHeightBelow.save(out);
			}
			if (ribbonEmitterAlpha != null) {
				ribbonEmitterAlpha.save(out);
			}
			if (ribbonEmitterTextureSlot != null) {
				ribbonEmitterTextureSlot.save(out);
			}
			if (ribbonEmitterColor != null) {
				ribbonEmitterColor.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += node.getSize();
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			if (ribbonEmitterVisibility != null) {
				a += ribbonEmitterVisibility.getSize();
			}
			if (ribbonEmitterHeightAbove != null) {
				a += ribbonEmitterHeightAbove.getSize();
			}
			if (ribbonEmitterHeightBelow != null) {
				a += ribbonEmitterHeightBelow.getSize();
			}
			if (ribbonEmitterAlpha != null) {
				a += ribbonEmitterAlpha.getSize();
			}
			if (ribbonEmitterTextureSlot != null) {
				a += ribbonEmitterTextureSlot.getSize();
			}
			if (ribbonEmitterColor != null) {
				a += ribbonEmitterColor.getSize();
			}

			return a;
		}

		public RibbonEmitter() {

		}

		public RibbonEmitter(final com.hiveworkshop.wc3.mdl.RibbonEmitter mdlEmitter) {
			boolean alphaFound = false;
			boolean colorFound = false;
			node = new Node(mdlEmitter);
			node.flags |= 0x4000;
			heightAbove = (float) mdlEmitter.getHeightAbove();
			heightBelow = (float) mdlEmitter.getHeightBelow();
			color = MdlxUtils.flipRGBtoBGR(mdlEmitter.getStaticColor().toFloatArray());
			lifeSpan = (float) mdlEmitter.getLifeSpan();
			emissionRate = mdlEmitter.getEmissionRate();
			rows = mdlEmitter.getRows();
			columns = mdlEmitter.getColumns();
			materialId = mdlEmitter.getMaterialId();
			gravity = (float) mdlEmitter.getGravity();
			textureSlot = (int) mdlEmitter.getTextureSlot();

			for (final AnimFlag af : mdlEmitter.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					ribbonEmitterVisibility = new RibbonEmitterVisibility();
					ribbonEmitterVisibility.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterVisibility.interpolationType = af.getInterpType();
					ribbonEmitterVisibility.scalingTrack = new RibbonEmitterVisibility.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterVisibility.ScalingTrack mdxEntry = ribbonEmitterVisibility.new ScalingTrack();
						ribbonEmitterVisibility.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("HeightAbove")) {
					ribbonEmitterHeightAbove = new RibbonEmitterHeightAbove();
					ribbonEmitterHeightAbove.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterHeightAbove.interpolationType = af.getInterpType();
					ribbonEmitterHeightAbove.scalingTrack = new RibbonEmitterHeightAbove.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterHeightAbove.ScalingTrack mdxEntry = ribbonEmitterHeightAbove.new ScalingTrack();
						ribbonEmitterHeightAbove.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.heightAbove = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("HeightBelow")) {
					ribbonEmitterHeightBelow = new RibbonEmitterHeightBelow();
					ribbonEmitterHeightBelow.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterHeightBelow.interpolationType = af.getInterpType();
					ribbonEmitterHeightBelow.scalingTrack = new RibbonEmitterHeightBelow.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterHeightBelow.ScalingTrack mdxEntry = ribbonEmitterHeightBelow.new ScalingTrack();
						ribbonEmitterHeightBelow.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.heightBelow = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Alpha")) {
					ribbonEmitterAlpha = new RibbonEmitterAlpha();
					ribbonEmitterAlpha.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterAlpha.interpolationType = af.getInterpType();
					ribbonEmitterAlpha.scalingTrack = new RibbonEmitterAlpha.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterAlpha.ScalingTrack mdxEntry = ribbonEmitterAlpha.new ScalingTrack();
						ribbonEmitterAlpha.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.alpha = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
					alphaFound = true;
				} else if (af.getName().equals("TextureSlot")) {
					ribbonEmitterTextureSlot = new RibbonEmitterTextureSlot();
					ribbonEmitterTextureSlot.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterTextureSlot.interpolationType = af.getInterpType();
					ribbonEmitterTextureSlot.scalingTrack = new RibbonEmitterTextureSlot.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterTextureSlot.ScalingTrack mdxEntry = ribbonEmitterTextureSlot.new ScalingTrack();
						ribbonEmitterTextureSlot.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.textureSlot = ((Number) mdlEntry.value).intValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).intValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).intValue();
						}
					}
				} else if (af.getName().equals("Color")) {
					ribbonEmitterColor = new RibbonEmitterColor();
					ribbonEmitterColor.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterColor.interpolationType = af.getInterpType();
					ribbonEmitterColor.scalingTrack = new RibbonEmitterColor.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final RibbonEmitterColor.ScalingTrack mdxEntry = ribbonEmitterColor.new ScalingTrack();
						ribbonEmitterColor.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.color = ((Vertex) mdlEntry.value).toFloatArray();
						// ========== RGB for some reason, mdl is BGR
						// ==============
						// final float blue = mdxEntry.color[0];
						// mdxEntry.color[0] = mdxEntry.color[2];
						// mdxEntry.color[2] = blue;
						// ========== RGB for some reason, mdl is BGR
						// ==============
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
						}
					}
					colorFound = true;
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
			if (alphaFound || (Math.abs(mdlEmitter.getAlpha() - (-1)) <= 0.001)) {
				alpha = 1.0f;
			} else {
				alpha = (float) mdlEmitter.getAlpha();
			}
		}
	}
}
