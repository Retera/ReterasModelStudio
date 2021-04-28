package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.UVVPGeosetRendererImpl;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class UVViewportModelRenderer implements MeshVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final UVVPGeosetRendererImpl geosetRenderer;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	// TODO Now that I added modelView to this class, why does RenderByViewModelRenderer exist???
	private ModelView modelView;
	private int uvLayerIndex;

	public UVViewportModelRenderer() {
		geosetRenderer = new UVVPGeosetRendererImpl();
	}

	public UVViewportModelRenderer reset(final Graphics2D graphics, final ProgramPreferences programPreferences,
	                                     final ViewportView viewportView, final CoordinateSystem coordinateSystem, final ModelView modelView) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		return this;
	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final Material material, final GeosetAnim geosetAnim) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == modelView.getModel().getGeoset(geosetId)) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		}
		return geosetRenderer.reset(graphics, coordinateSystem, uvLayerIndex);
	}

}
