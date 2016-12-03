package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class GeosetRotation {
	public int interpolationType;
	public int globalSequenceId;
	public RotationTrack[] rotationTrack = new RotationTrack[0];

	public static final String key = "KGRT";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "KGRT");
		int nrOfTracks = in.readInt();
		interpolationType = in.readInt();
		globalSequenceId = in.readInt();
		rotationTrack = new RotationTrack[nrOfTracks];
		for (int i = 0; i < nrOfTracks; i++) {
			rotationTrack[i] = new RotationTrack();
			rotationTrack[i].load(in);
		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTracks = rotationTrack.length;
		out.writeNByteString("KGRT", 4);
		out.writeInt(nrOfTracks);
		out.writeInt(interpolationType);
		out.writeInt(globalSequenceId);
		for (int i = 0; i < rotationTrack.length; i++) {
			rotationTrack[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4;
		for (int i = 0; i < rotationTrack.length; i++) {
			a += rotationTrack[i].getSize();
		}

		return a;
	}

	public class RotationTrack {
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
