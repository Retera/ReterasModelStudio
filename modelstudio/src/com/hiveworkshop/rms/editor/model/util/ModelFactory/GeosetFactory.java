package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeoset;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.ArrayList;
import java.util.List;

public class GeosetFactory {
	public static Geoset createGeoset(MdlxGeoset mdlxGeoset, ModelInfoHolder infoHolder, EditableModel model) {
		Geoset geoset = new Geoset();
		geoset.setExtents(new ExtLog(mdlxGeoset.extent));

		for (int i = 0; i < mdlxGeoset.sequenceExtents.size() && i<model.getAnimsSize(); i++) {
			ExtLog extents = new ExtLog(mdlxGeoset.sequenceExtents.get(i));
			geoset.add(model.getAnim(i), extents);
		}

		geoset.setMaterial(infoHolder.materials.get((int) mdlxGeoset.materialId));


		if (mdlxGeoset.selectionFlags == 4) {
			geoset.setUnselectable(true);
		}

		geoset.setSelectionGroup((int) mdlxGeoset.selectionGroup);
		geoset.setLevelOfDetail(mdlxGeoset.lod);
		geoset.setLevelOfDetailName(mdlxGeoset.lodName);

		int index = 0;
		List<Matrix> matrices = new ArrayList<>();
		for (long size : mdlxGeoset.matrixGroups) {
			Matrix m = new Matrix();
			for (int i = 0; i < size; i++) {
				int matrixIndex = (int) mdlxGeoset.matrixIndices[index];
				IdObject idObject = infoHolder.idObjMap.get(matrixIndex);
				if(idObject instanceof Bone){
					m.add((Bone) idObject);
				} else if (idObject != null) {
					System.err.println("Node " + matrixIndex + " is not of type Bone, but of type " + idObject.getClass().getSimpleName() + "");
				} else {
					int totNodes = infoHolder.idObjMap.size();
					long totBones = infoHolder.idObjMap.values().stream().filter(o -> o instanceof Bone).count();
					System.err.println("Invalid " + matrixIndex + " model only contains " + totNodes + " nodes of which " + totBones + " is bones");
				}
				index++;
			}
			matrices.add(m);
		}


		final short[] vertexGroups = mdlxGeoset.vertexGroups;
		final float[] vertices = mdlxGeoset.vertices;
		final float[] normals = mdlxGeoset.normals;
		final float[][] uvSets = mdlxGeoset.uvSets;

		final float[] tangents = mdlxGeoset.tangents;
		final short[] skin = mdlxGeoset.skin;
		ArrayList<short[]> skinList = new ArrayList<>();

		List<GeosetVertex> vertexList = new ArrayList<>();
		List<Triangle> triangleList = new ArrayList<>();

		int matrixMax = matrices.size();
		for (int i = 0; i < vertices.length / 3; i++) {
			GeosetVertex gv = new GeosetVertex(vertices[(i * 3)], vertices[(i * 3) + 1], vertices[(i * 3) + 2]);
			vertexList.add(gv);
			gv.setGeoset(geoset);

			geoset.add(gv);

			if (vertexGroups != null && i < vertexGroups.length) {
				int matInd = (256 + vertexGroups[i]) % 256;
//				System.out.println("vertGroup: " + vertexGroups[i] + ", -> " + matInd + "");
				if(matInd < matrices.size()){
					Matrix matrix = matrices.get(matInd);
					if (matrix != null) {
						for (Bone bone : matrix.getBones()) {
							gv.addBoneAttachment(bone);
						}
					}
				}
			}
			// this is an unsigned byte, the other guys java code will read as signed
			if (normals.length > 0) {
				gv.setNormal(new Vec3(normals[(i * 3)], normals[(i * 3) + 1], normals[(i * 3) + 2]));
			}

			for (float[] uvSet : uvSets) {
				gv.addTVertex(new Vec2(uvSet[(i * 2)], uvSet[(i * 2) + 1]));
			}


			if (tangents != null) {
				Vec4 tang = new Vec4(tangents[(i * 4)], tangents[(i * 4) + 1], tangents[(i * 4) + 2], tangents[(i * 4) + 3]);
				gv.setTangent(tang);
			}


			if (skin != null) {
				int skinInd = i * 8;
				Bone[] bones = {
						matrices.get(getValidIndex(((skin[skinInd + 0] + 256) % 256), matrixMax)).get(0),
						matrices.get(getValidIndex(((skin[skinInd + 1] + 256) % 256), matrixMax)).get(0),
						matrices.get(getValidIndex(((skin[skinInd + 2] + 256) % 256), matrixMax)).get(0),
						matrices.get(getValidIndex(((skin[skinInd + 3] + 256) % 256), matrixMax)).get(0)};
				short[] weights = {
						(short)((skin[skinInd + 4] + 256) % 256),
						(short)((skin[skinInd + 5] + 256) % 256),
						(short)((skin[skinInd + 6] + 256) % 256),
						(short)((skin[skinInd + 7] + 256) % 256)};

				gv.setSkinBones(bones, weights);

				skinList.add(new short[] {skin[skinInd], skin[skinInd + 1], skin[skinInd + 2], skin[skinInd + 3], skin[skinInd + 4], skin[skinInd + 5], skin[skinInd + 6], skin[skinInd + 7]});
			}
		}
		// guys, I didn't code this to allow experimental non-triangle faces that were suggested
		// to exist on the web (i.e. quads). if you wanted to fix that, you'd want to do it below
		final int[] facesVertIndices = mdlxGeoset.faces;

		for (int i = 0; i < facesVertIndices.length; i += 3) {
			if(facesVertIndices[i] < 0 || facesVertIndices[i + 1] < 0 || facesVertIndices[i + 2] < 0 ||
					facesVertIndices[i] > vertexList.size() || facesVertIndices[i + 1] > vertexList.size() || facesVertIndices[i + 2] > vertexList.size()){
				continue;
			}
			Triangle triangle = new Triangle(
					vertexList.get(facesVertIndices[i]),
					vertexList.get(facesVertIndices[i + 1]),
					vertexList.get(facesVertIndices[i + 2]),
					geoset);
			triangleList.add(triangle);
			geoset.add(triangle);
		}

		return geoset;
	}

	private static int getValidIndex(int ind, int max) {
		if (ind < max) return ind;
		return 0;
	}
}
