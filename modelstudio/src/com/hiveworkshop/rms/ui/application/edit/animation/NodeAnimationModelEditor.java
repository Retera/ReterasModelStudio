package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
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
	public UndoAction translate(Vec3 v, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = getSetupAction(selection, camSelection, ModelEditorActionType3.TRANSLATION);
		return new TranslationKeyframeAction(setup, selection, camSelection, renderModel, v, rotMat);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SCALING);
		return new ScalingKeyframeAction(setup, selection, center, scale, renderModel, rotMat);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.ROTATION);
		return new CompoundAction("rotate", changeListener::keyframesUpdated, setup,
				new RotationKeyframeAction(null, selection, renderModel, center, Vec3.X_AXIS, Math.toRadians(rotate.x), rotMat),
				new RotationKeyframeAction(null, selection, renderModel, center, Vec3.NEGATIVE_Y_AXIS, Math.toRadians(rotate.y), rotMat),
				new RotationKeyframeAction(null, selection, renderModel, center, Vec3.NEGATIVE_Z_AXIS, Math.toRadians(rotate.z), rotMat));
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
//		return new StaticMeshMoveAction(modelView, delta);
		return translate(delta, new Mat4());
	}

	@Override
	public AbstractTransformAction beginTranslation(Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = getSetupAction(selection, camSelection, ModelEditorActionType3.TRANSLATION);
		return new TranslationKeyframeAction(setup, selection, camSelection, renderModel, rotMat).doSetup();
	}
//	@Override
	public AbstractTransformAction beginExtrude(Mat4 rotMat) {
		return beginTranslation(new Mat4());
	}
//	@Override
	public AbstractTransformAction beginExtend(Mat4 rotMat) {
		return beginTranslation(new Mat4());
	}

	@Override
	public AbstractTransformAction beginScaling(Vec3 center, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SCALING);
		return new ScalingKeyframeAction(setup, selection, center, renderModel, rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = getSetupAction(selection, camSelection, ModelEditorActionType3.ROTATION);
		return new RotationKeyframeAction(setup, selection, camSelection, renderModel, center, axis, rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat) {
		System.out.println("begin Squat!");
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent())
					&& isBoneAndSameClass(idObject, idObject.getParent())) {
				selection.add(idObject);
			}
		}

		CompoundAction setup = getSetupAction(selection, ModelEditorActionType3.SQUAT);
		return new SquatToolKeyframeAction(setup, modelView.getSelectedIdObjects(), renderModel, center, axis, rotMat).doSetup();
	}

	public CompoundAction getSetupAction(Set<IdObject> selection, ModelEditorActionType3 actionType) {
		List<UndoAction> actions = createKeyframe(renderModel.getTimeEnvironment(), actionType, selection);

		return new CompoundAction("setup", actions, changeListener::keyframesUpdated);
	}

	public CompoundAction getSetupAction(Set<IdObject> selection, Set<CameraNode> camSelection, ModelEditorActionType3 squat) {
		List<UndoAction> actions = createKeyframe(renderModel.getTimeEnvironment(), squat, selection);
		actions.addAll(createKeyframeC(renderModel.getTimeEnvironment(), squat, camSelection));

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

	public List<UndoAction> createKeyframeC(TimeEnvironmentImpl timeEnvironmentImpl, ModelEditorActionType3 actionType, Set<CameraNode> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (CameraNode node : selection) {
			GlobalSeq globalSeq = timeEnvironmentImpl.getGlobalSeq();
			UndoAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> createRotationKeyframe(node, getRotationTimeline(node, actions, globalSeq), timeEnvironmentImpl);
				case SCALING -> null;
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
			} else if (sequence != null) {
				if (timeline.tans()) {
					entry.unLinearize();
				}

				return new AddFlagEntryAction<>(timeline, entry, sequence, null);
			}
//			if (sequence != null && !timeline.hasEntryAt(sequence, entry.getTime())) {
//				if (timeline.getInterpolationType().tangential()) {
//					entry.unLinearize();
//				}
//
//				return new AddFlagEntryAction<>(timeline, entry, sequence, null);
//			}
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

	public UndoAction createTranslationKeyframe(CameraNode node, AnimFlag<Vec3> timeline, TimeEnvironmentImpl timeEnvironmentImpl) {
		if(!timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), timeEnvironmentImpl.getEnvTrackTime())){
			int trackTime = timeEnvironmentImpl.getEnvTrackTime();
			RenderNodeCamera renderNode = renderModel.getRenderNode(node);
			return getAddKeyframeAction(timeline, new Entry<>(trackTime, new Vec3(renderNode.getLocalLocation())), timeEnvironmentImpl);
		}
		return null;
	}

	public UndoAction createRotationKeyframe(CameraNode node, AnimFlag<?> timeline, TimeEnvironmentImpl timeEnvironmentImpl) {
		if (timeline != null && !timeline.hasEntryAt(timeEnvironmentImpl.getCurrentSequence(), timeEnvironmentImpl.getEnvTrackTime())){
			int trackTime = timeEnvironmentImpl.getEnvTrackTime();
			RenderNodeCamera renderNode = renderModel.getRenderNode(node);
			if (timeline instanceof QuatAnimFlag){
				QuatAnimFlag qTimeline = (QuatAnimFlag) timeline;
				return getAddKeyframeAction(qTimeline, new Entry<>(trackTime, new Quat(renderNode.getLocalRotation())), timeEnvironmentImpl);
			} else if (timeline instanceof FloatAnimFlag){
				FloatAnimFlag fTimeline = (FloatAnimFlag) timeline;
				return getAddKeyframeAction(fTimeline, new Entry<>(trackTime, renderNode.getLocalRotationFloat()), timeEnvironmentImpl);
			} else if (timeline instanceof IntAnimFlag){
				IntAnimFlag iTimeline = (IntAnimFlag) timeline;
				return getAddKeyframeAction(iTimeline, new Entry<>(trackTime, renderNode.getLocalRotationInt()), timeEnvironmentImpl);
			}
		}
		return null;
	}

	public AnimFlag<Vec3> getTranslationTimeline(CameraNode node, List<UndoAction> actions, GlobalSeq globalSeq) {
		AnimFlag<Vec3> timeline = node.getTranslationFlag();
		if (timeline == null) {
			timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.HERMITE, globalSeq);

			actions.add(new AddTimelineAction<>(node, timeline));
		}
		return timeline;
	}

	private AnimFlag<?> getRotationTimeline(CameraNode node, List<UndoAction> actions, GlobalSeq globalSeq) {
		if (node instanceof CameraNode.TargetNode){
			return null;
		} else {

			AnimFlag<?> timeline = node.getRotationFlag();
			if (timeline == null) {
				timeline = new FloatAnimFlag(MdlUtils.TOKEN_ROTATION);
				actions.add(new AddTimelineAction<>(node, timeline));
			}
			return timeline;
		}
	}

	public UndoAction shrinkFatten(float amount) {
		return null;
	}
	public StaticMeshShrinkFattenAction beginShrinkFatten(float amount) {
		return null;
	}
}
