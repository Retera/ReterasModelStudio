package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/** MDX light track "KLSE": animated ShadowCastingEnd (Warcraft III 3.0, MDX 1300+). One float per key. */
public class LightShadowCastingEnd {
	public int interpolationType;
	public int globalSequenceId;
	public ScalingTrack[] scalingTrack = new ScalingTrack[0];

	public static final String key = "KLSE";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KLSE");
		final int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		scalingTrack = new ScalingTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			scalingTrack[i] = new ScalingTrack();
			scalingTrack[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = scalingTrack.length;
		out.writeNByteString("KLSE", 4);
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

	public class ScalingTrack {
		public int time;
		public float shadowCastingEnd;
		public float inTan;
		public float outTan;

		public void load(final BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			shadowCastingEnd = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(shadowCastingEnd);
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
