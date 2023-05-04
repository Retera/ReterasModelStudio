package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Vec3;

/**
 * ParticleEmitter specific animation tags
 *	 KPEE - Particle emitter emission rate
 *	 KPEG - Particle emitter gravity
 *	 KPLN - Particle emitter longitude
 *	 KPLT - Particle emitter latitude
 *	 KPEL - Particle emitter lifespan
 *	 KPES - Particle emitter initial velocity
 *	 KPEV - Particle emitter visibility
 *
 * ParticleEmitter2 specific animation tags
 *  KP2S - Particle emitter 2 speed
 *  KP2R - Particle emitter 2 variation
 *  KP2L - Particle emitter 2 latitude
 *  KP2G - Particle emitter 2 gravity
 *  KP2E - Particle emitter 2 emission rate
 *  KP2N - Particle emitter 2 length
 *  KP2W - Particle emitter 2 width
 *  KP2V - Particle emitter 2 visibility
 *
 * RibbonEmitter specific animation tags
 *	 KRHA - Ribbon emitter height above
 *	 KRHB - Ribbon emitter height below
 *	 KRAL - Ribbon emitter alpha
 *	 KRCO - Ribbon emitter color
 *	 KRTX - Ribbon emitter texture slot
 *	 KRVS - Ribbon emitter visibility
 *
 * ParticleEmitterCorn specific animation tags
 *	 KPPA - Popcorn emitter alpha
 *	 KPPC - Popcorn emitter color
 *	 KPPE - Popcorn emitter emission rate
 *	 KPPL - Popcorn emitter lifespan
 *	 KPPS - Popcorn emitter speed
 *	 KPPV - Popcorn emitter visibility
 */
public abstract class EmitterIdObject extends IdObject {
    double emissionRate = 0;
    double lifeSpan = 0;
    float initVelocity = 0;
    float alpha = 0;
    Vec3 color = new Vec3();
    double gravity = 0;


    public EmitterIdObject() {
    }

    public EmitterIdObject(EmitterIdObject emitter) {
        super(emitter);
    }

    public abstract int getBlendSrc();

    public abstract int getBlendDst();

    public abstract int getRows();

    public abstract int getCols();

    public abstract boolean isRibbonEmitter();
}
