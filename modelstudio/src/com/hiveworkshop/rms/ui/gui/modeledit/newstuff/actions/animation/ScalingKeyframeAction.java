package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class ScalingKeyframeAction implements GenericScaleAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vec3> nodeToLocalScale;
	private final Vec3 center;
	private final Integer trackGlobalSeq;
	ModelView modelView;

	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                             int trackTime,
	                             Integer trackGlobalSeq,
	                             Collection<IdObject> nodeSelection,
	                             Vec3 center, ModelView modelView) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelView = modelView;
		nodeToLocalScale = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalScale.put(node, new Vec3());
		}
		this.center = new Vec3(center);
	}
	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                             int trackTime,
	                             Integer trackGlobalSeq,
	                             Collection<IdObject> nodeSelection,
	                             Vec3 center, ModelView modelView, int i) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelView = modelView;
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();
		RenderModel renderModel = modelView.getEditorRenderModel();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

//		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> createScalingKeyframe(node, renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


//		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());


		nodeToLocalScale = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalScale.put(node, new Vec3());
		}
		this.center = new Vec3(center);
	}

	@Override
	public void undo() {
		for (IdObject node : nodeToLocalScale.keySet()) {
			Vec3 localTranslation = nodeToLocalScale.get(node);
			Vec3 tempInverse = new Vec3(1, 1, 1).divide(localTranslation);
			updateLocalScalingKeyframe(node, trackTime, trackGlobalSeq, tempInverse);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalScale.keySet()) {
			Vec3 localTranslation = nodeToLocalScale.get(node);
			updateLocalScalingKeyframe(node, trackTime, trackGlobalSeq, localTranslation);
		}
	}

	@Override
	public String actionName() {
		return "edit rotation";
	}

	@Override
	public void updateScale(Vec3 scale) {
//		modelEditor.rawScale(center, scale, nodeToLocalScale);
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			updateScalingKeyframe(idObject, modelView.getEditorRenderModel(), scale, nodeToLocalScale.get(idObject));
		}
	}

	public void updateScalingKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3 scale, Vec3 savedLocalScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find("Scaling", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}

		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = animationTime;

		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		if (translationFlag.hasEntryAt(trackTime)) {
			Entry<Vec3> entry = translationFlag.getEntryAt(trackTime);
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

	public void updateLocalScalingKeyframe(AnimatedNode animatedNode, int trackTime, Integer trackGlobalSeq, Vec3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		if (translationFlag.hasEntryAt(trackTime)) {
			Entry<Vec3> entry = translationFlag.getEntryAt(trackTime);
			entry.getValue().multiply(localScaling);
			if (translationFlag.tans()) {
				entry.getInTan().multiply(localScaling);
				entry.getOutTan().multiply(localScaling);
			}
		}
	}
}
