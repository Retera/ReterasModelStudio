package com.hiveworkshop.rms.editor.model.util.ModelSaving;


import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeoset;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeosetAnimation;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class GeosetToMdlx {

	public static MdlxGeoset toMdlx(Geoset geoset, EditableModel model) {
		final MdlxGeoset mdlxGeoset = new MdlxGeoset();

		if (geoset.getExtents() != null) {
			mdlxGeoset.extent = geoset.getExtents().toMdlx();
		}

		for (int i = 0; i < geoset.getAnims().size() && i < model.getAnims().size(); i++) {
			System.out.println("Geoset anim " + i + ": " + geoset.getAnim(i) + "(" + geoset.getAnim(i).getStart() + " - " + geoset.getAnim(i).getEnd() + ")");
			mdlxGeoset.sequenceExtents.add(geoset.getAnim(i).getExtents().toMdlx());
		}

		mdlxGeoset.materialId = model.computeMaterialID(geoset.getMaterial());

		final int numVertices = geoset.getVertices().size();
		final int nrOfTextureVertexGroups = geoset.numUVLayers();

		mdlxGeoset.vertices = new float[numVertices * 3];
		mdlxGeoset.normals = new float[numVertices * 3];

		mdlxGeoset.vertexGroups = new short[numVertices];
		mdlxGeoset.uvSets = new float[nrOfTextureVertexGroups][numVertices * 2];

		for (int vId = 0; vId < numVertices; vId++) {
			final GeosetVertex vertex = geoset.getVertex(vId);
			if(!vertex.isValid()){
				vertex.set(0,0,0);
			}

			mdlxGeoset.vertices[(vId * 3) + 0] = vertex.x;
			mdlxGeoset.vertices[(vId * 3) + 1] = vertex.y;
			mdlxGeoset.vertices[(vId * 3) + 2] = vertex.z;

			final Vec3 norm = vertex.getNormal();

			mdlxGeoset.normals[(vId * 3) + 0] = norm.x;
			mdlxGeoset.normals[(vId * 3) + 1] = norm.y;
			mdlxGeoset.normals[(vId * 3) + 2] = norm.z;

			for (int uvLayerIndex = 0; uvLayerIndex < nrOfTextureVertexGroups; uvLayerIndex++) {
				final Vec2 uv = vertex.getTVertex(uvLayerIndex);

				mdlxGeoset.uvSets[uvLayerIndex][(vId * 2) + 0] = uv.x;
				mdlxGeoset.uvSets[uvLayerIndex][(vId * 2) + 1] = uv.y;
			}

			mdlxGeoset.vertexGroups[vId] = (byte) vertex.getVertexGroup();
		}

		// Again, the current implementation of my mdl code is that it only handles triangle face types
		// (there's another note about this in the MDX -> MDL mdlxGeoset code)
		mdlxGeoset.faceGroups = new long[] {geoset.getTriangles().size() * 3L};
		mdlxGeoset.faceTypeGroups = new long[] {4}; // triangles!
		mdlxGeoset.faces = new int[geoset.getTriangles().size() * 3]; // triangles!

		int faceIndex = 0;
		for (final Triangle tri : geoset.getTriangles()) {
			for (int v = 0; v < /* tri.size() */3; v++) {
				mdlxGeoset.faces[faceIndex++] = tri.getId(v);
				if(tri.getId(v) < 0){
					mdlxGeoset.faces[faceIndex-1] = 0;
				}
			}
		}

		if (geoset.getUnselectable()) {
			mdlxGeoset.selectionFlags = 4;
		}

		mdlxGeoset.selectionGroup = geoset.getSelectionGroup();

		mdlxGeoset.matrixIndices = new long[getMatrixIndexesSize(geoset)];
		mdlxGeoset.matrixGroups = new long[geoset.getMatrix().size()];
		int matrixIndex = 0;
		int groupIndex = 0;
		for (final Matrix matrix : geoset.getMatrix()) {
			for (int index = 0; index < matrix.size(); index++) {
				mdlxGeoset.matrixIndices[matrixIndex++] = matrix.getBoneId(index);
			}
			if (matrix.size() <= 0) {
				mdlxGeoset.matrixIndices[matrixIndex++] = -1;
			}
			int size = matrix.size();
			if (size == -1) {
				size = 1;
			}
			mdlxGeoset.matrixGroups[groupIndex++] = size;
		}

		mdlxGeoset.lod = geoset.getLevelOfDetail();
		mdlxGeoset.lodName = geoset.getLevelOfDetailName();

		if ((numVertices > 0) && (geoset.getVertex(0).getSkinBoneBones() != null)) {
			// v900
			mdlxGeoset.skin = new short[8 * numVertices];
			mdlxGeoset.tangents = new float[4 * numVertices];

			for (int i = 0; i < numVertices; i++) {
				for (int j = 0; j < 4; j++) {
					final GeosetVertex vertex = geoset.getVertex(i);
					mdlxGeoset.skin[(i * 8) + j] = vertex.getSkinBoneIndexes()[j];
					mdlxGeoset.skin[(i * 8) + j + 4] = (byte) (vertex.getSkinBoneWeights()[j]);
					mdlxGeoset.tangents[(i * 4) + j] = vertex.getTangent().toFloatArray()[j];
				}
			}
		}

		return mdlxGeoset;
	}

	public static int getMatrixIndexesSize(Geoset geoset) {
		int matrixIndexesSize = 0;
		for (final Matrix matrix : geoset.getMatrix()) {
			int size = matrix.size();
			if (size == -1) {
				size = 1;
			}
			matrixIndexesSize += size;
		}

		if (matrixIndexesSize == -1) {
			matrixIndexesSize = 1;
		}
		return matrixIndexesSize;
	}

	public static MdlxGeosetAnimation toMdlx(GeosetAnim geosetAnim, EditableModel model) {
		MdlxGeosetAnimation animation = new MdlxGeosetAnimation();

		animation.geosetId = model.computeGeosetID(geosetAnim.getGeoset());

		if (geosetAnim.isDropShadow()) {
			animation.flags |= 1;
		}
		if (geosetAnim.find("Color") != null || !geosetAnim.getStaticColor().equals(new Vec3(1, 1, 1))) {
			animation.flags |= 0x2;
		}

//		animation.color = ModelUtils.flipRGBtoBGR(geosetAnim.getStaticColor().toFloatArray());
		animation.color = geosetAnim.getStaticColor().toFloatArray();

		geosetAnim.timelinesToMdlx(animation);

		return animation;
	}
}
