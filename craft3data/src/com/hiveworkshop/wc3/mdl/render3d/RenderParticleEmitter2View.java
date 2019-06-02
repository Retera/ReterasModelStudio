package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;

public class RenderParticleEmitter2View extends EmitterView {
    private ParticleEmitter2 emitter;

    public RenderParticleEmitter2View(ParticleEmitter2 emitter) {
        this.emitter = emitter;
    }

    @Override
    public void addToScene(InternalInstance internalInstance) {
        throw new UnsupportedOperationException();
    }

    public double getSpeed() {
        return emitter.getRenderSpeed(instance.getAnimatedRenderEnvironment());
    }

    public double getLatitude() {
        return emitter.getRenderLatitude(instance.getAnimatedRenderEnvironment());
    }

    public double getGravity() {
        return emitter.getRenderGravity(instance.getAnimatedRenderEnvironment());
    }

    public double getEmissionRate() {
        return emitter.getRenderEmissionRate(instance.getAnimatedRenderEnvironment());
    }

    public double getVisibility() {
        return emitter.getRenderVisibility(instance.getAnimatedRenderEnvironment());
    }

    public double getWidth() {
        return emitter.getRenderWidth(instance.getAnimatedRenderEnvironment());
    }

    public double getLength() {
        return emitter.getRenderLength(instance.getAnimatedRenderEnvironment());
    }

    public double getVariation() {
        return emitter.getRenderVariation(instance.getAnimatedRenderEnvironment());
    }
}
