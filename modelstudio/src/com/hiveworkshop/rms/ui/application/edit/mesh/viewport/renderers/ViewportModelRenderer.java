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
import java.util.ArrayList;
import java.util.List;

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
	private List<Vec2> verts = new ArrayList<>();
	private List<Vec2> selectedVerts = new ArrayList<>();
	private List<Vec2> highlightedVerts = new ArrayList<>();

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
		verts.clear();
		selectedVerts.clear();
		highlightedVerts.clear();

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			if (modelView.isVisible(geoset)) {
				renderGeoset(geoset, isHd(model, geoset));
			}
		}
		graphics.setColor(ProgramGlobals.getPrefs().getVertexColor());
		for (Vec2 v : verts) {
			GU.fillCenteredSquare(graphics, v, ProgramGlobals.getPrefs().getVertexSize());
		}
		graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
		for (Vec2 v : selectedVerts) {
			GU.fillCenteredSquare(graphics, v, ProgramGlobals.getPrefs().getVertexSize());
		}
		graphics.setColor(ProgramGlobals.getPrefs().getHighlighVertexColor());
		for (Vec2 v : highlightedVerts) {
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

		for (Triangle triangle : geoset.getTriangles()) {
			index = 0;
			triangleColor = new Color(1f, 0.75f, 0.45f, 0.3f);

			for (GeosetVertex vertex : triangle.getVerts()) {

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
				triV2[index] = vert2;
				if (modelView.getHighlightedGeoset() == geoset) {
					triangleColor = ProgramGlobals.getPrefs().getHighlighTriangleColor();
					highlightedVerts.add(vert2);
				} else if (!modelView.isSelected(vertex)) {
					triangleColor = ProgramGlobals.getPrefs().getTriangleColor();
					verts.add(vert2);
				} else {
					selectedVerts.add(vert2);
				}

				if (ProgramGlobals.getPrefs().showNormals() && normal != null) {
					Vec3 normalPoint = Vec3.getScaled(normalSumHeap, (float) (12 / coordinateSystem.getZoom())).add(vertexSumHeap);

					normalV2[index] = CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint);
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
}
