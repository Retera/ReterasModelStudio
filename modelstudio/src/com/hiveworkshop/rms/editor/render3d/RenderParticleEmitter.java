package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;

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
