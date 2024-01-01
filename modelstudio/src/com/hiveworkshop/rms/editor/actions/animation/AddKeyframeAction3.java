package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddKeyframeAction3 implements UndoAction {
	private final RenderModel renderModel ;
	private final int trackTime;
	private final TimeEnvironmentImpl timeEnvironmentImpl;
	private final ModelEditorActionType3 actionType;

	private final UndoAction createKeyframeCompoundAction;

	public AddKeyframeAction3(Collection<IdObject> selection, RenderModel renderModel,
	                          ModelEditorActionType3 actionType) {
		this.actionType = actionType;
		this.renderModel = renderModel;

		timeEnvironmentImpl = renderModel.getTimeEnvironment();
		trackTime = timeEnvironmentImpl.getEnvTrackTime();

		createKeyframeCompoundAction = createKeyframe(selection);
	}

	public UndoAction createKeyframe(Collection<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();

		GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
		for (IdObject node : selection) {
			UndoAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> createRotationKeyframe(node, globalSeq, actions);
				case SCALING -> createScalingKeyframe(node, globalSeq, actions);
				case TRANSLATION, EXTEND, EXTRUDE -> createTranslationKeyframe(node, globalSeq, actions);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
//				actions.add(keyframeAction.redo());
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	public UndoAction createTranslationKeyframe(IdObject node, GlobalSeq globalSeq, List<UndoAction> actions) {
		AnimFlag<Vec3> timeline = node.getTranslationFlag(globalSeq);
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation())));
	}

	public UndoAction createScalingKeyframe(IdObject node, GlobalSeq globalSeq, List<UndoAction> actions) {
		AnimFlag<Vec3> timeline = node.getScalingFlag(globalSeq);
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalScale())));
	}

	public UndoAction createRotationKeyframe(IdObject node, GlobalSeq globalSeq, List<UndoAction> actions) {
		AnimFlag<Quat> timeline = node.getRotationFlag(globalSeq);
		if (timeline == null) {
			timeline = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Quat(renderNode.getLocalRotation())));
	}

	private <T> UndoAction getAddKeyframeAction(AnimFlag<T> timeline, Entry<T> entry) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		if (timeline.tans()) {
			Entry<T> entryIn = timeline.getFloorEntry(trackTime, timeEnvironmentImpl.getCurrentSequence());
			Entry<T> entryOut = timeline.getCeilEntry(trackTime, timeEnvironmentImpl.getCurrentSequence());
			int animationLength = timeEnvironmentImpl.getCurrentSequence().getLength();

			float[] tcbFactor = timeline.getTcbFactor(0, 0, 0.5f);
			timeline.calcNewTans(tcbFactor, entryOut, entryIn, entry, animationLength);
			System.out.println("calc tans! " + entryIn + entryOut + entry);

			return new AddFlagEntryAction<>(timeline, entry, timeEnvironmentImpl.getCurrentSequence(), null);
		}
		if (!timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), trackTime)) {
			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			return new AddFlagEntryAction<>(timeline, entry, timeEnvironmentImpl.getCurrentSequence(), null);
		}
		return null;
	}


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
		return "add " + getKFTypeName() + " keyframe";
	}

	private String getKFTypeName() {
		return switch (actionType) {
			case ROTATION, SQUAT -> MdlUtils.TOKEN_ROTATION;
			case SCALING -> MdlUtils.TOKEN_SCALING;
			case TRANSLATION, EXTEND, EXTRUDE -> MdlUtils.TOKEN_TRANSLATION;
		};
	}

}
