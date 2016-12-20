package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class GeosetChunk {
	public Geoset[] geoset = new Geoset[0];

	public static final String key = "GEOS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "GEOS");
		int chunkSize = in.readInt();
		List<Geoset> geosetList = new ArrayList();
		int geosetCounter = chunkSize;
		while (geosetCounter > 0) {
			Geoset tempgeoset = new Geoset();
			geosetList.add(tempgeoset);
			tempgeoset.load(in);
			geosetCounter -= tempgeoset.getSize();
		}
		geoset = geosetList.toArray(new Geoset[geosetList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfGeosets = geoset.length;
		out.writeNByteString("GEOS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < geoset.length; i++) {
			geoset[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < geoset.length; i++) {
			a += geoset[i].getSize();
		}

		return a;
	}

	public class Geoset {
		public float[] vertexPositions = new float[0];
		public float[] vertexNormals = new float[0];
		public int[] faceTypeGroups = new int[0];
		public int[] faceGroups = new int[0];
		public short[] faces = new short[0];
		public byte[] vertexGroups = new byte[0];
		public int[] matrixGroups = new int[0];
		public int[] matrixIndexs = new int[0];
		public int materialId;
		public int selectionGroup;
		public int selectionType;
		public float boundsRadius;
		public float[] minimumExtent = new float[3];
		public float[] maximumExtent = new float[3];
		public Extent[] extent = new Extent[0];
		public int nrOfTextureVertexGroups;
		public float[] vertexTexturePositions = new float[0];

		public static final String key = "VRTX";

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			MdxUtils.checkId(in, "VRTX");
			int nrOfVertexPositions = in.readInt();
			vertexPositions = MdxUtils.loadFloatArray(in,
					nrOfVertexPositions * 3);
			MdxUtils.checkId(in, "NRMS");
			int nrOfVertexNormals = in.readInt();
			vertexNormals = MdxUtils.loadFloatArray(in, nrOfVertexNormals * 3);
			MdxUtils.checkId(in, "PTYP");
			int nrOfFaceTypeGroups = in.readInt();
			faceTypeGroups = MdxUtils.loadIntArray(in, nrOfFaceTypeGroups);
			MdxUtils.checkId(in, "PCNT");
			int nrOfFaceGroups = in.readInt();
			faceGroups = MdxUtils.loadIntArray(in, nrOfFaceGroups);
			MdxUtils.checkId(in, "PVTX");
			int nrOfIndexes = in.readInt();
			faces = MdxUtils.loadShortArray(in, nrOfIndexes);
			MdxUtils.checkId(in, "GNDX");
			int nrOfVertexGroups = in.readInt();
			vertexGroups = MdxUtils.loadByteArray(in, nrOfVertexGroups);
			MdxUtils.checkId(in, "MTGC");
			int nrOfMatrixGroups = in.readInt();
			matrixGroups = MdxUtils.loadIntArray(in, nrOfMatrixGroups);
			MdxUtils.checkId(in, "MATS");
			int nrOfMatrixIndexes = in.readInt();
			matrixIndexs = MdxUtils.loadIntArray(in, nrOfMatrixIndexes);
			materialId = in.readInt();
			selectionGroup = in.readInt();
			selectionType = in.readInt();
			boundsRadius = in.readFloat();
			minimumExtent = MdxUtils.loadFloatArray(in, 3);
			maximumExtent = MdxUtils.loadFloatArray(in, 3);
			int nrOfExtents = in.readInt();
			extent = new Extent[nrOfExtents];
			for (int i = 0; i < nrOfExtents; i++) {
				extent[i] = new Extent();
				extent[i].load(in);
			}
			MdxUtils.checkId(in, "UVAS");
			nrOfTextureVertexGroups = in.readInt();
			MdxUtils.checkId(in, "UVBS");
			int nrOfVertexTexturePositions = in.readInt();
			vertexTexturePositions = MdxUtils.loadFloatArray(in,
					nrOfVertexTexturePositions * 2);
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			int nrOfVertexTexturePositions = vertexTexturePositions.length / 2;
			int nrOfExtents = extent.length;
			int nrOfMatrixIndexes = matrixIndexs.length;
			int nrOfMatrixGroups = matrixGroups.length;
			int nrOfVertexGroups = vertexGroups.length;
			int nrOfIndexes = faces.length;
			int nrOfFaceGroups = faceGroups.length;
			int nrOfFaceTypeGroups = faceTypeGroups.length;
			int nrOfVertexNormals = vertexNormals.length / 3;
			int nrOfVertexPositions = vertexPositions.length / 3;
			out.writeInt(getSize());// InclusiveSize
			out.writeNByteString("VRTX", 4);
			out.writeInt(nrOfVertexPositions);
			if (vertexPositions.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array vertexPositions needs either the length 3 or a multiple of this number. (got "
								+ vertexPositions.length + ")");
			}
			MdxUtils.saveFloatArray(out, vertexPositions);
			out.writeNByteString("NRMS", 4);
			out.writeInt(nrOfVertexNormals);
			if (vertexNormals.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array vertexNormals needs either the length 3 or a multiple of this number. (got "
								+ vertexNormals.length + ")");
			}
			MdxUtils.saveFloatArray(out, vertexNormals);
			out.writeNByteString("PTYP", 4);
			out.writeInt(nrOfFaceTypeGroups);
			MdxUtils.saveIntArray(out, faceTypeGroups);
			out.writeNByteString("PCNT", 4);
			out.writeInt(nrOfFaceGroups);
			MdxUtils.saveIntArray(out, faceGroups);
			out.writeNByteString("PVTX", 4);
			out.writeInt(nrOfIndexes);
			MdxUtils.saveShortArray(out, faces);
			out.writeNByteString("GNDX", 4);
			out.writeInt(nrOfVertexGroups);
			MdxUtils.saveByteArray(out, vertexGroups);
			out.writeNByteString("MTGC", 4);
			out.writeInt(nrOfMatrixGroups);
			MdxUtils.saveIntArray(out, matrixGroups);
			out.writeNByteString("MATS", 4);
			out.writeInt(nrOfMatrixIndexes);
			MdxUtils.saveIntArray(out, matrixIndexs);
			out.writeInt(materialId);
			out.writeInt(selectionGroup);
			out.writeInt(selectionType);
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
			out.writeInt(nrOfExtents);
			for (int i = 0; i < extent.length; i++) {
				extent[i].save(out);
			}
			out.writeNByteString("UVAS", 4);
			out.writeInt(nrOfTextureVertexGroups);
			out.writeNByteString("UVBS", 4);
			out.writeInt(nrOfVertexTexturePositions);
			if (vertexTexturePositions.length % 2 != 0) {
				throw new IllegalArgumentException(
						"The array vertexTexturePositions needs either the length 2 or a multiple of this number. (got "
								+ vertexTexturePositions.length + ")");
			}
			MdxUtils.saveFloatArray(out, vertexTexturePositions);

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			a += 4 * vertexPositions.length;
			a += 4;
			a += 4;
			a += 4 * vertexNormals.length;
			a += 4;
			a += 4;
			a += 4 * faceTypeGroups.length;
			a += 4;
			a += 4;
			a += 4 * faceGroups.length;
			a += 4;
			a += 4;
			a += 2 * faces.length;
			a += 4;
			a += 4;
			a += 1 * vertexGroups.length;
			a += 4;
			a += 4;
			a += 4 * matrixGroups.length;
			a += 4;
			a += 4;
			a += 4 * matrixIndexs.length;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			a += 12;
			a += 4;
			for (int i = 0; i < extent.length; i++) {
				a += extent[i].getSize();
			}
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4 * vertexTexturePositions.length;

			return a;
		}

		public class Extent {
			public float[] minimumExtent = new float[3];
			public float[] maximumExtent = new float[3];
			public float bounds;

			public void load(BlizzardDataInputStream in) throws IOException {
				minimumExtent = MdxUtils.loadFloatArray(in, 3);
				maximumExtent = MdxUtils.loadFloatArray(in, 3);
				bounds = in.readFloat();
			}

			public void save(BlizzardDataOutputStream out) throws IOException {
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
				out.writeFloat(bounds);

			}

			public int getSize() {
				int a = 0;
				a += 12;
				a += 12;
				a += 4;

				return a;
			}
		}
	}
}
