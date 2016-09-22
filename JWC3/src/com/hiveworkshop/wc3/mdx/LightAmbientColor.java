package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class LightAmbientColor {
	public int interpolationType;
	public int globalSequenceId;
	public ScalingTrack[] scalingTrack = new ScalingTrack[0];

	public static final String key = "KLBC";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KLBC");
		int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		scalingTrack = new ScalingTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			scalingTrack[i] = new ScalingTrack();
			scalingTrack[i].load(in);
		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTracks = scalingTrack.length;
		out.writeNByteString("KLBC", 4);
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
		public float[] ambientColor = new float[3];
		public float[] inTan = new float[3];
		public float[] outTan = new float[3];

		public void load(BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			ambientColor = MdxUtils.loadFloatArray(in, 3);
			if (interpolationType > 1) {
				inTan = MdxUtils.loadFloatArray(in, 3);
				outTan = MdxUtils.loadFloatArray(in, 3);
			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			if (ambientColor.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array ambientColor needs either the length 3 or a multiple of this number. (got "
								+ ambientColor.length + ")");
			}
			MdxUtils.saveFloatArray(out, ambientColor);
			if (interpolationType > 1) {
				if (inTan.length % 3 != 0) {
					throw new IllegalArgumentException(
							"The array inTan needs either the length 3 or a multiple of this number. (got "
									+ inTan.length + ")");
				}
				MdxUtils.saveFloatArray(out, inTan);
				if (outTan.length % 3 != 0) {
					throw new IllegalArgumentException(
							"The array outTan needs either the length 3 or a multiple of this number. (got "
									+ outTan.length + ")");
				}
				MdxUtils.saveFloatArray(out, outTan);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 12;
			if (interpolationType > 1) {
				a += 12;
				a += 12;
			}

			return a;
		}
	}
}
