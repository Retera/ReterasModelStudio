package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class BindPoseChunk {
	public static final String key = "BPOS";

	public float[][] bindPose = null;

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "BPOS");
		final int chunkSize = in.readInt();
		final int matrixCount = in.readInt();
		bindPose = new float[matrixCount][];
		for (int i = 0; i < (matrixCount); i++) {
			bindPose[i] = MdxUtils.loadFloatArray(in, 12);
		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("BPOS", 4);
		out.writeInt(getSize() - 8);// ChunkSize

		out.writeInt(bindPose.length);
		for (int i = 0; i < bindPose.length; i++) {
			float[] toSave = bindPose[i];
			if (toSave == null) {
				toSave = new float[12];
			}
			MdxUtils.saveFloatArray(out, toSave);
		}
	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += bindPose.length * 48;

		return a;
	}

}
