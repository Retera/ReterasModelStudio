package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;

public class ScalingKeyframeAction implements GenericScaleAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vec3> nodeToLocalScale;
	private final Vec3 center;
	private final GlobalSeq trackGlobalSeq;
	private final Sequence anim;
	private final RenderModel editorRenderModel;

	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                             Collection<IdObject> nodeSelection,
	                             Vec3 center, RenderModel editorRenderModel) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.trackGlobalSeq = editorRenderModel.getTimeEnvironment().getGlobalSeq();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		nodeToLocalScale = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalScale.put(node, new Vec3());
		}
		this.center = new Vec3(center);
	}

	@Override
	public UndoAction undo() {
		for (IdObject node : nodeToLocalScale.keySet()) {
			Vec3 localScale = nodeToLocalScale.get(node);
			Vec3 tempInverse = new Vec3(1, 1, 1).divide(localScale);
			updateLocalScalingKeyframe(node, trackTime, trackGlobalSeq, tempInverse);
		}
		addingTimelinesOrKeyframesAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalScale.keySet()) {
			Vec3 localScale = nodeToLocalScale.get(node);
			updateLocalScalingKeyframe(node, trackTime, trackGlobalSeq, localScale);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "edit rotation";
	}

	@Override
	public GenericScaleAction updateScale(Vec3 scale) {
//		modelEditor.rawScale(center, scale, nodeToLocalScale);
		for (IdObject idObject : nodeToLocalScale.keySet()) {
			updateScalingKeyframe(idObject, editorRenderModel, scale, nodeToLocalScale.get(idObject));
		}
		return this;
	}

	public void updateScalingKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3 scale, Vec3 savedLocalScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_SCALING, timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}

		int trackTime = renderModel.getTimeEnvironment().getEnvTrackTime();

		if (translationFlag.hasEntryAt(anim, trackTime)) {
			Entry<Vec3> entry = translationFlag.getEntryAt(anim, trackTime);
			entry.getValue().multiply(scale);

			if (savedLocalScaling != null) {
				savedLocalScaling.multiply(scale);
			}

			if (translationFlag.tans()) {
				entry.getInTan().multiply(scale);
				entry.getOutTan().multiply(scale);
			}
		}
	}

	public void updateLocalScalingKeyframe(AnimatedNode animatedNode, int trackTime, GlobalSeq trackGlobalSeq, Vec3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_SCALING, trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}

		if (translationFlag.hasEntryAt(anim, trackTime)) {
			Entry<Vec3> entry = translationFlag.getEntryAt(anim, trackTime);
			entry.getValue().multiply(localScaling);
			if (translationFlag.tans()) {
				entry.getInTan().multiply(localScaling);
				entry.getOutTan().multiply(localScaling);
			}
		}
	}
}
