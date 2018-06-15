package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class ParticleEmitter2Gravity {
	private static final String MDX_KEY = "KP2G";
	public int interpolationType;
	public int globalSequenceId;
	public VariationTrack[] scalingTrack = new VariationTrack[0];

	public static final String key = MDX_KEY;

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, MDX_KEY);
		final int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		scalingTrack = new VariationTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			scalingTrack[i] = new VariationTrack();
			scalingTrack[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = scalingTrack.length;
		out.writeNByteString(MDX_KEY, 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < scalingTrack.length; i++) {
			scalingTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < scalingTrack.length; i++) {
			a += scalingTrack[i].getSize();
		}

		return a;
	}

	public class VariationTrack {
		public int time;
		public float gravity;
		public float inTan;
		public float outTan;

		public void load(final BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			gravity = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(gravity);
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
