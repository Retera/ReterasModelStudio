package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CornVisibility {
	public int interpolationType;
	public int globalSequenceId;
	public VisibilityTrack[] visibilityTrack = new VisibilityTrack[0];

	public static final String key = "KPPV";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KPPV");
		int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		visibilityTrack = new VisibilityTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			visibilityTrack[i] = new VisibilityTrack();
			visibilityTrack[i].load(in);
		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTracks = visibilityTrack.length;
		out.writeNByteString("KPPV", 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < visibilityTrack.length; i++) {
			visibilityTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < visibilityTrack.length; i++) {
			a += visibilityTrack[i].getSize();
		}

		return a;
	}

	public class VisibilityTrack {
		public int time;
		public float visibility;
		public float inTan;
		public float outTan;

		public void load(BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			visibility = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(visibility);
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
