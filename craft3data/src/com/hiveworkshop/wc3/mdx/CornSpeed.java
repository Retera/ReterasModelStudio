package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CornSpeed {
	public int interpolationType;
	public int globalSequenceId;
	public SpeedTrack[] speedTrack = new SpeedTrack[0];

	public static final String key = "KPPS";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, key);
		final int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		speedTrack = new SpeedTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			speedTrack[i] = new SpeedTrack();
			speedTrack[i].load(in);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = speedTrack.length;
		out.writeNByteString(key, 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < speedTrack.length; i++) {
			speedTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < speedTrack.length; i++) {
			a += speedTrack[i].getSize();
		}

		return a;
	}

	public class SpeedTrack {
		public int time;
		public float speed;
		public float inTan;
		public float outTan;

		public void load(final BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			speed = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(speed);
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
