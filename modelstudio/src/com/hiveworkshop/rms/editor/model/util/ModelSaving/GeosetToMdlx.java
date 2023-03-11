package com.hiveworkshop.rms.editor.model.util.ModelSaving;


import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeoset;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeosetAnimation;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GeosetToMdlx {

	public static MdlxGeoset toMdlx(Geoset geoset, EditableModel model) {
		final MdlxGeoset mdlxGeoset = new MdlxGeoset();

		if (geoset.getExtents() != null) {
			mdlxGeoset.extent = geoset.getExtents().toMdlx();
		}

		for (Animation anim : model.getAnims()) {
			if(geoset.getAnimExtent(anim) != null){
				mdlxGeoset.sequenceExtents.add(geoset.getAnimExtent(anim).toMdlx());
			}
		}

		mdlxGeoset.materialId = model.computeMaterialID(geoset.getMaterial());
		if (mdlxGeoset.materialId == -1) {
			mdlxGeoset.materialId = 0;
		}

		final int numVertices = geoset.getVertices().size();
		final int nrOfTextureVertexGroups = geoset.numUVLayers();

		mdlxGeoset.vertices = new float[numVertices * 3];
		mdlxGeoset.normals = new float[numVertices * 3];

		mdlxGeoset.vertexGroups = new short[numVertices];
		mdlxGeoset.uvSets = new float[nrOfTextureVertexGroups][numVertices * 2];
		List<Matrix> matrices = new ArrayList<>(geoset.collectMatrices());

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

			mdlxGeoset.vertexGroups[vId] = (short) (matrices.indexOf(vertex.getMatrix()));
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

		if (matrices.isEmpty()){
			List<Bone> bones = model.getBones();
			int size = bones.size();
			mdlxGeoset.matrixIndices = new long[size];
			mdlxGeoset.matrixGroups = new long[size];
			for (int i = 0; i < size; i++) {
				int objectId = model.getObjectId(bones.get(i));
				mdlxGeoset.matrixIndices[i] = objectId;
				mdlxGeoset.matrixGroups[i] = 1;
			}
		} else {
			mdlxGeoset.matrixIndices = new long[getMatrixIndexesSize(matrices)];
			mdlxGeoset.matrixGroups = new long[matrices.size()];
			int matrixIndex = 0;
			int groupIndex = 0;
			for (final Matrix matrix : matrices) {
				for (int index = 0; index < matrix.size() && matrixIndex < mdlxGeoset.matrixIndices.length; index++) {
					mdlxGeoset.matrixIndices[matrixIndex++] = model.getObjectId(matrix.get(index));
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
		}

		mdlxGeoset.lod = geoset.getLevelOfDetail();
		mdlxGeoset.lodName = geoset.getLevelOfDetailName();

		if ((numVertices > 0) && (geoset.getVertex(0).getSkinBoneBones() != null)) {
			// v900
			mdlxGeoset.vertexGroups = new short[0];
			mdlxGeoset.skin = new short[8 * numVertices];
			mdlxGeoset.tangents = new float[4 * numVertices];

			for (int i = 0; i < numVertices; i++) {
				final GeosetVertex vertex = geoset.getVertex(i);
				short[] skinBoneWeights = vertex.getSkinBoneWeights();
				Bone[] skinBoneBones = vertex.getSkinBoneBones();
				for (int j = 0; j < 4; j++) {
					int index = skinBoneBones[j] == null ? 0 : model.getObjectId(skinBoneBones[j]);
					mdlxGeoset.skin[(i * 8) + j] = (short) index;
					mdlxGeoset.skin[(i * 8) + j + 4] = skinBoneWeights[j];
					mdlxGeoset.tangents[(i * 4) + j] = vertex.getTangent().toFloatArray()[j];
				}
			}
		} else {
			System.out.println("no skin bones! :O");
		}

		return mdlxGeoset;
	}

	public static int getMatrixIndexesSize(Collection<Matrix> matrices) {
		int matrixIndexesSize = 0;
		for (final Matrix matrix : matrices) {
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

	public static MdlxGeosetAnimation animatedToMdlx(Geoset geoset, EditableModel model) {
		MdlxGeosetAnimation animation = new MdlxGeosetAnimation();

		animation.geosetId = model.getGeosetId(geoset);

		if (geoset.isDropShadow()) {
			animation.flags |= 1;
		}
		if (geoset.find("Color") != null || !geoset.getStaticColor().equals(new Vec3(1, 1, 1))) {
			animation.flags |= 0x2;
		}

//		animation.color = ModelUtils.flipRGBtoBGR(geosetAnim.getStaticColor().toFloatArray());
		animation.color = geoset.getStaticColor().toFloatArray();

		geoset.timelinesToMdlx(animation, model);

		return animation;
	}
}
