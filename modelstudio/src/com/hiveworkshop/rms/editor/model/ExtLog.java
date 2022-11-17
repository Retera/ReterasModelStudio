package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxExtent;
import com.hiveworkshop.rms.util.Vec3;

/**
 * MinimumExt, MaximumExt, BoundsRad
 *
 * Eric Theller 11/10/2011
 */
public class ExtLog {
	public static final double DEFAULT_BOUNDSRADIUS = 100.00;
	public static final double NO_BOUNDS_RADIUS = -99;
	private static final Vec3 DEFAULT_MINEXT = new Vec3(-100, -100, -100);
	private static final Vec3 DEFAULT_MAXEXT = new Vec3(100, 100, 100);
	private final Vec3 minimumExtent = new Vec3();
	private final Vec3 maximumExtent = new Vec3();
	private double boundsRadius;

	public ExtLog(final double boundsRadius) {
		this(DEFAULT_MINEXT, DEFAULT_MAXEXT, boundsRadius);
	}

	public ExtLog() {
		this(DEFAULT_MINEXT, DEFAULT_MAXEXT, NO_BOUNDS_RADIUS);
	}

	public ExtLog(final MdlxExtent extent) {
		this(extent.min, extent.max, extent.boundsRadius);
	}

	public ExtLog(final Vec3 minE, final Vec3 maxE) {
		this(minE, maxE, NO_BOUNDS_RADIUS);
	}

	public ExtLog(final Vec3 minE, final Vec3 maxE, final double boundsRad) {
		minimumExtent.set(minE);
		maximumExtent.set(maxE);
		boundsRadius = boundsRad;
	}

	public ExtLog(final float[] minE, final float[] maxE, final double boundsRad) {
		minimumExtent.set(minE);
		maximumExtent.set(maxE);
		boundsRadius = boundsRad;
	}

	private ExtLog(final ExtLog other) {
		minimumExtent.set(other.minimumExtent);
		maximumExtent.set(other.maximumExtent);
		boundsRadius = other.boundsRadius;
	}

	public MdlxExtent toMdlx() {
		final MdlxExtent extent = new MdlxExtent();

		extent.boundsRadius = (float) boundsRadius;
		extent.min = minimumExtent.toFloatArray();
		extent.max = maximumExtent.toFloatArray();

		return extent;
	}

	public ExtLog setMinMax(final ExtLog other) {
		if (other != null) {
			minimumExtent.minimize(other.minimumExtent);
			maximumExtent.maximize(other.maximumExtent);
			boundsRadius = Math.max(boundsRadius, other.boundsRadius);
		}
		return this;
	}

	public ExtLog set(final ExtLog other) {
		if (other != null) {
			minimumExtent.set(other.minimumExtent);
			maximumExtent.set(other.maximumExtent);
			boundsRadius = other.boundsRadius;
		} else {
			setDefault();
		}
		return this;
	}

	public ExtLog setDefault() {
		minimumExtent.set(DEFAULT_MINEXT);
		maximumExtent.set(DEFAULT_MAXEXT);
		return this;
	}

	public ExtLog setExt(final Vec3 minE, final Vec3 maxE) {
		minimumExtent.set(minE);
		maximumExtent.set(maxE);
		return this;
	}

	public Vec3 getMinimumExtent() {
		return minimumExtent;
	}

	public ExtLog setMinExt(final Vec3 v) {
		minimumExtent.set(v);
		return this;
	}

	public Vec3 getMaximumExtent() {
		return maximumExtent;
	}

	public ExtLog setMaxExt(final Vec3 v) {
		maximumExtent.set(v);
		return this;
	}

	public double getBoundsRadius() {
		return boundsRadius;
	}

	public ExtLog setBoundsRadius(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
		return this;
	}

	public boolean hasBoundsRadius() {
		return boundsRadius != NO_BOUNDS_RADIUS;
	}

	public ExtLog deepCopy() {
		return new ExtLog(this);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof ExtLog){
			return minimumExtent.equalLocs(((ExtLog)obj).minimumExtent)
					&& maximumExtent.equalLocs(((ExtLog)obj).maximumExtent)
					&& boundsRadius == ((ExtLog)obj).boundsRadius;
		}
		return false;
	}

	public String toString() {
		return "minExt: " + minimumExtent + "\nmaxExt: " + maximumExtent + "\nbonusRad: " + boundsRadius;
//		return "minExt: " + minimumExtent.toString() + "\nmaxExt: " + maximumExtent.toString() + "\nbonusRad: " + boundsRadius;
	}
}
