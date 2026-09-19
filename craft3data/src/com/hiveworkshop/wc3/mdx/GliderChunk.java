package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * Warcraft III 3.0.0 chunk with the tag bytes "DILG" ("Glider" in the game's MDL dialect): a list of geoset ids
 * that acts as a ray-picking whitelist. When the list is non-empty only the geosets it names can be hit by the
 * game's world-picking ray; rendering and collision shapes are unaffected. Each entry is one u32 geoset index;
 * the chunk is written last, after BPOS, and omitted when empty. The raw payload is kept so an odd-sized chunk
 * still round-trips.
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

	/** The geoset ids, one per 32-bit little-endian word; an odd-sized payload yields the whole words only. */
	public int[] getGeosetIds() {
		final int[] ids = new int[data.length / 4];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = (data[i * 4] & 0xFF) | ((data[(i * 4) + 1] & 0xFF) << 8) | ((data[(i * 4) + 2] & 0xFF) << 16)
					| ((data[(i * 4) + 3] & 0xFF) << 24);
		}
		return ids;
	}

	public void setGeosetIds(final int[] ids) {
		data = new byte[ids.length * 4];
		for (int i = 0; i < ids.length; i++) {
			data[i * 4] = (byte) ids[i];
			data[(i * 4) + 1] = (byte) (ids[i] >> 8);
			data[(i * 4) + 2] = (byte) (ids[i] >> 16);
			data[(i * 4) + 3] = (byte) (ids[i] >> 24);
		}
	}
}
