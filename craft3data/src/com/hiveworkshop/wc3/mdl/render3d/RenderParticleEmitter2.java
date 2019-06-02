package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;

public class RenderParticleEmitter2 extends RenderSharedGeometryEmitter<ParticleEmitter2> {
    private int elementsPerEmit;
    private ParticleEmitter2 emitter;

    public RenderParticleEmitter2(ParticleEmitter2 emitter) {
        super(emitter,(emitter.isBoth() ? 2 : 1) * 30, null);
        this.emitter = emitter;
        elementsPerEmit = (emitter.isBoth() ? 2 : 1) * 30;
    }

    @Override
    protected void emit(EmitterView emitterView) {

    }

    @Override
    protected EmittedObject createObject() {
        return null;
    }
}
