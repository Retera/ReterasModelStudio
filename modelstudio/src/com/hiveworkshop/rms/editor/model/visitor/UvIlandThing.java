package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class UvIlandThing {

	public static void render(final ModelVisitor renderer, EditableModel model) {
		visit(renderer, model);
//		for (final IdObject object : idObjects) {
		for (final IdObject object : model.getAllObjects()) {
			object.apply(renderer);
		}
		for (final Camera camera : model.getCameras()) {
			renderer.camera(camera);
		}
	}

	public static void visit(final MeshVisitor renderer, EditableModel model) {
		int geosetId = 0;
		for (final Geoset geoset : model.getGeosets()) {
			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
			visitVert(geoset, geosetRenderer, isHd(model, geoset));
			geosetRenderer.geosetFinished();
		}
	}

	public static boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	private static void visitVert(Geoset geoset, GeosetVisitor geosetRenderer, boolean isHD) {
		for (final Triangle triangle : geoset.getTriangles()) {
			final TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
			for (final GeosetVertex vertex : triangle.getVerts()) {
				final VertexVisitor vertexRenderer;
				// TODO redesign for nullable normals
				Vec3 normal = vertex.getNormal() == null ? new Vec3(0, 0, 0) : vertex.getNormal();
				if (isHD) {
					vertexRenderer = triangleRenderer.hdVertex(vertex, normal, vertex.getSkinBoneBones(), vertex.getSkinBoneWeights());
				} else {
					vertexRenderer = triangleRenderer.vertex(vertex, normal, vertex.getBoneAttachments());
				}
				for (final Vec2 tvert : vertex.getTverts()) {
					vertexRenderer.textureCoords(tvert.x, tvert.y);
				}
				vertexRenderer.vertexFinished();
			}
			triangleRenderer.triangleFinished();
		}
	}

//	EditableModel model;
//
//	public void render(final ModelVisitor renderer, EditableModel model) {
//		visit(renderer, model);
////		for (final IdObject object : idObjects) {
//		for (final IdObject object : model.getAllObjects()) {
//			object.apply(renderer);
//		}
//		for (final Camera camera : model.getCameras()) {
//			renderer.camera(camera);
//		}
//	}
//
//	public void visit(final MeshVisitor renderer, EditableModel model) {
//		this.model = model;
//		int geosetId = 0;
//		for (final Geoset geoset : model.getGeosets()) {
//			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
//			visitVert(geoset, geosetRenderer, isHd(geoset));
//			geosetRenderer.geosetFinished();
//		}
//	}
//
//	public boolean isHd(Geoset geoset) {
//		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
//				&& (geoset.getVertices().size() > 0)
//				&& (geoset.getVertex(0).getSkinBoneBones() != null);
//	}
//
//	private void visitVert(Geoset geoset, GeosetVisitor geosetRenderer, boolean isHD) {
//		for (final Triangle triangle : geoset.getTriangles()) {
//			final TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
//			for (final GeosetVertex vertex : triangle.getVerts()) {
//				final VertexVisitor vertexRenderer;
//				// TODO redesign for nullable normals
//				Vec3 normal = vertex.getNormal() == null ? new Vec3(0, 0, 0) : vertex.getNormal();
//				if (isHD) {
//					vertexRenderer = triangleRenderer.hdVertex(vertex, normal, vertex.getSkinBoneBones(), vertex.getSkinBoneWeights());
//				} else {
//					vertexRenderer = triangleRenderer.vertex(vertex, normal, vertex.getBoneAttachments());
//				}
//				for (final Vec2 tvert : vertex.getTverts()) {
//					vertexRenderer.textureCoords(tvert.x, tvert.y);
//				}
//				vertexRenderer.vertexFinished();
//			}
//			triangleRenderer.triangleFinished();
//		}
//	}
}
