package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CornAlpha {
	public int interpolationType;
	public int globalSequenceId;
	public AlphaTrack[] alphaTrack = new AlphaTrack[0];

	public static final String key = "KPPA";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KPPA");
		final int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		alphaTrack = new AlphaTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			alphaTrack[i] = new AlphaTrack();
			alphaTrack[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = alphaTrack.length;
		out.writeNByteString("KPPA", 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < alphaTrack.length; i++) {
			alphaTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < alphaTrack.length; i++) {
			a += alphaTrack[i].getSize();
		}

		return a;
	}

	public class AlphaTrack {
		public int time;
		public float alpha;
		public float inTan;
		public float outTan;

		public void load(final BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			alpha = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(alpha);
			if (interpolationType > 1) {
				out.writeFloat(inTan);
				out.writeFloat(outTan);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			if (interpolationType > 1) {
				a += 4;
				a += 4;
			}

			return a;
		}
	}
}
