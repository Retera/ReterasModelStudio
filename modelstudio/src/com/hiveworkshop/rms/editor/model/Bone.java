package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

/**
 * Bones that make geometry animate.
 *
 * Eric Theller 11/10/2011
 */
public class Bone extends IdObject {
	private int geosetId = -1;
	private boolean multiGeoId;
	private Geoset geoset;
	private GeosetAnim geosetAnim;
	private boolean hasGeoAnim;// Sometimes its "None," sometimes it's not used

	public Bone() {

	}

	public Bone(String name) {
		this.name = name;
		pivotPoint = new Vec3(0, 0, 0);
	}

	protected Bone(Bone b) {
		super(b);

		multiGeoId = b.multiGeoId;
		geoset = b.geoset;
		geosetAnim = b.geosetAnim;
		hasGeoAnim = b.hasGeoAnim;
	}

	@Override
	public Bone copy() {
		return new Bone(this);
	}

//	public void copyMotionFrom(IdObject b) {
//		for (AnimFlag<?> baf : b.getAnimFlags()) {
//			boolean foundMatch = false;
//			for (AnimFlag<?> af : getAnimFlags()) {
//				boolean sameSeq = false;
//				if (baf.getGlobalSeq() == null && af.getGlobalSeq() == null) {
//					sameSeq = true;
//				} else if (baf.getGlobalSeq() != null && af.getGlobalSeq() != null) {
//					sameSeq = baf.getGlobalSeq().equals(af.getGlobalSeq()); // todo check if this should be equals or identical (==)
//				}
//				if (baf.getName().equals(af.getName()) && sameSeq && baf.hasGlobalSeq() == af.hasGlobalSeq()) {
//					// if( && baf.tags.equals(af.tags)
//					foundMatch = true;
//					AnimFlagUtils.copyFrom(af, baf);
//				}
//			}
//			if (!foundMatch) {
//				add(baf);
//			}
//		}
//	}

//	public void clearAnimation(Animation a) {
//		for (AnimFlag<?> af : getAnimFlags()) {
//			af.deleteAnim(a);
//		}
//	}

	/**
	 * Returns true if this bone contains some type of data that moves, scales, rotates, or otherwise changes based on
	 * the time track.
	 */
	public boolean animates() {
		for (AnimFlag<?> af : getAnimFlags()) {
			if (af.size() > 1) {
				return true;
			}
		}
		return false;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public Bone setGeoset(Geoset geoset) {
		this.geoset = geoset;
		return this;
	}

	public GeosetAnim getGeosetAnim() {
		return geosetAnim;
	}

	public Bone setGeosetAnim(GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
		return this;
	}

	public boolean isMultiGeo() {
		return multiGeoId;
	}

	public Bone setMultiGeoId(boolean multiGeoId) {
		this.multiGeoId = multiGeoId;
		return this;
	}

	public boolean controlsGeoset(Geoset geoset) {
		return this.geoset.equals(geoset);
	}

	public boolean controlsGeoset2(Geoset geoset) {
		return this.geoset.equals(geoset) || multiGeoId;
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		if (geosetAnim != null) {
			return geosetAnim.getRenderVisibility(animatedRenderEnvironment);
		}

		return 1;
	}
}
