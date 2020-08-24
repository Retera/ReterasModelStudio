package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGeosetAnimation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

/**
 * The geoset anims, heaven forbid they be forgotten.
 * <p>
 * Eric Theller 11/10/2011
 */
public class GeosetAnim extends TimelineContainer implements Named {
	double staticAlpha = 1;
	Vertex staticColor = new Vertex(1, 1, 1);
	int geosetId = -1;
	Geoset geoset;
	boolean dropShadow = false;

	public GeosetAnim(final AnimFlag flag) {
		add(flag);
	}

	public GeosetAnim(final List<AnimFlag> flags) {
		setAnimFlags(flags);
	}

	public GeosetAnim(final Geoset g) {
		geoset = g;
	}

	public GeosetAnim(final Geoset geoset, final GeosetAnim other) {
		addAll(other.getAnimFlags());
		staticAlpha = other.staticAlpha;
		staticColor = other.staticColor;
		geosetId = other.geosetId;
		this.geoset = geoset;
		dropShadow = other.dropShadow;
	}

	public GeosetAnim(final MdlxGeosetAnimation animation) {
		geosetId = animation.geosetId;
		setStaticAlpha(animation.alpha);

		final int flags = animation.flags;

		setDropShadow((flags & 1) == 1);

		setStaticColor(new Vertex(ModelUtils.flipRGBtoBGR(animation.color)));

		loadTimelines(animation);
	}

	public MdlxGeosetAnimation toMdlx() {
		final MdlxGeosetAnimation animation = new MdlxGeosetAnimation();

		animation.geosetId = getGeosetId();

		if (dropShadow) {
			animation.flags |= 1;
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

	public GeosetAnim getMostVisible(final GeosetAnim partner) {
		if ((getVisibilityFlag() != null) && (partner != null)) {
			final AnimFlag thisFlag = getVisibilityFlag();
			final AnimFlag thatFlag = partner.getVisibilityFlag();
			if (thatFlag != null) {
				final AnimFlag result = thisFlag.getMostVisible(thatFlag);
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

	public Vertex getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vertex staticColor) {
		this.staticColor = staticColor;
	}

	/**
	 * @return
	 * @deprecated Used for MDX -> MDL code
	 */
	@Deprecated
	public int getGeosetId() {
		return geosetId;
	}

	/**
	 * @param geosetId
	 * @deprecated Used for MDX -> MDL code
	 */
	@Deprecated
	public void setGeosetId(final int geosetId) {
		this.geosetId = geosetId;
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

	private static final Vector3f renderColorVector = new Vector3f();

	public Vector3f getRenderColor(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final Vertex c = getInterpolatedVector(animatedRenderEnvironment, "Color", staticColor);

		renderColorVector.x = (float) c.x;
		renderColorVector.y = (float) c.y;
		renderColorVector.z = (float) c.z;

		return renderColorVector;
	}

	public void copyVisibilityFrom(final VisibilitySource other, final EditableModel mdlr) {
		final VisibilitySource temp = this;
		final AnimFlag visFlag = temp.getVisibilityFlag();// might be
		// null
		final AnimFlag newVisFlag;
		boolean tans = false;
		if (visFlag != null) {
			newVisFlag = AnimFlag.buildEmptyFrom(visFlag);
			tans = visFlag.tans();
		} else {
			newVisFlag = new AnimFlag(temp.visFlagName());
		}
		// newVisFlag = new AnimFlag(temp.visFlagName());
		final AnimFlag flagNew = other.getVisibilityFlag();
		// this is an element not favoring existing over imported
		for (final Animation a : mdlr.getAnims()) {
			if (newVisFlag != null) {
				if (!newVisFlag.hasGlobalSeq()) {
					newVisFlag.deleteAnim(a);// All entries for
					// visibility are
					// deleted from
					// original-based
					// sources during
					// imported animation
					// times
				}
			}
		}
		if (flagNew != null) {
			newVisFlag.copyFrom(flagNew);
		}
		setVisibilityFlag(newVisFlag);
	}
}
