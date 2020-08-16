package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class SoundTracks {
	public int globalSequenceId;
	public int[] tracks = new int[0];

	public static final String key = "KESK";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, key);
		final int nrOfTracks = in.readInt();
		globalSequenceId = in.readInt();
		tracks = MdxUtils.loadIntArray(in, nrOfTracks);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfTracks = tracks.length;
		out.writeNByteString(key, 4);
		out.writeInt(nrOfTracks);
		out.writeInt(globalSequenceId);
		MdxUtils.saveIntArray(out, tracks);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;
		a += 4 * tracks.length;

		return a;
	}
}
