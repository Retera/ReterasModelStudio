package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

/**
 * The geoset anims, heaven forbid they be forgotten.
 * <p>
 * Eric Theller 11/10/2011
 */
//public class GeosetAnim extends TimelineContainer implements Named {
public class GeosetAnim extends TimelineContainer {
	private double staticAlpha = 1;
	private final Vec3 staticColor = new Vec3(1, 1, 1);
	private boolean dropShadow = false;
	private Geoset geoset;

	public GeosetAnim(AnimFlag<?> flag) {
		add(flag);
	}

	public GeosetAnim(List<AnimFlag<?>> flags) {
		setAnimFlags(flags);
	}

	public GeosetAnim(Geoset g) {
		geoset = g;
	}

	private GeosetAnim(GeosetAnim other) {
		copyTimelines(other);
		geoset = other.geoset;
		staticAlpha = other.staticAlpha;
		staticColor.set(other.staticColor);
		dropShadow = other.dropShadow;
	}

	@Override
	public String visFlagName() {
		return MdlUtils.TOKEN_ALPHA;
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(Vec3 staticColor) {
		this.staticColor.set(staticColor);
	}

	public boolean isDropShadow() {
		return dropShadow;
	}

	public void setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public GeosetAnim setGeoset(Geoset geoset) {
		this.geoset = geoset;
		return this;
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	public Vec3 getRenderColor(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Color", staticColor);
	}

	public GeosetAnim deepCopy(){
		return new GeosetAnim(this);
	}
}
