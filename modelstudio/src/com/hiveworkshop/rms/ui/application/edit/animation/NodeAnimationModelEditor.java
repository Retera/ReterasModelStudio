package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class NodeAnimationModelEditor extends ModelEditor {
	private final RenderModel renderModel;
	private final ModelStructureChangeListener changeListener;

	public NodeAnimationModelEditor(SelectionManager selectionManager, ModelStructureChangeListener changeListener, ModelHandler modelHandler,
	                                SelectionItemTypes selectionMode) {
		super(selectionManager, modelHandler.getModelView());
		this.changeListener = changeListener;
		this.renderModel = modelHandler.getRenderModel();
	}

	private static AddKeyframeAction getAddKeyframeAction(AnimFlag<?> timeline, int trackTime, Entry<?> entry) {
		if (!timeline.hasEntryAt(trackTime)) {
			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}
			return new AddKeyframeAction(timeline, entry);
		}
		return null;
	}

	@Override
	public UndoAction translate(Vec3 v) {
		return beginTranslation().updateTranslation(v).redo();
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		return beginScaling(center).updateScale(scale).redo();
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		return new CompoundAction("rotate", Arrays.asList(
				beginRotation(center, (byte) 2, (byte) 1).updateRotation(rotate.x),
				beginRotation(center, (byte) 0, (byte) 2).updateRotation(rotate.y),
				beginRotation(center, (byte) 1, (byte) 0).updateRotation(rotate.z)))
				.redo();
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return new StaticMeshMoveAction(modelView, delta).redo();
	}

	private List<UndoAction> generateKeyframes(TimeEnvironmentImpl timeEnvironmentImpl, String name, ModelEditorActionType3 actionType, Set<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject node : selection) {
			AnimFlag<?> timeline = node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (timeline == null) {
				timeline = getNewAnimFlag(timeEnvironmentImpl, actionType);

				actions.add(new AddTimelineAction(node, timeline));
			}

			int trackTime = timeEnvironmentImpl.getTrackTime();
			RenderNode renderNode = renderModel.getRenderNode(node);

			Entry<?> newEntry = getEntry(actionType, trackTime, renderNode);
			AddKeyframeAction keyframeAction = getAddKeyframeAction(timeline, trackTime, newEntry);

//			AddKeyframeAction keyframeAction = switch (actionType) {
//				case ROTATION, SQUAT -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalRotation()));
//				case SCALING -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalScale()));
//				case TRANSLATION, EXTEND, EXTRUDE -> getAddKeyframeAction(timeline, trackTime, new Entry<>(trackTime, renderNode.getLocalLocation()));
//			};

			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
		return actions;
	}

	private AnimFlag<?> getNewAnimFlag(TimeEnvironmentImpl timeEnvironmentImpl, ModelEditorActionType3 actionType) {
		return switch (actionType) {
			case ROTATION, SQUAT -> new QuatAnimFlag("Rotation", InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
			case SCALING -> new Vec3AnimFlag("Scaling", InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
			case TRANSLATION, EXTEND, EXTRUDE -> new Vec3AnimFlag("Translation", InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
		};
	}

	private AnimFlag<?> findAnimFlag(TimeEnvironmentImpl timeEnvironmentImpl, IdObject node, ModelEditorActionType3 actionType) {
		return switch (actionType) {
			case ROTATION, SQUAT -> node.find("Rotation", timeEnvironmentImpl.getGlobalSeq());
			case SCALING -> node.find("Scaling", timeEnvironmentImpl.getGlobalSeq());
			case TRANSLATION, EXTEND, EXTRUDE -> node.find("Translation", timeEnvironmentImpl.getGlobalSeq());
		};
	}

	private List<UndoAction> generateKeyframes2(TimeEnvironmentImpl timeEnvironmentImpl, ModelEditorActionType3 actionType, Set<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject node : selection) {
			AnimFlag<?> timeline = findAnimFlag(timeEnvironmentImpl, node, actionType);

			if (timeline == null) {
				timeline = getNewAnimFlag(timeEnvironmentImpl, actionType);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, timeline);
				addTimelineAction.redo();
				actions.add(addTimelineAction);
			}

			RenderNode renderNode = renderModel.getRenderNode(node);
			int trackTime = timeEnvironmentImpl.getTrackTime();
			Entry<?> newEntry = getEntry(actionType, trackTime, renderNode);

			AddKeyframeAction keyframeAction = getAddKeyframeAction(timeline, trackTime, newEntry);

			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
		return actions;
	}

	private Entry<?> getEntry(ModelEditorActionType3 actionType, int trackTime, RenderNode renderNode) {
		return switch (actionType) {
			case ROTATION, SQUAT -> new Entry<>(trackTime, new Quat(renderNode.getLocalRotation()));
			case SCALING -> new Entry<>(trackTime, new Vec3(renderNode.getLocalScale()));
			case TRANSLATION, EXTEND, EXTRUDE -> new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation()));
		};
	}

	@Override
	public GenericMoveAction beginTranslation() {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Translation", ModelEditorActionType3.TRANSLATION, selection);

		int trackTime = timeEnvironmentImpl.getTrackTime();
		return new TranslationKeyframeAction(new CompoundAction("setup", actions, changeListener::keyframesUpdated).redo(), trackTime, timeEnvironmentImpl.getGlobalSeq(), selection, modelView);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Scaling", ModelEditorActionType3.SCALING, selection);


		int trackTime = timeEnvironmentImpl.getTrackTime();
		return new ScalingKeyframeAction(new CompoundAction("setup", actions, changeListener::keyframesUpdated).redo(), trackTime, timeEnvironmentImpl.getGlobalSeq(), selection, center, modelView);
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Rotation", ModelEditorActionType3.ROTATION, selection);

		int trackTime = timeEnvironmentImpl.getTrackTime();
		return new RotationKeyframeAction(new CompoundAction("setup", actions, changeListener::keyframesUpdated).redo(), trackTime, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				selection.add(idObject);
			}
		}

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();
		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Rotation", ModelEditorActionType3.SQUAT, selection);

		int trackTime = timeEnvironmentImpl.getTrackTime();
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions, changeListener::keyframesUpdated).redo(), trackTime, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}
}
