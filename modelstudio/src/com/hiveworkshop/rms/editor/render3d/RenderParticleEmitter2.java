package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

public class RenderParticleEmitter2 {
	private final ParticleEmitter2 particleEmitter2;
	private final RenderModel renderModel;
	private final RenderNode2 renderNode2;
	private final TimeEnvironmentImpl timeEnvironment;
	private final Queue<RenderParticle2Inst> deadQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2Inst> aliveQueue = new ArrayDeque<>();
//	private static final int MAX_POWER_OF_TWO = 1 << 30;
//	private final int elementsPerEmit;
	private float currentEmission = 0;

	private double lastEmissionRate = -1;

	public RenderParticleEmitter2(ParticleEmitter2 particleEmitter2, RenderNode2 renderNode2, RenderModel renderModel) {
		this.particleEmitter2 = particleEmitter2;
		this.renderNode2 = renderNode2;
		this.renderModel = renderModel;

		this.timeEnvironment = renderModel.getTimeEnvironment();
//		this.elementsPerEmit = (particleEmitter2.isBoth() ? 2 : 1) * 30;

		AnimFlag<?> emissionRateFlag = particleEmitter2.find("EmissionRate");
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if (emissionRateFlag != null && currentSequence != null && emissionRateFlag.getCeilEntry(0, currentSequence) != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(currentSequence, 0);
			}
		}
	}

	public ParticleEmitter2 getParticleEmitter2() {
		return particleEmitter2;
	}

	public void fill() {
		double emissionRate = particleEmitter2.getRenderEmissionRate(timeEnvironment);
		if (particleEmitter2.getSquirt()) {
			// TODO not correct for any interp type other than "DontInterp", ghostwolf did this differently
			if (emissionRate != lastEmissionRate) {
				currentEmission += emissionRate;
			}

			lastEmissionRate = emissionRate;
		} else {
			currentEmission += emissionRate * TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001 * timeEnvironment.getAnimationSpeed();
		}

		for (;currentEmission>=1; currentEmission--) {
			emit();
		}
	}

	public void emit() {
		if (particleEmitter2.isHead() || particleEmitter2.isBoth()) {
			emitObject(true);
		}

		if (particleEmitter2.isTail() || particleEmitter2.isBoth()) {
			emitObject(false);
		}
	}
	public RenderParticleEmitter2 emitObject(boolean isHead) {
		RenderParticle2Inst inst = deadQueue.poll();
		if(inst == null){
			inst = new RenderParticle2Inst(particleEmitter2);
		}
//		inst.updateRenderData();
		aliveQueue.offer(inst.reset(renderNode2, isHead, timeEnvironment));

		return this;
	}


	public void update() {
		// Choose between a default rectangle or a billboarded one
//		int maxParticles = (int) (particleEmitter2.getEmissionRate()*particleEmitter2.getLifeSpan()+.5);
		Vec3[] vectors;
		Vec3[] billboardVectors = renderModel.getBillboardVectors();
		if (particleEmitter2.getXYQuad()) {
			vectors = renderModel.getSpacialVectors();
		} else {
			vectors = billboardVectors;
		}

		float dt = TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001f * timeEnvironment.getAnimationSpeed();
		for (int i = 0; i< aliveQueue.size(); i++) {

			RenderParticle2Inst inst = aliveQueue.poll();
			if(inst != null){
				inst.update(dt, timeEnvironment.getCurrentSequence(), vectors, billboardVectors[6]);
				inst.updateRenderData();

				if (inst.health <= 0) {
					deadQueue.offer(inst);
				} else {
					inst.updateRenderData();
					aliveQueue.offer(inst);
				}
			}
		}
	}

	public int getPriorityPlane() {
		return particleEmitter2.getPriorityPlane();
	}

	public Queue<RenderParticle2Inst> getAliveQueue() {
		return aliveQueue;
	}
}
