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

public class AddKeyframeAction2 implements UndoAction {
	private final ModelView modelView;
	private final ModelHandler modelHandler;
	private final ModelEditorActionType3 actionType;

	private final UndoAction createKeyframeCompoundAction;

	public AddKeyframeAction2(ModelHandler modelHandler,
	                          ModelEditorActionType3 actionType) {
		this.actionType = actionType;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();

		createKeyframeCompoundAction = createKeyframe();
	}

	public UndoAction createKeyframe() {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = modelHandler.getEditTimeEnv();
		int trackTime = getTrackTime(modelHandler.getRenderModel());

		for (IdObject node : selection) {
			AnimFlag<?> timeline = getAnimFlag(actions, timeEnvironmentImpl, node);
			if (!node.has(timeline.getName())) {
				actions.add(new AddTimelineAction(node, timeline));
			}

			RenderNode renderNode = modelHandler.getRenderModel().getRenderNode(node);

			AddKeyframeAction keyframeAction = getAddKeyframeAction(timeline, trackTime, getEntry(actionType, trackTime, renderNode));
			;
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	private Entry<?> getEntry(ModelEditorActionType3 actionType, int trackTime, RenderNode renderNode) {
		return switch (actionType) {
			case ROTATION, SQUAT -> new Entry<>(trackTime, new Quat(renderNode.getLocalRotation()));
			case SCALING -> new Entry<>(trackTime, new Vec3(renderNode.getLocalScale()));
			case TRANSLATION, EXTEND, EXTRUDE -> new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation()));
		};
	}


	private AnimFlag<?> getAnimFlag(List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl, IdObject node) {
		AnimFlag<?> timeline = switch (actionType) {
			case ROTATION, SQUAT -> node.getRotationFlag(timeEnvironmentImpl.getGlobalSeq());
			case SCALING -> node.getScalingFlag(timeEnvironmentImpl.getGlobalSeq());
			case TRANSLATION, EXTEND, EXTRUDE -> node.getTranslationFlag(timeEnvironmentImpl.getGlobalSeq());
		};

		if (timeline == null) {
			timeline = switch (actionType) {
				case ROTATION, SQUAT -> new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				case SCALING -> new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				case TRANSLATION, EXTEND, EXTRUDE -> new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
			};
		}
		return timeline;
	}

	private int getTrackTime(RenderModel renderModel) {
		int trackTime = renderModel.getTimeEnvironment().getAnimationTime();

		Integer globalSeq = renderModel.getTimeEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getTimeEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private AddKeyframeAction getAddKeyframeAction(AnimFlag<?> timeline,
	                                               int trackTime, Entry<?> entry) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		if (!timeline.hasEntryAt(trackTime)) {
			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			AddKeyframeAction addKeyframeAction = new AddKeyframeAction(timeline, entry);
			return addKeyframeAction;
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
		return "add keyframe";
	}

}
