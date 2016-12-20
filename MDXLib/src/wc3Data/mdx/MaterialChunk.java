package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class MaterialChunk {
	public Material[] material = new Material[0];

	public static final String key = "MTLS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "MTLS");
		int chunkSize = in.readInt();
		List<Material> materialList = new ArrayList();
		int materialCounter = chunkSize;
		while (materialCounter > 0) {
			Material tempmaterial = new Material();
			materialList.add(tempmaterial);
			tempmaterial.load(in);
			materialCounter -= tempmaterial.getSize();
		}
		material = materialList.toArray(new Material[materialList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfMaterials = material.length;
		out.writeNByteString("MTLS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < material.length; i++) {
			material[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < material.length; i++) {
			a += material[i].getSize();
		}

		return a;
	}

	public class Material {
		public int priorityPlane;
		public int flags;
		public LayerChunk layerChunk;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			priorityPlane = in.readInt();
			flags = in.readInt();
			if (MdxUtils.checkOptionalId(in, layerChunk.key)) {
				layerChunk = new LayerChunk();
				layerChunk.load(in);
			}

		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			out.writeInt(priorityPlane);
			out.writeInt(flags);
			if (layerChunk != null) {
				layerChunk.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			if (layerChunk != null) {
				a += layerChunk.getSize();
			}

			return a;
		}
	}
}
