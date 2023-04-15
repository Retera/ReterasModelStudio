package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

public class RenderRibbonEmitter {

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
	private final RibbonEmitter ribbon;
	private final RenderModel renderModel;
	private final RenderNode2 renderNode2;
	private final TimeEnvironmentImpl timeEnvironment;
	private final Queue<RenderParticleRibbonInst> deadQueue = new ArrayDeque<>();
	private final Queue<RenderParticleRibbonInst> aliveQueue = new ArrayDeque<>();
	private float currentEmission = 0;

	private double lastEmissionRate = -1;

	public RenderRibbonEmitter(RibbonEmitter ribbon, RenderNode2 renderNode2, RenderModel renderModel) {
		this.ribbon = ribbon;
		this.renderNode2 = renderNode2;
		this.renderModel = renderModel;

		this.timeEnvironment = renderModel.getTimeEnvironment();

		AnimFlag<?> emissionRateFlag = ribbon.find("EmissionRate");
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if (emissionRateFlag != null && currentSequence != null && emissionRateFlag.getCeilEntry(0, currentSequence) != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(currentSequence, 0);
			}
		}
	}

	public RibbonEmitter getRibbon() {
		return ribbon;
	}

	Vec2 uvAdv = new Vec2();
	Vec2 uvAdv2 = new Vec2();
	Vec2 uvScale = new Vec2();
	Vec2 uvScale2 = new Vec2();
	public void fill() {
		uvScale2.set(ribbon.getCols(), ribbon.getRows());
//		uvAdv.set(ribbon.getEmissionRate()*ribbon.getLifeSpan() * 0.001, 0).div(uvScale2);
		uvAdv.set(1f/(ribbon.getEmissionRate()*ribbon.getLifeSpan()), 0).div(uvScale2);
//		uvAdv.set(.2, 0);
		uvScale.set(Vec2.ONE).div(uvScale2);
		float emissionRate = ribbon.getEmissionRate();

		currentEmission += emissionRate * TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001 * timeEnvironment.getAnimationSpeed();

		for (;currentEmission>=1; currentEmission--) {
			emit();
		}
	}

	public void emit() {
		emitObject();
	}
	public RenderRibbonEmitter emitObject() {
		RenderParticleRibbonInst inst = deadQueue.poll();
		if(inst == null){
			inst = new RenderParticleRibbonInst(ribbon);
		}

		aliveQueue.offer(inst.reset(renderNode2, uvScale, uvAdv, timeEnvironment));

		return this;
	}



	public void update() {
		uvScale2.set(ribbon.getCols(), ribbon.getRows());
		uvAdv.set(1f/(ribbon.getEmissionRate()*ribbon.getLifeSpan()), 0).div(uvScale2);
//		uvAdv.set(.2, 0);
		uvScale.set(Vec2.ONE).div(uvScale2);

		float dt = TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001f * timeEnvironment.getAnimationSpeed();
		for (int i = 0; i< aliveQueue.size(); i++) {

			RenderParticleRibbonInst inst = aliveQueue.poll();
			if(inst != null){
				inst.update(dt, timeEnvironment.getCurrentSequence());
				if (inst.health <= 0) {
					deadQueue.offer(inst);
				} else {
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
		return ribbon.getMaterial().getPriorityPlane();
	}

	public Queue<RenderParticleRibbonInst> getAliveQueue() {
		return aliveQueue;
	}
}
