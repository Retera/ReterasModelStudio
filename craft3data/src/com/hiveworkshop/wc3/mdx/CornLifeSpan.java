package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CornLifeSpan {
	public int interpolationType;
	public int globalSequenceId;
	public LifeSpanTrack[] lifeSpanTrack = new LifeSpanTrack[0];

	public static final String key = "KPPL";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, key);
		final int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		lifeSpanTrack = new LifeSpanTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			lifeSpanTrack[i] = new LifeSpanTrack();
			lifeSpanTrack[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = lifeSpanTrack.length;
		out.writeNByteString(key, 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < lifeSpanTrack.length; i++) {
			lifeSpanTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < lifeSpanTrack.length; i++) {
			a += lifeSpanTrack[i].getSize();
		}

		return a;
	}

	public class LifeSpanTrack {
		public int time;
		public float lifeSpan;
		public float inTan;
		public float outTan;

		public void load(final BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			lifeSpan = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(lifeSpan);
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
