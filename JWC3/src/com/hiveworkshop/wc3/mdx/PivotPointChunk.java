package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class PivotPointChunk {
	public float[] pivotPoints = new float[0];

	public static final String key = "PIVT";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "PIVT");
		int chunkSize = in.readInt();
		pivotPoints = MdxUtils.loadFloatArray(in, chunkSize / 4);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfPivotPoints = pivotPoints.length / 3;
		out.writeNByteString("PIVT", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		if (pivotPoints.length % 3 != 0) {
			throw new IllegalArgumentException(
					"The array pivotPoints needs either the length 3 or a multiple of this number. (got "
							+ pivotPoints.length + ")");
		}
		MdxUtils.saveFloatArray(out, pivotPoints);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4 * pivotPoints.length;

		return a;
	}
}
