package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;

public class RenderRibbonEmitter extends RenderSharedGeometryEmitter<RibbonEmitter, RenderRibbonEmitterView> {
	private final RibbonEmitter emitter;

	public RenderRibbonEmitter(final RibbonEmitter emitter, final InternalResource textureResource) {
		super(emitter, 4 * 30, textureResource);
		this.emitter = emitter;
	}

	@Override
	protected void emit(final RenderRibbonEmitterView emitterView) {
		emitterView.lastEmit = emitObject(emitterView, true);
	}

	@Override
	public void render(final RenderModel modelView, final ParticleEmitterShader shader) {
		if ((internalResource != null) && (alive > 0)) {
			shader.renderParticles(modelObject.getBlendSrc(), modelObject.getBlendDst(), modelObject.getRows(),
					modelObject.getCols(), internalResource, data, modelObject.isRibbonEmitter(), alive * 6);
		}
	}

	@Override
	protected EmittedObject<RenderRibbonEmitterView> createObject() {
		return new RenderRibbon(this);
	}

	public RibbonEmitter getEmitter() {
		return emitter;
	}

	public int getPriorityPlane() {
		final Material material = emitter.getMaterial();
		if (material != null) {
			return material.getPriorityPlane();
		}
		return 0;
	}
}
