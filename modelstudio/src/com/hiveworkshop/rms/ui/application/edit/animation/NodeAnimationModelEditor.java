package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.editor.actions.util.*;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.TRANSLATION);
		return new TranslationKeyframeAction(setup, selection, renderModel, v);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SCALING);
		return new ScalingKeyframeAction(setup, selection, center, scale, renderModel);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.ROTATION);
		DoNothingAction doNothingAction = new DoNothingAction("");
		return new CompoundAction("rotate", changeListener::keyframesUpdated, setup,
				new RotationKeyframeAction(doNothingAction, selection, renderModel, center, Vec3.X_AXIS, Math.toRadians(rotate.x)),
				new RotationKeyframeAction(doNothingAction, selection, renderModel, center, Vec3.NEGATIVE_Y_AXIS, Math.toRadians(rotate.y)),
				new RotationKeyframeAction(doNothingAction, selection, renderModel, center, Vec3.NEGATIVE_Z_AXIS, Math.toRadians(rotate.z)));
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
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.TRANSLATION);
		return new TranslationKeyframeAction(setup, selection, renderModel).doSetup();
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SCALING);
		return new ScalingKeyframeAction(setup, selection, center, renderModel).doSetup();
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.ROTATION);
		return new RotationKeyframeAction(setup, selection, renderModel, center, firstXYZ, secondXYZ).doSetup();
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

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SQUAT);
		return new SquatToolKeyframeAction(setup, selection, renderModel, center, firstXYZ, secondXYZ).doSetup();
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, Vec3 axis) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.ROTATION);
		return new RotationKeyframeAction(setup, selection, renderModel, center, axis).doSetup();
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

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SQUAT);
		return new SquatToolKeyframeAction(setup, modelView.getSelectedIdObjects(), renderModel, center, axis).doSetup();
	}

	public CompoundAction getSetupAction(Set<IdObject> selection, ModelEditorActionType3 squat) {
		List<UndoAction> actions = createKeyframe(renderModel.getTimeEnvironment(), squat, selection);

		return new CompoundAction("setup", actions, changeListener::keyframesUpdated);
	}

	private boolean isBoneAndSameClass(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone
				&& idObject2 instanceof Bone
				&& idObject1.getClass() == idObject2.getClass();
	}

	public List<UndoAction> createKeyframe(TimeEnvironmentImpl timeEnvironmentImpl, ModelEditorActionType3 actionType, Set<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject node : selection) {
			GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
			UndoAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> createRotationKeyframe(node, getRotationTimeline(node, actions, globalSeq), timeEnvironmentImpl);
				case SCALING -> createScalingKeyframe(node, getScalingTimeline(node, actions, globalSeq), timeEnvironmentImpl);
				case TRANSLATION, EXTEND, EXTRUDE -> createTranslationKeyframe(node, getTranslationTimeline(node, actions, globalSeq), timeEnvironmentImpl);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
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


	public UndoAction createTranslationKeyframe(IdObject node, AnimFlag<Vec3> timeline, TimeEnvironmentImpl timeEnvironmentImpl) {
		if(!timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), timeEnvironmentImpl.getEnvTrackTime())){
			int trackTime = timeEnvironmentImpl.getEnvTrackTime();
			RenderNode2 renderNode = renderModel.getRenderNode(node);
			return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation())), timeEnvironmentImpl);
		}
		return null;
	}

	public UndoAction createScalingKeyframe(IdObject node, AnimFlag<Vec3> timeline, TimeEnvironmentImpl timeEnvironmentImpl) {
		if(!timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), timeEnvironmentImpl.getEnvTrackTime())){
			int trackTime = timeEnvironmentImpl.getEnvTrackTime();
			RenderNode2 renderNode = renderModel.getRenderNode(node);
			return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalScale())), timeEnvironmentImpl);
		}
		return null;
	}

	public UndoAction createRotationKeyframe(IdObject node, AnimFlag<Quat> timeline, TimeEnvironmentImpl timeEnvironmentImpl) {
		if(!timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), timeEnvironmentImpl.getEnvTrackTime())){
			int trackTime = timeEnvironmentImpl.getEnvTrackTime();
			RenderNode2 renderNode = renderModel.getRenderNode(node);
			return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Quat(renderNode.getLocalRotation())), timeEnvironmentImpl);
		}
		return null;
	}

	public AnimFlag<Vec3> getTranslationTimeline(IdObject node, List<UndoAction> actions, GlobalSeq globalSeq) {
		AnimFlag<Vec3> timeline = node.getTranslationFlag();
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		return timeline;
	}

	public AnimFlag<Vec3> getScalingTimeline(IdObject node, List<UndoAction> actions, GlobalSeq globalSeq) {
		AnimFlag<Vec3> timeline = node.getScalingFlag();
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		return timeline;
	}

	private AnimFlag<Quat> getRotationTimeline(IdObject node, List<UndoAction> actions, GlobalSeq globalSeq) {
		AnimFlag<Quat> timeline = node.getRotationFlag();
		if (timeline == null) {
			timeline = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		return timeline;
	}
}
