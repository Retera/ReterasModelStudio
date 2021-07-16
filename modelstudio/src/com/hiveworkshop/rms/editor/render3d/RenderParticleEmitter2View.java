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

	public RenderParticleEmitter2View(RenderModel instance, RenderParticleEmitter2 emitter) {
		this.instance = instance;
		this.renderEmitter = emitter;
		this.particleEmitter2 = emitter.getEmitter();
		currentEmission = 0;
		lastEmissionRate = -1;
		AnimFlag<?> emissionRateFlag = emitter.getEmitter().find("EmissionRate");
		if (emissionRateFlag != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(0);
			}
		}
	}

	public RenderParticleEmitter2View(RenderModel instance, ParticleEmitter2 particleEmitter, Particle2TextureInstance textureInstance) {
		this.instance = instance;
		this.renderEmitter = new RenderParticleEmitter2(particleEmitter, textureInstance);
		this.particleEmitter2 = particleEmitter;
		currentEmission = 0;
		lastEmissionRate = -1;
		AnimFlag<?> emissionRateFlag = particleEmitter.find("EmissionRate");
		if (emissionRateFlag != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(0);
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
				currentEmission += emissionRate * TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001 * instance.getAnimatedRenderEnvironment().getAnimationSpeed();
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
		return particleEmitter2.getRenderSpeed(instance.getAnimatedRenderEnvironment());
	}

	public double getTimeScale() {
		return instance.getAnimatedRenderEnvironment().getAnimationSpeed();
	}

	public double getLatitude() {
		return particleEmitter2.getRenderLatitude(instance.getAnimatedRenderEnvironment());
	}

	public double getGravity() {
		return particleEmitter2.getRenderGravity(instance.getAnimatedRenderEnvironment());
	}

	public double getEmissionRate() {
		return particleEmitter2.getRenderEmissionRate(instance.getAnimatedRenderEnvironment());
	}

	public double getVisibility() {
		return particleEmitter2.getRenderVisibility(instance.getAnimatedRenderEnvironment());
	}

	public double getWidth() {
		return particleEmitter2.getRenderWidth(instance.getAnimatedRenderEnvironment());
	}

	public double getLength() {
		return particleEmitter2.getRenderLength(instance.getAnimatedRenderEnvironment());
	}

	public double getVariation() {
		return particleEmitter2.getRenderVariation(instance.getAnimatedRenderEnvironment());
	}
}
