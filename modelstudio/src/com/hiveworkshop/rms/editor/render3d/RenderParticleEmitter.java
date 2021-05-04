package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;

public class RenderParticleEmitter extends RenderSharedGeometryEmitter<ParticleEmitter, RenderParticleEmitterView> {

	public RenderParticleEmitter(ParticleEmitter particleEmitter, int elementsPerEmit, Particle2TextureInstance internalResource) {
		super(particleEmitter, elementsPerEmit, internalResource);
	}

	@Override
	protected void emit(RenderParticleEmitterView emitterView) {

	}

	@Override
	protected EmittedObject<RenderParticleEmitterView> createObject() {
		return null;
	}
}
