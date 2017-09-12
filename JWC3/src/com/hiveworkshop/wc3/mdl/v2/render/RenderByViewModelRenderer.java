package com.hiveworkshop.wc3.mdl.v2.render;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.renderer.ModelRenderer;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

public final class RenderByViewModelRenderer implements ModelRenderer {
	private ModelVisitor fullModelRenderer;
	private final ModelView modelView;

	public RenderByViewModelRenderer(final ModelView modelView) {
		this.modelView = modelView;
	}

	public RenderByViewModelRenderer reset(final ModelVisitor fullModelRenderer) {
		this.fullModelRenderer = fullModelRenderer;
		return this;

	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final MaterialView material, final GeosetAnim geosetAnim) {
		if (modelView.getEditableGeosets().contains(modelView.getModel().getGeoset(geosetId))) {
			return fullModelRenderer.beginGeoset(geosetId, material, geosetAnim);
		}
		return GeosetVisitor.NO_ACTION;
	}

	@Override
	public void bone(final Bone object) {
		if (modelView.getEditableIdObjects().contains(object)) {
			fullModelRenderer.bone(object);
		}
	}

	@Override
	public void light(final Light light) {
		if (modelView.getEditableIdObjects().contains(light)) {
			fullModelRenderer.light(light);
		}
	}

	@Override
	public void helper(final Helper object) {
		if (modelView.getEditableIdObjects().contains(object)) {
			fullModelRenderer.helper(object);
		}
	}

	@Override
	public void attachment(final Attachment attachment) {
		if (modelView.getEditableIdObjects().contains(attachment)) {
			fullModelRenderer.attachment(attachment);
		}
	}

	@Override
	public void particleEmitter(final ParticleEmitter particleEmitter) {
		if (modelView.getEditableIdObjects().contains(particleEmitter)) {
			fullModelRenderer.particleEmitter(particleEmitter);
		}
	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		if (modelView.getEditableIdObjects().contains(particleEmitter)) {
			fullModelRenderer.particleEmitter2(particleEmitter);
		}
	}

	@Override
	public void ribbonEmitter(final RibbonEmitter particleEmitter) {
		if (modelView.getEditableIdObjects().contains(particleEmitter)) {
			fullModelRenderer.ribbonEmitter(particleEmitter);
		}
	}

	@Override
	public void eventObject(final EventObject eventObject) {
		if (modelView.getEditableIdObjects().contains(eventObject)) {
			fullModelRenderer.eventObject(eventObject);
		}
	}

	@Override
	public void collisionShape(final CollisionShape collisionShape) {
		if (modelView.getEditableIdObjects().contains(collisionShape)) {
			fullModelRenderer.collisionShape(collisionShape);
		}
	}

	@Override
	public void camera(final Camera camera) {
		if (modelView.getEditableCameras().contains(camera)) {
			fullModelRenderer.camera(camera);
		}
	}

}
