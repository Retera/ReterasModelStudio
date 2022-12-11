package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewportModelRenderer {
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
	private Map<GeosetVertex, Vec2> normalMap = new HashMap<>();
	private Map<GeosetVertex, Vec2> vertsMap = new HashMap<>();



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
		idObjectRenderer.reset(coordinateSystem, graphics, modelHandler.getRenderModel(), this.isAnimated);

		vertsMap.clear();

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			if (modelView.isVisible(geoset)) {
				renderGeoset2(geoset);
			}
		}

		graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
		for (Geoset geoset : modelView.getEditableGeosets()) {
			drawVerts(graphics, geoset.getVertices());
		}

		graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX_SELECTED));
		drawVerts(graphics, modelView.getSelectedVertices());

		graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX_HIGHLIGHTED));
		if (modelView.getHighlightedGeoset() != null) {
			drawVerts(graphics, modelView.getHighlightedGeoset().getVertices());
		}
		for (IdObject object : model.getIdObjects()) {
			if (modelView.isVisible(object) || (object == modelView.getHighlightedNode())) {
				// ToDo mark children of selected parent
				idObjectRenderer.renderObject(modelView.getHighlightedNode() == object, modelView.isSelected(object), object);
			}
		}
		for (final Camera camera : model.getCameras()) {
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

	private void renderGeoset2(Geoset geoset) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geoset);
		for (RenderGeoset.RenderVert vertex : renderGeoset.getRenderVerts()) {

			Vec2 vert2 = convertToViewVec2(coordinateSystem, vertex.getRenderPos());
			vertsMap.put(vertex.getVertex(), vert2);

			if (ProgramGlobals.getPrefs().showNormals()) {
				Vec3 normalPoint = Vec3.getScaled(vertex.getRenderNorm(), (float) (12 / coordinateSystem.getZoom())).add(vertex.getRenderPos());
				normalMap.put(vertex.getVertex(), convertToViewVec2(coordinateSystem, normalPoint));
			}

		}
//		Color hlCol = ProgramGlobals.getPrefs().getHighlighTriangleColor();
//		Color hlCol2 = new Color(hlCol.getRed(), hlCol.getBlue(), hlCol.getGreen(), FACE_ALPHA);
		for (Triangle triangle : geoset.getTriangles()) {
			GeosetVertex v0 = triangle.get(0);
			GeosetVertex v1 = triangle.get(1);
			GeosetVertex v2 = triangle.get(2);
			triV2[0] = vertsMap.get(v0);
			triV2[1] = vertsMap.get(v1);
			triV2[2] = vertsMap.get(v2);

			if (modelView.getHighlightedGeoset() == geoset) {
				triangleColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA_HIGHLIGHTED);
			} else if (modelView.isHidden(v0) || modelView.isHidden(v1) || modelView.isHidden(v2) || !modelView.isEditable(triangle)) {
				triangleColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA_UNEDITABLE);
			} else if (modelView.isSelected(v0) && modelView.isSelected(v1) && modelView.isSelected(v2)) {
				triangleColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA_SELECTED);
			} else {
				triangleColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA);
			}


			for(int i = 0; i<4; i++){
				GeosetVertex gv0 = triangle.get(i%3);
				GeosetVertex gv1 = triangle.get((i+1)%3);
				if (modelView.getHighlightedGeoset() == geoset) {
					triangleLineColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
				} else if (modelView.isHidden(gv0) || modelView.isHidden(gv1) || !modelView.isEditable(geoset)) {
					triangleLineColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_UNEDITABLE);
				} else if (modelView.isSelected(gv0) && modelView.isSelected(gv1)) {
					triangleLineColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_SELECTED);
				} else {
					triangleLineColor = ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE);
				}

				if (triV2[0] != null && triV2[1] != null && triV2[2] != null) {
					graphics.setColor(triangleColor);
					GU.fillPolygon(graphics, triV2);
					graphics.setColor(triangleLineColor);
					GU.drawLines(graphics, triV2[i % 3], triV2[(i + 1) % 3]);
				}
			}
		}
	}

	public static Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, Vec3 vertex) {
		Vec2 pointA = coordinateSystem.viewVN(vertex);
		return pointA.set(pointA.x * coordinateSystem.getParentWidth(), pointA.y * coordinateSystem.getParentHeight());
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

	}

	public Mat4 getWorldMatrix(AnimatedNode object) {
		if (!isAnimated || renderModel == null || renderModel.getRenderNode(object) == null) {
			return null;
		}
		return renderModel.getRenderNode(object).getWorldMatrix();
	}

}
