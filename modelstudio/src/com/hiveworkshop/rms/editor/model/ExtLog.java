package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxExtent;
import com.hiveworkshop.rms.util.Vector3;

/**
 * MinimumExt,MaximumExt,BoundsRad
 *
 * Eric Theller 11/10/2011
 */
public class ExtLog {
	public static final double NO_BOUNDS_RADIUS = -99;
	private Vector3 minimumExtent;
	private Vector3 maximumExtent;
	private double boundsRadius = NO_BOUNDS_RADIUS;
	static double DEFAULT_BOUNDSRADIUS = 100.00;
	static Vector3 DEFAULT_MINEXT = new Vector3(-100, -100, -100);
	static Vector3 DEFAULT_MAXEXT = new Vector3(100, 100, 100);

	public ExtLog(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}

	public ExtLog(final MdlxExtent extent) {
		boundsRadius = extent.boundsRadius;
		minimumExtent = new Vector3(extent.min);
		maximumExtent = new Vector3(extent.max);
	}

	public MdlxExtent toMdlx() {
		final MdlxExtent extent = new MdlxExtent();

		extent.boundsRadius = (float)boundsRadius;
		extent.min = minimumExtent.toFloatArray();
		extent.max = maximumExtent.toFloatArray();

		return extent;
	}

	public ExtLog(final Vector3 minE, final Vector3 maxE) {
		minimumExtent = minE;
		maximumExtent = maxE;
	}

	public ExtLog(final Vector3 minE, final Vector3 maxE, final double boundsRad) {
		minimumExtent = minE;
		maximumExtent = maxE;
		boundsRadius = boundsRad;
	}

	public ExtLog(final float[] minE, final float[] maxE, final double boundsRad) {
		minimumExtent = new Vector3(minE);
		maximumExtent = new Vector3(maxE);
		boundsRadius = boundsRad;
	}

	public ExtLog(final ExtLog other) {
		minimumExtent = other.minimumExtent;
		maximumExtent = other.maximumExtent;
		boundsRadius = other.boundsRadius;
	}

	public void setMinExt(final Vector3 v) {
		minimumExtent = v;
	}

	public void setMaxExt(final Vector3 v) {
		maximumExtent = v;
	}

	public void setBounds(final double b) {
		boundsRadius = b;
	}

	public boolean hasBoundsRadius() {
		return boundsRadius != NO_BOUNDS_RADIUS;
	}

	public Vector3 getMinimumExtent() {
		return minimumExtent;
	}

	public void setMinimumExtent(final Vector3 minimumExtent) {
		this.minimumExtent = minimumExtent;
	}

	public Vector3 getMaximumExtent() {
		return maximumExtent;
	}

	public void setMaximumExtent(final Vector3 maximumExtent) {
		this.maximumExtent = maximumExtent;
	}

	public double getBoundsRadius() {
		return boundsRadius;
	}

	public void setBoundsRadius(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}
}
