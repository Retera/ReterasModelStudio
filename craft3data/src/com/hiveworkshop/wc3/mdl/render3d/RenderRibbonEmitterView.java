package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;

public class RenderRibbonEmitterView extends EmitterView {
	private final RibbonEmitter emitter;
	protected EmittedObject<RenderRibbonEmitterView> lastEmit;
	protected int currentRibbon;
	protected int ribbonCount;
	private double lastEmissionRate;
	private final int lastAnimationTime = Integer.MIN_VALUE;
	private final RenderRibbonEmitter renderEmitter;

	public RenderRibbonEmitterView(final RenderModel instance, final RenderRibbonEmitter emitter) {
		this.instance = instance;
		this.renderEmitter = emitter;
		this.emitter = emitter.getEmitter();
		this.currentEmission = 0;
		this.currentRibbon = -1;
		this.ribbonCount = 0;
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
			this.currentEmission += getEmissionRate() * 10.5f * AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001;
			renderEmitter.fill(this);
		}
	}

	public RibbonEmitter getEmitter() {
		return emitter;
	}

	@Override
	public void addToScene(final InternalInstance internalInstance) {
		throw new UnsupportedOperationException();
	}

	private boolean shouldRender() {
		return this.emitter.getRenderVisibility(instance.getAnimatedRenderEnvironment()) > 0.75;
	}

	public double getHeightBelow() {
		return emitter.getRenderHeightBelow(instance.getAnimatedRenderEnvironment());
	}

	public double getHeightAbove() {
		return emitter.getRenderHeightAbove(instance.getAnimatedRenderEnvironment());
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

	public double getAlpha() {
		return emitter.getRenderAlpha(instance.getAnimatedRenderEnvironment());
	}

	public Vector3f getColor() {
		return emitter.getRenderColor(instance.getAnimatedRenderEnvironment());
	}

	public int getTextureSlot() {
		return emitter.getRenderTextureSlot(instance.getAnimatedRenderEnvironment());
	}
}
