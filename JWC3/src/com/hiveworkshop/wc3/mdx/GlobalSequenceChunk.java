package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class GlobalSequenceChunk {
	public int[] globalSequences = new int[0];

	public static final String key = "GLBS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "GLBS");
		int chunkSize = in.readInt();
		globalSequences = MdxUtils.loadIntArray(in, chunkSize / 4);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfGlobalSequences = globalSequences.length;
		out.writeNByteString("GLBS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		MdxUtils.saveIntArray(out, globalSequences);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4 * globalSequences.length;

		return a;
	}
}
