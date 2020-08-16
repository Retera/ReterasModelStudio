package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;

public class RenderParticleEmitterView extends EmitterView {
    private ParticleEmitter emitter;

    public RenderParticleEmitterView(ParticleEmitter emitter) {
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

    public double getLongitude() {
        return emitter.getRenderLongitude(instance.getAnimatedRenderEnvironment());
    }

    public double getLifeSpan() {
        return emitter.getRenderLifeSpan(instance.getAnimatedRenderEnvironment());
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
}
