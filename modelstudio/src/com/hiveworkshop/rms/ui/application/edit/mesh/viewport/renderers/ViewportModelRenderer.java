package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewportModelRenderer {
//	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
//	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
//	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);
//	private static final Color CLUSTER_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);
//	private static final Color CLUSTER_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
//	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.75f, 0.45f, 0.3f);
//	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
//
//	private static final Color FACE_SELECTED_COLOR_Line = new Color(1f, 0.45f, 0.45f, 1f);
//	private static final Color FACE_HIGHLIGHT_COLOR_Line = new Color(0.45f, 1f, 0.45f, 1f);
//	private static final Color FACE_NOT_SELECTED_COLOR_Line = new Color(0.45f, 0.45f, 1f, 1f);
//	private static final Color CLUSTER_SELECTED_COLOR_Line = new Color(1f, 0.45f, 0.75f, 1f);
//	private static final Color CLUSTER_HIGHLIGHT_COLOR_Line = new Color(0.45f, 1f, 0.45f, 1f);
//	private static final Color GROUP_SELECTED_COLOR_Line = new Color(1f, 0.75f, 0.45f, 1f);
//	private static final Color GROUP_HIGHLIGHT_COLOR_Line = new Color(0.45f, 1f, 0.45f, 1f);
	private static final int FACE_ALPHA = 30;
	private static final Color FACE_SELECTED_COLOR = new Color(255, 115, 115, FACE_ALPHA);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(115, 255, 115, FACE_ALPHA);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(115, 115, 255, FACE_ALPHA);
	private static final Color FACE_NOT_EDITABLE_COLOR = new Color(115, 115, 115, FACE_ALPHA);
	private static final Color CLUSTER_SELECTED_COLOR = new Color(255, 115, 190, FACE_ALPHA);
	private static final Color CLUSTER_HIGHLIGHT_COLOR = new Color(115, 255, 115, FACE_ALPHA);
	private static final Color GROUP_SELECTED_COLOR = new Color(255, 190, 115, FACE_ALPHA);
	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(115, 255, 115, FACE_ALPHA);

	private static final Color FACE_SELECTED_COLOR_Line = new Color(255, 115, 115, 255);
	private static final Color FACE_HIGHLIGHT_COLOR_Line = new Color(115, 255, 115, 255);
	private static final Color FACE_NOT_SELECTED_COLOR_Line = new Color(115, 115, 255, 255);
	private static final Color FACE_NOT_EDITABLE_COLOR_Line = new Color(115, 115, 115, 255);
	private static final Color CLUSTER_SELECTED_COLOR_Line = new Color(255, 115, 190, 255);
	private static final Color CLUSTER_HIGHLIGHT_COLOR_Line = new Color(115, 255, 115, 255);
	private static final Color GROUP_SELECTED_COLOR_Line = new Color(255, 190, 115, 255);
	private static final Color GROUP_HIGHLIGHT_COLOR_Line = new Color(115, 255, 115, 255);
	private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	private ModelView modelView;
	private RenderModel renderModel;
	boolean isAnimated;
	Color triangleColor;
	Color triangleLineColor;
	int index;
	private Vec2[] triV2 = new Vec2[3];
//	private Vec2[] normalV2 = new Vec2[3];
//	private Map<GeosetVertex, Vec2> editableNotSelectedVerts = new HashMap<>();
//	private Map<GeosetVertex, Vec2> notEditableVerts = new HashMap<>();
	private Map<GeosetVertex, Vec2> normalMap = new HashMap<>();
//	private Map<GeosetVertex, Vec2> selectedVerts = new HashMap<>();
//	private Map<GeosetVertex, Vec2> highlightedVertsMap = new HashMap<>();
	private Map<GeosetVertex, Vec2> vertsMap = new HashMap<>();

//	private Map<Geoset, RenderGeoset> renderGeosetMap = new HashMap<>();


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

//		editableNotSelectedVerts.clear();
//		notEditableVerts.clear();
//		normalMap.clear();
//		selectedVerts.clear();
//		highlightedVertsMap.clear();
		vertsMap.clear();

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			if (modelView.isVisible(geoset)) {
				renderGeoset2(geoset);
			}
		}
		graphics.setColor(ProgramGlobals.getPrefs().getVertexColor());

		for (Geoset geoset : modelView.getEditableGeosets()) {
			drawVerts(graphics, geoset.getVertices());
		}
		graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
		drawVerts(graphics, modelView.getSelectedVertices());

		graphics.setColor(ProgramGlobals.getPrefs().getHighlighVertexColor());
		if (modelView.getHighlightedGeoset() != null) {
			drawVerts(graphics, modelView.getHighlightedGeoset().getVertices());
		}
		for (IdObject object : model.getIdObjects()) {
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
			if (modelView.isVisible(camera)) {
				renderCamera(camera);
			}
		}
	}

	public void drawVerts(Graphics2D graphics, Collection<GeosetVertex> vertices) {
		for (GeosetVertex v : vertices) {
			if(vertsMap.get(v) != null){
				GU.fillCenteredSquare(graphics, vertsMap.get(v), ProgramGlobals.getPrefs().getVertexSize());
			}
		}
	}

	private boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

//	private void renderGeoset2(Geoset geoset, Function<GeosetVertex, Boolean> passFunction) {
	private void renderGeoset2(Geoset geoset) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geoset);
		for (RenderGeoset.RenderVert vertex : renderGeoset.getRenderVerts()) {

			Vec2 vert2 = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex.getRenderPos());
			vertsMap.put(vertex.getVertex(), vert2);

			if (ProgramGlobals.getPrefs().showNormals()) {
				Vec3 normalPoint = Vec3.getScaled(vertex.getRenderNorm(), (float) (12 / coordinateSystem.getZoom())).add(vertex.getRenderPos());
				normalMap.put(vertex.getVertex(), CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint));
			}

		}
		Color hlCol = ProgramGlobals.getPrefs().getHighlighTriangleColor();
		Color hlCol2 = new Color(hlCol.getRed(), hlCol.getBlue(), hlCol.getGreen(), FACE_ALPHA);
		for (Triangle triangle : geoset.getTriangles()) {
			GeosetVertex v0 = triangle.get(0);
			GeosetVertex v1 = triangle.get(1);
			GeosetVertex v2 = triangle.get(2);
			triV2[0] = vertsMap.get(v0);
			triV2[1] = vertsMap.get(v1);
			triV2[2] = vertsMap.get(v2);

			if (modelView.getHighlightedGeoset() == geoset) {
				triangleColor = hlCol2;
			} else if (modelView.isHidden(v0) || modelView.isHidden(v1) || modelView.isHidden(v2) || !modelView.isEditable(geoset)) {
				triangleColor = FACE_NOT_EDITABLE_COLOR;
			} else if (modelView.isSelected(v0) && modelView.isSelected(v1) && modelView.isSelected(v2)) {
				triangleColor = FACE_SELECTED_COLOR;
			} else {
				triangleColor = FACE_NOT_SELECTED_COLOR;
			}


			for(int i = 0; i<4; i++){
				GeosetVertex gv0 = triangle.get(i%3);
				GeosetVertex gv1 = triangle.get((i+1)%3);
				if (modelView.getHighlightedGeoset() == geoset) {
					triangleLineColor = hlCol;
				} else if (modelView.isHidden(gv0) || modelView.isHidden(gv1) || !modelView.isEditable(geoset)) {
					triangleLineColor = FACE_NOT_EDITABLE_COLOR_Line;
				} else if (modelView.isSelected(gv0) && modelView.isSelected(gv1)) {
					triangleLineColor = FACE_SELECTED_COLOR_Line;
				} else {
					triangleLineColor = FACE_NOT_SELECTED_COLOR_Line;
				}

				if(triV2[0] != null && triV2[1] != null && triV2[2] != null){
					graphics.setColor(triangleColor);
					GU.fillPolygon(graphics, triV2);
					graphics.setColor(triangleLineColor);
					GU.drawLines(graphics, triV2[i%3], triV2[(i+1)%3]);
				}
			}
		}
	}

	public Color getTriangleColor(int i) {
		return switch (i){
			case 1 -> FACE_SELECTED_COLOR;
			default -> FACE_NOT_SELECTED_COLOR;
		};
//		return triangleColor;
	}
	public Color getTriangleEdgeColor(int i) {
		return switch (i){
			case 1 -> FACE_SELECTED_COLOR;
			default -> FACE_NOT_SELECTED_COLOR;
		};
//		return triangleColor;
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
		if (renderModel != null && renderModel.getTimeEnvironment() != null) {
			renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getTimeEnvironment());
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
