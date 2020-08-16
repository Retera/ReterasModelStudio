package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;

public class RenderParticleEmitter2View extends EmitterView {
	private final ParticleEmitter2 emitter;
	private double lastEmissionRate;
	private final int lastAnimationTime = Integer.MIN_VALUE;
	private final RenderParticleEmitter2 renderEmitter;

	public RenderParticleEmitter2View(final RenderModel instance, final RenderParticleEmitter2 emitter) {
		this.instance = instance;
		this.renderEmitter = emitter;
		this.emitter = emitter.getEmitter();
		this.currentEmission = 0;
		this.lastEmissionRate = -1;
		final AnimFlag emissionRateFlag = AnimFlag.find(emitter.getEmitter().getAnimFlags(), "EmissionRate");
		if (emissionRateFlag != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = ((Double) emissionRateFlag.getValues().get(0)).doubleValue();
			}
		}
	}

	public void update() {
	}

	public void fill() {
		if (this.instance.allowParticleSpawn()) {
			final double emissionRate = this.getEmissionRate();

			if (this.emitter.isSquirt()) {
				// TODO TODO TODO not correct for any interp type other than "DontInterp",
				// ghostwolf did this differently
				if (emissionRate != lastEmissionRate) {
					this.currentEmission += emissionRate;
				}

				this.lastEmissionRate = emissionRate;
			} else {
				this.currentEmission += emissionRate * AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001;
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
