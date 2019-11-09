package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class MaterialChunk {
	public Material[] material = new Material[0];

	public static final String key = "MTLS";

	public void load(final BlizzardDataInputStream in, final int version) throws IOException {
		MdxUtils.checkId(in, "MTLS");
		final int chunkSize = in.readInt();
		final List<Material> materialList = new ArrayList();
		int materialCounter = chunkSize;
		while (materialCounter > 0) {
			final Material tempmaterial = new Material();
			materialList.add(tempmaterial);
			tempmaterial.load(in, version);
			materialCounter -= tempmaterial.getSize(version);
		}
		material = materialList.toArray(new Material[materialList.size()]);
	}

	public void save(final BlizzardDataOutputStream out, final int version) throws IOException {
		final int nrOfMaterials = material.length;
		out.writeNByteString("MTLS", 4);
		out.writeInt(getSize(version) - 8);// ChunkSize
		for (int i = 0; i < material.length; i++) {
			material[i].save(out, version);
		}

	}

	public int getSize(final int version) {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < material.length; i++) {
			a += material[i].getSize(version);
		}

		return a;
	}

	public class Material {
		private static final int SHADER_PART_LEN_V900 = 80;
		public int priorityPlane;
		public int flags;
		public LayerChunk layerChunk;
		public String shader;

		public void load(final BlizzardDataInputStream in, final int version) throws IOException {
			final int inclusiveSize = in.readInt();
			priorityPlane = in.readInt();
			flags = in.readInt();
			if (version == 900) {
				// 900 settings
				shader = in.readCharsAsString(SHADER_PART_LEN_V900); // todo any special charset to use here?
			}
			if (MdxUtils.checkOptionalId(in, LayerChunk.key)) {
				layerChunk = new LayerChunk();
				layerChunk.load(in, version);
			}

		}

		public void save(final BlizzardDataOutputStream out, final int version) throws IOException {
			out.writeInt(getSize(version));// InclusiveSize
			out.writeInt(priorityPlane);
			out.writeInt(flags);

			if (version == 900) {
				out.writeNByteString(shader, SHADER_PART_LEN_V900);
			}

			if (layerChunk != null) {
				layerChunk.save(out, version);
			}

		}

		public int getSize(final int version) {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			if (layerChunk != null) {
				a += layerChunk.getSize(version);
			}
			if (version == 900) {
				a += SHADER_PART_LEN_V900;
			}

			return a;
		}

		public Material() {

		}

		public Material(final com.hiveworkshop.wc3.mdl.Material mat) {
			layerChunk = new LayerChunk();
			layerChunk.layer = new LayerChunk.Layer[mat.getLayers().size()];
			for (int i = 0; i < mat.getLayers().size(); i++) {
				layerChunk.layer[i] = layerChunk.new Layer(mat.getLayers().get(i));
			}
			priorityPlane = mat.getPriorityPlane();
			for (final String flag : mat.getFlags()) {
				if (flag.equals("ConstantColor")) {
					flags |= 0x1;
				}
				if (flag.equals("SortPrimsFarZ")) {
					flags |= 0x10;
				}
				if (flag.equals("FullResolution")) {
					flags |= 0x20;
				}
			}
			shader = mat.getShaderString();
		}
	}
}
