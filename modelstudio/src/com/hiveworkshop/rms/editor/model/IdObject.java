package com.hiveworkshop.rms.editor.model;

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
	protected int objectId = -1;
	protected int parentId = -1;
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

	public IdObject() {
	}

	public IdObject(final IdObject other) {
		name = other.name;
		objectId = other.objectId;
		parentId = other.parentId;
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

	/**
	 * @return The Object ID
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public int getObjectId() {
		return objectId;
	}

	public int getObjectId(EditableModel model) {
		return model.getObjectId(this);
	}

	/**
	 * @param objectId New object ID value
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public IdObject setObjectId(int objectId) {
		this.objectId = objectId;
		return this;
	}

	/**
	 * @param parentId new Parent ID
	 * @deprecated IF UNSURE, YOU SHOULD USE setParent(), note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public IdObject setParentId(int parentId) {
		this.parentId = parentId;
		return this;
	}

	/**
	 * @return Parent ID
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public int getParentId() {
		if (parent == null) {
			return -1;
		}
		return parent.getObjectId();
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

	@Override
	public List<IdObject> getChildrenNodes() {
		return childrenNodes;
	}

	public float[] getBindPose() {
		return bindPose;
	}

	public void setBindPose(float[] bindPose) {
		this.bindPose = bindPose;
	}
}
