package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public class ViewportModelRenderer {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	private ModelView modelView;
	private RenderModel renderModel;
	boolean isAnimated;
	Color triangleColor;
	int index;
	private Vec2[] triV2 = new Vec2[3];
	private Vec2[] normalV2 = new Vec2[3];

	public ViewportModelRenderer(int vertexSize) {
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public void renderModel(Graphics2D graphics,
	                        ProgramPreferences programPreferences,
	                        CoordinateSystem coordinateSystem,
	                        ModelHandler modelHandler, boolean isAnimated) {
		this.isAnimated = isAnimated;
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelHandler.getModelView();
		this.renderModel = modelHandler.getRenderModel();
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences, modelHandler.getRenderModel(), this.isAnimated, false);

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			renderGeoset(geoset, isHd(model, geoset));
		}
		for (IdObject object : model.getAllObjects()) {
			if (modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode())) {
				idObjectRenderer.reset(coordinateSystem, graphics, programPreferences, renderModel, isAnimated, modelView.getHighlightedNode() == object);
				idObjectRenderer.visitIdObject(object);
			}
		}
		for (final Camera camera : model.getCameras()) {
			idObjectRenderer.camera(camera);
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
			triangleColor = programPreferences.getHighlighTriangleColor();
		} else if (!modelView.getEditableGeosets().contains(geoset)) {
			triangleColor = programPreferences.getVisibleUneditableColor();
		} else {
			triangleColor = programPreferences.getTriangleColor();
		}

		for (Triangle triangle : geoset.getTriangles()) {
			index = 0;

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

				if (programPreferences.showNormals() && normal != null) {
					Vec3 normalPoint = Vec3.getScaled(normalSumHeap, (float) (12 / coordinateSystem.getZoom())).add(vertexSumHeap);

					normalV2[index] = CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint);
				}
				index++;

			}

			graphics.setColor(triangleColor);
			GU.drawPolygon(graphics, triV2);

			if (programPreferences.showNormals()) {
				graphics.setColor(programPreferences.getNormalsColor());

				GU.drawLines(graphics, triV2, normalV2);
			}
		}
	}
}
