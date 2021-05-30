package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxExtent;
import com.hiveworkshop.rms.util.Vec3;

/**
 * MinimumExt,MaximumExt,BoundsRad
 *
 * Eric Theller 11/10/2011
 */
public class ExtLog {
	public static final double NO_BOUNDS_RADIUS = -99;
	private Vec3 minimumExtent;
	private Vec3 maximumExtent;
	private double boundsRadius = NO_BOUNDS_RADIUS;
	static double DEFAULT_BOUNDSRADIUS = 100.00;
	static Vec3 DEFAULT_MINEXT = new Vec3(-100, -100, -100);
	static Vec3 DEFAULT_MAXEXT = new Vec3(100, 100, 100);

	public ExtLog(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}

	public ExtLog(final MdlxExtent extent) {
		boundsRadius = extent.boundsRadius;
		minimumExtent = new Vec3(extent.min);
		maximumExtent = new Vec3(extent.max);
	}

	public MdlxExtent toMdlx() {
		final MdlxExtent extent = new MdlxExtent();

		extent.boundsRadius = (float)boundsRadius;
		extent.min = minimumExtent.toFloatArray();
		extent.max = maximumExtent.toFloatArray();

		return extent;
	}

	public ExtLog(final Vec3 minE, final Vec3 maxE) {
		minimumExtent = minE;
		maximumExtent = maxE;
	}

	public ExtLog(final Vec3 minE, final Vec3 maxE, final double boundsRad) {
		minimumExtent = minE;
		maximumExtent = maxE;
		boundsRadius = boundsRad;
	}

	public ExtLog(final float[] minE, final float[] maxE, final double boundsRad) {
		minimumExtent = new Vec3(minE);
		maximumExtent = new Vec3(maxE);
		boundsRadius = boundsRad;
	}

	public ExtLog(final ExtLog other) {
		minimumExtent = new Vec3(other.minimumExtent);
		maximumExtent = new Vec3(other.maximumExtent);
		boundsRadius = other.boundsRadius;
	}

	public void setMinMax(final ExtLog other) {
		if (other != null) {
			if (other.minimumExtent != null) {
				minimumExtent.minimize(other.minimumExtent);
			}
			if (other.maximumExtent != null) {
				maximumExtent.maximize(other.maximumExtent);
			}
			boundsRadius = Math.max(boundsRadius, other.boundsRadius);
		}
	}

	public ExtLog setDefault() {
		minimumExtent = new Vec3(DEFAULT_MINEXT);
		maximumExtent = new Vec3(DEFAULT_MAXEXT);
		return this;
	}

	public void setMinExt(final Vec3 v) {
		minimumExtent = v;
	}

	public void setMaxExt(final Vec3 v) {
		maximumExtent = v;
	}

	public void setBounds(final double b) {
		boundsRadius = b;
	}

	public boolean hasBoundsRadius() {
		return boundsRadius != NO_BOUNDS_RADIUS;
	}

	public Vec3 getMinimumExtent() {
		return minimumExtent;
	}

	public void setMinimumExtent(final Vec3 minimumExtent) {
		this.minimumExtent = minimumExtent;
	}

	public Vec3 getMaximumExtent() {
		return maximumExtent;
	}

	public void setMaximumExtent(final Vec3 maximumExtent) {
		this.maximumExtent = maximumExtent;
	}

	public double getBoundsRadius() {
		return boundsRadius;
	}

	public void setBoundsRadius(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}

	public String toString() {
		return "minExt: " + minimumExtent.toString() + "\nmaxExt: " + maximumExtent.toString() + "\nbonusRad: " + boundsRadius;
	}
}
