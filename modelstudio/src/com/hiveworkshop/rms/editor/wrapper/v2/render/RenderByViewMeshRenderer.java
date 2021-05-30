package com.hiveworkshop.rms.editor.wrapper.v2.render;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class RenderByViewMeshRenderer implements MeshVisitor {
	private MeshVisitor fullModelRenderer;
	private final ModelView modelView;

	public RenderByViewMeshRenderer(ModelView modelView) {
		this.modelView = modelView;
	}

	public RenderByViewMeshRenderer reset(MeshVisitor fullModelRenderer) {
		this.fullModelRenderer = fullModelRenderer;
		return this;

	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		Geoset geoset = modelView.getModel().getGeoset(geosetId);
		if (modelView.getEditableGeosets().contains(geoset)
				|| modelView.getHighlightedGeoset() == geoset
				|| modelView.getVisibleGeosets().contains(geoset)) {
			return fullModelRenderer.beginGeoset(geosetId, material, geosetAnim);
		}
		return GeosetVisitor.NO_ACTION;
	}

	private boolean isVisibleNode(IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || object == modelView.getHighlightedNode();
	}

}
