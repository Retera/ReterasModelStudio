package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

public class RenderParticleEmitter2View extends EmitterView {
	private final ParticleEmitter2 emitter;
	private double lastEmissionRate;
	private final int lastAnimationTime = Integer.MIN_VALUE;
	private final RenderParticleEmitter2 renderEmitter;

	public RenderParticleEmitter2View(final RenderModel instance, final RenderParticleEmitter2 emitter) {
		this.instance = instance;
		renderEmitter = emitter;
		this.emitter = emitter.getEmitter();
		currentEmission = 0;
		lastEmissionRate = -1;
		final AnimFlag emissionRateFlag = emitter.getEmitter().find("EmissionRate");
		if (emissionRateFlag != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValues().get(0);
			}
		}
	}

	public void update() {
	}

	public void fill() {
		if (instance.allowParticleSpawn()) {
			final double emissionRate = getEmissionRate();
			if (emitter.getSquirt()) {
				// TODO TODO TODO not correct for any interp type other than "DontInterp",
				//  ghostwolf did this differently
				if (emissionRate != lastEmissionRate) {
					currentEmission += emissionRate;
				}

				lastEmissionRate = emissionRate;
			} else {
				currentEmission += emissionRate * AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001 * instance.getAnimatedRenderEnvironment().getAnimationSpeed();
			}
			renderEmitter.fill(this);
		}
	}

	public ParticleEmitter2 getEmitter() {
		return emitter;
	}

	@Override
	public void addToScene(final InternalInstance internalInstance) {
		throw new UnsupportedOperationException();
	}

	public double getSpeed() {
		return emitter.getRenderSpeed(instance.getAnimatedRenderEnvironment());
	}

	public double getTimeScale() {
		return instance.getAnimatedRenderEnvironment().getAnimationSpeed();
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
