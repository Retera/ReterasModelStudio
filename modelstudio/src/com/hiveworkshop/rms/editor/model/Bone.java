package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxBone;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

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

	public Bone(String name) {
		this.name = name;
		pivotPoint = new Vec3(0, 0, 0);
	}

	public Bone(Bone b) {
		copyObject(b);

		geosetId = b.geosetId;
		multiGeoId = b.multiGeoId;
		geoset = b.geoset;
		geosetAnimId = b.geosetAnimId;
		geosetAnim = b.geosetAnim;
		hasGeoAnim = b.hasGeoAnim;
	}

	public Bone(MdlxBone mdlxBone) {
		if ((mdlxBone.flags & 256) != 256) {
			System.err.println("MDX -> MDL error: A bone '" + mdlxBone.name + "' not flagged as bone in MDX!");
		}

		loadObject(mdlxBone);

		geosetId = mdlxBone.geosetId;
		geosetAnimId = mdlxBone.geosetAnimationId;
	}

	public MdlxBone toMdlx(EditableModel model) {
		MdlxBone bone = new MdlxBone();

		objectToMdlx(bone, model);

		bone.geosetId = geosetId;
		bone.geosetAnimationId = geosetAnimId;

		return bone;
	}

	@Override
	public Bone copy() {
		return new Bone(this);
	}
	
	public void copyMotionFrom(Bone b) {
		for (AnimFlag<?> baf : b.animFlags.values()) {
			boolean foundMatch = false;
			for (AnimFlag<?> af : animFlags.values()) {
				boolean sameSeq = false;
				if (baf.globalSeqLength == null && af.globalSeqLength == null) {
					sameSeq = true;
				} else if (baf.globalSeqLength != null && af.globalSeqLength != null) {
					sameSeq = baf.globalSeqLength.equals(af.globalSeqLength);
				}
				if (baf.getName().equals(af.getName()) && sameSeq && baf.hasGlobalSeq == af.hasGlobalSeq) {
					// if( && baf.tags.equals(af.tags)
					foundMatch = true;
					af.copyFrom(baf);
				}
			}
			if (!foundMatch) {
				add(baf);
			}
		}
	}

	public void clearAnimation(Animation a) {
		for (AnimFlag<?> af : animFlags.values()) {
			af.deleteAnim(a);
		}
	}

	/**
	 * Returns true if this bone contains some type of data that moves, scales, rotates, or otherwise changes based on
	 * the time track.
	 */
	public boolean animates() {
		for (AnimFlag<?> af : animFlags.values()) {
			if (af.size() > 1) {
				return true;
			}
		}
		return false;
	}

	public boolean isMultiGeo() {
		return multiGeoId;
	}

	public boolean controlsGeoset(Geoset geoset) {
		return this.geoset.equals(geoset);
	}

	public boolean controlsGeoset2(Geoset geoset) {
		return this.geoset.equals(geoset) || multiGeoId;
	}

	@Override
	public double getClickRadius(CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
//		return DEFAULT_CLICK_RADIUS * 3 / coordinateSystem.getZoom();
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		if (geosetAnim != null) {
			return geosetAnim.getRenderVisibility(animatedRenderEnvironment);
		}

		return 1;
	}
}
