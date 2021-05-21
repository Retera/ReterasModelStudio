package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;

public class TranslationKeyframeAction implements GenericMoveAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vec3> nodeToLocalTranslation;
	private final Integer trackGlobalSeq;
	public ModelView modelView;

	public TranslationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 int trackTime,
	                                 Integer trackGlobalSeq,
	                                 Collection<IdObject> nodeSelection,
	                                 ModelView modelView) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelView = modelView;
		nodeToLocalTranslation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalTranslation.put(node, new Vec3());
		}
	}

	@Override
	public void undo() {

		for (IdObject node : nodeToLocalTranslation.keySet()) {
			Vec3 localTranslation = Vec3.getScaled(nodeToLocalTranslation.get(node), -1);
			updateLocalTranslationKeyframe(node, trackTime, trackGlobalSeq, localTranslation);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalTranslation.keySet()) {
			Vec3 localTranslation = nodeToLocalTranslation.get(node);
			updateLocalTranslationKeyframe(node, trackTime, trackGlobalSeq, localTranslation);
		}
	}

	@Override
	public String actionName() {
		return "edit translation";
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		Vec3 delta = new Vec3(deltaX, deltaY, deltaZ);
		for (IdObject idObject : nodeToLocalTranslation.keySet()) {
			updateTranslationKeyframe(idObject, modelView.getEditorRenderModel(), delta, nodeToLocalTranslation.get(idObject));
		}
	}

	@Override
	public void updateTranslation(Vec3 delta1) {
		Vec3 delta = new Vec3(delta1);
		for (IdObject idObject : nodeToLocalTranslation.keySet()) {
			updateTranslationKeyframe(idObject, modelView.getEditorRenderModel(), delta, nodeToLocalTranslation.get(idObject));
		}
	}

	public void updateLocalTranslationKeyframe(AnimatedNode animatedNode, int trackTime, Integer trackGlobalSeq, Vec3 localTranslation) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find("Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).add(localTranslation);

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).add(localTranslation);
				translationFlag.getOutTans().get(floorIndex).add(localTranslation);
			}
		}
	}

	public void updateTranslationKeyframe(AnimatedNode animatedNode,
	                                      RenderModel renderModel,
	                                      Vec3 newDelta,
	                                      Vec3 savedLocalTranslation) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must  make AnimFlag.find seek on globalSeqId

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) animatedNode.find("Translation", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		int floorIndex = translationFlag.floorIndex(trackTime);
		//final RenderNode renderNode = renderModel.getRenderNode(this);
		AnimatedNode parent = null;// = getParent();
		if (animatedNode instanceof IdObject) {
			parent = ((IdObject) animatedNode).getParent();
		}

		Vec4 translationHeap = new Vec4(0, 0, 0, 1);
		if (parent != null) {
			RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			translationHeap.transform(parentRenderNode.getWorldMatrix());
			translationHeap.add(new Vec4(newDelta, 0));
			translationHeap.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			translationHeap.set(newDelta, 1);
		}

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).add(translationHeap.getVec3());

			if (savedLocalTranslation != null) {
				savedLocalTranslation.add(translationHeap.getVec3());
			}

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).add(translationHeap.getVec3());
				translationFlag.getOutTans().get(floorIndex).add(translationHeap.getVec3());
			}
		}

	}

}
