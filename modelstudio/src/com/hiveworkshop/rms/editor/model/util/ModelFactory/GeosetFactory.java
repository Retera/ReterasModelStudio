package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxExtent;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeoset;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class GeosetFactory {
	public static Geoset createGeoset(MdlxGeoset mdlxGeoset, ModelInfoHolder infoHolder) {
		Geoset geoset = new Geoset();
		geoset.setExtLog(new ExtLog(mdlxGeoset.extent));

		for (final MdlxExtent extent : mdlxGeoset.sequenceExtents) {
			final ExtLog extents = new ExtLog(extent);
			final Animation anim = new Animation(extents);
			geoset.add(anim);
		}

		geoset.setMaterial(infoHolder.materials.get((int) mdlxGeoset.materialId));


		if (mdlxGeoset.selectionFlags == 4) {
			geoset.setUnselectable(true);
		}

		geoset.setSelectionGroup((int) mdlxGeoset.selectionGroup);
		geoset.setLevelOfDetail(mdlxGeoset.lod);
		geoset.setLevelOfDetailName(mdlxGeoset.lodName);

		int index = 0;
		for (long size : mdlxGeoset.matrixGroups) {
			Matrix m = new Matrix();
			for (int i = 0; i < size; i++) {
				m.add((Bone) infoHolder.idObjMap.get((int) mdlxGeoset.matrixIndices[index]));
				m.addId((int) mdlxGeoset.matrixIndices[index]);
				index++;
			}
			geoset.addMatrix(m);
		}


		final short[] vertexGroups = mdlxGeoset.vertexGroups;
		final float[] vertices = mdlxGeoset.vertices;
		final float[] normals = mdlxGeoset.normals;
		final float[][] uvSets = mdlxGeoset.uvSets;

		final float[] tangents = mdlxGeoset.tangents;
		final short[] skin = mdlxGeoset.skin;
		ArrayList<short[]> skinList = new ArrayList<>();
		ArrayList<float[]> tangentList = new ArrayList<>();

		List<GeosetVertex> vertexList = new ArrayList<>();
		List<Triangle> triangleList = new ArrayList<>();

		for (int i = 0; i < vertices.length / 3; i++) {
			GeosetVertex gv = new GeosetVertex(vertices[(i * 3)], vertices[(i * 3) + 1], vertices[(i * 3) + 2]);
			vertexList.add(gv);
			gv.setGeoset(geoset);

			geoset.add(gv);

			if (i >= vertexGroups.length) {
				gv.setVertexGroup(-1);
			} else {
				gv.setVertexGroup((256 + vertexGroups[i]) % 256);
			}
			// this is an unsigned byte, the other guys java code will read as signed
			if (normals.length > 0) {
				gv.setNormal(new Vec3(normals[(i * 3)], normals[(i * 3) + 1], normals[(i * 3) + 2]));
			}

			for (float[] uvSet : uvSets) {
				gv.addTVertex(new Vec2(uvSet[(i * 2)], uvSet[(i * 2) + 1]));
			}


//			if (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()) && tangents != null && (i*4 + 3) < tangents.length) {
			if (infoHolder.isTangentAndSkinSupported() && tangents != null) {
				// version 900
				float[] tang = {tangents[(i * 4)], tangents[(i * 4) + 1], tangents[(i * 4) + 2], tangents[(i * 4) + 3]};
				gv.initV900();
				gv.setTangent(tang);

				Bone[] bones = {(Bone) infoHolder.idObjMap.get(((skin[(i * 8)] + 256) % 256)),
						(Bone) infoHolder.idObjMap.get(((skin[(i * 8) + 1] + 256) % 256)),
						(Bone) infoHolder.idObjMap.get(((skin[(i * 8) + 2] + 256) % 256)),
						(Bone) infoHolder.idObjMap.get(((skin[(i * 8) + 3] + 256) % 256))};

				short[] weights = {skin[(i * 8) + 4], skin[(i * 8) + 5], skin[(i * 8) + 6], skin[(i * 8) + 7]};

				gv.setSkinBones(bones, weights);

				tangentList.add(tang);
				skinList.add(new short[] {skin[(i * 8)], skin[(i * 8) + 1], skin[(i * 8) + 2], skin[(i * 8) + 3], skin[(i * 8) + 4], skin[(i * 8) + 5], skin[(i * 8) + 6], skin[(i * 8) + 7]});

			}

			if (!(gv.getVertexGroup() == -1 && infoHolder.isTangentAndSkinSupported())) {
				Matrix matrix = geoset.getMatrix(gv.getVertexGroup());
				if (matrix != null) {
					for (Bone bone : matrix.getBones()) {
						gv.addBoneAttachment(bone);
					}
				}
			}

		}
		geoset.setTangents(tangentList);
		geoset.setSkin(skinList);
		// guys I didn't code this to allow experimental non-triangle faces that were suggested to exist
		// on the web (i.e. quads). if you wanted to fix that, you'd want to do it below
		final int[] facesVertIndices = mdlxGeoset.faces;

		for (int i = 0; i < facesVertIndices.length; i += 3) {
//			Triangle triangle = new Triangle(facesVertIndices[i], facesVertIndices[i + 1], facesVertIndices[i + 2], geoset);
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

	private static void setSkinBones(Geoset geoset, EditableModel model, int i, GeosetVertex gv) {
		if ((ModelUtils.isTangentAndSkinSupported(model.getFormatVersion())) && (geoset.getTangents() != null)) {
			gv.initV900();
			for (int j = 0; j < 4; j++) {
				short boneLookupId = (short) ((geoset.getSkin().get(i)[j] + 256) % 256);

				short boneWeight = (short) ((geoset.getSkin().get(i)[j + 4] + 256) % 256);

				final IdObject idObject = model.getIdObject(boneLookupId);
				if (idObject instanceof Bone) {
					gv.setSkinBone((Bone) idObject, boneWeight, j);
				} else {
					gv.setSkinBone(null, boneWeight, j);
				}
			}
			gv.setTangent(geoset.getTangents().get(i));
		}
	}

	public static void updateToObjects(Geoset geoset, final EditableModel model) {
		// upload the temporary UVLayer and Matrix objects into the vertices themselves
		for (final Matrix m : geoset.getMatrix()) {
			m.updateBones(model);
		}
		List<GeosetVertex> vertices = geoset.getVertices();
		for (GeosetVertex gv : vertices) {
			if (!(gv.getVertexGroup() == -1 && ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))) {
				Matrix mx = geoset.getMatrix(gv.getVertexGroup());
				if (mx != null) {
					int szmx = mx.size();
					gv.clearBoneAttachments();
					for (int m = 0; m < szmx; m++) {
						int boneId = mx.getBoneId(m);
						if ((boneId >= 0) && (boneId < model.getIdObjectsSize())) {
							gv.addBoneAttachment((Bone) model.getIdObject(boneId));
						}
					}
				}
			}
			for (final Triangle triangle : geoset.getTriangles()) {
				if (triangle.containsRef(gv)) {
					gv.addTriangle(triangle);
				}
				triangle.setGeoset(geoset);
			}
			gv.setGeoset(geoset);

			// gv.addBoneAttachment(null);//Why was this here?
		}
		geoset.setParentModel(model);
	}
}
