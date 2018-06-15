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

import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.Node;

/**
 * Write a description of class ObjectId here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public abstract class IdObject implements Named, TimelineContainer {
	public static final int DEFAULT_CLICK_RADIUS = 8;

	public static enum NodeFlags {
		DONTINHERIT_TRANSLATION("DontInherit { Translation }"),
		DONTINHERIT_SCALING("DontInherit { Scaling }"),
		DONTINHERIT_ROTATION("DontInherit { Rotation }"),
		BILLBOARDED("Billboarded"),
		BILLBOARD_LOCK_X("BillboardLockX"),
		BILLBOARD_LOCK_Y("BillboardLockY"),
		BILLBOARD_LOCK_Z("BillboardLockZ"),
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

	String name;
	Vertex pivotPoint;
	int objectId = -1;
	int parentId = -1;
	private IdObject parent;
	private final List<IdObject> childrenNodes = new ArrayList<>();

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
				for (int i = 0; !deepChild && i < children.size(); i++) {
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
	 * @param objectId
	 *            New object ID value
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
	 * @param parentId
	 *            new Parent ID
	 * @deprecated IF UNSURE, YOU SHOULD USE setParent(), note that all object IDs are deleted and regenerated at save
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

	public Vertex getPivotPoint() {
		return pivotPoint;
	}

	public IdObject getParent() {
		return parent;
	}

	@Override
	public abstract void add(AnimFlag af);

	public abstract void add(String flag);

	public abstract List<String> getFlags();

	public abstract List<AnimFlag> getAnimFlags();

	public abstract void apply(IdObjectVisitor visitor);

	public abstract float getRenderVisibility(AnimatedRenderEnvironment animatedRenderEnvironment);

	public abstract Vertex getRenderTranslation(AnimatedRenderEnvironment animatedRenderEnvironment);

	public abstract QuaternionRotation getRenderRotation(AnimatedRenderEnvironment animatedRenderEnvironment);

	public abstract Vertex getRenderScale(AnimatedRenderEnvironment animatedRenderEnvironment);

	@Override
	public void remove(final AnimFlag af) {
		getAnimFlags().remove(af);
	}

	public List<IdObject> getChildrenNodes() {
		return childrenNodes;
	}

	private static final Vector4f translationHeap = new Vector4f();
	private static final Matrix4f matrixHeap = new Matrix4f();
	private static final Quaternion rotationHeap = new Quaternion();
	private static final Quaternion rotationDeltaHeap = new Quaternion();
	private static final Vector4f axisAngleHeap = new Vector4f();

	private static final Vector3f IDENTITY = new Vector3f(0, 0, 0);

	public void addOrUpdateTranslationKeyframe(final RenderModel renderModel, final double newDeltaX,
			final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			translationFlag = AnimFlag.createEmpty2018("Translation", InterpolationType.HERMITE,
					timeEnvironmentImpl.getGlobalSeq());
			getAnimFlags().add(translationFlag);
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		final int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart()
				+ animationTime;
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
			Matrix4f.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			translationHeap.x = 0;
			translationHeap.y = 0;
			translationHeap.z = 0;
			translationHeap.w = 1;

			Matrix4f.transform(parentRenderNode.getWorldMatrix(), translationHeap, translationHeap);

			translationHeap.x = (float) (translationHeap.x + newDeltaX);
			translationHeap.y = (float) (translationHeap.y + newDeltaY);
			translationHeap.z = (float) (translationHeap.z + newDeltaZ);
			translationHeap.w = 1;

			Matrix4f.transform(matrixHeap, translationHeap, translationHeap);
		} else {
			translationHeap.x = (float) (newDeltaX);
			translationHeap.y = (float) (newDeltaY);
			translationHeap.z = (float) (newDeltaZ);
			translationHeap.w = 1;
		}

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += translationHeap.x;
			oldTranslationValue.y += translationHeap.y;
			oldTranslationValue.z += translationHeap.z;

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += translationHeap.x;
				oldInTan.y += translationHeap.y;
				oldInTan.z += translationHeap.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += translationHeap.x;
				oldOutTan.y += translationHeap.y;
				oldOutTan.z += translationHeap.z;
			}
		} else {
			final Vector3f localLocation = renderNode.getLocalLocation();
			final int insertIndex = (translationFlag.getTimes().size() == 0
					|| translationFlag.getTimes().get(0) > trackTime) ? 0 : floorIndex + 1;
			translationFlag.getTimes().add(insertIndex, trackTime);
			translationFlag.getValues().add(insertIndex, new Vertex(localLocation.x + translationHeap.x,
					localLocation.y + translationHeap.y, localLocation.z + translationHeap.z));
			if (translationFlag.tans()) {
				translationFlag.getInTans().add(insertIndex,
						new Vertex(translationHeap.x, translationHeap.y, translationHeap.z));
				translationFlag.getOutTans().add(insertIndex,
						new Vertex(translationHeap.x, translationHeap.y, translationHeap.z));
			}
		}

	}

	public AddKeyframeAction createTranslationKeyframe(final RenderModel renderModel, final AnimFlag translationFlag,
			final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			return null;
		} else {
			final Vector3f localLocation = renderNode.getLocalLocation();
			final int insertIndex = (translationFlag.getTimes().size() == 0
					|| translationFlag.getTimes().get(0) > trackTime) ? 0 : floorIndex + 1;
			translationFlag.getTimes().add(insertIndex, trackTime);
			final Vertex keyframeValue = new Vertex(localLocation.x, localLocation.y, localLocation.z);
			translationFlag.getValues().add(insertIndex, keyframeValue);
			if (translationFlag.tans()) {
				final Vertex inTan = new Vertex(localLocation.x, localLocation.y, localLocation.z);
				translationFlag.getInTans().add(insertIndex, inTan);
				final Vertex outTan = new Vertex(localLocation.x, localLocation.y, localLocation.z);
				translationFlag.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, translationFlag, trackTime);
				return new AddKeyframeAction(this, translationFlag, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, translationFlag, trackTime);
				return new AddKeyframeAction(this, translationFlag, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public AddKeyframeAction createRotationKeyframe(final RenderModel renderModel, final AnimFlag rotationTimeline,
			final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if (rotationTimeline.getTimes().size() > 0 && rotationTimeline.getTimes().get(floorIndex) == trackTime) {
			return null;
		} else {
			final Quaternion localRotation = renderNode.getLocalRotation();
			final int insertIndex = (rotationTimeline.getTimes().size() == 0
					|| rotationTimeline.getTimes().get(0) > trackTime) ? 0 : floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);
			final QuaternionRotation keyframeValue = new QuaternionRotation(localRotation.x, localRotation.y,
					localRotation.z, localRotation.w);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
			if (rotationTimeline.tans()) {
				final QuaternionRotation inTan = new QuaternionRotation(localRotation.x, localRotation.y,
						localRotation.z, localRotation.w);
				rotationTimeline.getInTans().add(insertIndex, inTan);
				final QuaternionRotation outTan = new QuaternionRotation(localRotation.x, localRotation.y,
						localRotation.z, localRotation.w);
				rotationTimeline.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public AddKeyframeAction createScalingKeyframe(final RenderModel renderModel, final AnimFlag scalingTimeline,
			final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = scalingTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if (scalingTimeline.getTimes().size() > 0 && scalingTimeline.getTimes().get(floorIndex) == trackTime) {
			return null;
		} else {
			final Vector3f localScale = renderNode.getLocalScale();
			final int insertIndex = (scalingTimeline.getTimes().size() == 0
					|| scalingTimeline.getTimes().get(0) > trackTime) ? 0 : floorIndex + 1;
			scalingTimeline.getTimes().add(insertIndex, trackTime);
			final Vertex keyframeValue = new Vertex(localScale.x, localScale.y, localScale.z);
			scalingTimeline.getValues().add(insertIndex, keyframeValue);
			if (scalingTimeline.tans()) {
				final Vertex inTan = new Vertex(localScale.x, localScale.y, localScale.z);
				scalingTimeline.getInTans().add(insertIndex, inTan);
				final Vertex outTan = new Vertex(localScale.x, localScale.y, localScale.z);
				scalingTimeline.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, scalingTimeline, trackTime);
				return new AddKeyframeAction(this, scalingTimeline, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, scalingTimeline, trackTime);
				return new AddKeyframeAction(this, scalingTimeline, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public void updateTranslationKeyframe(final RenderModel renderModel, final double newDeltaX, final double newDeltaY,
			final double newDeltaZ, final Vector3f savedLocalTranslation) {
		// Note to future author: the reason for saved local translation is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation",
				timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
			Matrix4f.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			translationHeap.x = 0;
			translationHeap.y = 0;
			translationHeap.z = 0;
			translationHeap.w = 1;

			Matrix4f.transform(parentRenderNode.getWorldMatrix(), translationHeap, translationHeap);

			translationHeap.x = (float) (translationHeap.x + newDeltaX);
			translationHeap.y = (float) (translationHeap.y + newDeltaY);
			translationHeap.z = (float) (translationHeap.z + newDeltaZ);
			translationHeap.w = 1;

			Matrix4f.transform(matrixHeap, translationHeap, translationHeap);
		} else {
			translationHeap.x = (float) (newDeltaX);
			translationHeap.y = (float) (newDeltaY);
			translationHeap.z = (float) (newDeltaZ);
			translationHeap.w = 1;
		}

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += translationHeap.x;
			oldTranslationValue.y += translationHeap.y;
			oldTranslationValue.z += translationHeap.z;

			if (savedLocalTranslation != null) {
				savedLocalTranslation.x += translationHeap.x;
				savedLocalTranslation.y += translationHeap.y;
				savedLocalTranslation.z += translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += translationHeap.x;
				oldInTan.y += translationHeap.y;
				oldInTan.z += translationHeap.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += translationHeap.x;
				oldOutTan.y += translationHeap.y;
				oldOutTan.z += translationHeap.z;
			}
		}

	}

	public void updateRotationKeyframe(final RenderModel renderModel, final double centerX, final double centerY,
			final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ,
			final Quaternion savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", timeEnvironmentImpl.getGlobalSeq());
		if (rotationTimeline == null) {
			return;
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);
		final byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(firstXYZ, secondXYZ);
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
			Matrix4f.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			axisAngleHeap.x = 0;
			axisAngleHeap.y = 0;
			axisAngleHeap.z = 0;
			axisAngleHeap.w = 1;

			Matrix4f.transform(parentRenderNode.getWorldMatrix(), axisAngleHeap, axisAngleHeap);

			switch (unusedXYZ) {
			case 0:
				axisAngleHeap.x = axisAngleHeap.x + 1;
				axisAngleHeap.y = axisAngleHeap.y + 0;
				axisAngleHeap.z = axisAngleHeap.z + 0;
				break;
			case 1:
				axisAngleHeap.x = axisAngleHeap.x + 0;
				axisAngleHeap.y = axisAngleHeap.y + -1;
				axisAngleHeap.z = axisAngleHeap.z + 0;
				break;
			case 2:
				axisAngleHeap.x = axisAngleHeap.x + 0;
				axisAngleHeap.y = axisAngleHeap.y + 0;
				axisAngleHeap.z = axisAngleHeap.z + -1;
				break;
			}
			axisAngleHeap.w = 1;

			Matrix4f.transform(matrixHeap, axisAngleHeap, axisAngleHeap);
		} else {
			switch (unusedXYZ) {
			case 0:
				axisAngleHeap.x = 1;
				axisAngleHeap.y = 0;
				axisAngleHeap.z = 0;
				break;
			case 1:
				axisAngleHeap.x = 0;
				axisAngleHeap.y = -1;
				axisAngleHeap.z = 0;
				break;
			case 2:
				axisAngleHeap.x = 0;
				axisAngleHeap.y = 0;
				axisAngleHeap.z = -1;
				break;
			}
		}
		axisAngleHeap.w = (float) radians;
		rotationDeltaHeap.setFromAxisAngle(axisAngleHeap);

		if (rotationTimeline.getTimes().size() > 0 && rotationTimeline.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (savedLocalRotation != null) {
				Quaternion.mul(savedLocalRotation, rotationDeltaHeap, savedLocalRotation);
			}

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vector3f savedLocalScaling) {
		// Note to future author: the reason for saved local scaling is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Scaling", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);
		// final RenderNode renderNode = renderModel.getRenderNode(this);
		// if (parent != null) {
		// final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
		// translationHeap.x = (float)scaleX * parentRenderNode.getInverseWorldScale().x;
		// translationHeap.y = (float)scaleY * parentRenderNode.getInverseWorldScale().y;
		// translationHeap.z = (float)scaleZ * parentRenderNode.getInverseWorldScale().z;
		// translationHeap.w = 1;
		// } else {
		translationHeap.x = (float) scaleX;
		translationHeap.y = (float) scaleY;
		translationHeap.z = (float) scaleZ;
		// translationHeap.w = 1;
		// }

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= translationHeap.x;
			oldTranslationValue.y *= translationHeap.y;
			oldTranslationValue.z *= translationHeap.z;

			if (savedLocalScaling != null) {
				savedLocalScaling.x *= translationHeap.x;
				savedLocalScaling.y *= translationHeap.y;
				savedLocalScaling.z *= translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= translationHeap.x;
				oldInTan.y *= translationHeap.y;
				oldInTan.z *= translationHeap.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= translationHeap.x;
				oldOutTan.y *= translationHeap.y;
				oldOutTan.z *= translationHeap.z;
			}
		}
	}

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Quaternion localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if (rotationTimeline.getTimes().size() > 0 && rotationTimeline.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			Quaternion.mul(localRotation, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				Quaternion.mul(localRotation, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				Quaternion.mul(localRotation, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq,
			final Quaternion localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if (rotationTimeline.getTimes().size() > 0 && rotationTimeline.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			rotationDeltaHeap.setIdentity();
			Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
			Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				rotationDeltaHeap.setIdentity();
				Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				rotationDeltaHeap.setIdentity();
				Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += newDeltaX;
			oldTranslationValue.y += newDeltaY;
			oldTranslationValue.z += newDeltaZ;

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += newDeltaX;
				oldInTan.y += newDeltaY;
				oldInTan.z += newDeltaZ;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += newDeltaX;
				oldOutTan.y += newDeltaY;
				oldOutTan.z += newDeltaZ;
			}
		}

	}

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Vector3f localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if (translationFlag.getTimes().size() > 0 && translationFlag.getTimes().get(floorIndex) == trackTime) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= localScaling.x;
			oldTranslationValue.y *= localScaling.y;
			oldTranslationValue.z *= localScaling.z;

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= localScaling.x;
				oldInTan.y *= localScaling.y;
				oldInTan.z *= localScaling.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= localScaling.x;
				oldOutTan.y *= localScaling.y;
				oldOutTan.z *= localScaling.z;
			}
		}

	}
}
