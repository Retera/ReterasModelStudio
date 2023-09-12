package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.*;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NodeAnimationModelEditor extends ModelEditor {
	private final RenderModel renderModel;
	private final TimeEnvironmentImpl timeEnvironment;
	private final ModelStructureChangeListener changeListener;
	private final boolean worldSpace = true;

	public NodeAnimationModelEditor(SelectionManager selectionManager, ModelHandler modelHandler) {
		super(selectionManager, modelHandler.getModelView());
		this.changeListener = ModelStructureChangeListener.changeListener;
		this.renderModel = modelHandler.getRenderModel();
		this.timeEnvironment = renderModel.getTimeEnvironment();
	}

	@Override
	public UndoAction translate(Vec3 v, Mat4 rotMat) {
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = KeyframeActionHelpers.getTranslSetupAction(nodeSelection, camSelection, timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new TranslationKeyframeAction(setup, nodeSelection, camSelection, renderModel, v, worldSpace, rotMat);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat) {
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();

		CompoundAction setup = KeyframeActionHelpers.getScaleSetupAction(nodeSelection, Collections.emptySet(), timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new ScalingKeyframeAction(setup, nodeSelection, center, scale, renderModel, rotMat);
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 radAngles, Mat4 rotMat) {
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();
		CompoundAction setup = KeyframeActionHelpers.getRotSetupAction(nodeSelection, camSelection, timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new CompoundAction("radAngles", changeListener::keyframesUpdated, setup,
				new RotationKeyframeAction(null, nodeSelection, camSelection, renderModel, center, Vec3.X_AXIS, Math.toRadians(radAngles.x), worldSpace, rotMat),
				new RotationKeyframeAction(null, nodeSelection, camSelection, renderModel, center, Vec3.NEGATIVE_Y_AXIS, Math.toRadians(radAngles.y), worldSpace, rotMat),
				new RotationKeyframeAction(null, nodeSelection, camSelection, renderModel, center, Vec3.NEGATIVE_Z_AXIS, Math.toRadians(radAngles.z), worldSpace, rotMat));
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return translate(delta, new Mat4());
	}

	@Override
	public AbstractTransformAction beginTranslation(Mat4 rotMat) {
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = KeyframeActionHelpers.getTranslSetupAction(nodeSelection, camSelection, timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new TranslationKeyframeAction(setup, nodeSelection, camSelection, renderModel, Vec3.ZERO, worldSpace, rotMat).doSetup();
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
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();

		CompoundAction setup = KeyframeActionHelpers.getScaleSetupAction(nodeSelection, Collections.emptySet(), timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new ScalingKeyframeAction(setup, nodeSelection, center, Vec3.ONE, renderModel, rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat) {
		Set<IdObject> nodeSelection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();

		CompoundAction setup = KeyframeActionHelpers.getRotSetupAction(nodeSelection, camSelection, timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new RotationKeyframeAction(setup, nodeSelection, camSelection, renderModel, center, axis, 0, worldSpace, rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat) {
		System.out.println("begin Squat!");
		Set<IdObject> nodeSelection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent())
					&& isBoneAndSameClass(idObject, idObject.getParent())) {
				nodeSelection.add(idObject);
			}
		}

		CompoundAction setup = KeyframeActionHelpers.getRotSetupAction(nodeSelection, Collections.emptySet(), timeEnvironment.getCurrentSequence(), timeEnvironment.getEnvTrackTime());
		return new SquatToolKeyframeAction(setup, modelView.getSelectedIdObjects(), renderModel, center, axis, rotMat).doSetup();
	}

	private boolean isBoneAndSameClass(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone
				&& idObject2 instanceof Bone
				&& idObject1.getClass() == idObject2.getClass();
	}

	public UndoAction shrinkFatten(float amount, boolean scaleApart) {
		return null;
	}
	public StaticMeshShrinkFattenAction beginShrinkFatten(float amount, boolean scaleApart) {
		return null;
	}
}
