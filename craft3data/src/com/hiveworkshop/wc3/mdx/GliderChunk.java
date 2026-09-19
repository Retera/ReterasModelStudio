package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * MDX 1800 (Forsaken Kingdom) chunk with the tag bytes "DILG" (a reversed "GLID", matching the new "Glider"
 * keyword in the game's MDL parser). In the 3.0.0 stock data it only appears on a handful of Definitive Edition
 * stair and bridge doodads and always contains N zero-valued 32-bit words (N does not match the geoset or node
 * count). Its meaning is unknown, so the raw payload is preserved verbatim for round-tripping.
 */
public class GliderChunk {
	public static final String key = "DILG";
	public byte[] data = new byte[0];

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, key);
		final int chunkSize = in.readInt();
		data = MdxUtils.loadByteArray(in, chunkSize);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString(key, 4);
		out.writeInt(data.length);
		MdxUtils.saveByteArray(out, data);
	}

	public int getSize() {
		return 8 + data.length;
	}
}
