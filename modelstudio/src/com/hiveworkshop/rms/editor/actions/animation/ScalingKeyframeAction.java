package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ScalingKeyframeAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final List<IdObject> nodeList;
	private final Vec3 scale;
	private final Sequence anim;
	private final HashMap<AnimatedNode, Entry<Vec3>> nodeToOrgScale;
	private final RenderModel editorRenderModel;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                             Collection<IdObject> nodeSelection,
	                             Vec3 center, Vec3 scale, RenderModel editorRenderModel, Mat4 rotMat) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		nodeList = new ArrayList<>(nodeSelection);
		this.editorRenderModel = editorRenderModel;

		this.scale = new Vec3(scale);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		nodeToOrgScale = new HashMap<>();
		for (AnimatedNode node : nodeSelection) {
			Vec3AnimFlag animFlag = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_SCALING);
			if (animFlag != null && animFlag.hasEntryAt(anim, trackTime) && (anim instanceof GlobalSeq || animFlag.getGlobalSeq() != anim)) {
				nodeToOrgScale.put(node, animFlag.getEntryAt(anim, trackTime).deepCopy());
			}
		}
	}

	@Override
	public ScalingKeyframeAction undo() {
		for (AnimatedNode node : nodeToOrgScale.keySet()) {
			Entry<Vec3> orgScale = nodeToOrgScale.get(node);
			if (orgScale != null) {
				Vec3AnimFlag animFlag = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_SCALING);
				animFlag.getEntryAt(anim, orgScale.getTime()).setValues(orgScale);
			}
		}
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.undo();
		}
		return this;
	}

	@Override
	public ScalingKeyframeAction redo() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scale);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Scaling";
	}

	public ScalingKeyframeAction doSetup() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		return this;
	}

	@Override
	public ScalingKeyframeAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scale);
		}
		return this;
	}
	@Override
	public ScalingKeyframeAction setScale(Vec3 scale) {
		scaleDiff.set(scale).divide(this.scale);
		this.scale.set(scale);
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scaleDiff);
		}
		return this;
	}

	Vec3 scaleDiff = new Vec3();
	public void updateScalingKeyframe(AnimatedNode animatedNode, int trackTime, Vec3 scale) {
		Vec3AnimFlag animFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_SCALING);
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animFlag.hasEntryAt(anim, trackTime)) {
			Entry<Vec3> entry = animFlag.getEntryAt(anim, trackTime);
			entry.getValue().multiply(scale);

			if (animFlag.tans()) {
				entry.getInTan().multiply(scale);
				entry.getOutTan().multiply(scale);
			}
		}
	}
}
