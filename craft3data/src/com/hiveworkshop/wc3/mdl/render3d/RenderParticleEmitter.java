package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;

public class RenderParticleEmitter extends RenderSharedGeometryEmitter<ParticleEmitter> {

    public RenderParticleEmitter(ParticleEmitter particleEmitter, int elementsPerEmit, InternalResource internalResource) {
        super(particleEmitter, elementsPerEmit, internalResource);
    }

    @Override
    protected void emit(EmitterView emitterView) {

    }

    @Override
    protected EmittedObject createObject() {
        return null;
    }
}
