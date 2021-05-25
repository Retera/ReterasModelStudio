package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddKeyframeAction2 implements UndoAction {
	//	private final AnimFlag timeline;
	private final ModelStructureChangeListener structureChangeListener;
	private final ModelView modelView;
	private final ModelHandler modelHandler;
	private final ModelEditorActionType3 actionType;

	private final UndoAction createKeyframeCompoundAction;

	public AddKeyframeAction2(ModelStructureChangeListener structureChangeListener,
	                          ModelHandler modelHandler,
	                          ModelEditorActionType3 actionType) {
		this.actionType = actionType;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();

		this.structureChangeListener = structureChangeListener;

		createKeyframeCompoundAction = createKeyframe();
	}

	public UndoAction createKeyframe() {
		String keyframeMdlTypeName = switch (actionType) {
			case ROTATION, SQUAT -> "Rotation";
			case SCALING -> "Scaling";
			case TRANSLATION, EXTEND, EXTRUDE -> "Translation";
		};

		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = modelHandler.getEditTimeEnv();
		for (IdObject node : selection) {
			AnimFlag<?> transformationTimeline = node.find(keyframeMdlTypeName, timeEnvironmentImpl.getGlobalSeq());

			if (transformationTimeline == null) {
				if (keyframeMdlTypeName.equals("Rotation")) {
					transformationTimeline = new QuatAnimFlag(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					transformationTimeline = new Vec3AnimFlag(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				}
				node.add(transformationTimeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, transformationTimeline, structureChangeListener);

				actions.add(addTimelineAction);
			}
			AddKeyframeAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> createRotationKeyframe(node, modelHandler.getRenderModel(), (QuatAnimFlag) transformationTimeline, structureChangeListener);
				case SCALING -> createScalingKeyframe(node, modelHandler.getRenderModel(), (Vec3AnimFlag) transformationTimeline, structureChangeListener);
				case TRANSLATION, EXTEND, EXTRUDE -> createTranslationKeyframe(node, modelHandler.getRenderModel(), (Vec3AnimFlag) transformationTimeline, structureChangeListener);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	public AddKeyframeAction createTranslationKeyframe(AnimatedNode animatedNode,
	                                                   RenderModel renderModel,
	                                                   Vec3AnimFlag translationFlag,
	                                                   ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);
		if (translationFlag.hasEntryAt(trackTime)) {
			Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(animatedNode, translationFlag, changeListener, trackTime, localScale);
		}
		return null;
	}

	public AddKeyframeAction createRotationKeyframe(AnimatedNode animatedNode,
	                                                RenderModel renderModel,
	                                                QuatAnimFlag rotationTimeline,
	                                                ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);
		if (rotationTimeline.hasEntryAt(trackTime)) {
			Quat localRotation = renderNode.getLocalRotation();
			return getAddKeyframeAction(animatedNode, rotationTimeline, changeListener, trackTime, localRotation);
		}
		return null;
	}

	private AddKeyframeAction getAddKeyframeAction(AnimatedNode animatedNode,
	                                               QuatAnimFlag rotationTimeline,
	                                               ModelStructureChangeListener changeListener,
	                                               int trackTime,
	                                               Quat localRotation) {
		Entry<Quat> entry = new Entry<>(trackTime, localRotation);

		if (rotationTimeline.getInterpolationType().tangential()) {
			entry.unLinearize();
		}

		changeListener.keyframeAdded(animatedNode, rotationTimeline, trackTime);
		AddKeyframeAction addKeyframeAction = new AddKeyframeAction(animatedNode, rotationTimeline, entry, changeListener);
		addKeyframeAction.redo();
		return addKeyframeAction;
	}

	public AddKeyframeAction createScalingKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3AnimFlag scalingTimeline,
	                                                      ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);
		if (scalingTimeline.hasEntryAt(trackTime)) {
			Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(animatedNode, scalingTimeline, changeListener, trackTime, localScale);
		}
		return null;
	}

	private int getTrackTime(RenderModel renderModel) {
		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = animationTime;

		Integer globalSeq = renderModel.getAnimatedRenderEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private AddKeyframeAction getAddKeyframeAction(AnimatedNode animatedNode,
	                                               Vec3AnimFlag timeline,
	                                               ModelStructureChangeListener changeListener,
	                                               int trackTime, Vec3 vec3) {
		Entry<Vec3> entry = new Entry<>(trackTime, vec3);

		if (timeline.getInterpolationType().tangential()) {
			entry.unLinearize();
		}

		changeListener.keyframeAdded(animatedNode, timeline, trackTime);
		AddKeyframeAction addKeyframeAction = new AddKeyframeAction(animatedNode, timeline, entry, changeListener);
		addKeyframeAction.redo();
		return addKeyframeAction;
	}



	@Override
	public void undo() {
		createKeyframeCompoundAction.undo();
//		structureChangeListener.keyframeRemoved(node, timeline, entry.time);
	}

	@Override
	public void redo() {
		createKeyframeCompoundAction.redo();
//		structureChangeListener.keyframeAdded(node, timeline, entry.time);
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
