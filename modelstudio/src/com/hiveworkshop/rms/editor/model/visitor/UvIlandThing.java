//package com.hiveworkshop.rms.editor.model.visitor;
//
//import com.hiveworkshop.rms.editor.model.*;
//import com.hiveworkshop.rms.editor.model.util.ModelUtils;
//
//public class UvIlandThing {
//
//	public static void render(ModelVisitor renderer, EditableModel model) {
//		renderGeosets(renderer, model);
//		for (IdObject object : model.getAllObjects()) {
//			renderer.visitIdObject(object);
//		}
//		for (final Camera camera : model.getCameras()) {
//			renderer.camera(camera);
//		}
//	}
//
//	public static boolean isHd(EditableModel model, Geoset geoset) {
//		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
//				&& (geoset.getVertices().size() > 0)
//				&& (geoset.getVertex(0).getSkinBoneBones() != null);
//	}
//
//	public static void renderGeosets(MeshVisitor renderer, EditableModel model) {
//		int geosetId = 0;
//		for (final Geoset geoset : model.getGeosets()) {
//			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
//			boolean isHD = isHd(model, geoset);
////			renderGeosetTries(geoset, geosetRenderer, isHD);
//			for (Triangle triangle : geoset.getTriangles()) {
//				TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
//				for (GeosetVertex vertex : triangle.getVerts()) {
//					triangleRenderer.vertex(vertex, isHD);
//				}
//				triangleRenderer.triangleFinished();
//			}
//		}
//	}
//
//	private static void renderGeosetTries(Geoset geoset, GeosetVisitor geosetRenderer, boolean isHD) {
//		for (Triangle triangle : geoset.getTriangles()) {
//			TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
//			for (GeosetVertex vertex : triangle.getVerts()) {
//				triangleRenderer.vertex(vertex, isHD);
//			}
//			triangleRenderer.triangleFinished();
//		}
//	}
//
//	public static void render2(ModelVisitor renderer, EditableModel model) {
//		int geosetId = 0;
//		for (final Geoset geoset : model.getGeosets()) {
//			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
//			boolean isHD = isHd(model, geoset);
////			renderGeosetTries(geoset, geosetRenderer, isHD);
//			for (Triangle triangle : geoset.getTriangles()) {
//				TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
//				for (GeosetVertex vertex : triangle.getVerts()) {
//					triangleRenderer.vertex(vertex, isHD);
//				}
//				triangleRenderer.triangleFinished();
//			}
//		}
//		for (IdObject object : model.getAllObjects()) {
//			renderer.visitIdObject(object);
//		}
//		for (final Camera camera : model.getCameras()) {
//			renderer.camera(camera);
//		}
//	}
//
////	public static void renderGeosets(final MeshVisitor renderer, EditableModel model) {
////		int geosetId = 0;
////		for (final Geoset geoset : model.getGeosets()) {
////			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
////			renderGeosetTries(geoset, geosetRenderer, isHd(model, geoset));
////		}
////	}
////
////	private static void renderGeosetTries(Geoset geoset, GeosetVisitor geosetRenderer, boolean isHD) {
////		for (Triangle triangle : geoset.getTriangles()) {
////			TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
////			for (GeosetVertex vertex : triangle.getVerts()) {
////				triangleRenderer.vertex(vertex, isHD);
////			}
////			triangleRenderer.triangleFinished();
////		}
////	}
//}
