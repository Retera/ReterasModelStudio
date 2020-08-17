package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;

import com.etheller.warsmash.parsers.mdlx.MdlxBone;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * Bones that make geometry animate.
 *
 * Eric Theller 11/10/2011
 */
public class Bone extends IdObject {
	int geosetId = -1;
	boolean multiGeoId;
	Geoset geoset;
	int geosetAnimId = -1;
	GeosetAnim geosetAnim;
	boolean hasGeoAnim;// Sometimes its "None," sometimes it's not used

	public Bone() {

	}

	public Bone(final String name) {
		this.name = name;
		this.pivotPoint = new Vertex(0, 0, 0);
	}

	public Bone(final Bone b) {
		name = b.name;
		pivotPoint = new Vertex(b.pivotPoint);
		objectId = b.objectId;
		parentId = b.parentId;
		setParent(b.getParent());

		geosetId = b.geosetId;
		multiGeoId = b.multiGeoId;
		geoset = b.geoset;
		geosetAnimId = b.geosetAnimId;
		geosetAnim = b.geosetAnim;
		hasGeoAnim = b.hasGeoAnim;
		for (final AnimFlag af : b.animFlags) {
			animFlags.add(new AnimFlag(af));
		}
		flags = new ArrayList<>(b.flags);
	}

	public Bone(final MdlxBone bone) {
		if ((bone.flags & 256) != 256) {
			System.err.println("MDX -> MDL error: A bone '" + bone.name + "' not flagged as bone in MDX!");
		}

		loadObject(bone);

		geosetId = bone.geosetId;
		geosetAnimId = bone.geosetAnimationId;
	}

	public MdlxBone toMdlx() {
		MdlxBone bone = new MdlxBone();

		objectToMdlx(bone);
		
		bone.geosetId = getGeosetId();
		bone.geosetAnimationId = getGeosetAnimId();

		return bone;
	}

	public void copyMotionFrom(final Bone b) {
		for (final AnimFlag baf : b.animFlags) {
			boolean foundMatch = false;
			for (final AnimFlag af : animFlags) {
				boolean sameSeq = false;
				if (baf.globalSeq == null && af.globalSeq == null) {
					sameSeq = true;
				} else if (baf.globalSeq != null && af.globalSeq != null) {
					sameSeq = baf.globalSeq.equals(af.globalSeq);
				}
				if (baf.getName().equals(af.getName()) && sameSeq && baf.hasGlobalSeq == af.hasGlobalSeq) {
					// if( && baf.tags.equals(af.tags)
					foundMatch = true;
					af.copyFrom(baf);
				}
			}
			if (!foundMatch) {
				animFlags.add(baf);
			}
		}
	}

	public void clearAnimation(final Animation a) {
		for (final AnimFlag af : animFlags) {
			af.deleteAnim(a);
		}
	}

	/**
	 * Returns true if this bone contains some type of data that moves, scales, rotates, or otherwise changes based on
	 * the time track.
	 *
	 * @return
	 */
	public boolean animates() {
		for (final AnimFlag af : animFlags) {
			if (af.size() > 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IdObject copy() {
		return new Bone(this);
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setGeosetAnim(final GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public int getGeosetId() {
		return geosetId;
	}

	/**
	 * @param geosetId
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setGeosetId(final int geosetId) {
		this.geosetId = geosetId;
		setMultiGeoId(geosetId == -1);
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public boolean isMultiGeoId() {
		return multiGeoId;
	}

	/**
	 * @param multiGeoId
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setMultiGeoId(final boolean multiGeoId) {
		this.multiGeoId = multiGeoId;
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public int getGeosetAnimId() {
		return geosetAnimId;
	}

	/**
	 * @param geosetAnimId
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setGeosetAnimId(final int geosetAnimId) {
		this.geosetAnimId = geosetAnimId;
		setHasGeoAnim(geosetAnimId != -1);
	}

	/**
	 * @return
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public boolean isHasGeoAnim() {
		return hasGeoAnim;
	}

	/**
	 * @param hasGeoAnim
	 * @deprecated Recalculated on save
	 */
	@Deprecated
	public void setHasGeoAnim(final boolean hasGeoAnim) {
		this.hasGeoAnim = hasGeoAnim;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.bone(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS * 3 / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		if (geosetAnim != null) {
			return geosetAnim.getRenderVisibility(animatedRenderEnvironment);
		}
		
		return 1;
	}
}
