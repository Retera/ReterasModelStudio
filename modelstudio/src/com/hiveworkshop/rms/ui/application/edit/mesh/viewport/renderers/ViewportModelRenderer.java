package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ViewportModelRenderer {
	private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	private ModelView modelView;
	private RenderModel renderModel;
	boolean isAnimated;
	Color triangleColor;
	int index;
	private Vec2[] triV2 = new Vec2[3];
	private Vec2[] normalV2 = new Vec2[3];
	private Map<GeosetVertex, Vec2> vertsMap = new HashMap<>();
	private Map<GeosetVertex, Vec2> normalMap = new HashMap<>();
	private Map<GeosetVertex, Vec2> selectedVertsMap = new HashMap<>();
	private Map<GeosetVertex, Vec2> highlightedVertsMap = new HashMap<>();


	public ViewportModelRenderer(int vertexSize) {
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public void renderModel(Graphics2D graphics,
	                        CoordinateSystem coordinateSystem,
	                        ModelHandler modelHandler, boolean isAnimated) {
		this.isAnimated = isAnimated;
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelHandler.getModelView();
		this.renderModel = modelHandler.getRenderModel();
//		idObjectRenderer.reset(coordinateSystem, graphics, modelHandler.getRenderModel(), this.isAnimated, false);
		idObjectRenderer.reset(coordinateSystem, graphics, modelHandler.getRenderModel(), this.isAnimated);

		vertsMap.clear();
		normalMap.clear();
		selectedVertsMap.clear();
		highlightedVertsMap.clear();

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			if (modelView.isVisible(geoset) && modelView.isEditable(geoset)) {
				renderGeoset(geoset, isHd(model, geoset));
			}
		}
		graphics.setColor(ProgramGlobals.getPrefs().getVertexColor());
		for (Vec2 v : vertsMap.values()) {
			GU.fillCenteredSquare(graphics, v, ProgramGlobals.getPrefs().getVertexSize());
		}
		graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
		for (Vec2 v : selectedVertsMap.values()) {
			GU.fillCenteredSquare(graphics, v, ProgramGlobals.getPrefs().getVertexSize());
		}
		graphics.setColor(ProgramGlobals.getPrefs().getHighlighVertexColor());
		for (Vec2 v : highlightedVertsMap.values()) {
			GU.fillCenteredSquare(graphics, v, ProgramGlobals.getPrefs().getVertexSize());
		}
		for (IdObject object : model.getAllObjects()) {
			if (modelView.isVisible(object) || (object == modelView.getHighlightedNode())) {
//				idObjectRenderer.renderObject(modelView.getHighlightedNode() == object, object);
				// ToDo mark children of selected parent
				idObjectRenderer.renderObject(modelView.getHighlightedNode() == object, modelView.isSelected(object), object);

//				idObjectRenderer.renderObject(coordinateSystem, graphics, renderModel, isAnimated, modelView.getHighlightedNode() == object, object);
//				idObjectRenderer.renderIdObject(object);
			}
//			if (modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode())) {
//				idObjectRenderer.renderObject(coordinateSystem, graphics, renderModel, isAnimated, modelView.getHighlightedNode() == object, object);
////				idObjectRenderer.renderIdObject(object);
//			}
		}
		for (final Camera camera : model.getCameras()) {
//			idObjectRenderer.camera(camera);
			renderCamera(camera);
		}
	}

	private boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	private void renderGeoset(Geoset geoset, boolean isHD) {
//		if (modelView.getEditableGeosets().contains(geoset)
//				|| (modelView.getHighlightedGeoset() == geoset)
//				|| modelView.getVisibleGeosets().contains(geoset)) {
//			System.out.println("woop");
//		}
		if (modelView.getHighlightedGeoset() == geoset) {
			triangleColor = ProgramGlobals.getPrefs().getHighlighTriangleColor();
		} else if (!modelView.getEditableGeosets().contains(geoset)) {
			triangleColor = ProgramGlobals.getPrefs().getVisibleUneditableColor();
		} else {
//			triangleColor = ProgramGlobals.getPrefs().getTriangleColor();
			triangleColor = new Color(1f, 0.75f, 0.45f, 0.3f);
		}

		for (GeosetVertex vertex : geoset.getVertices()) {
			Vec3 vertexSumHeap = vertex;
			Vec3 normal = vertex.getNormal();
			Vec3 normalSumHeap = normal;
			if (isAnimated) {
				Mat4 bonesMatrixSumHeap = ModelUtils.processBones(renderModel, vertex, geoset);
				vertexSumHeap = Vec3.getTransformed(vertex, bonesMatrixSumHeap);
				if (normal != null) {
					normalSumHeap = Vec3.getTransformed(normal, bonesMatrixSumHeap);
					normalSumHeap.normalize();
				}
			}

			Vec2 vert2 = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexSumHeap);

			if (modelView.getHighlightedGeoset() == geoset) {
				highlightedVertsMap.put(vertex, vert2);
			} else if (!modelView.isSelected(vertex)) {
				vertsMap.put(vertex, vert2);
			} else {
				selectedVertsMap.put(vertex, vert2);
			}

			if (ProgramGlobals.getPrefs().showNormals() && normal != null) {
				Vec3 normalPoint = Vec3.getScaled(normalSumHeap, (float) (12 / coordinateSystem.getZoom())).add(vertexSumHeap);

				normalMap.put(vertex, CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint));
//				normalV2[index] = CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint);
			}

		}

		for (Triangle triangle : geoset.getTriangles()) {
			index = 0;
			triangleColor = new Color(1f, 0.75f, 0.45f, 0.3f);

			for (GeosetVertex vertex : triangle.getVerts()) {

				if (modelView.getHighlightedGeoset() == geoset) {
					triV2[index] = highlightedVertsMap.get(vertex);
					triangleColor = ProgramGlobals.getPrefs().getHighlighTriangleColor();
				} else if (!modelView.isSelected(vertex)) {
					triV2[index] = vertsMap.get(vertex);
					triangleColor = ProgramGlobals.getPrefs().getTriangleColor();
				} else {
					triV2[index] = selectedVertsMap.get(vertex);
				}

				if (normalMap.containsKey(vertex)) {
					normalV2[index] = normalMap.get(vertex);
				}
				index++;

			}

			graphics.setColor(triangleColor);
			GU.drawPolygon(graphics, triV2);

			if (ProgramGlobals.getPrefs().showNormals()) {
				graphics.setColor(ProgramGlobals.getPrefs().getNormalsColor());

				GU.drawLines(graphics, triV2, normalV2);
			}
		}
	}

	public void renderCamera(Camera camera) {
		graphics.setColor(Color.GREEN.darker());
		Graphics2D g2 = ((Graphics2D) graphics.create());

		Vec3 vec3Start = new Vec3(camera.getPosition());
		Mat4 worldMatrix = getWorldMatrix(camera.getSourceNode());
		if (worldMatrix != null) {
			vec3Start.transform(worldMatrix);
		}

		Vec3 vec3End = new Vec3(camera.getTargetPosition());
		worldMatrix = getWorldMatrix(camera.getTargetNode());
		if (worldMatrix != null) {
			vec3Start.transform(worldMatrix);
		}

		float renderRotationScalar = 0;
		if (renderModel != null && renderModel.getAnimatedRenderEnvironment() != null) {
			renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());
		}

		renderableCameraProp.render(g2, coordinateSystem, vec3Start, vec3End, renderRotationScalar);

//		Point start = CoordSysUtils.convertToViewPoint(coordinateSystem, position);
//		Point end = CoordSysUtils.convertToViewPoint(coordinateSystem, targetPosition);
//
//		g2.translate(end.x, end.y);
//		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
//		double zoom = CoordSysUtils.getZoom(coordinateSystem);
//		int size = (int) (20 * zoom);
//		double dist = start.distance(end);
//
//		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawRect((int) dist - size, -size, size * 2, size * 2);
//
//		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawLine(0, 0, size, size);
//		g2.drawLine(0, 0, size, -size);
//
//		g2.drawLine(0, 0, (int) dist, 0);
	}

	public Mat4 getWorldMatrix(AnimatedNode object) {
		if (!isAnimated || renderModel == null || renderModel.getRenderNode(object) == null) {
			return null;
		}
		return renderModel.getRenderNode(object).getWorldMatrix();
	}


//	public void renderSelectionPiv(ModelElementRenderer renderer, CoordinateSystem coordinateSystem, ModelView modelView) {
////		Set<Vec3> drawnSelection = new HashSet<>();
////		for (IdObject object : modelView.getSelectedIdObjects()) {
////			renderer.renderIdObject(object);
////			drawnSelection.add(object.getPivotPoint());
////		}
////		for (Camera camera : modelView.getEditableCameras()) {
////			Color targetColor = selection.contains(camera.getTargetPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker();
////			Color boxColor = selection.contains(camera.getPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker();
////			renderer.renderCamera(camera, boxColor, camera.getPosition(), targetColor, camera.getTargetPosition());
////			drawnSelection.add(camera.getPosition());
////			drawnSelection.add(camera.getTargetPosition());
////		}
////		for (Vec3 vertex : selection) {
////			if (!drawnSelection.contains(vertex)) {
////				renderBoneDummy.setPivotPoint(vertex);
////				renderer.renderIdObject(renderBoneDummy);
////			}
////		}
//
////		Set<Vec3> drawnSelection = new HashSet<>();
////		for (IdObject object : modelView.getEditableIdObjects()) {
////			if (selection.contains(object.getPivotPoint())) {
////				renderer.renderIdObject(object);
////				drawnSelection.add(object.getPivotPoint());
////			}
////		}
////		for (Camera camera : modelView.getEditableCameras()) {
////			renderer.renderCamera(camera, selection.contains(camera.getPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(), camera.getPosition(), selection.contains(camera.getTargetPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(), camera.getTargetPosition());
////			drawnSelection.add(camera.getPosition());
////			drawnSelection.add(camera.getTargetPosition());
////		}
////		for (Vec3 vertex : selection) {
////			if (!drawnSelection.contains(vertex)) {
////				renderBoneDummy.setPivotPoint(vertex);
////				renderer.renderIdObject(renderBoneDummy);
////			}
////		}
//	}

//private final Bone renderBoneDummy = new Bone();
//	public void renderSelectionTpos(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
//	                            ModelView model) {
////		Set<IdObject> drawnSelection = new HashSet<>();
////		Set<IdObject> parentedNonSelection = new HashSet<>();
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (selection.contains(object)) {
////				renderer.renderIdObject(object);
////				drawnSelection.add(object);
////			} else {
////				IdObject parent = object.getParent();
////				while (parent != null) {
////					if (selection.contains(parent)) {
////						parentedNonSelection.add(object);
////					}
////					parent = parent.getParent();
////				}
////			}
////		}
////		for (IdObject selectedObject : selection) {
////			if (!drawnSelection.contains(selectedObject)) {
////				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
////				renderer.renderIdObject(renderBoneDummy);
////				drawnSelection.add(selectedObject);
////			}
////		}
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
////				renderer.renderIdObject(object);
////			}
////		}
//	}

	//	@Override
//	public void renderSelectionAnimNode(ModelElementRenderer renderer, CoordinateSystem coordinateSystem, ModelView model) {
////		// TODO !!! apply rendering
////		Set<IdObject> drawnSelection = new HashSet<>();
////		Set<IdObject> parentedNonSelection = new HashSet<>();
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (modelView.getSelectedIdObjects().contains(object)) {
////				renderer.renderIdObject(object);
////				drawnSelection.add(object);
////			} else {
////				IdObject parent = object.getParent();
////				while (parent != null) {
////					if (modelView.getSelectedIdObjects().contains(parent)) {
////						parentedNonSelection.add(object);
////					}
////					parent = parent.getParent();
////				}
////			}
////		}
////		for (IdObject selectedObject : modelView.getSelectedIdObjects()) {
////			if (!drawnSelection.contains(selectedObject)) {
////				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
////				renderer.renderIdObject(renderBoneDummy);
////				drawnSelection.add(selectedObject);
////			}
////		}
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
////				renderer.renderIdObject(object);
////			}
////		}
//	}
}
