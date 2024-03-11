package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.*;
import com.hiveworkshop.rms.editor.actions.mesh.ExtrudeAction;
import com.hiveworkshop.rms.editor.actions.mesh.SplitTrisAndFillGap;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

public class GeometryModelEditor extends ModelEditor {
	protected final ModelStructureChangeListener changeListener;
	protected ModelHandler modelHandler;

	public GeometryModelEditor(SelectionManager selectionManager,
	                           ModelHandler modelHandler) {
		super(selectionManager, modelHandler.getModelView());
		this.modelHandler = modelHandler;
		this.changeListener = ModelStructureChangeListener.changeListener;
	}

	@Override
	public UndoAction translate(Vec3 v, Mat4 rotMat) {
		Vec3 delta = new Vec3(v);
		return new StaticMeshMoveAction(modelView, delta, rotMat);
	}

	@Override
	public UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat) {
		return new StaticMeshScaleAction(modelView, center, rotMat, scale);
	}

	public UndoAction shrinkFatten(float amount, boolean scaleApart) {
		return new StaticMeshShrinkFattenAction(modelView, amount, scaleApart);
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		return new StaticMeshMoveAction(modelView, delta, new Mat4());
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate, Mat4 rotMat) {
		return new CompoundAction("rotate", null,
				new StaticMeshRotateAction(modelView, center, Vec3.X_AXIS, Math.toRadians(rotate.x)),
				new StaticMeshRotateAction(modelView, center, Vec3.NEGATIVE_Y_AXIS, Math.toRadians(rotate.y)),
				new StaticMeshRotateAction(modelView, center, Vec3.NEGATIVE_Z_AXIS, Math.toRadians(rotate.z)));
	}

	@Override
	public boolean editorWantsAnimation() {
		return false;
	}

	@Override
	public AbstractTransformAction beginTranslation(Mat4 rotMat) {
		return new StaticMeshMoveAction(modelView, Vec3.ZERO, rotMat);
	}

	@Override
	public AbstractTransformAction beginExtrude(Mat4 rotMat) {
		return new StaticMeshMoveAction(modelView, Vec3.ZERO, "Extrude", new ExtrudeAction(modelView.getSelectedVertices(), changeListener), rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginExtend(Mat4 rotMat) {
		return new StaticMeshMoveAction(modelView, Vec3.ZERO, "Extend", new SplitTrisAndFillGap(modelView.getSelectedVertices(), changeListener), rotMat).doSetup();
	}

	@Override
	public AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat) {
		return new StaticMeshRotateAction(modelView, new Vec3(center), axis, 0, rotMat);
	}

	@Override
	public AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat) {
		return this.beginRotation(center, axis, rotMat);
//		throw new WrongModeException("Unable to use squat tool outside animation editor mode");
	}
	@Override
	public AbstractTransformAction beginScaling(Vec3 center, Mat4 rotMat) {
		return new StaticMeshScaleAction(modelView, center, rotMat);
	}

	public StaticMeshShrinkFattenAction beginShrinkFatten(float amount, boolean scaleApart) {
		return new StaticMeshShrinkFattenAction(modelView, amount, scaleApart);
	}
}
