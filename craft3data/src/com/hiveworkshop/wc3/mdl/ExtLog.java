package com.hiveworkshop.wc3.mdl;

import com.etheller.warsmash.parsers.mdlx.MdlxExtent;

/**
 * MinimumExt,MaximumExt,BoundsRad
 *
 * Eric Theller 11/10/2011
 */
public class ExtLog {
	public static final double NO_BOUNDS_RADIUS = -99;
	private Vertex minimumExtent;
	private Vertex maximumExtent;
	private double boundsRadius = NO_BOUNDS_RADIUS;
	static double DEFAULT_BOUNDSRADIUS = 100.00;
	static Vertex DEFAULT_MINEXT = new Vertex(-100, -100, -100);
	static Vertex DEFAULT_MAXEXT = new Vertex(100, 100, 100);

	public ExtLog(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}

	public ExtLog(final MdlxExtent extent) {
		boundsRadius = extent.boundsRadius;
		minimumExtent = new Vertex(extent.min);
		maximumExtent = new Vertex(extent.max);
	}

	public MdlxExtent toMdlx() {
		MdlxExtent extent = new MdlxExtent();

		extent.boundsRadius = (float)boundsRadius;
		extent.min = minimumExtent.toFloatArray();
		extent.max = maximumExtent.toFloatArray();

		return extent;
	}

	public ExtLog(final Vertex minE, final Vertex maxE) {
		minimumExtent = minE;
		maximumExtent = maxE;
	}

	public ExtLog(final Vertex minE, final Vertex maxE, final double boundsRad) {
		minimumExtent = minE;
		maximumExtent = maxE;
		boundsRadius = boundsRad;
	}

	public ExtLog(final float[] minE, final float[] maxE, final double boundsRad) {
		minimumExtent = new Vertex(minE);
		maximumExtent = new Vertex(maxE);
		boundsRadius = boundsRad;
	}

	public ExtLog(final ExtLog other) {
		minimumExtent = other.minimumExtent;
		maximumExtent = other.maximumExtent;
		boundsRadius = other.boundsRadius;
	}

	public void setMinExt(final Vertex v) {
		minimumExtent = v;
	}

	public void setMaxExt(final Vertex v) {
		maximumExtent = v;
	}

	public void setBounds(final double b) {
		boundsRadius = b;
	}

	public boolean hasBoundsRadius() {
		return boundsRadius != NO_BOUNDS_RADIUS;
	}

	public Vertex getMinimumExtent() {
		return minimumExtent;
	}

	public void setMinimumExtent(final Vertex minimumExtent) {
		this.minimumExtent = minimumExtent;
	}

	public Vertex getMaximumExtent() {
		return maximumExtent;
	}

	public void setMaximumExtent(final Vertex maximumExtent) {
		this.maximumExtent = maximumExtent;
	}

	public double getBoundsRadius() {
		return boundsRadius;
	}

	public void setBoundsRadius(final double boundsRadius) {
		this.boundsRadius = boundsRadius;
	}
}
