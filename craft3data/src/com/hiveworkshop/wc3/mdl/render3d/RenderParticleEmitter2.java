package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter2;

public class RenderParticleEmitter2 {
    private int elementsPerEmit;
    private ParticleEmitter2 emitter;

    public RenderParticleEmitter2(ParticleEmitter2 emitter) {
        this.emitter = emitter;
        elementsPerEmit = (emitter.isBoth() ? 2 : 1) * 30;
    }
}
