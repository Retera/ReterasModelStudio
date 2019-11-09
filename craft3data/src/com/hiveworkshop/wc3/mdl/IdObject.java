package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.Node;

/**
 * Write a description of class ObjectId here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public abstract class IdObject extends AbstractAnimatedNode implements Named {
	public static final int DEFAULT_CLICK_RADIUS = 8;

	public static enum NodeFlags {
		DONTINHERIT_TRANSLATION("DontInherit { Translation }"), DONTINHERIT_SCALING("DontInherit { Scaling }"),
		DONTINHERIT_ROTATION("DontInherit { Rotation }"), BILLBOARDED("Billboarded"),
		BILLBOARD_LOCK_X("BillboardLockX"), BILLBOARD_LOCK_Y("BillboardLockY"), BILLBOARD_LOCK_Z("BillboardLockZ"),
		CAMERA_ANCHORED("CameraAnchored");

		String mdlText;

		NodeFlags(final String str) {
			this.mdlText = str;
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

	public static IdObject read(final BufferedReader mdl) {
		return null;
	}

	public abstract void printTo(PrintWriter writer);

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
		return getFlags().contains(flag.getMdlText());
	}

	public abstract void flipOver(byte axis);

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

	protected void loadFrom(final Node node) {
		// ----- Convert Base NODE to "IDOBJECT" -----
		name = node.name;
		parentId = node.parentId;
		objectId = node.objectId;
		int shift = 0;
		for (final IdObject.NodeFlags flag : IdObject.NodeFlags.values()) {
			if (((node.flags >>> shift) & 1) == 1) {
				add(flag.getMdlText());
			}
			shift++;
		}
		// translations next
		if (node.geosetTranslation != null) {
			add(new AnimFlag(node.geosetTranslation));
		}
		if (node.geosetScaling != null) {
			add(new AnimFlag(node.geosetScaling));
		}
		if (node.geosetRotation != null) {
			add(new AnimFlag(node.geosetRotation));
		}
		// ----- End Base NODE to "IDOBJECT" -----
	}

	@Override
	public Vertex getPivotPoint() {
		return pivotPoint;
	}

	@Override
	public IdObject getParent() {
		return parent;
	}

	@Override
	public abstract void add(AnimFlag af);

	public abstract void add(String flag);

	public abstract List<String> getFlags();

	@Override
	public abstract List<AnimFlag> getAnimFlags();

	public abstract void apply(IdObjectVisitor visitor);

	@Override
	public abstract float getRenderVisibility(AnimatedRenderEnvironment animatedRenderEnvironment);

	@Override
	public abstract Vertex getRenderTranslation(AnimatedRenderEnvironment animatedRenderEnvironment);

	@Override
	public abstract QuaternionRotation getRenderRotation(AnimatedRenderEnvironment animatedRenderEnvironment);

	@Override
	public abstract Vertex getRenderScale(AnimatedRenderEnvironment animatedRenderEnvironment);

	@Override
	public void remove(final AnimFlag af) {
		getAnimFlags().remove(af);
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
