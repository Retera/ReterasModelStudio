package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddKeyframeAction2 implements UndoAction {
	//	private final AnimFlag timeline;
	private final ModelStructureChangeListener changeListener;
	private final ModelView modelView;
	private final ModelHandler modelHandler;
	private final ModelEditorActionType3 actionType;

	private final UndoAction createKeyframeCompoundAction;

	public AddKeyframeAction2(ModelStructureChangeListener changeListener,
	                          ModelHandler modelHandler,
	                          ModelEditorActionType3 actionType) {
		this.actionType = actionType;
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();

		this.changeListener = changeListener;

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
			AnimFlag<?> timeline = node.find(keyframeMdlTypeName, timeEnvironmentImpl.getGlobalSeq());

			if (timeline == null) {
				if (keyframeMdlTypeName.equals("Rotation")) {
					timeline = new QuatAnimFlag(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					timeline = new Vec3AnimFlag(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				}
				node.add(timeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, timeline);

				actions.add(addTimelineAction);
			}
			int trackTime = getTrackTime(modelHandler.getRenderModel());
			RenderNode renderNode = modelHandler.getRenderModel().getRenderNode(node);

			AddKeyframeAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalRotation()));
				case SCALING -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalScale()));
				case TRANSLATION, EXTEND, EXTRUDE -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalLocation()));
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	private int getTrackTime(RenderModel renderModel) {
		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();

		Integer globalSeq = renderModel.getAnimatedRenderEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
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
			addKeyframeAction.redo();
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
