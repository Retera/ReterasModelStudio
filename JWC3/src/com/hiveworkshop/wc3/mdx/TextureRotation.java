package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class TextureRotation {
	public int interpolationType;
	public int globalSequenceId;
	public TranslationTrack[] translationTrack = new TranslationTrack[0];

	public static final String key = "KTAR";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KTAR");
		int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		translationTrack = new TranslationTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			translationTrack[i] = new TranslationTrack();
			translationTrack[i].load(in);
		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTracks = translationTrack.length;
		out.writeNByteString("KTAR", 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < translationTrack.length; i++) {
			translationTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < translationTrack.length; i++) {
			a += translationTrack[i].getSize();
		}

		return a;
	}

	public class TranslationTrack {
		public int time;
		public float[] rotation = new float[4];
		public float[] inTan = new float[4];
		public float[] outTan = new float[4];

		public void load(BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			rotation = MdxUtils.loadFloatArray(in, 4);
			if (interpolationType > 1) {
				inTan = MdxUtils.loadFloatArray(in, 4);
				outTan = MdxUtils.loadFloatArray(in, 4);
			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			if (rotation.length % 4 != 0) {
				throw new IllegalArgumentException(
						"The array rotation needs either the length 4 or a multiple of this number. (got "
								+ rotation.length + ")");
			}
			MdxUtils.saveFloatArray(out, rotation);
			if (interpolationType > 1) {
				if (inTan.length % 4 != 0) {
					throw new IllegalArgumentException(
							"The array inTan needs either the length 4 or a multiple of this number. (got "
									+ inTan.length + ")");
				}
				MdxUtils.saveFloatArray(out, inTan);
				if (outTan.length % 4 != 0) {
					throw new IllegalArgumentException(
							"The array outTan needs either the length 4 or a multiple of this number. (got "
									+ outTan.length + ")");
				}
				MdxUtils.saveFloatArray(out, outTan);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 16;
			if (interpolationType > 1) {
				a += 16;
				a += 16;
			}

			return a;
		}
	}
}
