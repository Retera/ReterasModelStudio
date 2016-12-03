package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class RibbonEmitterChunk {
	public RibbonEmitter[] ribbonEmitter = new RibbonEmitter[0];

	public static final String key = "RIBB";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "RIBB");
		int chunkSize = in.readInt();
		List<RibbonEmitter> ribbonEmitterList = new ArrayList();
		int ribbonEmitterCounter = chunkSize;
		while (ribbonEmitterCounter > 0) {
			RibbonEmitter tempribbonEmitter = new RibbonEmitter();
			ribbonEmitterList.add(tempribbonEmitter);
			tempribbonEmitter.load(in);
			ribbonEmitterCounter -= tempribbonEmitter.getSize();
		}
		ribbonEmitter = ribbonEmitterList
				.toArray(new RibbonEmitter[ribbonEmitterList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfRibbonEmitters = ribbonEmitter.length;
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
		public int unknownNull;
		public int emissionRate;
		public int rows;
		public int columns;
		public int materialId;
		public float gravity;
		public RibbonEmitterVisibility ribbonEmitterVisibility;
		public RibbonEmitterHeightAbove ribbonEmitterHeightAbove;
		public RibbonEmitterHeightBelow ribbonEmitterHeightBelow;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			heightAbove = in.readFloat();
			heightBelow = in.readFloat();
			alpha = in.readFloat();
			color = MdxUtils.loadFloatArray(in, 3);
			lifeSpan = in.readFloat();
			unknownNull = in.readInt();
			emissionRate = in.readInt();
			rows = in.readInt();
			columns = in.readInt();
			materialId = in.readInt();
			gravity = in.readFloat();
			for (int i = 0; i < 3; i++) {
				if (MdxUtils.checkOptionalId(in, RibbonEmitterVisibility.key)) {
					ribbonEmitterVisibility = new RibbonEmitterVisibility();
					ribbonEmitterVisibility.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						RibbonEmitterHeightAbove.key)) {
					ribbonEmitterHeightAbove = new RibbonEmitterHeightAbove();
					ribbonEmitterHeightAbove.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						RibbonEmitterHeightBelow.key)) {
					ribbonEmitterHeightBelow = new RibbonEmitterHeightBelow();
					ribbonEmitterHeightBelow.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeFloat(heightAbove);
			out.writeFloat(heightBelow);
			out.writeFloat(alpha);
			if (color.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array color needs either the length 3 or a multiple of this number. (got "
								+ color.length + ")");
			}
			MdxUtils.saveFloatArray(out, color);
			out.writeFloat(lifeSpan);
			out.writeInt(unknownNull);
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

			return a;
		}
		
		public RibbonEmitter() {
			
		}
		public RibbonEmitter(com.hiveworkshop.wc3.mdl.RibbonEmitter mdlEmitter) {
			node = new Node(mdlEmitter);
			node.flags |= 0x4000;
			heightAbove = (float)mdlEmitter.getHeightAbove();
			heightBelow = (float)mdlEmitter.getHeightBelow();
			alpha = (float)mdlEmitter.getAlpha();
			color = mdlEmitter.getStaticColor().toFloatArray();
			lifeSpan = (float)mdlEmitter.getLifeSpan();
			emissionRate = mdlEmitter.getEmissionRate();
			rows = mdlEmitter.getRows();
			columns = mdlEmitter.getColumns();
			materialId = mdlEmitter.getMaterialId();
			gravity = (float)mdlEmitter.getGravity();
			

			for( AnimFlag af: mdlEmitter.getAnimFlags() ) {
				if( af.getName().equals("Visibility") ) {
					ribbonEmitterVisibility = new RibbonEmitterVisibility();
					ribbonEmitterVisibility.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterVisibility.interpolationType = af.getInterpType();
					ribbonEmitterVisibility.scalingTrack = new RibbonEmitterVisibility.ScalingTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						RibbonEmitterVisibility.ScalingTrack mdxEntry = ribbonEmitterVisibility.new ScalingTrack();
						ribbonEmitterVisibility.scalingTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number)mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((Number)mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number)mdlEntry.outTan).floatValue();
						}
					}
				} else if( af.getName().equals("HeightAbove") ) {
					ribbonEmitterHeightAbove = new RibbonEmitterHeightAbove();
					ribbonEmitterHeightAbove.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterHeightAbove.interpolationType = af.getInterpType();
					ribbonEmitterHeightAbove.scalingTrack = new RibbonEmitterHeightAbove.ScalingTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						RibbonEmitterHeightAbove.ScalingTrack mdxEntry = ribbonEmitterHeightAbove.new ScalingTrack();
						ribbonEmitterHeightAbove.scalingTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.heightAbove = ((Number)mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((Number)mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number)mdlEntry.outTan).floatValue();
						}
					}
				} else if( af.getName().equals("HeightBelow") ) {
					ribbonEmitterHeightBelow = new RibbonEmitterHeightBelow();
					ribbonEmitterHeightBelow.globalSequenceId = af.getGlobalSeqId();
					ribbonEmitterHeightBelow.interpolationType = af.getInterpType();
					ribbonEmitterHeightBelow.scalingTrack = new RibbonEmitterHeightBelow.ScalingTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						RibbonEmitterHeightBelow.ScalingTrack mdxEntry = ribbonEmitterHeightBelow.new ScalingTrack();
						ribbonEmitterHeightBelow.scalingTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.heightBelow = ((Number)mdlEntry.value).floatValue();
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
