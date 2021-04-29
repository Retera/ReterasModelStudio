package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.AnimVPGeosetRendererImpl;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class AnimatedViewportModelRenderer implements ModelVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	//	private final GeosetRendererImpl geosetRenderer;
	private final AnimVPGeosetRendererImpl geosetRenderer;
	private final int vertexSize;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	private final ResettableAnimatedIdObjectRenderer idObjectRenderer;
	// TODO Now that I added modelView to this class, why does RenderByViewModelRenderer exist???
	private ModelView modelView;
	private RenderModel renderModel;

	public AnimatedViewportModelRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
//		geosetRenderer = new GeosetRendererImpl();
		geosetRenderer = new AnimVPGeosetRendererImpl();
		idObjectRenderer = new ResettableAnimatedIdObjectRenderer(vertexSize);
	}

	public AnimatedViewportModelRenderer reset(Graphics2D graphics,
	                                           ProgramPreferences programPreferences,
	                                           ViewportView viewportView,
	                                           CoordinateSystem coordinateSystem,
	                                           ModelView modelView,
	                                           RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		this.renderModel = renderModel;
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences.getLightsColor(), programPreferences.getAnimatedBoneUnselectedColor(), NodeIconPalette.UNSELECTED, renderModel, programPreferences.isUseBoxesForPivotPoints());
//		geosetRenderer.reset(graphics, programPreferences, xDimension, yDimension, coordinateSystem, renderModel);
		return this;
	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == modelView.getModel().getGeoset(geosetId)) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		} else {
			Geoset geoset = modelView.getModel().getGeoset(geosetId);
			if (!modelView.getEditableGeosets().contains(geoset)) {
				graphics.setColor(programPreferences.getVisibleUneditableColor());
			}
		}
		return geosetRenderer.reset(graphics, programPreferences, coordinateSystem, renderModel);
	}

	private void resetIdObjectRendererWithNode(IdObject object) {
		idObjectRenderer.reset(coordinateSystem, graphics,
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getLightsColor(),
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor() : programPreferences.getAnimatedBoneUnselectedColor(),
				modelView.getHighlightedNode() == object ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED,
				renderModel, programPreferences.isUseBoxesForPivotPoints());
	}

	@Override
	public void visitIdObject(IdObject object) {
		resetIdObjectRendererWithNode(object);
		idObjectRenderer.visitIdObject(object);
	}

	@Override
	public void camera(Camera cam) {
		idObjectRenderer.camera(cam);
	}

}
