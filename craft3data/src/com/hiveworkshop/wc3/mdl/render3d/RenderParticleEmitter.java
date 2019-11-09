package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;

public class RenderParticleEmitter extends RenderSharedGeometryEmitter<ParticleEmitter, RenderParticleEmitterView> {

	public RenderParticleEmitter(final ParticleEmitter particleEmitter, final int elementsPerEmit,
			final InternalResource internalResource) {
		super(particleEmitter, elementsPerEmit, internalResource);
	}

	@Override
	protected void emit(final RenderParticleEmitterView emitterView) {

	}

	@Override
	protected EmittedObject<RenderParticleEmitterView> createObject() {
		return null;
	}
}
