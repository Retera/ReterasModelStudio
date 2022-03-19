package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;

public class RotationKeyframeAction implements GenericRotateAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Quat> nodeToLocalRotation;
	private final Vec3 center;
	private final Vec3 axis;
	private final Sequence anim;
	private final RenderModel editorRenderModel;

	// ToDo should maybe consider center and make a rotation around a point; ie do a combined rotation and translation
	//  maybe also have an option for individual origins

	public RotationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              RenderModel editorRenderModel,
	                              Vec3 center,
	                              Vec3 axis) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();

		nodeToLocalRotation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalRotation.put(node, new Quat());
		}
		this.center = new Vec3(center);
		this.axis = axis;
	}
	public RotationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              RenderModel editorRenderModel,
	                              Vec3 center,
	                              Vec3 axis, double radians) {
		this(addingTimelinesOrKeyframesAction, nodeSelection, editorRenderModel, center, axis);
		Quat rotation = new Quat();
		for (IdObject idObject : nodeToLocalRotation.keySet()) {
			if (nodeToLocalRotation.get(idObject) != null) {
				setRotation((float) radians, idObject, rotation);
				nodeToLocalRotation.get(idObject).mul(rotation);
			}
		}
	}

	public RotationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              RenderModel editorRenderModel,
	                              Vec3 center,
	                              byte dim1, byte dim2) {
		this(addingTimelinesOrKeyframesAction, nodeSelection, editorRenderModel, center, getUnusedAxis(getUnusedXYZ(dim1, dim2)));
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
		return "edit rotation";
	}

	public GenericRotateAction doSetup(){
		addingTimelinesOrKeyframesAction.redo();
		return this;
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		// Note to future author: the reason for saved local rotation is that
		// the input rotation took place in world space and got converted into local space
		Quat rotation = new Quat();
		for (IdObject idObject : nodeToLocalRotation.keySet()) {
			if (nodeToLocalRotation.get(idObject) != null) {
				setRotation((float) radians, idObject, rotation);
				nodeToLocalRotation.get(idObject).mul(rotation);
				updateLocalRotationKeyframe(idObject, rotation);
			}
		}
		return this;
	}

	public void updateLocalRotationKeyframe(AnimatedNode animatedNode, Quat localRotation) {
		QuatAnimFlag animFlag = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION);
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animFlag.hasEntryAt(anim, trackTime)) {
			Entry<Quat> entry = animFlag.getEntryAt(anim, trackTime);
			entry.getValue().mul(localRotation);

			if (animFlag.tans()) {
				entry.getInTan().mul(localRotation);
				entry.getOutTan().mul(localRotation);
			}
		}
	}

	private Quat setRotation(float radians, IdObject idObject, Quat rotation) {
		rotation.set(0, 0, 0, 1);

		RenderNode2 renderNode = editorRenderModel.getRenderNode(idObject);
		Mat4 worldMatrix = renderNode.getWorldMatrix();

		rotation.transform(worldMatrix);
		rotation.add(axis);
		rotation.transformInverted(worldMatrix);

		System.out.println("rotated axis: " + rotation + " ( axis: " + axis + ")");
		rotation.w = radians;
		rotation.setFromAxisAngle(rotation).normalize();

		System.out.println(rotation + ", angle: " + radians);
		return rotation;
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

	static Vec3 getUnusedAxis(byte unusedXYZ) {
		return switch (unusedXYZ) {
			case 0 -> new Vec3(1, 0, 0);
			case 1 -> new Vec3(0, -1, 0);
			default -> new Vec3(0, 0, -1);
		};
	}

	static Vec3 getUnusedAxis(byte portFirstXYZ, byte portSecondXYZ) {
		byte unusedXYZ = getUnusedXYZ(portFirstXYZ, portSecondXYZ);
		return switch (unusedXYZ) {
			case 0 -> new Vec3(1, 0, 0);
			case 1 -> new Vec3(0, -1, 0);
			default -> new Vec3(0, 0, -1);
		};
	}

}
