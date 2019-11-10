package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CornChunk {
	public PopcornFxEmitter[] corns = new PopcornFxEmitter[0];

	public static final String key = "CORN";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "CORN");
		final int chunkSize = in.readInt();
		final List<PopcornFxEmitter> cornList = new ArrayList();
		int lightCounter = chunkSize;
		while (lightCounter > 0) {
			final PopcornFxEmitter tempcorn = new PopcornFxEmitter();
			cornList.add(tempcorn);
			tempcorn.load(in);
			lightCounter -= tempcorn.getSize();
		}
		corns = cornList.toArray(new PopcornFxEmitter[cornList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfLights = corns.length;
		out.writeNByteString("CORN", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < nrOfLights; i++) {
			corns[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < corns.length; i++) {
			a += corns[i].getSize();
		}

		return a;
	}

	public class PopcornFxEmitter {
		public Node node = new Node();
		public float[] maybeColor = null;
		public String path;
		public String flags;
		public CornAlpha cornAlpha;
		public CornEmissionRate cornEmissionRate;
		public CornVisibility cornVisibility;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			maybeColor = MdxUtils.loadFloatArray(in, 8);
			path = in.readCharsAsString(260);
			flags = in.readCharsAsString(260);
			for (int i = 0; i < 3; i++) {
				if (MdxUtils.checkOptionalId(in, CornAlpha.key)) {
					cornAlpha = new CornAlpha();
					cornAlpha.load(in);
				} else if (MdxUtils.checkOptionalId(in, CornEmissionRate.key)) {
					cornEmissionRate = new CornEmissionRate();
					cornEmissionRate.load(in);
				} else if (MdxUtils.checkOptionalId(in, CornVisibility.key)) {
					cornVisibility = new CornVisibility();
					cornVisibility.load(in);
				}

			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			MdxUtils.saveFloatArray(out, maybeColor);
			out.writeNByteString(path, 260);
			out.writeNByteString(flags, 260);
			if (cornAlpha != null) {
				cornAlpha.save(out);
			}
			if (cornEmissionRate != null) {
				cornEmissionRate.save(out);
			}
			if (cornVisibility != null) {
				cornVisibility.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += node.getSize();
			a += 32;
			a += 260;
			a += 260;
			if (cornAlpha != null) {
				a += cornAlpha.getSize();
			}
			if (cornEmissionRate != null) {
				a += cornEmissionRate.getSize();
			}
			if (cornVisibility != null) {
				a += cornVisibility.getSize();
			}

			return a;
		}

		public PopcornFxEmitter() {

		}

		public PopcornFxEmitter(final com.hiveworkshop.wc3.mdl.PopcornFxEmitter light) {
			node = new Node(light);
//			node.flags |= 0x200;
			// more to do here
			for (final AnimFlag af : light.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					cornVisibility = new CornVisibility();
					cornVisibility.globalSequenceId = af.getGlobalSeqId();
					cornVisibility.interpolationType = af.getInterpType();
					cornVisibility.visibilityTrack = new CornVisibility.VisibilityTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CornVisibility.VisibilityTrack mdxEntry = cornVisibility.new VisibilityTrack();
						cornVisibility.visibilityTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("EmissionRate")) {
					cornEmissionRate = new CornEmissionRate();
					cornEmissionRate.globalSequenceId = af.getGlobalSeqId();
					cornEmissionRate.interpolationType = af.getInterpType();
					cornEmissionRate.emissionRateTrack = new CornEmissionRate.EmissionRateTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CornEmissionRate.EmissionRateTrack mdxEntry = cornEmissionRate.new EmissionRateTrack();
						cornEmissionRate.emissionRateTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.emissionRate = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Alpha")) {
					cornAlpha = new CornAlpha();
					cornAlpha.globalSequenceId = af.getGlobalSeqId();
					cornAlpha.interpolationType = af.getInterpType();
					cornAlpha.alphaTrack = new CornAlpha.AlphaTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CornAlpha.AlphaTrack mdxEntry = cornAlpha.new AlphaTrack();
						cornAlpha.alphaTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.alpha = ((Number) mdlEntry.value).floatValue();
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

			maybeColor = light.getMaybeColor().clone();
			path = light.getPath();
			flags = light.getFlagString();
		}
	}
}
