package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;

public class RecalculateTangents extends ActionFunction {
	public RecalculateTangents(){
		super(TextKey.RECALC_TANGENTS, () -> recalculateTangents());
	}

	public static void recalculateTangents() {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		// copied from
		// https://github.com/TaylorMouse/MaxScripts/blob/master/Warcraft%203%20Reforged/GriffonStudios/GriffonStudios_Warcraft_3_Reforged_Export.ms#L169
		int zeroAreaUVTris = 0;
		TempSaveModelStuff.doSavePreps(model); // I wanted to use VertexId on the triangle
		for (Geoset geoset : model.getGeosets()) {
			double[][] tan1 = new double[geoset.getVertices().size()][];
			double[][] tan2 = new double[geoset.getVertices().size()][];
			for (int nFace = 0; nFace < geoset.getTriangles().size(); nFace++) {
				Triangle triangle = geoset.getTriangle(nFace);

				GeosetVertex v1 = triangle.get(0);
				GeosetVertex v2 = triangle.get(1);
				GeosetVertex v3 = triangle.get(2);

				Vec3 vv1 = Vec3.getDiff(v2, v1);
				double x1 = v2.x - v1.x;
				double y1 = v2.y - v1.y;
				double z1 = v2.z - v1.z;


				Vec3 vv2 = Vec3.getDiff(v3, v1);
				double x2 = v3.x - v1.x;
				double y2 = v3.y - v1.y;
				double z2 = v3.z - v1.z;

				Vec2 w1 = v1.getTVertex(0);
				Vec2 w2 = v2.getTVertex(0);
				Vec2 w3 = v3.getTVertex(0);

				Vec2 st1 = Vec2.getDif(w2, w1);
				double s1 = w2.x - w1.x;
				double t1 = w2.y - w1.y;

				Vec2 st2 = Vec2.getDif(w3, w1);
				double s2 = w3.x - w1.x;
				double t2 = w3.y - w1.y;


				double tVertWeight = (s1 * t2) - (s2 * t1);
				if (tVertWeight == 0) {
					tVertWeight = 0.00000001;
					zeroAreaUVTris++;
				}

				double r = 1.0 / tVertWeight;

				double[] sdir = {((t2 * x1) - (t1 * x2)) * r, ((t2 * y1) - (t1 * y2)) * r, ((t2 * z1) - (t1 * z2)) * r};
				double[] tdir = {((s1 * x2) - (s2 * x1)) * r, ((s1 * y2) - (s2 * y1)) * r, ((s1 * z2) - (s2 * z1)) * r};

				tan1[triangle.getId(0)] = sdir;
				tan1[triangle.getId(1)] = sdir;
				tan1[triangle.getId(2)] = sdir;

				tan2[triangle.getId(0)] = tdir;
				tan2[triangle.getId(1)] = tdir;
				tan2[triangle.getId(2)] = tdir;
			}
			for (int vertexId = 0; vertexId < geoset.getVertices().size(); vertexId++) {
				GeosetVertex gv = geoset.getVertex(vertexId);
				Vec3 n = gv.getNormal();
				Vec3 t = new Vec3(tan1[vertexId]);

//				Vec3 v = new Vec3(t).sub(n).scale(n.dot(t)).normalize();
				Vec3 v = Vec3.getDiff(t, n).normalize();
				Vec3 cross = Vec3.getCross(n, t);

				Vec3 tanAsVert = new Vec3(tan2[vertexId]);

				double w = cross.dot(tanAsVert);

				if (w < 0.0) {
					w = -1.0;
				} else {
					w = 1.0;
				}
				gv.setTangent(v, (float) w);
			}
		}
		int goodTangents = 0;
		int badTangents = 0;
		for (Geoset theMesh : model.getGeosets()) {
			for (GeosetVertex gv : theMesh.getVertices()) {
				double dotProduct = gv.getNormal().dot(gv.getTang().getVec3());
//				System.out.println("dotProduct: " + dotProduct);
				if (Math.abs(dotProduct) <= 0.000001) {
					goodTangents += 1;
				} else {
					badTangents += 1;
				}
			}
		}
		JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
				"Tangent generation completed." +
						"\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents + "" +
						"\nFound " + zeroAreaUVTris + " uv triangles with no area");
//		if (parent != null) {
//		} else {
//			System.out.println(
//					"Tangent generation completed." +
//							"\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents +
//							"\nFound " + zeroAreaUVTris + " uv triangles with no area");
//		}
	}

	public static void recalculateTangentsOld(EditableModel currentMDL) {
		for (Geoset theMesh : currentMDL.getGeosets()) {
			for (int nFace = 0; nFace < theMesh.getTriangles().size(); nFace++) {
				Triangle face = theMesh.getTriangle(nFace);

				GeosetVertex v1 = face.getVerts()[0];
				GeosetVertex v2 = face.getVerts()[0];
				GeosetVertex v3 = face.getVerts()[0];

				Vec2 uv1 = v1.getTVertex(0);
				Vec2 uv2 = v2.getTVertex(0);
				Vec2 uv3 = v3.getTVertex(0);

				Vec3 dV1 = new Vec3(v1).sub(v2);
				Vec3 dV2 = new Vec3(v1).sub(v3);

				Vec2 dUV1 = new Vec2(uv1).sub(uv2);
				Vec2 dUV2 = new Vec2(uv1).sub(uv3);
				double area = (dUV1.x * dUV2.y) - (dUV1.y * dUV2.x);
				int sign = (area < 0) ? -1 : 1;
				Vec3 tangent = new Vec3(1, 0, 0);

				tangent.x = (dV1.x * dUV2.y) - (dUV1.y * dV2.x);
				tangent.y = (dV1.y * dUV2.y) - (dUV1.y * dV2.y);
				tangent.z = (dV1.z * dUV2.y) - (dUV1.y * dV2.z);

				tangent.normalize();
				tangent.scale(sign);

				Vec3 faceNormal = new Vec3(v1.getNormal());
				faceNormal.add(v2.getNormal());
				faceNormal.add(v3.getNormal());
				faceNormal.normalize();
			}
		}
	}
}
