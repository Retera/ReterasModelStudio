package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
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

	public AddKeyframeAction2(ModelStructureChangeListener structureChangeListener, ModelHandler modelHandler, ModelEditorActionType3 actionType) {
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
					transformationTimeline = QuatAnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					transformationTimeline = Vec3AnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
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

	public AddKeyframeAction createTranslationKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3AnimFlag translationFlag,
	                                                          ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = translationFlag.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localLocation = renderNode.getLocalLocation();
			return getAddKeyframeAction(animatedNode, translationFlag, changeListener, trackTime, floorIndex, localLocation);
		}
	}

	public AddKeyframeAction createRotationKeyframe(AnimatedNode animatedNode, RenderModel renderModel, QuatAnimFlag rotationTimeline,
	                                                       ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = rotationTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Quat localRotation = renderNode.getLocalRotation();
			int insertIndex = floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);

			Quat keyframeValue = new Quat(localRotation);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
			if (rotationTimeline.interpolationType.tangential()) {
				Quat inTan = new Quat(localRotation);
				rotationTimeline.getInTans().add(insertIndex, inTan);

				Quat outTan = new Quat(localRotation);
				rotationTimeline.getOutTans().add(insertIndex, outTan);

				changeListener.keyframeAdded(animatedNode, rotationTimeline, trackTime);
				return new AddKeyframeAction(animatedNode, rotationTimeline, trackTime, keyframeValue, inTan, outTan, changeListener);
			} else {
				changeListener.keyframeAdded(animatedNode, rotationTimeline, trackTime);
				return new AddKeyframeAction(animatedNode, rotationTimeline, trackTime, keyframeValue, changeListener);
			}
		}
	}

	public AddKeyframeAction createScalingKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3AnimFlag scalingTimeline,
	                                                      ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = scalingTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(animatedNode, scalingTimeline, changeListener, trackTime, floorIndex, localScale);
		}
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

	private AddKeyframeAction getAddKeyframeAction(AnimatedNode animatedNode, Vec3AnimFlag timeline, ModelStructureChangeListener changeListener,
	                                                      int trackTime, int floorIndex, Vec3 vec3) {
		int insertIndex = floorIndex + 1;
		timeline.getTimes().add(insertIndex, trackTime);

		Vec3 keyframeValue = new Vec3(vec3);
		timeline.getValues().add(insertIndex, keyframeValue);
//		if (timeline.tans()) {
		if (timeline.interpolationType.tangential()) {
			Vec3 inTan = new Vec3(vec3);
			timeline.getInTans().add(insertIndex, inTan);

			Vec3 outTan = new Vec3(vec3);
			timeline.getOutTans().add(insertIndex, outTan);

			changeListener.keyframeAdded(animatedNode, timeline, trackTime);
			return new AddKeyframeAction(animatedNode, timeline, trackTime, keyframeValue, inTan, outTan, changeListener);
		} else {
			changeListener.keyframeAdded(animatedNode, timeline, trackTime);
			return new AddKeyframeAction(animatedNode, timeline, trackTime, keyframeValue, changeListener);
		}
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
