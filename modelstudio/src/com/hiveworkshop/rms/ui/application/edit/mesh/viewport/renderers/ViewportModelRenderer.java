package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.model.visitor.VPGeosetRenderer;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class ViewportModelRenderer implements ModelVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final VPGeosetRenderer geosetRenderer;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	private ModelView modelView;
	private RenderModel renderModel;
	private ModelHandler modelHandler;
	boolean isAnimated;

	public ViewportModelRenderer(int vertexSize) {
		geosetRenderer = new VPGeosetRenderer();
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public ViewportModelRenderer reset(Graphics2D graphics,
	                                   ProgramPreferences programPreferences,
	                                   CoordinateSystem coordinateSystem,
	                                   ModelHandler modelHandler, boolean isAnimated) {
		this.modelHandler = modelHandler;
		this.isAnimated = isAnimated;
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelHandler.getModelView();
		this.renderModel = modelHandler.getRenderModel();
		idObjectRenderer.reset(coordinateSystem, graphics,
				programPreferences,
				modelHandler.getRenderModel(), this.isAnimated, false);

		EditableModel model = modelHandler.getModel();
		for (final Geoset geoset : model.getGeosets()) {
			beginGeoset(geoset, isHd(model, geoset));
		}
		for (IdObject object : model.getAllObjects()) {
			visitIdObject(object);
		}
		for (final Camera camera : model.getCameras()) {
			camera(camera);
		}

		return this;
	}

	public boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	public void beginGeoset(Geoset geoset, boolean isHD) {
//		if (modelView.getEditableGeosets().contains(geoset)
//				|| (modelView.getHighlightedGeoset() == geoset)
//				|| modelView.getVisibleGeosets().contains(geoset)) {
//			System.out.println("woop");
//		}
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == geoset) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		} else {
			if (!modelView.getEditableGeosets().contains(geoset)) {
				graphics.setColor(programPreferences.getVisibleUneditableColor());
			}
		}
		geosetRenderer.reset(graphics, programPreferences, coordinateSystem, renderModel, geoset, isAnimated);
		geosetRenderer.beginTriangle(isHD);
	}

	@Override
	public void visitIdObject(IdObject object) {
		if (isVisibleNode(object)) {
			resetIdObjectRendererWithNode(object);
			idObjectRenderer.visitIdObject(object);
		}
	}

	@Override
	public void camera(Camera cam) {
		idObjectRenderer.camera(cam);
	}

	private void resetIdObjectRendererWithNode(IdObject object) {
//		idObjectRenderer.reset(coordinateSystem, graphics,
//				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getLightsColor(),
//				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getAnimatedBoneUnselectedColor(),
//				modelView.getHighlightedNode() == object ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED,
//				renderModel, programPreferences.isUseBoxesForPivotPoints(), isAnimated);
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences, renderModel, isAnimated, modelView.getHighlightedNode() == object);
	}

	private boolean isVisibleNode(IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode());
	}
}
