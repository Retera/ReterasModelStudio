package com.hiveworkshop.wc3.mdl;

import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.render3d.EmitterIdObject;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * ParticleEmitter2 class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class ParticleEmitter extends EmitterIdObject {
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

	public static enum TimeDoubles {
		EmissionRate, Gravity, Longitude, Latitude, LifeSpan, InitVelocity;
	}

	static final String[] timeDoubleNames = { "EmissionRate", "Gravity", "Longitude", "Latitude", "LifeSpan",
			"InitVelocity" };
	double[] timeDoubleData = new double[timeDoubleNames.length];

	boolean MDLEmitter = true;

	String path = null;

	private ParticleEmitter() {

	}

	public ParticleEmitter(final String name) {
		this.name = name;
	}

	public ParticleEmitter(final MdlxParticleEmitter emitter) {
		// debug print:
		if ((emitter.flags & 4096) != 4096) {
			System.err.println("MDX -> MDL error: A particle emitter '" + emitter.name
					+ "' not flagged as particle emitter in MDX!");
		}

		loadObject(emitter);

		setEmissionRate(emitter.emissionRate);
		setGravity(emitter.gravity);
		setInitVelocity(emitter.speed);
		setLatitude(emitter.latitude);
		setLifeSpan(emitter.lifeSpan);
		setLongitude(emitter.longitude);
		setPath(emitter.path);

		setMDLEmitter(((emitter.flags >> 15) & 1) == 1);
		if (!isMDLEmitter() && (((emitter.flags >> 8) & 1) == 1)) {
			System.err.println(
					"WARNING in MDX -> MDL: ParticleEmitter of unknown type! Defaults to EmitterUsesTGA in my MDL code!");
		}
	}

	public MdlxParticleEmitter toMdlx() {
		MdlxParticleEmitter emitter = new MdlxParticleEmitter();

		objectToMdlx(emitter);

		emitter.emissionRate = (float)getEmissionRate();
		emitter.gravity = (float)getGravity();
		emitter.speed = (float)getInitVelocity();
		emitter.latitude = (float)getLatitude();
		emitter.lifeSpan = (float)getLifeSpan();
		emitter.longitude = (float)getLongitude();
		emitter.path = getPath();

		if (isMDLEmitter()) {
			emitter.flags |= 0x8000;
		} else {
			emitter.flags |= 0x10000;
		}
		
		return emitter;
	}

	@Override
	public IdObject copy() {
		final ParticleEmitter x = new ParticleEmitter();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.timeDoubleData = timeDoubleData.clone();
		x.MDLEmitter = MDLEmitter;
		x.path = path;
		x.flags.addAll(flags);

		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
	}

	public double getEmissionRate() {
		return timeDoubleData[TimeDoubles.EmissionRate.ordinal()];
	}

	public void setEmissionRate(final double emissionRate) {
		timeDoubleData[TimeDoubles.EmissionRate.ordinal()] = emissionRate;
	}

	public double getGravity() {
		return timeDoubleData[TimeDoubles.Gravity.ordinal()];
	}

	public void setGravity(final double gravity) {
		timeDoubleData[TimeDoubles.Gravity.ordinal()] = gravity;
	}

	public double getLongitude() {
		return timeDoubleData[TimeDoubles.Longitude.ordinal()];
	}

	public void setLongitude(final double longitude) {
		timeDoubleData[TimeDoubles.Longitude.ordinal()] = longitude;
	}

	public double getLatitude() {
		return timeDoubleData[TimeDoubles.Latitude.ordinal()];
	}

	public void setLatitude(final double latitude) {
		timeDoubleData[TimeDoubles.Latitude.ordinal()] = latitude;
	}

	public double getLifeSpan() {
		return timeDoubleData[TimeDoubles.LifeSpan.ordinal()];
	}

	public void setLifeSpan(final double lifeSpan) {
		timeDoubleData[TimeDoubles.LifeSpan.ordinal()] = lifeSpan;
	}

	public double getInitVelocity() {
		return timeDoubleData[TimeDoubles.InitVelocity.ordinal()];
	}

	public void setInitVelocity(final double initVelocity) {
		timeDoubleData[TimeDoubles.InitVelocity.ordinal()] = initVelocity;
	}

	public boolean isMDLEmitter() {
		return MDLEmitter;
	}

	public void setMDLEmitter(final boolean mDLEmitter) {
		MDLEmitter = mDLEmitter;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.particleEmitter(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	public double getRenderSpeed(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "InitVelocity", (float)getInitVelocity());
	}

	public double getRenderLatitude(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Latitude", (float)getLatitude());
	}

	public double getRenderLongitude(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Longitude", (float)getLongitude());
	}

	public double getRenderLifeSpan(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "LifeSpan", (float)getLifeSpan());
	}

	public double getRenderGravity(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Gravity", (float)getGravity());
	}

	public double getRenderEmissionRate(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "EmissionRate", (float)getEmissionRate());
	}
}
