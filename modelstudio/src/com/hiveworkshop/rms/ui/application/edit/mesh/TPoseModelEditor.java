package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.CompoundRotateAction;
import com.hiveworkshop.rms.editor.actions.editor.CompoundScaleAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.nodes.RotateNodesTPoseAction;
import com.hiveworkshop.rms.editor.actions.nodes.TranslateNodesTPoseAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TPoseModelEditor extends ModelEditor {
	private final RenderModel renderModel;
	private final ModelStructureChangeListener changeListener;
	private static boolean preserveAnimations = true;
	boolean indvOrigins = false;

	public TPoseModelEditor(SelectionManager selectionManager, ModelHandler modelHandler) {
		super(selectionManager, modelHandler.getModelView());
		this.changeListener = ModelStructureChangeListener.changeListener;
		this.renderModel = modelHandler.getRenderModel();
	}

	@Override
	public UndoAction translate(Vec3 v, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();
		List<Geoset> geosets = renderModel.getModel().getGeosets();

		if (preserveAnimations) {
			List<UndoAction> actions = new ArrayList<>();
			List<Sequence> allSequences = modelView.getModel().getAllSequences();
			GlobalSeq globalSeq = getGlobasSeq(actions, allSequences);

			CompoundAction setup = new CompoundAction("setup", actions, changeListener::refreshRenderGeosets);
			return new TranslateNodesTPoseAction(setup, selection, camSelection, geosets, v, rotMat, preserveAnimations, globalSeq, allSequences, null);
		} else {
			return new TranslateNodesTPoseAction(null, selection, camSelection, geosets, v, rotMat, preserveAnimations, null, Collections.emptyList(), null);
		}
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat) {
		return new CompoundAction("Not Implemented", new ArrayList<>());
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();
		List<Geoset> geosets = renderModel.getModel().getGeosets();

		Quat tempRot = new Quat();
		Quat rot = new Quat();
		rot.mul(tempRot.setFromAxisAngle(Vec3.X_AXIS,          (float) Math.toRadians(rotate.x)));
		rot.mul(tempRot.setFromAxisAngle(Vec3.NEGATIVE_Y_AXIS, (float) Math.toRadians(rotate.y)));
		rot.mul(tempRot.setFromAxisAngle(Vec3.NEGATIVE_Z_AXIS, (float) Math.toRadians(rotate.z)));

		Vec3 axis = new Vec3().setAsAxis(rot);
		float angle = rot.getAxisAngle();

//		Vec3 temp = new Vec3().wikiToEuler(rot).toDeg();
//		System.out.println("rot: " + rot);
//		System.out.println("axis: " + axis + ", angle: " + Math.toDegrees(angle));
//		System.out.println("euler: " + temp);

		center = indvOrigins ? null : center;
		if (preserveAnimations) {

			List<UndoAction> actions = new ArrayList<>();
			List<Sequence> allSequences = modelView.getModel().getAllSequences();
			GlobalSeq globalSeq = getGlobasSeq(actions, allSequences);

			CompoundAction setup = new CompoundAction("setup", actions, changeListener::refreshRenderGeosets);
			return new RotateNodesTPoseAction(setup, selection, camSelection, geosets, center, axis, angle, rotMat, preserveAnimations, indvOrigins, globalSeq, allSequences, changeListener);
		} else {
			return new RotateNodesTPoseAction(null, selection, camSelection, geosets, center, axis, angle, rotMat, preserveAnimations, indvOrigins, null, Collections.emptyList(), changeListener);
		}
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
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();
		List<Geoset> geosets = renderModel.getModel().getGeosets();

		if (preserveAnimations) {
			List<UndoAction> actions = new ArrayList<>();
			List<Sequence> allSequences = modelView.getModel().getAllSequences();
			GlobalSeq globalSeq = getGlobasSeq(actions, allSequences);

			CompoundAction setup = new CompoundAction("setup", actions, changeListener::refreshRenderGeosets);
			return new TranslateNodesTPoseAction(setup, selection, camSelection, geosets, new Vec3(), rotMat, preserveAnimations, globalSeq, allSequences, null).doSetup();
		} else {
			return new TranslateNodesTPoseAction(null, selection, camSelection, geosets, new Vec3(), rotMat, preserveAnimations, null, Collections.emptyList(), null).doSetup();
		}
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
		return new CompoundScaleAction("Not Implemented", new ArrayList<>());
	}

	@Override
	public AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		Set<CameraNode> camSelection = modelView.getSelectedCameraNodes();
		List<Geoset> geosets = renderModel.getModel().getGeosets();

		center = indvOrigins ? null : center;
		if (preserveAnimations) {

			List<UndoAction> actions = new ArrayList<>();
			List<Sequence> allSequences = modelView.getModel().getAllSequences();
			GlobalSeq globalSeq = getGlobasSeq(actions, allSequences);

			CompoundAction setup = new CompoundAction("setup", actions, changeListener::refreshRenderGeosets);

			return new RotateNodesTPoseAction(setup, selection, camSelection, geosets, center, axis, 0.0, rotMat, preserveAnimations, indvOrigins, globalSeq, allSequences, null).doSetup();
		} else {
			return new RotateNodesTPoseAction(null, selection, camSelection, geosets, center, axis, 0.0, rotMat, preserveAnimations, indvOrigins, null, Collections.emptyList(), null).doSetup();
		}
	}

	@Override
	public AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat) {
		return new CompoundRotateAction("Not Implemented", new ArrayList<>());
	}


	boolean useGlobalSeqForUnAnimated = true;

	private GlobalSeq getGlobasSeq(List<UndoAction> actions, List<Sequence> anims){
		if (useGlobalSeqForUnAnimated) {
			GlobalSeq globalSeq = modelView.getModel().getGlobalSeqByLength(100);
			if (globalSeq == null) {
				globalSeq = new GlobalSeq(100);
				actions.add(new AddSequenceAction(modelView.getModel(), globalSeq, null));
				anims.add(globalSeq);
			}
			return globalSeq;
		}
		return null;
	}

	public UndoAction shrinkFatten(float amount, boolean scaleApart) {
		return null;
	}
	public StaticMeshShrinkFattenAction beginShrinkFatten(float amount, boolean scaleApart) {
		return null;
	}

	public static boolean isPreserveAnimations() {
		return preserveAnimations;
	}
	public static void setPreserveAnimations(boolean b) {
		preserveAnimations = b;
	}
}
