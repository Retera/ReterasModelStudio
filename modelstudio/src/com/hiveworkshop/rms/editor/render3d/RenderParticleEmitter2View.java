package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;

public class RenderParticleEmitter2View extends EmitterView {
	private final ParticleEmitter2 particleEmitter2;
	private double lastEmissionRate;
	private final int lastAnimationTime = Integer.MIN_VALUE;
	private final RenderParticleEmitter2 renderEmitter;

	public RenderParticleEmitter2View(RenderModel instance, ParticleEmitter2 particleEmitter, Particle2TextureInstance textureInstance) {
		this.instance = instance;
		this.renderEmitter = new RenderParticleEmitter2(particleEmitter, textureInstance);
		this.particleEmitter2 = particleEmitter;
		currentEmission = 0;
		lastEmissionRate = -1;
		AnimFlag<?> emissionRateFlag = particleEmitter.find("EmissionRate");
		if (emissionRateFlag != null && instance.getTimeEnvironment().getCurrentSequence() != null && emissionRateFlag.getCeilEntry(0, instance.getTimeEnvironment().getCurrentSequence()) != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(instance.getTimeEnvironment().getCurrentSequence(), 0);
			}
		}
	}

	public void update() {
		renderEmitter.update();
	}

	public void fill() {
		if (instance.allowParticleSpawn()) {
			final double emissionRate = getEmissionRate();
			if (particleEmitter2.getSquirt()) {
				// TODO TODO TODO not correct for any interp type other than "DontInterp",
				//  ghostwolf did this differently
				if (emissionRate != lastEmissionRate) {
					currentEmission += emissionRate;
				}

				lastEmissionRate = emissionRate;
			} else {
				currentEmission += emissionRate * TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001 * instance.getTimeEnvironment().getAnimationSpeed();
			}
			renderEmitter.fill(this);
		}
	}

	public ParticleEmitter2 getParticleEmitter2() {
		return particleEmitter2;
	}

	public RenderParticleEmitter2 getRenderEmitter(){
		return renderEmitter;
	}

	@Override
	public void addToScene(Particle2TextureInstance internalInstance) {
		throw new UnsupportedOperationException();
	}

	public double getSpeed() {
		return particleEmitter2.getRenderSpeed(instance.getTimeEnvironment());
	}

	public double getTimeScale() {
		return instance.getTimeEnvironment().getAnimationSpeed();
	}

	public double getLatitude() {
		return particleEmitter2.getRenderLatitude(instance.getTimeEnvironment());
	}

	public double getGravity() {
		return particleEmitter2.getRenderGravity(instance.getTimeEnvironment());
	}

	public double getEmissionRate() {
		return particleEmitter2.getRenderEmissionRate(instance.getTimeEnvironment());
	}

	public double getVisibility() {
		return particleEmitter2.getRenderVisibility(instance.getTimeEnvironment());
	}

	public double getWidth() {
		return particleEmitter2.getRenderWidth(instance.getTimeEnvironment());
	}

	public double getLength() {
		return particleEmitter2.getRenderLength(instance.getTimeEnvironment());
	}

	public double getVariation() {
		return particleEmitter2.getRenderVariation(instance.getTimeEnvironment());
	}
}
