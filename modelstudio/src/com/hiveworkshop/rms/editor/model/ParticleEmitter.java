package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

/**
 * ParticleEmitter2 class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
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

	public ParticleEmitter(ParticleEmitter emitter) {
		copyObject(emitter);

		emissionRate = emitter.emissionRate;
		gravity = emitter.gravity;
		longitude = emitter.longitude;
		latitude = emitter.latitude;
		lifeSpan = emitter.lifeSpan;
		initVelocity = emitter.initVelocity;
		MDLEmitter = emitter.MDLEmitter;
		path = emitter.path;
	}

	public ParticleEmitter(MdlxParticleEmitter mdlxEmitter) {
		if ((mdlxEmitter.flags & 4096) != 4096) {
			System.err.println("MDX -> MDL error: A particle emitter '" + mdlxEmitter.name
					+ "' not flagged as particle emitter in MDX!");
		}

		loadObject(mdlxEmitter);

		setEmissionRate(mdlxEmitter.emissionRate);
		setGravity(mdlxEmitter.gravity);
		setInitVelocity(mdlxEmitter.speed);
		setLatitude(mdlxEmitter.latitude);
		setLifeSpan(mdlxEmitter.lifeSpan);
		setLongitude(mdlxEmitter.longitude);
		setPath(mdlxEmitter.path);

		setMDLEmitter(((mdlxEmitter.flags >> 15) & 1) == 1);
		if (!isMDLEmitter() && (((mdlxEmitter.flags >> 8) & 1) == 1)) {
			System.err.println(
					"WARNING in MDX -> MDL: ParticleEmitter of unknown type! Defaults to EmitterUsesTGA in my MDL code!");
		}
	}

	public MdlxParticleEmitter toMdlx(EditableModel model) {
		MdlxParticleEmitter emitter = new MdlxParticleEmitter();

		objectToMdlx(emitter, model);

		emitter.emissionRate = (float) getEmissionRate();
		emitter.gravity = (float) getGravity();
		emitter.speed = (float) getInitVelocity();
		emitter.latitude = (float) getLatitude();
		emitter.lifeSpan = (float) getLifeSpan();
		emitter.longitude = (float) getLongitude();
		emitter.path = getPath();

		if (isMDLEmitter()) {
			emitter.flags |= 0x8000;
		} else {
			emitter.flags |= 0x10000;
		}
		
		return emitter;
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
	public double getClickRadius(CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
	}

	public double getRenderSpeed(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "InitVelocity", (float) getInitVelocity());
	}

	public double getRenderLatitude(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Latitude", (float) getLatitude());
	}

	public double getRenderLongitude(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Longitude", (float) getLongitude());
	}

	public double getRenderLifeSpan(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "LifeSpan", (float) getLifeSpan());
	}

	public double getRenderGravity(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Gravity", (float) getGravity());
	}

	public double getRenderEmissionRate(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "EmissionRate", (float) getEmissionRate());
	}
}
