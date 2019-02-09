package com.hiveworkshop.wc3.mdl.v2.render;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;

public final class RenderByViewMeshRenderer implements MeshVisitor {
	private MeshVisitor fullModelRenderer;
	private final ModelView modelView;

	public RenderByViewMeshRenderer(final ModelView modelView) {
		this.modelView = modelView;
	}

	public RenderByViewMeshRenderer reset(final MeshVisitor fullModelRenderer) {
		this.fullModelRenderer = fullModelRenderer;
		return this;

	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final MaterialView material, final GeosetAnim geosetAnim) {
		final Geoset geoset = modelView.getModel().getGeoset(geosetId);
		if (modelView.getEditableGeosets().contains(geoset) || modelView.getHighlightedGeoset() == geoset) {
			return fullModelRenderer.beginGeoset(geosetId, material, geosetAnim);
		}
		return GeosetVisitor.NO_ACTION;
	}

	private boolean isVisibleNode(final IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || object == modelView.getHighlightedNode();
	}

}
