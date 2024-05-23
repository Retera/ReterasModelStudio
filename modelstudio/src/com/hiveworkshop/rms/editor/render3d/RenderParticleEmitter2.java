package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

public class RenderParticleEmitter2 {

	// These guys form the corners of a 2x2 rectangle, for use in Ghostwolf particle emitter algorithm
	private static final Vec3[] SPACIAL_VECTORS = {
			new Vec3(-1, 1, 0),
			new Vec3(1, 1, 0),
			new Vec3(1, -1, 0),
			new Vec3(-1, -1, 0),
			new Vec3(1, 0, 0),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1)};
	private final Vec3[] billboardVectors = {
			new Vec3(0, 1, -1),
			new Vec3(0, -1, -1),
			new Vec3(0, -1, 1),
			new Vec3(0, 1, 1),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1),
			new Vec3(1, 0, 0)};
	private static final Vec3[] BILLBOARD_BASE_VECTORS = {
			new Vec3(0, 1, -1),
			new Vec3(0, -1, -1),
			new Vec3(0, -1, 1),
			new Vec3(0, 1, 1),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1),
			new Vec3(1, 0, 0)};

	Quat inverseCameraRotation = new Quat();
	private final ParticleEmitter2 particleEmitter2;
	private final RenderModel renderModel;
	private final RenderNode2 renderNode2;
	private final TimeEnvironmentImpl timeEnvironment;
	private final Queue<RenderParticle2Inst> deadQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2Inst> aliveQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2HeadInst> deadHeadQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2HeadInst> aliveHeadQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2TailInst> deadTailQueue = new ArrayDeque<>();
	private final Queue<RenderParticle2TailInst> aliveTailQueue = new ArrayDeque<>();

	private double lastEmissionRate = -1;
	private int lastAnimTime = 0;

	private Sequence currentSequence;
	private float currentEmission = 0;
	public RenderParticleEmitter2(ParticleEmitter2 particleEmitter2, RenderNode2 renderNode2, RenderModel renderModel) {
		this.particleEmitter2 = particleEmitter2;
		this.renderNode2 = renderNode2;
		this.renderModel = renderModel;

		this.timeEnvironment = renderModel.getTimeEnvironment();

		AnimFlag<?> emissionRateFlag = particleEmitter2.find("EmissionRate");
		currentSequence = timeEnvironment.getCurrentSequence();
		if (emissionRateFlag != null && currentSequence != null && emissionRateFlag.getCeilEntry(0, currentSequence) != null) {
			if (0 < emissionRateFlag.size()) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(currentSequence, 0);
			}
		}
	}

	public ParticleEmitter2 getParticleEmitter2() {
		return particleEmitter2;
	}

	public void update(boolean spawn) {
		if (timeEnvironment.getCurrentSequence() != currentSequence) {
			lastAnimTime = 0;
			currentSequence = timeEnvironment.getCurrentSequence();
		}

		int animTime = timeEnvironment.getAnimationTime();
		int animLength = timeEnvironment.getLength();
		float dt = animLength == 0 ? 0 : (((animTime + animLength) - lastAnimTime)%animLength)/1000f;

		lastAnimTime = animTime;
		update(dt, currentSequence);
		if (spawn) {
			spawn(dt);
		}
	}

	public void spawn(float dt) {
		double emissionRate = particleEmitter2.getRenderEmissionRate(timeEnvironment);
		if (particleEmitter2.getSquirt()) {
			// TODO not correct for any interp type other than "DontInterp", ghostwolf did this differently
			if (emissionRate != lastEmissionRate) {
				currentEmission += emissionRate;
			}
			lastEmissionRate = emissionRate;
		} else {
			currentEmission += emissionRate * dt;
		}

		for (; 1 <= currentEmission; currentEmission--) {
			if (particleEmitter2.isHead()) emitHead();
			if (particleEmitter2.isTail()) emitTail();
		}
	}

	public RenderParticleEmitter2 emitHead() {
		RenderParticle2HeadInst headInst = deadHeadQueue.poll();
		if (headInst == null) {
			headInst = new RenderParticle2HeadInst(particleEmitter2);
		}
		aliveHeadQueue.offer(headInst.reset(renderNode2, true, timeEnvironment));
		return this;
	}
	public RenderParticleEmitter2 emitTail() {
		RenderParticle2TailInst tailInst = deadTailQueue.poll();
		if (tailInst == null) {
			tailInst = new RenderParticle2TailInst(particleEmitter2);
		}
		aliveTailQueue.offer(tailInst.reset(renderNode2, false, timeEnvironment));

		return this;
	}

	public void update(float dt, Sequence currentSequence) {
		int heads = aliveHeadQueue.size();
		for (int i = 0; i < heads; i++) {
			RenderParticle2HeadInst inst = aliveHeadQueue.poll();
			if (inst != null) {
				inst.update(dt, currentSequence, inverseCameraRotation);
				inst.updateRenderData();

				if (inst.health <= 0) {
					deadHeadQueue.offer(inst);
				} else {
					inst.updateRenderData();
					aliveHeadQueue.offer(inst);
				}
			}
		}

		int tails = aliveTailQueue.size();
		for (int i = 0; i < tails; i++) {
			RenderParticle2TailInst inst = aliveTailQueue.poll();
			if (inst != null) {
				inst.update(dt, currentSequence, inverseCameraRotation);
				inst.updateRenderData();

				if (inst.health <= 0) {
					deadTailQueue.offer(inst);
				} else {
					inst.updateRenderData();
					aliveTailQueue.offer(inst);
				}
			}
		}
	}
	public void emit1() {
		// I'm not sure if separating RenderParticle2Inst into RenderParticle2HeadInst and
		// RenderParticle2TailInst is a good idea or not. Leaving this here to make it easy to revert.
		if (particleEmitter2.isHead()) {
			emitObject1(true);
		}

		if (particleEmitter2.isTail()) {
			emitObject1(false);
		}
	}
	public RenderParticleEmitter2 emitObject1(boolean isHead) {
		RenderParticle2Inst inst = deadQueue.poll();
		if (inst == null) {
			inst = new RenderParticle2Inst(particleEmitter2);
		}
		aliveQueue.offer(inst.reset(renderNode2, isHead, timeEnvironment));

		return this;
	}

	public void update1(float dt, Sequence currentSequence) {
		// I'm not sure if separating RenderParticle2Inst into RenderParticle2HeadInst and
		// RenderParticle2TailInst is a good idea or not. Leaving this here to make it easy to revert.
//		// Choose between a default rectangle or a billboarded one
//		Vec3[] vectors;
//		if (particleEmitter2.getXYQuad()) {
//			vectors = SPACIAL_VECTORS;
//		} else {
//			vectors = billboardVectors;
//		}

		for (int i = 0; i < aliveQueue.size(); i++) {
			RenderParticle2Inst inst = aliveQueue.poll();
			if (inst != null) {
				inst.update(dt, currentSequence, inverseCameraRotation);
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

	public void updateBilllBoardVectors(Quat inverseCameraRotation) {
		this.inverseCameraRotation.set(inverseCameraRotation);
		for (int i = 0; i < billboardVectors.length; i++) {
			billboardVectors[i].set(BILLBOARD_BASE_VECTORS[i]).transform(inverseCameraRotation);
		}
	}
	public int getPriorityPlane() {
		return particleEmitter2.getPriorityPlane();
	}

	public Queue<RenderParticle2Inst> getAliveQueue() {
		return aliveQueue;
	}
	public Queue<RenderParticle2HeadInst> getAliveHeadQueue() {
		return aliveHeadQueue;
	}
	public Queue<RenderParticle2TailInst> getAliveTailQueue() {
		return aliveTailQueue;
	}
}
