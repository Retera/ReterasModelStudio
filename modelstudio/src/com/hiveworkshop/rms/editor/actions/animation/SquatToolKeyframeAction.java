package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SquatToolKeyframeAction implements GenericRotateAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Quat> nodeToLocalRotation;
	private final Vec3 center;
	private final Vec3 axis;
	private final GlobalSeq trackGlobalSeq;
	private final Sequence anim;
	private final RenderModel editorRenderModel;
	private final Collection<IdObject> allIdObjects;

	public SquatToolKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
	                               RenderModel editorRenderModel,
	                               Collection<IdObject> allIdObjects,
	                               Vec3 center,
	                               Vec3 axis) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.trackGlobalSeq = editorRenderModel.getTimeEnvironment().getGlobalSeq();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();

		this.editorRenderModel = editorRenderModel;
		this.allIdObjects = allIdObjects;
		this.axis = axis;
		nodeToLocalRotation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalRotation.put(node, new Quat());
		}
		this.center = new Vec3(center);
	}

	public SquatToolKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
	                               RenderModel editorRenderModel,
	                               List<IdObject> allIdObjects,
	                               Vec3 center,
	                               byte dim1, byte dim2) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.trackGlobalSeq = editorRenderModel.getTimeEnvironment().getGlobalSeq();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		this.allIdObjects = allIdObjects;
		nodeToLocalRotation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalRotation.put(node, new Quat());
		}
		this.center = new Vec3(center);
		this.axis = getUnusedAxis(getUnusedXYZ(dim1, dim2));
	}

	public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}

	@Override
	public UndoAction undo() {
		Quat localRotation = new Quat();
		for (IdObject node : nodeToLocalRotation.keySet()) {
			localRotation.set(nodeToLocalRotation.get(node)).invertRotation();
			updateLocalRotationKeyframe(node, localRotation);
		}
		addingTimelinesOrKeyframesAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalRotation.keySet()) {
			Quat localRotation = nodeToLocalRotation.get(node);
			updateLocalRotationKeyframe(node, localRotation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "edit rotation w/ squat";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		for (IdObject idObject : nodeToLocalRotation.keySet()) {
			updateRotationKeyframe(idObject, radians, nodeToLocalRotation.get(idObject));
		}
		//todo check if this should be here....
		for (IdObject idObject : allIdObjects) {
			if (nodeToLocalRotation.containsKey(idObject.getParent())
					&& (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class))
					|| ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				updateRotationKeyframe(idObject, -radians, nodeToLocalRotation.get(idObject));
			}
		}
		return this;
	}

	public void updateLocalRotationKeyframe(AnimatedNode animatedNode, Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION, trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		Sequence anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		if (rotationTimeline.hasEntryAt(anim, trackTime)) {
			Entry<Quat> entry = rotationTimeline.getEntryAt(anim, trackTime);
			entry.getValue().mul(localRotation);
			if (rotationTimeline.tans()) {
				entry.getInTan().mul(localRotation);
				entry.getOutTan().mul(localRotation);
			}
		}
	}

	public void updateRotationKeyframe(AnimatedNode animatedNode, double radians, Quat savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be  constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION, trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}

		AnimatedNode parent = null;// = getParent();
		if (animatedNode instanceof IdObject) {
			parent = ((IdObject) animatedNode).getParent();
		}

		Vec4 rotationAxis = new Vec4(0, 0, 0, 1);

		if (parent != null) {
			RenderNode parentRenderNode = editorRenderModel.getRenderNode(parent);

			rotationAxis.transform(parentRenderNode.getWorldMatrix());
			rotationAxis.add(axis);
			rotationAxis.transformInverted(parentRenderNode.getWorldMatrix());
		} else {
			rotationAxis.add(axis);
		}
		rotationAxis.w = (float) radians;
		Quat rotation = new Quat().setFromAxisAngle(rotationAxis);


		if (rotationTimeline.hasEntryAt(anim, trackTime)) {
			Entry<Quat> entry = rotationTimeline.getEntryAt(anim, trackTime);
			entry.getValue().mulLeft(rotation);

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotation);
			}

			if (rotationTimeline.tans()) {
				entry.getInTan().mul(rotation);
				entry.getOutTan().mul(rotation);
			}
		}
	}

	Vec3 getUnusedAxis(byte unusedXYZ) {
		return switch (unusedXYZ) {
			case 0 -> new Vec3(1, 0, 0);
			case 1 -> new Vec3(0, -1, 0);
			default -> new Vec3(0, 0, -1);
		};
	}

}
