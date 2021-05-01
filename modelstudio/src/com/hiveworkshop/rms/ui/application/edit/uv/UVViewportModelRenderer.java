package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.UVVPGeosetRendererImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class UVViewportModelRenderer implements MeshVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final UVVPGeosetRendererImpl geosetRenderer;
	private CoordinateSystem coordinateSystem;
	private ModelHandler modelHandler;

	public UVViewportModelRenderer() {
		geosetRenderer = new UVVPGeosetRendererImpl();
	}

	public UVViewportModelRenderer reset(Graphics2D graphics,
	                                     ProgramPreferences programPreferences,
	                                     CoordinateSystem coordinateSystem,
	                                     ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			beginGeoset(geoset, isHd(modelHandler.getModel(), geoset));
		}
		return this;
	}

	//	@Override
	public void beginGeoset(Geoset geoset, boolean isHD) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelHandler.getModelView().getHighlightedGeoset() == geoset) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		}
		geosetRenderer.reset(graphics, coordinateSystem, 0, geoset);
		geosetRenderer.beginTriangle(isHD);
	}

	public boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}
}
