package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;

public class RenderParticleEmitterView extends EmitterView {
    private final ParticleEmitter emitter;

    public RenderParticleEmitterView(final ParticleEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void addToScene(final Particle2TextureInstance internalInstance) {
        throw new UnsupportedOperationException();
    }

    public double getSpeed() {
        return emitter.getRenderSpeed(instance.getTimeEnvironment());
    }

    public double getLatitude() {
	    return emitter.getRenderLatitude(instance.getTimeEnvironment());
    }

    public double getLongitude() {
	    return emitter.getRenderLongitude(instance.getTimeEnvironment());
    }

    public double getLifeSpan() {
	    return emitter.getRenderLifeSpan(instance.getTimeEnvironment());
    }

    public double getGravity() {
	    return emitter.getRenderGravity(instance.getTimeEnvironment());
    }

    public double getEmissionRate() {
	    return emitter.getRenderEmissionRate(instance.getTimeEnvironment());
    }

    public double getVisibility() {
	    return emitter.getRenderVisibility(instance.getTimeEnvironment());
    }
}
