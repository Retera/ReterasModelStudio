package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class LayerChunk {
	public Layer[] layer = new Layer[0];

	public static final String key = "LAYS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "LAYS");
		int nrOfLayers = in.readInt();
		layer = new Layer[nrOfLayers];
		for (int i = 0; i < nrOfLayers; i++) {
			layer[i] = new Layer();
			layer[i].load(in);
		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfLayers = layer.length;
		out.writeNByteString("LAYS", 4);
		out.writeInt(nrOfLayers);
		for (int i = 0; i < layer.length; i++) {
			layer[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < layer.length; i++) {
			a += layer[i].getSize();
		}

		return a;
	}

	public class Layer {
		public int filterMode;
		public int shadingFlags;
		public int textureId;
		public int textureAnimationId;
		public int unknownNull_CoordID;
		public float alpha;
		public MaterialAlpha materialAlpha;
		public MaterialTextureId materialTextureId;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			filterMode = in.readInt();
			shadingFlags = in.readInt();
			textureId = in.readInt();
			textureAnimationId = in.readInt();
			unknownNull_CoordID = in.readInt();
			alpha = in.readFloat();
			for (int i = 0; i < 2; i++) {
				if (MdxUtils.checkOptionalId(in, materialAlpha.key)) {
					materialAlpha = new MaterialAlpha();
					materialAlpha.load(in);
				} else if (MdxUtils.checkOptionalId(in, materialTextureId.key)) {
					materialTextureId = new MaterialTextureId();
					materialTextureId.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			out.writeInt(filterMode);
			out.writeInt(shadingFlags);
			out.writeInt(textureId);
			out.writeInt(textureAnimationId);
			out.writeInt(unknownNull_CoordID);
			out.writeFloat(alpha);
			if (materialAlpha != null) {
				materialAlpha.save(out);
			}
			if (materialTextureId != null) {
				materialTextureId.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			if (materialAlpha != null) {
				a += materialAlpha.getSize();
			}
			if (materialTextureId != null) {
				a += materialTextureId.getSize();
			}

			return a;
		}
	}
}
