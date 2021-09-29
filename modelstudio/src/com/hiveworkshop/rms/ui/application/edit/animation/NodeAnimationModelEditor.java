package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.Bone;
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

	public NodeAnimationModelEditor(SelectionManager selectionManager, ModelHandler modelHandler,
	                                SelectionItemTypes selectionMode) {
		super(selectionManager, modelHandler.getModelView());
		this.changeListener = ModelStructureChangeListener.changeListener;
		this.renderModel = modelHandler.getRenderModel();
	}

	@Override
	public UndoAction translate(Vec3 v) {
		return beginTranslation().updateTranslation(v);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		return beginScaling(center).updateScale(scale);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		return new CompoundAction("rotate", Arrays.asList(
				beginRotation(center, (byte) 2, (byte) 1).updateRotation(Math.toRadians(rotate.x)),
				beginRotation(center, (byte) 0, (byte) 2).updateRotation(Math.toRadians(rotate.y)),
				beginRotation(center, (byte) 1, (byte) 0).updateRotation(Math.toRadians(rotate.z))));
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return new StaticMeshMoveAction(modelView, delta);
	}

	@Override
	public GenericMoveAction beginTranslation() {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();

		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.TRANSLATION, selection);

		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new TranslationKeyframeAction(setup.redo(), selection, renderModel);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();

		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.SCALING, selection);


		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new ScalingKeyframeAction(setup.redo(), selection, center, renderModel);
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();

		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.ROTATION, selection);

		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new RotationKeyframeAction(setup.redo(), selection, renderModel, center, firstXYZ, secondXYZ);
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent())
					&& isBoneAndSameClass(idObject, idObject.getParent())) {
				selection.add(idObject);
			}
		}

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.SQUAT, selection);

		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new SquatToolKeyframeAction(setup.redo(), selection, renderModel, modelView.getModel().getIdObjects(), center, firstXYZ, secondXYZ);
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, Vec3 axis) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();

		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.ROTATION, selection);

		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new RotationKeyframeAction(setup.redo(), selection, renderModel, center, axis);
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, Vec3 axis) {
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent())
					&& isBoneAndSameClass(idObject, idObject.getParent())) {
				selection.add(idObject);
			}
		}

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		List<UndoAction> actions = createKeyframe(timeEnvironmentImpl, ModelEditorActionType3.SQUAT, selection);

		CompoundAction setup = new CompoundAction("setup", actions, changeListener::keyframesUpdated);
		return new SquatToolKeyframeAction(setup.redo(), selection, renderModel, modelView.getModel().getIdObjects(), center, axis);
	}

	private boolean isBoneAndSameClass(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone
				&& idObject2 instanceof Bone
				&& idObject1.getClass() == idObject2.getClass();
	}

	public List<UndoAction> createKeyframe(TimeEnvironmentImpl timeEnvironmentImpl, ModelEditorActionType3 actionType, Set<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject node : selection) {
			UndoAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> createRotationKeyframe(node, actions, timeEnvironmentImpl);
				case SCALING -> createScalingKeyframe(node, actions, timeEnvironmentImpl);
				case TRANSLATION, EXTEND, EXTRUDE -> createTranslationKeyframe(node, actions, timeEnvironmentImpl);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction.redo());
			}
		}

		return actions;
	}

	private <T> UndoAction getAddKeyframeAction(AnimFlag<T> timeline, Entry<T> entry, TimeEnvironmentImpl timeEnvironmentImpl) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		if (!timeline.hasGlobalSeq() && timeEnvironmentImpl.getGlobalSeq() == null
				|| timeline.getAnimMap().isEmpty()
				|| timeline.getGlobalSeq() == timeEnvironmentImpl.getGlobalSeq()){
			Sequence sequence = timeEnvironmentImpl.getCurrentSequence();
			if (sequence != null && timeline.tans()) {
				Entry<T> entryIn = timeline.getFloorEntry(entry.getTime(), sequence);
				Entry<T> entryOut = timeline.getCeilEntry(entry.getTime(), sequence);

				int animationLength = sequence.getLength();

				float[] tbcFactor = timeline.getTbcFactor(0, 0.5f, 0);
				timeline.calcNewTans(tbcFactor, entryOut, entryIn, entry, animationLength);

				System.out.println("calc tans! " + entryIn + entryOut + entry);

				return new AddFlagEntryAction<>(timeline, entry, sequence, null);
			}
			if (sequence != null && !timeline.hasEntryAt(sequence, entry.getTime())) {
				if (timeline.getInterpolationType().tangential()) {
					entry.unLinearize();
				}

				return new AddFlagEntryAction<>(timeline, entry, sequence, null);
			}
		}
		return null;
	}


	public UndoAction createTranslationKeyframe(IdObject node, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl) {
		GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
		int trackTime = timeEnvironmentImpl.getEnvTrackTime();
		AnimFlag<Vec3> timeline = node.getTranslationFlag();
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation())), timeEnvironmentImpl);
	}

	public UndoAction createScalingKeyframe(IdObject node, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl) {
		GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
		int trackTime = timeEnvironmentImpl.getEnvTrackTime();
		AnimFlag<Vec3> timeline = node.getScalingFlag();
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalScale())), timeEnvironmentImpl);
	}

	public UndoAction createRotationKeyframe(IdObject node, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl) {
		GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
		int trackTime = timeEnvironmentImpl.getEnvTrackTime();
		AnimFlag<Quat> timeline = node.getRotationFlag();
		if (timeline == null) {
			timeline = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		RenderNode2 renderNode = renderModel.getRenderNode(node);
		return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Quat(renderNode.getLocalRotation())), timeEnvironmentImpl);
	}
}
