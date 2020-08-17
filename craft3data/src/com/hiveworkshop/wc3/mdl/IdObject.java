package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.etheller.warsmash.parsers.mdlx.MdlxGenericObject;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * Write a description of class ObjectId here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public abstract class IdObject extends AnimatedNode implements Named {
	public static final int DEFAULT_CLICK_RADIUS = 8;

	public static enum NodeFlags {
		DONTINHERIT_TRANSLATION("DontInherit { Translation }"), DONTINHERIT_SCALING("DontInherit { Scaling }"),
		DONTINHERIT_ROTATION("DontInherit { Rotation }"), BILLBOARDED("Billboarded"),
		BILLBOARD_LOCK_X("BillboardedLockX", "BillboardLockX"), BILLBOARD_LOCK_Y("BillboardedLockY", "BillboardLockY"),
		BILLBOARD_LOCK_Z("BillboardedLockZ", "BillboardLockZ"), CAMERA_ANCHORED("CameraAnchored");

		String mdlText;
		private String[] otherAcceptedStrings;

		NodeFlags(final String str) {
			this.mdlText = str;
		}

		NodeFlags(final String str, final String... otherAcceptedStrings) {
			this.mdlText = str;
			this.otherAcceptedStrings = otherAcceptedStrings;
		}

		public boolean matches(final String text) {
			if (mdlText.equals(text)) {
				return true;
			}
			if (otherAcceptedStrings != null) {
				for (final String otherAcceptedString : otherAcceptedStrings) {
					if (otherAcceptedString.equals(text)) {
						return true;
					}
				}
			}
			return false;
		}

		public String getMdlText() {
			return mdlText;
		}

		public static NodeFlags fromId(final int id) {
			return values()[id];
		}
	}

	protected String name;
	protected Vertex pivotPoint;
	protected int objectId = -1;
	protected int parentId = -1;
	protected IdObject parent;
	private final List<IdObject> childrenNodes = new ArrayList<>();
	protected float[] bindPose;

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public IdObject() {

	}

	public IdObject(final IdObject host) {
		name = host.name;
		pivotPoint = host.pivotPoint;
		objectId = host.objectId;
		parentId = host.parentId;
		setParent(host.parent);
	}

	public void setPivotPoint(final Vertex p) {
		pivotPoint = p;
	}

	public void setParent(final IdObject p) {
		if (parent != null) {
			parent.childrenNodes.remove(this);
		}
		parent = p;
		if (parent != null) {
			parent.childrenNodes.add(this);
		}
	}

	public IdObject copy() {
		return null;
	}

	public boolean childOf(final IdObject other) {
		if (parent != null) {
			if (parent == other) {
				return true;
			} else {
				return parent.childOf(other);
			}
		}
		return false;
	}

	public abstract double getClickRadius(CoordinateSystem coordinateSystem);

	public boolean parentOf(final IdObject other, final HashMap<IdObject, ArrayList<IdObject>> childMap) {
		final ArrayList<IdObject> children = childMap.get(this);
		if (children != null) {
			if (children.contains(other)) {
				return true;
			} else {
				boolean deepChild = false;
				for (int i = 0; !deepChild && (i < children.size()); i++) {
					deepChild = children.get(i).parentOf(other, childMap);
				}
				return deepChild;
			}
		}
		return false;
	}

	public ArrayList<IdObject> getAllChildren(final HashMap<IdObject, ArrayList<IdObject>> childMap) {
		final ArrayList<IdObject> children = childMap.get(this);
		final ArrayList<IdObject> allChildren = new ArrayList<>();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				final IdObject child = children.get(i);
				if (!allChildren.contains(child)) {
					allChildren.add(child);
					allChildren.addAll(child.getAllChildren(childMap));
				}
			}
		}

		return allChildren;
	}

	@Override
	public boolean hasFlag(final NodeFlags flag) {
		for (final String flagInThisObject : getFlags()) {
			if (flag.matches(flagInThisObject)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 *
	 * @return The Object ID
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId New object ID value
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public void setObjectId(final int objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return Parent ID
	 * @deprecated Note that all object IDs are deleted and regenerated at save
	 */
	@Deprecated
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId new Parent ID
	 * @deprecated IF UNSURE, YOU SHOULD USE setParent(), note that all object IDs
	 *             are deleted and regenerated at save
	 */
	@Deprecated
	public void setParentId(final int parentId) {
		this.parentId = parentId;
	}

	public void loadObject(final MdlxGenericObject object) {
		name = object.name;
		objectId = object.objectId;
		parentId = object.parentId;

		int flags = object.flags;
		int shift = 0;
		for (final IdObject.NodeFlags flag : IdObject.NodeFlags.values()) {
			if (((flags >>> shift) & 1) == 1) {
				add(flag.getMdlText());
			}

			shift++;
		}

		loadTimelines(object);
	}

	public void objectToMdlx(final MdlxGenericObject object) {
		object.name = getName();
		object.objectId = getObjectId();
		object.parentId = getParentId();
		
		for (final String flag : getFlags()) {
			if (flag.equals("BillboardedLockZ")) {
				object.flags |= 0x40;
			} else if (flag.equals("BillboardedLockY")) {
				object.flags |= 0x20;
			} else if (flag.equals("BillboardedLockX")) {
				object.flags |= 0x10;
			} else if (flag.equals("Billboarded")) {
				object.flags |= 0x8;
			} else if (flag.equals("CameraAnchored")) {
				object.flags |= 0x80;
			} else if (flag.equals("DontInherit { Rotation }")) {
				object.flags |= 0x2;
			} else if (flag.equals("DontInherit { Translation }")) {
				object.flags |= 0x1;
			} else if (flag.equals("DontInherit { Scaling }")) {
				object.flags |= 0x4;
			}
		}

		timelinesToMdlx(object);
	}

	@Override
	public Vertex getPivotPoint() {
		return pivotPoint;
	}

	@Override
	public IdObject getParent() {
		return parent;
	}

	public abstract void apply(IdObjectVisitor visitor);

	public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Scaling", null);
	}

	@Override
	public List<IdObject> getChildrenNodes() {
		return childrenNodes;
	}

	public float[] getBindPose() {
		return bindPose;
	}

	public void setBindPose(final float[] bindPose) {
		this.bindPose = bindPose;
	}

	private static final Vector4f translationHeap = new Vector4f();
	private static final Matrix4f matrixHeap = new Matrix4f();
	private static final Quaternion rotationHeap = new Quaternion();
	private static final Quaternion rotationDeltaHeap = new Quaternion();
	private static final Vector4f axisAngleHeap = new Vector4f();

	private static final Vector3f IDENTITY = new Vector3f(0, 0, 0);
}
