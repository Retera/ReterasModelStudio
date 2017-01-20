package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import com.hiveworkshop.wc3.mdl.AnimFlag;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class LayerChunk {
	public Layer[] layer = new Layer[0];

	public static final String key = "LAYS";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "LAYS");
		final int nrOfLayers = in.readInt();
		layer = new Layer[nrOfLayers];
		for (int i = 0; i < nrOfLayers; i++) {
			layer[i] = new Layer();
			layer[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfLayers = layer.length;
		out.writeNByteString("LAYS", 4);
		out.writeInt(nrOfLayers);
		for (int i = 0; i < layer.length; i++) {
			layer[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < layer.length; i++) {
			a += layer[i].getSize();
		}

		return a;
	}

	public class Layer {
		public int filterMode;
		public int shadingFlags;
		public int textureId;
		public int textureAnimationId;
		public int unknownNull_CoordID;
		public float alpha = 1;
		public MaterialAlpha materialAlpha;
		public MaterialTextureId materialTextureId;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			filterMode = in.readInt();
			shadingFlags = in.readInt();
			textureId = in.readInt();
			textureAnimationId = in.readInt();
			unknownNull_CoordID = in.readInt();
			alpha = in.readFloat();
			for (int i = 0; i < 2; i++) {
				if (MdxUtils.checkOptionalId(in, MaterialAlpha.key)) {
					materialAlpha = new MaterialAlpha();
					materialAlpha.load(in);
				} else if (MdxUtils.checkOptionalId(in, MaterialTextureId.key)) {
					materialTextureId = new MaterialTextureId();
					materialTextureId.load(in);
				}

			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			out.writeInt(filterMode);
			out.writeInt(shadingFlags);
			out.writeInt(textureId);
			out.writeInt(textureAnimationId);
			out.writeInt(unknownNull_CoordID);
			out.writeFloat(alpha);
			if (materialAlpha != null) {
				materialAlpha.save(out);
			}
			if (materialTextureId != null) {
				materialTextureId.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			if (materialAlpha != null) {
				a += materialAlpha.getSize();
			}
			if (materialTextureId != null) {
				a += materialTextureId.getSize();
			}

			return a;
		}

		public Layer() {

		}

		public Layer(final com.hiveworkshop.wc3.mdl.Layer layer) {
			filterMode = com.hiveworkshop.wc3.mdl.Layer.FilterMode.nameToId(layer.getFilterModeString());
			for (final String flag : layer.getFlags()) {
				switch (flag) {
				case "Unshaded":
					shadingFlags |= 0x1;
					break;
				case "SphereEnvironmentMap":
					shadingFlags |= 0x2;
					break;
				case "TwoSided":
					shadingFlags |= 0x10;
					break;
				case "Unfogged":
					shadingFlags |= 0x20;
					break;
				case "NoDepthTest":
					shadingFlags |= 0x40;
					break;
				case "NoDepthSet":
					shadingFlags |= 0x80;
					break;
				}
			}
			textureAnimationId = layer.getTVertexAnimId();
			unknownNull_CoordID = layer.getCoordId();
			boolean alphaFound = false;
			for (final AnimFlag af : layer.getAnims()) {
				if (af.getName().equals("Alpha")) {
					materialAlpha = new MaterialAlpha();
					materialAlpha.globalSequenceId = af.getGlobalSeqId();
					materialAlpha.interpolationType = af.getInterpType();
					materialAlpha.scalingTrack = new MaterialAlpha.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final MaterialAlpha.ScalingTrack mdxEntry = materialAlpha.new ScalingTrack();
						materialAlpha.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.alpha = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
					alphaFound = true;
				} else if (af.getName().equals("TextureID")) {
					materialTextureId = new MaterialTextureId();
					materialTextureId.globalSequenceId = af.getGlobalSeqId();
					materialTextureId.interpolationType = af.getInterpType();
					materialTextureId.scalingTrack = new MaterialTextureId.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final MaterialTextureId.ScalingTrack mdxEntry = materialTextureId.new ScalingTrack();
						materialTextureId.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.textureId = ((Number) mdlEntry.value).intValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).intValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).intValue();
						}
					}
				} else {
					System.err.println("discarded flag " + af.getName());
				}
			}
			if (alphaFound || Math.abs(layer.getStaticAlpha() - (-1)) <= 0.001) {
				alpha = 1.0f;
			} else {
				alpha = (float) layer.getStaticAlpha();
			}
			textureId = layer.getTextureId() == -1 ? 0 : layer.getTextureId();
		}
	}
}
