package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeosetAnimation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

/**
 * The geoset anims, heaven forbid they be forgotten.
 * <p>
 * Eric Theller 11/10/2011
 */
public class GeosetAnim extends TimelineContainer implements Named {
	double staticAlpha = 1;
	Vec3 staticColor = new Vec3(1, 1, 1);
	Geoset geoset;
	boolean dropShadow = false;

	public GeosetAnim(final AnimFlag<?> flag) {
		add(flag);
	}

	public GeosetAnim(final List<AnimFlag<?>> flags) {
		setAnimFlags(flags);
	}

	public GeosetAnim(final Geoset g) {
		geoset = g;
	}

	public GeosetAnim(final Geoset geoset, final GeosetAnim other) {
		addAll(other.getAnimFlags());
		staticAlpha = other.staticAlpha;
		staticColor = other.staticColor;
		this.geoset = geoset;
		dropShadow = other.dropShadow;
	}

	public GeosetAnim(final MdlxGeosetAnimation animation, final EditableModel model) {
		geoset = model.getGeoset(animation.geosetId);
		staticAlpha = animation.alpha;
		staticColor = new Vec3(ModelUtils.flipRGBtoBGR(animation.color));

		final int flags = animation.flags;
		dropShadow = ((flags & 1) == 1);

		loadTimelines(animation);
	}

	public MdlxGeosetAnimation toMdlx(final EditableModel model) {
		final MdlxGeosetAnimation animation = new MdlxGeosetAnimation();

		animation.geosetId = model.computeGeosetID(geoset);

		if (dropShadow) {
			animation.flags |= 1;
		}
		if (find("Color") != null || !staticColor.equals(new Vec3(1, 1, 1))) {
			animation.flags |= 0x2;
		}

		animation.color = ModelUtils.flipRGBtoBGR(getStaticColor().toFloatArray());

		timelinesToMdlx(animation);

		return animation;
	}

	public String getVisTagname() {
		return geoset.getName();
	}

	@Override
	public String getName() {
		return geoset.getName() + "'s Anim";
	}

	@Override
	public void setName(String text) {
	}

	public GeosetAnim getMostVisible(final GeosetAnim partner) {
		if ((getVisibilityFlag() != null) && (partner != null)) {
			final AnimFlag<?> thisFlag = getVisibilityFlag();
			final AnimFlag thatFlag = partner.getVisibilityFlag();
			if (thatFlag != null) {
				final AnimFlag<?> result = thisFlag.getMostVisible(thatFlag);
				if (result == thisFlag) {
					return this;
				} else if (result == thatFlag) {
					return partner;
				}
			}
		}
		return null;
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(final double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vec3 staticColor) {
		this.staticColor = staticColor;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	public boolean isDropShadow() {
		return dropShadow;
	}

	public void setDropShadow(final boolean dropShadow) {
		this.dropShadow = dropShadow;
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	public Vec3 getRenderColor(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Color", staticColor);
	}

	public void copyVisibilityFrom(final VisibilitySource other, final EditableModel mdlr) {
		final VisibilitySource temp = this;
		final AnimFlag<?> visFlag = getVisibilityFlag();// might be null
		final FloatAnimFlag newVisFlag;
		if (visFlag != null) {
			newVisFlag = (FloatAnimFlag) AnimFlag.buildEmptyFrom(visFlag);
		} else {
			newVisFlag = new FloatAnimFlag(temp.visFlagName());
		}
		// newVisFlag = new AnimFlag(temp.visFlagName());
		final FloatAnimFlag flagNew = (FloatAnimFlag) other.getVisibilityFlag();
		// this is an element not favoring existing over imported
		for (final Animation a : mdlr.getAnims()) {
			if (newVisFlag != null) {
				if (!newVisFlag.hasGlobalSeq()) {
					newVisFlag.deleteAnim(a);
					// All entries for visibility are deleted from original-based sources during imported animation times
				}
			}
		}
		if (flagNew != null && newVisFlag != null) {
			newVisFlag.copyFrom(flagNew);
		}
		setVisibilityFlag(newVisFlag);
	}
}
