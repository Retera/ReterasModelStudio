package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableAnimatedIdObjectParentLinkRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class LinkRenderingVisitorAdapter implements ModelVisitor {
	private ResettableAnimatedIdObjectParentLinkRenderer linkRenderer;
	ModelView modelView;
	private ModelHandler modelHandler;

	public LinkRenderingVisitorAdapter(ProgramPreferences programPreferences) {
		linkRenderer = new ResettableAnimatedIdObjectParentLinkRenderer(programPreferences.getVertexSize());
	}

	public ResettableAnimatedIdObjectParentLinkRenderer reset(Graphics2D graphics,
	                                                          CoordinateSystem coordinateSystem,
	                                                          ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		linkRenderer.reset(coordinateSystem, graphics, NodeIconPalette.HIGHLIGHT, modelHandler.getRenderModel());

		EditableModel model = modelHandler.getModel();

		for (IdObject object : model.getAllObjects()) {
			visitIdObject(object);
		}

		return linkRenderer;
	}

	public boolean isHd(EditableModel model, Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	//	@Override
	public void beginGeoset(Geoset geoset, boolean isHD) {
	}

	private boolean isVisibleNode(IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode());
	}

	@Override
	public void visitIdObject(IdObject object) {
		if (isVisibleNode(object)) {
			linkRenderer.visitIdObject(object);
		}
	}

	@Override
	public void camera(Camera camera) {
	}
}
