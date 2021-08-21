package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddKeyframeAction3 implements UndoAction {
	private final ModelView modelView;
	private final ModelHandler modelHandler;
	private final int trackTime;
	private final TimeEnvironmentImpl timeEnvironmentImpl;
	private final ModelEditorActionType3 actionType;

	private final UndoAction createKeyframeCompoundAction;

	public AddKeyframeAction3(ModelHandler modelHandler,
	                          ModelEditorActionType3 actionType) {
		this.actionType = actionType;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();

		timeEnvironmentImpl = modelHandler.getEditTimeEnv();
		trackTime = getTrackTime(modelHandler.getRenderModel());

		//	private final AnimFlag timeline;

		createKeyframeCompoundAction = createKeyframe();
	}

	public UndoAction createKeyframe() {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		for (IdObject node : selection) {
			UndoAction keyframeAction = switch (actionType){
				case ROTATION, SQUAT -> createRotationKeyframe(node, globalSeq, actions);
				case SCALING -> createScalingKeyframe(node, globalSeq, actions);
				case TRANSLATION, EXTEND, EXTRUDE -> createTranslationKeyframe(node, globalSeq, actions);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	public UndoAction createTranslationKeyframe(IdObject node, Integer globalSeq, List<UndoAction> actions) {
		AnimFlag<Vec3> timeline = node.getTranslationFlag(globalSeq);
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction(node, timeline));
		}
		RenderNode renderNode = modelHandler.getRenderModel().getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation())));
	}

	public UndoAction createScalingKeyframe(IdObject node, Integer globalSeq, List<UndoAction> actions) {
		AnimFlag<Vec3> timeline = node.getScalingFlag(globalSeq);
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction(node, timeline));
		}
		RenderNode renderNode = modelHandler.getRenderModel().getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalScale())));
	}

	public UndoAction createRotationKeyframe(IdObject node, Integer globalSeq, List<UndoAction> actions) {
		AnimFlag<Quat> timeline = node.getRotationFlag(globalSeq);
		if (timeline == null) {
			timeline = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction(node, timeline));
		}
		RenderNode renderNode = modelHandler.getRenderModel().getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Quat(renderNode.getLocalRotation())));
	}


//	private String getKeyframeMdlTypeName() {
//		return switch (actionType) {
//			case ROTATION, SQUAT -> MdlUtils.TOKEN_ROTATION;
//			case SCALING -> MdlUtils.TOKEN_SCALING;
//			case TRANSLATION, EXTEND, EXTRUDE -> MdlUtils.TOKEN_TRANSLATION;
//		};
//	}

	private int getTrackTime(RenderModel renderModel) {
		int trackTime = renderModel.getTimeEnvironment().getAnimationTime();

		Integer globalSeq = renderModel.getTimeEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getTimeEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private <T>AddKeyframeAction_T<T> getAddKeyframeAction(AnimFlag<T> timeline, Entry<T> entry) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		if (timeline.tans()) {
			Entry<T> entryIn = timeline.getFloorEntry(trackTime, timeEnvironmentImpl);
			Entry<T> entryOut = timeline.getCeilEntry(trackTime, timeEnvironmentImpl);
			int animationLength = timeEnvironmentImpl.getCurrentAnimation().length();
//				float factor = getTimeFactor(trackTime, animationLength, entryIn.time, entryOut.time);
			float[] tbcFactor = timeline.getTbcFactor(0, 0.5f, 0);
			timeline.calcNewTans(tbcFactor, entryOut, entryIn, entry, animationLength);
			System.out.println("calc tans! " + entryIn + entryOut + entry);

			AddKeyframeAction_T<T> addKeyframeAction = new AddKeyframeAction_T<>(timeline, entry);
			addKeyframeAction.redo();
			return addKeyframeAction;
		}
		if (!timeline.hasEntryAt(trackTime)) {
			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			AddKeyframeAction_T<T> addKeyframeAction = new AddKeyframeAction_T<>(timeline, entry);
			addKeyframeAction.redo();
			return addKeyframeAction;
		}
		return null;
	}
//	private <T>AddKeyframeAction_T<T> getAddKeyframeAction(AnimFlag<T> timeline, Entry<T> entry) {
//		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
//		if (!timeline.hasEntryAt(trackTime)) {
//			if (timeline.getInterpolationType().tangential()) {
//				entry.unLinearize();
//			}
//
//			AddKeyframeAction_T<T> addKeyframeAction = new AddKeyframeAction_T<>(timeline, entry);
//			addKeyframeAction.redo();
//			return addKeyframeAction;
//		}
//		return null;
//	}


	@Override
	public UndoAction undo() {
		createKeyframeCompoundAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		createKeyframeCompoundAction.redo();
		return this;
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
