package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

/**
 * ParticleEmitter specific animation tags
 * 	KPEE - Particle emitter emission rate
 * 	KPEG - Particle emitter gravity
 * 	KPLN - Particle emitter longitude
 * 	KPLT - Particle emitter latitude
 * 	KPEL - Particle emitter lifespan
 * 	KPES - Particle emitter initial velocity
 * 	KPEV - Particle emitter visibility
 */
public class ParticleEmitter extends EmitterIdObject {
	double emissionRate = 0;
	double gravity = 0;
	double longitude = 0;
	double latitude = 0;
	double lifeSpan = 0;
	double initVelocity = 0;
	boolean MDLEmitter = true;
	String path = "";

	public ParticleEmitter() {

	}

	public ParticleEmitter(String name) {
		this.name = name;
	}

	private ParticleEmitter(ParticleEmitter emitter) {
		super(emitter);

		emissionRate = emitter.emissionRate;
		gravity = emitter.gravity;
		longitude = emitter.longitude;
		latitude = emitter.latitude;
		lifeSpan = emitter.lifeSpan;
		initVelocity = emitter.initVelocity;
		MDLEmitter = emitter.MDLEmitter;
		path = emitter.path;
	}

	@Override
	public ParticleEmitter copy() {
		return new ParticleEmitter(this);
	}

	@Override
	public int getBlendSrc() {
		return 0;
	}

	@Override
	public int getBlendDst() {
		return 0;
	}

	@Override
	public int getRows() {
		return 0;
	}

	@Override
	public int getCols() {
		return 0;
	}

	@Override
	public boolean isRibbonEmitter() {
		return false;
	}

	public double getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(double emissionRate) {
		this.emissionRate = emissionRate;
	}

	public double getGravity() {
		return gravity;
	}

	public void setGravity(double gravity) {
		this.gravity = gravity;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLifeSpan() {
		return this.lifeSpan;
	}

	public void setLifeSpan(double lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public double getInitVelocity() {
		return initVelocity;
	}

	public void setInitVelocity(double initVelocity) {
		this.initVelocity = initVelocity;
	}

	public boolean isMDLEmitter() {
		return MDLEmitter;
	}

	public void setMDLEmitter(boolean mDLEmitter) {
		MDLEmitter = mDLEmitter;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}

	public double getRenderSpeed(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_INIT_VELOCITY, (float) getInitVelocity());
	}

	public double getRenderLatitude(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_LATITUDE, (float) getLatitude());
	}

	public double getRenderLongitude(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_LONGITUDE, (float) getLongitude());
	}

	public double getRenderLifeSpan(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_LIFE_SPAN, (float) getLifeSpan());
	}

	public double getRenderGravity(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_GRAVITY, (float) getGravity());
	}

	public double getRenderEmissionRate(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_EMISSION_RATE, (float) getEmissionRate());
	}
}
