package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.TreeMap;

public class ScaleParticleAction extends AbstractTransformAction {
	private final Vec3 center;
	private final Vec3 scale;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final String[] flagNames;
	EmitterIdObject emitter;
	EmitterIdObject old;
	private final boolean scaleTranslation;

	public ScaleParticleAction(EmitterIdObject emitter, Vec3 center, Mat4 rotMat, Vec3 scale, boolean scaleTranslation){
		this.center = center;
		this.scale = scale;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.emitter = emitter;
		this.old = (EmitterIdObject) emitter.copy();
		this.scaleTranslation = scaleTranslation;

		if(emitter instanceof ParticleEmitter){
			flagNames = new String[] {MdlUtils.TOKEN_LATITUDE, MdlUtils.TOKEN_LONGITUDE, MdlUtils.TOKEN_INIT_VELOCITY, MdlUtils.TOKEN_GRAVITY};
		} else if (emitter instanceof ParticleEmitter2){
			flagNames = new String[] {MdlUtils.TOKEN_LATITUDE, MdlUtils.TOKEN_WIDTH, MdlUtils.TOKEN_LENGTH, MdlUtils.TOKEN_SPEED, MdlUtils.TOKEN_GRAVITY};
		} else if (emitter instanceof RibbonEmitter){
			flagNames = new String[] {MdlUtils.TOKEN_HEIGHT_ABOVE, MdlUtils.TOKEN_HEIGHT_BELOW, MdlUtils.TOKEN_GRAVITY};
		} else if (emitter instanceof ParticleEmitterPopcorn){
			flagNames = new String[] {MdlUtils.TOKEN_SPEED};
		} else {
			flagNames = new String[0];
		}
	}

	@Override
	public ScaleParticleAction undo() {
		if (scaleTranslation) {
			resetAnimatedTranslations();
		}
		resetEmitterStuff1();
		return this;
	}

	@Override
	public ScaleParticleAction redo() {
		rawScale2(center, scale);
		return this;
	}

	@Override
	public String actionName() {
		return "Scale particle";
	}

	@Override
	public ScaleParticleAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		rawScale2(center, this.scale);
		return this;
	}
	@Override
	public ScaleParticleAction setScale(Vec3 scale) {
		this.scale.set(scale);
		rawScale2(center, scale);
		return this;
	}

	Vec3 tempVec = new Vec3();

	private void rawScale2(Vec3 center, Vec3 totScale) {
		double avgScale = (totScale.x + totScale.y + totScale.z) / 3;

		if (scaleTranslation) {
			resetAnimatedTranslations();
			emitter.getPivotPoint().set(old.getPivotPoint())
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(totScale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(totScale);
		}

		resetEmitterStuff1();
		scaleEmitterStuff1(totScale, avgScale);
	}

	private void scaleEmitterStuff1(Vec3 scale, double avgScale) {
		if (emitter instanceof ParticleEmitter2 particle) {
			particle.setLatitude(particle.getLatitude() * scale.z);
			particle.setWidth(particle.getWidth() * scale.y);
			particle.setLength(particle.getLength() * scale.x);
			particle.getParticleScaling().set(particle.getParticleScaling()).scale((float) avgScale);
			particle.setSpeed(particle.getSpeed() * avgScale);
			particle.setGravity(particle.getGravity() * avgScale);
		} else if (emitter instanceof ParticleEmitter particle) {
			particle.setLatitude(particle.getLatitude() * scale.z);
			particle.setLongitude(particle.getLongitude() * scale.y);
			particle.setInitVelocity(particle.getInitVelocity() * avgScale);
			particle.setGravity(particle.getGravity() * scale.z);
		} else if (emitter instanceof RibbonEmitter particle) {
			particle.setHeightAbove(particle.getHeightAbove() * scale.z);
			particle.setHeightBelow(particle.getHeightBelow() * scale.z);
			particle.setGravity(particle.getGravity() * scale.z);
		} else if (emitter instanceof ParticleEmitterPopcorn particle) {
			particle.setInitVelocity((float) (particle.getInitVelocity() * avgScale));
		}

		for (String flagName : flagNames) {
			AnimFlag<?> animFlag = emitter.find(flagName);
			if (animFlag instanceof FloatAnimFlag flag) {
				for (TreeMap<Integer, Entry<Float>> entryMap : flag.getAnimMap().values()) {
					if (entryMap != null) {
						for (Entry<Float> entry : entryMap.values()) {
							entry.value *= (float) avgScale;
							if (flag.tans()) {
								entry.inTan *= (float) avgScale;
								entry.outTan *= (float) avgScale;
							}
						}
					}
				}
			}
		}
	}

	public void scaleAnimatedTranslations(Vec3 totScale) {
		AnimFlag<?> animFlagT = emitter.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlagT instanceof Vec3AnimFlag translation) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(totScale);
						if (translation.tans()) {
							entry.getInTan().multiply(totScale);
							entry.getOutTan().multiply(totScale);
						}
					}
				}
			}
		}
	}

	private void resetEmitterStuff1() {
		if (emitter instanceof ParticleEmitter2 particle && old instanceof ParticleEmitter2 oldP) {
			particle.setLatitude(oldP.getLatitude());
			particle.setWidth(oldP.getWidth());
			particle.setLength(oldP.getLength());
			particle.getParticleScaling().set(oldP.getParticleScaling());
			particle.setSpeed(oldP.getSpeed());
			particle.setGravity(oldP.getGravity());
		} else if (emitter instanceof ParticleEmitter particle && old instanceof ParticleEmitter oldP) {
			particle.setLatitude(oldP.getLatitude());
			particle.setLongitude(oldP.getLongitude());
			particle.setInitVelocity(oldP.getInitVelocity());
			particle.setGravity(oldP.getGravity());

		} else if (emitter instanceof RibbonEmitter particle && old instanceof RibbonEmitter oldP) {
			particle.setHeightAbove(oldP.getHeightAbove());
			particle.setHeightBelow(oldP.getHeightBelow());
			particle.setGravity(oldP.getGravity());

		} else if (emitter instanceof ParticleEmitterPopcorn particle && old instanceof ParticleEmitterPopcorn oldP) {
			particle.setInitVelocity(oldP.getInitVelocity());
		}

		for (String flagName : flagNames) {
			AnimFlag<?> animFlag = emitter.find(flagName);
			AnimFlag<?> orgAnimFlag = old.find(flagName);
			if (animFlag instanceof FloatAnimFlag flag && orgAnimFlag instanceof FloatAnimFlag orgFlag) {
				flag.setSequenceMap(orgFlag.getAnimMap());
			}
		}
	}

	public void resetAnimatedTranslations() {
		AnimFlag<?> animFlagT = emitter.find(MdlUtils.TOKEN_TRANSLATION);
		AnimFlag<?> orgAnimFlagT = old.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlagT instanceof Vec3AnimFlag translation && orgAnimFlagT instanceof Vec3AnimFlag opgTranslation) {
			translation.setSequenceMap(opgTranslation.getAnimMap());
		}
	}
}
