package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * MDX 1800 (Forsaken Kingdom) camera depth-of-field focus distance track. The chunk tag is stored as the bytes
 * "IDUF" (Blizzard's new code writes reversed multi-char constants, compare "DILG" for the Glider chunk). One
 * float per key, in world units; corresponds to the "DoF Distance" camera value in the World Editor and the
 * "DOFDistance" / "FocusDistanceKeys" MDL keywords.
 */
public class CameraFocusDistance {
	public int interpolationType;
	public int globalSequenceId;
	public TranslationTrack[] translationTrack = new TranslationTrack[0];

	public static final String key = "IDUF";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "IDUF");
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
		out.writeNByteString("IDUF", 4);
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
		public float focusDistance;
		public float inTan;
		public float outTan;

		public void load(BlizzardDataInputStream in) throws IOException {
			time = in.readInt();
			focusDistance = in.readFloat();
			if (interpolationType > 1) {
				inTan = in.readFloat();
				outTan = in.readFloat();
			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(time);
			out.writeFloat(focusDistance);
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
