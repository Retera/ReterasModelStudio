package com.hiveworkshop.wc3.mdx;
import hiveworkshop.localizationmanager.LocalizationManager;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class ModelChunk {
	public String name = "";
	public int unknownNull;
	public float boundsRadius;
	public float[] minimumExtent = new float[3];
	public float[] maximumExtent = new float[3];
	public int blendTime;

	public static final String key = "MODL";

	public void load(final BlizzardDataInputStream in, final int version) throws IOException {
		MdxUtils.checkId(in, "MODL");
		final int chunkSize = in.readInt();
		name = in.readCharsAsString(336);
		unknownNull = in.readInt();
		if (unknownNull != 0) {
			System.err
					.println(LocalizationManager.getInstance().get("println.modelchunk_load_know") + unknownNull + LocalizationManager.getInstance().get("println.modelchunk_load_possible"));
		}
		boundsRadius = in.readFloat();
		minimumExtent = MdxUtils.loadFloatArray(in, 3);
		maximumExtent = MdxUtils.loadFloatArray(in, 3);
		blendTime = in.readInt();
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("MODL", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		out.writeNByteString(name, 336);
		out.writeInt(unknownNull);
		out.writeFloat(boundsRadius);
		if ((minimumExtent.length % 3) != 0) {
			throw new IllegalArgumentException(
					LocalizationManager.getInstance().get("exception.modelchunk_save_array_minimumextent")
							+ minimumExtent.length + ")");
		}
		MdxUtils.saveFloatArray(out, minimumExtent);
		if ((maximumExtent.length % 3) != 0) {
			throw new IllegalArgumentException(
					LocalizationManager.getInstance().get("exception.modelchunk_save_array_maximumextent")
							+ maximumExtent.length + ")");
		}
		MdxUtils.saveFloatArray(out, maximumExtent);
		out.writeInt(blendTime);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 336;
		a += 4;
		a += 4;
		a += 12;
		a += 12;
		a += 4;

		return a;
	}
}
