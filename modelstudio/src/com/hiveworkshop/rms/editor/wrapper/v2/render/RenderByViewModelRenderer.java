package com.hiveworkshop.rms.editor.wrapper.v2.render;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class RenderByViewModelRenderer implements ModelVisitor {
	private ModelVisitor fullModelRenderer;
	private final ModelView modelView;

	public RenderByViewModelRenderer(ModelView modelView) {
		this.modelView = modelView;
	}

	public RenderByViewModelRenderer reset(ModelVisitor fullModelRenderer) {
		this.fullModelRenderer = fullModelRenderer;
		return this;

	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		Geoset geoset = modelView.getModel().getGeoset(geosetId);
		if (modelView.getEditableGeosets().contains(geoset)
				|| (modelView.getHighlightedGeoset() == geoset)
				|| modelView.getVisibleGeosets().contains(geoset)) {
			return fullModelRenderer.beginGeoset(geosetId, material, geosetAnim);
		}
		return GeosetVisitor.NO_ACTION;
	}


	private boolean isVisibleNode(IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode());
	}

	@Override
	public void visitIdObject(IdObject object) {
		if (isVisibleNode(object)) {
			fullModelRenderer.visitIdObject(object);
		}
	}

	@Override
	public void camera(Camera camera) {
		if (modelView.getEditableCameras().contains(camera)) {
			fullModelRenderer.camera(camera);
		}
	}

}
