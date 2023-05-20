package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

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
	private int lastAnimTime = 0;

	Vec4 colorHeap = new Vec4();
	private double lastEmissionRate = -1;
	Sequence currentSequence;

	public RenderRibbonEmitter(RibbonEmitter ribbon, RenderNode2 renderNode2, RenderModel renderModel) {
		this.ribbon = ribbon;
		this.renderNode2 = renderNode2;
		this.renderModel = renderModel;

		this.timeEnvironment = renderModel.getTimeEnvironment();

		AnimFlag<?> emissionRateFlag = ribbon.find("EmissionRate");
		currentSequence = timeEnvironment.getCurrentSequence();
		if (emissionRateFlag != null && currentSequence != null && emissionRateFlag.getCeilEntry(0, currentSequence) != null) {
			if (0 < emissionRateFlag.size()) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(currentSequence, 0);
			}
		}
	}

	public RibbonEmitter getRibbon() {
		return ribbon;
	}

	Vec2 uvAdv = new Vec2();
	Vec2 uvScale = new Vec2();
	Vec2 uvScale2 = new Vec2();

	public void update(boolean spawn) {
		if(timeEnvironment.getCurrentSequence() != currentSequence){
			lastAnimTime = 0;
			currentSequence = timeEnvironment.getCurrentSequence();
		}
		int animTime = timeEnvironment.getAnimationTime();
		int animLength = timeEnvironment.getLength();
		float dt = (((animTime + animLength) - lastAnimTime)%(animLength))/1000f;
		lastAnimTime = animTime;

		update(dt, currentSequence);
		if(spawn){
			spawn(dt);
		}
	}

	public void spawn(float dt) {
		uvScale2.set(ribbon.getCols(), ribbon.getRows());
		uvAdv.set(1f/(ribbon.getLifeSpan()), 0).div(uvScale2);
		uvScale.set(Vec2.ONE).div(uvScale2);
		float emissionRate = ribbon.getEmissionRate();

		currentEmission += emissionRate * dt;

		for (;currentEmission>=1; currentEmission--) {
			emitObject();
		}
	}

	public RenderRibbonEmitter emitObject() {
		RenderParticleRibbonInst inst = deadQueue.poll();
		if(inst == null){
			inst = new RenderParticleRibbonInst(ribbon);
		}
		aliveQueue.offer(inst.reset(renderNode2, uvScale, uvAdv, timeEnvironment));
		return this;
	}

	public void update(float dt, Sequence sequence) {
		uvScale2.set(ribbon.getCols(), ribbon.getRows());
		uvAdv.set(1f/(ribbon.getEmissionRate()*(ribbon.getLifeSpan())), 0).div(uvScale2);
		uvScale.set(Vec2.ONE).div(uvScale2);

		colorHeap.set(ribbon.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_COLOR, ribbon.getStaticColor()), ribbon.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_ALPHA, (float) ribbon.getAlpha()));

		int size = aliveQueue.size();
		for (int i = 0; i< size; i++) {

			RenderParticleRibbonInst inst = aliveQueue.poll();
			if(inst != null){
				inst.update(dt, sequence);
				if (inst.health <= 0) {
					deadQueue.offer(inst);
				} else {
					aliveQueue.offer(inst);
				}
			}
		}
	}

	public Vec4 getColorHeap() {
		return colorHeap;
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
