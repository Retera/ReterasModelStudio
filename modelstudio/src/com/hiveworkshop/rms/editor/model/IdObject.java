package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.NodeUtils;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public abstract class IdObject extends AnimatedNode implements Named {
	public static final int DEFAULT_CLICK_RADIUS = 8;

	protected String name = "";
	protected final Vec3 pivotPoint = new Vec3();
	protected IdObject parent;
	protected final List<IdObject> childrenNodes = new ArrayList<>();
	protected final Mat4 bindPoseM4 = new Mat4();
	private final EnumSet<NodeFlag> nodeFlags = EnumSet.noneOf(NodeFlag.class);

	public IdObject() {
	}

	protected IdObject(final IdObject other) {
		name = other.name;
		nodeFlags.addAll(other.nodeFlags);
		pivotPoint.set(other.pivotPoint);
		setParent(other.parent);
		bindPoseM4.set(other.bindPoseM4);
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

	public void setParent(final IdObject p) {
		if (NodeUtils.isValidHierarchy(this, p)) {
			if (parent != null) {
				parent.childrenNodes.remove(this);
			}
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
		setFlag(NodeFlag.DONT_INHERIT_TRANSLATION, dontInheritTranslation);
	}

	public boolean getDontInheritTranslation() {
		return nodeFlags.contains(NodeFlag.DONT_INHERIT_TRANSLATION);
	}

	public void setDontInheritRotation(boolean dontInheritRotation) {
		setFlag(NodeFlag.DONT_INHERIT_ROTATION, dontInheritRotation);
	}

	public boolean getDontInheritRotation() {
		return nodeFlags.contains(NodeFlag.DONT_INHERIT_ROTATION);
	}

	public void setDontInheritScaling(boolean dontInheritScaling) {
		setFlag(NodeFlag.DONT_INHERIT_SCALING, dontInheritScaling);
	}

	public boolean getDontInheritScaling() {
		return nodeFlags.contains(NodeFlag.DONT_INHERIT_SCALING);
	}

	public void setBillboarded(boolean billboarded) {
		setFlag(NodeFlag.BILLBOARDED, billboarded);
	}

	public boolean getBillboarded() {
		return nodeFlags.contains(NodeFlag.BILLBOARDED);
	}

	public void setBillboardLockX(boolean billboardLockX) {
		setFlag(NodeFlag.BILLBOARDED_LOCK_X, billboardLockX);
	}

	public boolean getBillboardLockX() {
		return nodeFlags.contains(NodeFlag.BILLBOARDED_LOCK_X);
	}

	public void setBillboardLockY(boolean billboardLockY) {
		setFlag(NodeFlag.BILLBOARDED_LOCK_Y, billboardLockY);
	}

	public boolean getBillboardLockY() {
		return nodeFlags.contains(NodeFlag.BILLBOARDED_LOCK_Y);
	}

	public void setBillboardLockZ(boolean billboardLockZ) {
		setFlag(NodeFlag.BILLBOARDED_LOCK_Z, billboardLockZ);
	}

	public boolean getBillboardLockZ() {
		return nodeFlags.contains(NodeFlag.BILLBOARDED_LOCK_Z);
	}

	public boolean isFlagSet(NodeFlag flag){
		return nodeFlags.contains(flag);
	}

	public IdObject setFlag(NodeFlag flag, boolean set){
		if(set){
			nodeFlags.add(flag);
		} else {
			nodeFlags.remove(flag);
		}
		return this;
	}

	public IdObject toggleFlag(NodeFlag flag){
		return setFlag(flag, !nodeFlags.contains(flag));
	}

	public EnumSet<NodeFlag> getNodeFlags() {
		return nodeFlags;
	}

	public Mat4 getBindPoseM4() {
		return bindPoseM4;
	}

	public void setBindPoseM4(Mat4 bindPose) {
		bindPoseM4.set(bindPose);
	}
	public void setBindPoseM4(float[] bindPose) {
		bindPoseM4.setFromBindPose(bindPose);
	}

	public void clearAnimation(Animation a) {
		for (AnimFlag<?> af : getAnimFlags()) {
			af.deleteAnim(a);
		}
	}
	public enum NodeFlag {
		DONT_INHERIT_TRANSLATION(MdlUtils.TOKEN_DONT_INHERIT + MdlUtils.TOKEN_TRANSLATION, 0x01),
		DONT_INHERIT_SCALING(MdlUtils.TOKEN_DONT_INHERIT + MdlUtils.TOKEN_SCALING, 0x02),
		DONT_INHERIT_ROTATION(MdlUtils.TOKEN_DONT_INHERIT + MdlUtils.TOKEN_ROTATION, 0x04),
		BILLBOARDED(MdlUtils.TOKEN_BILLBOARDED, 0x08),
		BILLBOARDED_LOCK_X(MdlUtils.TOKEN_BILLBOARDED_LOCK_X, 0x10),
		BILLBOARDED_LOCK_Y(MdlUtils.TOKEN_BILLBOARDED_LOCK_Y, 0x20),
		BILLBOARDED_LOCK_Z(MdlUtils.TOKEN_BILLBOARDED_LOCK_Z, 0x40),
		CAMERA_ANCHORED(MdlUtils.TOKEN_CAMERA_ANCHORED, 0x80),;
		int flagBit;
		String name;
		NodeFlag(String name, int flagBit){
			this.name = name;
			this.flagBit = flagBit;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public int getFlagBit() {
			return flagBit;
		}

		public static EnumSet<NodeFlag> fromBits(int bits){
			EnumSet<NodeFlag> flagSet = EnumSet.noneOf(NodeFlag.class);
			for (NodeFlag f : NodeFlag.values()){
				if ((f.flagBit & bits) == f.flagBit){
					flagSet.add(f);
				}
			}
			return flagSet;
		}
	}
}
