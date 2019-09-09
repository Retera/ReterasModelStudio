package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Triangle;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class GeosetChunk {
	public Geoset[] geoset = new Geoset[0];

	public static final String key = "GEOS";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "GEOS");
		final int chunkSize = in.readInt();
		final List<Geoset> geosetList = new ArrayList();
		int geosetCounter = chunkSize;
		while (geosetCounter > 0) {
			final Geoset tempgeoset = new Geoset();
			geosetList.add(tempgeoset);
			tempgeoset.load(in);
			geosetCounter -= tempgeoset.getSize();
		}
		geoset = geosetList.toArray(new Geoset[geosetList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfGeosets = geoset.length;
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
		public float[][] vertexTexturePositions = new float[0][];

		public static final String key = "VRTX";

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			MdxUtils.checkId(in, "VRTX");
			final int nrOfVertexPositions = in.readInt();
			vertexPositions = MdxUtils.loadFloatArray(in, nrOfVertexPositions * 3);
			MdxUtils.checkId(in, "NRMS");
			final int nrOfVertexNormals = in.readInt();
			vertexNormals = MdxUtils.loadFloatArray(in, nrOfVertexNormals * 3);
			MdxUtils.checkId(in, "PTYP");
			final int nrOfFaceTypeGroups = in.readInt();
			faceTypeGroups = MdxUtils.loadIntArray(in, nrOfFaceTypeGroups);
			MdxUtils.checkId(in, "PCNT");
			final int nrOfFaceGroups = in.readInt();
			faceGroups = MdxUtils.loadIntArray(in, nrOfFaceGroups);
			MdxUtils.checkId(in, "PVTX");
			final int nrOfIndexes = in.readInt();
			faces = MdxUtils.loadShortArray(in, nrOfIndexes);
			MdxUtils.checkId(in, "GNDX");
			final int nrOfVertexGroups = in.readInt();
			vertexGroups = MdxUtils.loadByteArray(in, nrOfVertexGroups);
			MdxUtils.checkId(in, "MTGC");
			final int nrOfMatrixGroups = in.readInt();
			matrixGroups = MdxUtils.loadIntArray(in, nrOfMatrixGroups);
			MdxUtils.checkId(in, "MATS");
			final int nrOfMatrixIndexes = in.readInt();
			matrixIndexs = MdxUtils.loadIntArray(in, nrOfMatrixIndexes);
			materialId = in.readInt();
			selectionGroup = in.readInt();
			selectionType = in.readInt();
			boundsRadius = in.readFloat();
			minimumExtent = MdxUtils.loadFloatArray(in, 3);
			maximumExtent = MdxUtils.loadFloatArray(in, 3);
			final int nrOfExtents = in.readInt();
			extent = new Extent[nrOfExtents];
			for (int i = 0; i < nrOfExtents; i++) {
				extent[i] = new Extent();
				extent[i].load(in);
			}
			MdxUtils.checkId(in, "UVAS");
			nrOfTextureVertexGroups = in.readInt();
			vertexTexturePositions = new float[nrOfTextureVertexGroups][];
			for (int i = 0; i < nrOfTextureVertexGroups; i++) {
				MdxUtils.checkId(in, "UVBS");
				final int nrOfVertexTexturePositions = in.readInt();
				vertexTexturePositions[i] = MdxUtils.loadFloatArray(in, nrOfVertexTexturePositions * 2);
			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			int nrOfVertexTexturePositions = vertexTexturePositions.length / 2;
			final int nrOfExtents = extent.length;
			final int nrOfMatrixIndexes = matrixIndexs.length;
			final int nrOfMatrixGroups = matrixGroups.length;
			final int nrOfVertexGroups = vertexGroups.length;
			final int nrOfIndexes = faces.length;
			final int nrOfFaceGroups = faceGroups.length;
			final int nrOfFaceTypeGroups = faceTypeGroups.length;
			final int nrOfVertexNormals = vertexNormals.length / 3;
			final int nrOfVertexPositions = vertexPositions.length / 3;
			out.writeInt(getSize());// InclusiveSize
			out.writeNByteString("VRTX", 4);
			out.writeInt(nrOfVertexPositions);
			if ((vertexPositions.length % 3) != 0) {
				throw new IllegalArgumentException(
						"The array vertexPositions needs either the length 3 or a multiple of this number. (got "
								+ vertexPositions.length + ")");
			}
			MdxUtils.saveFloatArray(out, vertexPositions);
			out.writeNByteString("NRMS", 4);
			out.writeInt(nrOfVertexNormals);
			if ((vertexNormals.length % 3) != 0) {
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
			if ((minimumExtent.length % 3) != 0) {
				throw new IllegalArgumentException(
						"The array minimumExtent needs either the length 3 or a multiple of this number. (got "
								+ minimumExtent.length + ")");
			}
			MdxUtils.saveFloatArray(out, minimumExtent);
			if ((maximumExtent.length % 3) != 0) {
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
			for (int i = 0; i < nrOfTextureVertexGroups; i++) {
				out.writeNByteString("UVBS", 4);
				nrOfVertexTexturePositions = vertexTexturePositions[i].length / 2;
				out.writeInt(nrOfVertexTexturePositions);
				if ((vertexTexturePositions[i].length % 2) != 0) {
					throw new IllegalArgumentException(
							"The array vertexTexturePositions needs either the length 2 or a multiple of this number. (got "
									+ vertexTexturePositions[i].length + ")");
				}
				MdxUtils.saveFloatArray(out, vertexTexturePositions[i]);
			}

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
			for (int i = 0; i < vertexTexturePositions.length; i++) {
				a += 4;
				a += 4;
				a += 4 * vertexTexturePositions[i].length;
			}

			return a;
		}

		public class Extent {
			public float[] minimumExtent = new float[3];
			public float[] maximumExtent = new float[3];
			public float bounds;

			public void load(final BlizzardDataInputStream in) throws IOException {
				bounds = in.readFloat();
				minimumExtent = MdxUtils.loadFloatArray(in, 3);
				maximumExtent = MdxUtils.loadFloatArray(in, 3);
			}

			public void save(final BlizzardDataOutputStream out) throws IOException {
				if ((minimumExtent.length % 3) != 0) {
					throw new IllegalArgumentException(
							"The array minimumExtent needs either the length 3 or a multiple of this number. (got "
									+ minimumExtent.length + ")");
				}
				out.writeFloat(bounds);
				MdxUtils.saveFloatArray(out, minimumExtent);
				if ((maximumExtent.length % 3) != 0) {
					throw new IllegalArgumentException(
							"The array maximumExtent needs either the length 3 or a multiple of this number. (got "
									+ maximumExtent.length + ")");
				}
				MdxUtils.saveFloatArray(out, maximumExtent);

			}

			public int getSize() {
				int a = 0;
				a += 12;
				a += 12;
				a += 4;

				return a;
			}

			public Extent() {

			}

			public Extent(final ExtLog ext) {
				bounds = (float) ext.getBoundsRadius();
				minimumExtent = ext.getMinimumExtent().toFloatArray();
				maximumExtent = ext.getMaximumExtent().toFloatArray();
			}
		}

		public Geoset() {

		}

		public Geoset(final com.hiveworkshop.wc3.mdl.Geoset mdlGeo) {
			if (mdlGeo.getExtents() != null) {
				boundsRadius = (float) mdlGeo.getExtents().getBoundsRadius();
				if (mdlGeo.getExtents().getMinimumExtent() != null) {
					minimumExtent = mdlGeo.getExtents().getMinimumExtent().toFloatArray();
				}
				if (mdlGeo.getExtents().getMaximumExtent() != null) {
					maximumExtent = mdlGeo.getExtents().getMaximumExtent().toFloatArray();
				}
			}
			extent = new Extent[mdlGeo.getAnims().size()];
			for (int i = 0; i < extent.length; i++) {
				extent[i] = new Extent(mdlGeo.getAnim(i).getExtents());
			}
			materialId = mdlGeo.getMaterialID();
			final int numVertices = mdlGeo.getVertices().size();
			nrOfTextureVertexGroups = mdlGeo.getUVLayers().size();
			vertexPositions = new float[numVertices * 3];
			final boolean hasNormals = mdlGeo.getNormals().size() > 0;
			if (hasNormals) {
				vertexNormals = new float[numVertices * 3];
			} else {
				vertexNormals = new float[0];
			}
			vertexGroups = new byte[numVertices];
			vertexTexturePositions = new float[nrOfTextureVertexGroups][numVertices * 2];
			for (int vId = 0; vId < numVertices; vId++) {
				final GeosetVertex vertex = mdlGeo.getVertex(vId);
				vertexPositions[(vId * 3) + 0] = (float) vertex.getX();
				vertexPositions[(vId * 3) + 1] = (float) vertex.getY();
				vertexPositions[(vId * 3) + 2] = (float) vertex.getZ();
				if (hasNormals) {
					vertexNormals[(vId * 3) + 0] = (float) vertex.getNormal().getX();
					vertexNormals[(vId * 3) + 1] = (float) vertex.getNormal().getY();
					vertexNormals[(vId * 3) + 2] = (float) vertex.getNormal().getZ();
				}
				for (int uvLayerIndex = 0; uvLayerIndex < nrOfTextureVertexGroups; uvLayerIndex++) {
					vertexTexturePositions[uvLayerIndex][(vId * 2) + 0] = (float) vertex.getTVertex(uvLayerIndex)
							.getX();
					vertexTexturePositions[uvLayerIndex][(vId * 2) + 1] = (float) vertex.getTVertex(uvLayerIndex)
							.getY();
				}
				vertexGroups[vId] = (byte) vertex.getVertexGroup();
			}

			// Again, the current implementation of my mdl code is that it only
			// handles triangle facetypes
			// (there's another note about this in the MDX -> MDL geoset code)
			faceGroups = new int[] { mdlGeo.getTriangles().size() * 3 };
			faceTypeGroups = new int[] { 4 }; // triangles!
			faces = new short[mdlGeo.getTriangles().size() * 3]; // triangles!
			int i = 0;
			for (final Triangle tri : mdlGeo.getTriangles()) {
				for (int v = 0; v < /* tri.size() */3; v++) {
					faces[i++] = (short) tri.getId(v);
				}
			}
			if (mdlGeo.getFlags().contains("Unselectable")) {
				selectionType = 4;
			}
			selectionGroup = mdlGeo.getSelectionGroup();

			int matrixIndexsSize = 0;
			for (final Matrix matrix : mdlGeo.getMatrix()) {
				int size = matrix.size();
				if (size == -1) {
					size = 1;
				}
				matrixIndexsSize += size;
			}
			matrixGroups = new int[mdlGeo.getMatrix().size()];
			if (matrixIndexsSize == -1) {
				matrixIndexsSize = 1;
			}
			matrixIndexs = new int[matrixIndexsSize];
			i = 0;
			int groupIndex = 0;
			for (final Matrix matrix : mdlGeo.getMatrix()) {
				for (int index = 0; index < matrix.size(); index++) {
					matrixIndexs[i++] = matrix.getBoneId(index);
				}
				if (matrix.size() <= 0) {
					matrixIndexs[i++] = -1;
				}
				matrixGroups[groupIndex++] = matrix.size();
			}
		}
	}
}
