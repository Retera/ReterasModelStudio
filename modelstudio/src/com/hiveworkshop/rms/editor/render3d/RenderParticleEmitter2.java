package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;

public class RenderParticleEmitter2 extends RenderSharedGeometryEmitter<ParticleEmitter2, RenderParticleEmitter2View> {
	private final ParticleEmitter2 emitter;

	public RenderParticleEmitter2(final ParticleEmitter2 emitter, final InternalResource textureResource) {
		super(emitter, (emitter.isBoth() ? 2 : 1) * 30, textureResource);
		this.emitter = emitter;
	}

	@Override
	protected void emit(final RenderParticleEmitter2View emitterView) {
		if (modelObject.isHead()) {
			emitObject(emitterView, true);
		}

		if (modelObject.isTail()) {
			emitObject(emitterView, false);
		}
	}

	@Override
	protected EmittedObject<RenderParticleEmitter2View> createObject() {
		return new RenderParticle2(this);
	}

	public ParticleEmitter2 getEmitter() {
		return emitter;
	}

	public int getPriorityPlane() {
		return emitter.getPriorityPlane();
	}
}
