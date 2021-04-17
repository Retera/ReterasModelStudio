package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;

public class RenderParticleEmitterView extends EmitterView {
    private final ParticleEmitter emitter;

    public RenderParticleEmitterView(final ParticleEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void addToScene(final InternalInstance internalInstance) {
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
