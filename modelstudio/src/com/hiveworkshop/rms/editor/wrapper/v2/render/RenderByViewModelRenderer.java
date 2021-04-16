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

	@Override
	public void bone(Bone object) {
		if (isVisibleNode(object)) {
			fullModelRenderer.bone(object);
		}
	}

	@Override
	public void light(Light light) {
		if (isVisibleNode(light)) {
			fullModelRenderer.light(light);
		}
	}

	@Override
	public void helper(Helper object) {
		if (isVisibleNode(object)) {
			fullModelRenderer.helper(object);
		}
	}

	@Override
	public void attachment(Attachment attachment) {
		if (isVisibleNode(attachment)) {
			fullModelRenderer.attachment(attachment);
		}
	}

	@Override
	public void particleEmitter(ParticleEmitter particleEmitter) {
		if (isVisibleNode(particleEmitter)) {
			fullModelRenderer.particleEmitter(particleEmitter);
		}
	}

	@Override
	public void particleEmitter2(ParticleEmitter2 particleEmitter) {
		if (isVisibleNode(particleEmitter)) {
			fullModelRenderer.particleEmitter2(particleEmitter);
		}
	}

	@Override
	public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
		if (isVisibleNode(popcornFxEmitter)) {
			fullModelRenderer.popcornFxEmitter(popcornFxEmitter);
		}
	}

	@Override
	public void ribbonEmitter(RibbonEmitter particleEmitter) {
		if (isVisibleNode(particleEmitter)) {
			fullModelRenderer.ribbonEmitter(particleEmitter);
		}
	}

	@Override
	public void eventObject(EventObject eventObject) {
		if (isVisibleNode(eventObject)) {
			fullModelRenderer.eventObject(eventObject);
		}
	}

	@Override
	public void collisionShape(CollisionShape collisionShape) {
		if (isVisibleNode(collisionShape)) {
			fullModelRenderer.collisionShape(collisionShape);
		}
	}

	@Override
	public void camera(Camera camera) {
		if (modelView.getEditableCameras().contains(camera)) {
			fullModelRenderer.camera(camera);
		}
	}

	private boolean isVisibleNode(IdObject object) {
		return modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode());
	}

}
