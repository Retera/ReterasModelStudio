package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class ModelChunk {
	public String name = "";
	public int unknownNull;
	public float boundsRadius;
	public float[] minimumExtent = new float[3];
	public float[] maximumExtent = new float[3];
	public int blendTime;

	public static final String key = "MODL";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "MODL");
		int chunkSize = in.readInt();
		name = in.readCharsAsString(336);
		unknownNull = in.readInt();
		boundsRadius = in.readFloat();
		minimumExtent = MdxUtils.loadFloatArray(in, 3);
		maximumExtent = MdxUtils.loadFloatArray(in, 3);
		blendTime = in.readInt();
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("MODL", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		out.writeNByteString(name, 336);
		out.writeInt(unknownNull);
		out.writeFloat(boundsRadius);
		if (minimumExtent.length % 3 != 0) {
			throw new IllegalArgumentException(
					"The array minimumExtent needs either the length 3 or a multiple of this number. (got "
							+ minimumExtent.length + ")");
		}
		MdxUtils.saveFloatArray(out, minimumExtent);
		if (maximumExtent.length % 3 != 0) {
			throw new IllegalArgumentException(
					"The array maximumExtent needs either the length 3 or a multiple of this number. (got "
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
