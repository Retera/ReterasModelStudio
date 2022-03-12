package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Write a description of class ObjectId here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public abstract class IdObject extends AnimatedNode implements Named {
	public static final int DEFAULT_CLICK_RADIUS = 8;

	protected String name = "";
	protected boolean dontInheritTranslation = false;
	protected boolean dontInheritRotation = false;
	protected boolean dontInheritScaling = false;
	protected boolean billboarded = false;
	protected boolean billboardLockX = false;
	protected boolean billboardLockY = false;
	protected boolean billboardLockZ = false;
	protected Vec3 pivotPoint = new Vec3();
	protected IdObject parent;
	protected final List<IdObject> childrenNodes = new ArrayList<>();
	protected float[] bindPose;
	protected Mat4 bindPoseM4;

	public IdObject() {
	}

	protected IdObject(final IdObject other) {
		name = other.name;
		dontInheritTranslation = other.dontInheritTranslation;
		dontInheritRotation = other.dontInheritRotation;
		dontInheritScaling = other.dontInheritScaling;
		billboarded = other.billboarded;
		billboardLockX = other.billboardLockX;
		billboardLockY = other.billboardLockY;
		billboardLockZ = other.billboardLockZ;
//		pivotPoint = new Vec3(other.pivotPoint);
		pivotPoint.set(other.pivotPoint);
		setParent(other.parent);
		if (other.bindPose != null) {
			bindPose = other.bindPose.clone();
		}
		if (other.bindPoseM4 != null){
			bindPoseM4 = new Mat4().set(other.bindPoseM4);
		}
		copyTimelines(other);
	}

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setPivotPoint(final Vec3 p) {
		pivotPoint.set(p);
	}

	public void setParent(final IdObject p) {
		if (parent != null) {
			parent.childrenNodes.remove(this);
		}
		if (parent != this) {
			parent = p;
			if (parent != null) {
				parent.childrenNodes.add(this);
			}
		}
	}

	public abstract IdObject copy();

	public abstract double getClickRadius();

	public int getObjectId(EditableModel model) {
		return model.getObjectId(this);
	}

	public int getParentId(EditableModel model) {
		if (parent == null) {
			return -1;
		}
		return model.getObjectId(parent);
	}

	public void setDontInheritTranslation(boolean dontInheritTranslation) {
		this.dontInheritTranslation = dontInheritTranslation;
	}

	public boolean getDontInheritTranslation() {
		return dontInheritTranslation;
	}

	public void setDontInheritRotation(boolean dontInheritRotation) {
		this.dontInheritRotation = dontInheritRotation;
	}

	public boolean getDontInheritRotation() {
		return dontInheritRotation;
	}

	public void setDontInheritScaling(boolean dontInheritScaling) {
		this.dontInheritScaling = dontInheritScaling;
	}

	public boolean getDontInheritScaling() {
		return dontInheritScaling;
	}

	public void setBillboarded(boolean billboarded) {
		this.billboarded = billboarded;
	}

	public boolean getBillboarded() {
		return billboarded;
	}

	public void setBillboardLockX(boolean billboardLockX) {
		this.billboardLockX = billboardLockX;
	}

	public boolean getBillboardLockX() {
		return billboardLockX;
	}

	public void setBillboardLockY(boolean billboardLockY) {
		this.billboardLockY = billboardLockY;
	}

	public boolean getBillboardLockY() {
		return billboardLockY;
	}

	public void setBillboardLockZ(boolean billboardLockZ) {
		this.billboardLockZ = billboardLockZ;
	}

	public boolean getBillboardLockZ() {
		return billboardLockZ;
	}

	@Override
	public Vec3 getPivotPoint() {
		return pivotPoint;
	}

	public IdObject getParent() {
		return parent;
	}

	public List<IdObject> getChildrenNodes() {
		return childrenNodes;
	}

	public float[] getBindPose() {
		return bindPose;
	}
	public Mat4 getBindPoseM4() {
		return bindPoseM4;
	}

	public void setBindPose(float[] bindPose) {
		this.bindPose = bindPose;
		if(bindPose != null){
			if(bindPoseM4 == null){
				bindPoseM4 = new Mat4();
			}
			bindPoseM4.setFromBindPose(bindPose);
		} else {
			bindPoseM4 = null;
		}
	}
	public void setBindPoseM4(Mat4 bindPose) {
		bindPoseM4 = bindPose;
		if(bindPose != null){
			this.bindPose = bindPose.getBindPose();
		} else {
			this.bindPose = null;
		}
	}


	public void copyMotionFrom(IdObject b) {
		for (AnimFlag<?> baf : b.getAnimFlags()) {
			boolean foundMatch = false;
			for (AnimFlag<?> af : getAnimFlags()) {
				boolean sameSeq = false;
				if (baf.getGlobalSeq() == null && af.getGlobalSeq() == null) {
					sameSeq = true;
				} else if (baf.getGlobalSeq() != null && af.getGlobalSeq() != null) {
					sameSeq = baf.getGlobalSeq().equals(af.getGlobalSeq()); // todo check if this should be equals or identical (==)
				}
				if (baf.getName().equals(af.getName()) && sameSeq && baf.hasGlobalSeq() == af.hasGlobalSeq()) {
					// if( && baf.tags.equals(af.tags)
					foundMatch = true;
					AnimFlagUtils.copyFrom(af, baf);
				}
			}
			if (!foundMatch) {
				add(baf);
			}
		}
	}

	public void clearAnimation(Animation a) {
		for (AnimFlag<?> af : getAnimFlags()) {
			af.deleteAnim(a);
		}
	}
}
