package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;


public class DrawNodeAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final UndoAction setupAction;
	private final IdObject idObject;
	private final Vec3 startPoint = new Vec3();
	private final Vec3 currPos = new Vec3();
	private final String actionName;

	public DrawNodeAction(String actionName,
	                      Vec3 startPoint,
	                      Mat4 rotMat,
						  IdObject idObject,
	                      UndoAction setupAction,
	                      ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.actionName = actionName;
		this.startPoint.set(startPoint);
		this.setupAction = setupAction;
		this.idObject = idObject;

	}

	public DrawNodeAction doSetup() {
		if (setupAction != null) {
			setupAction.redo();
		}
		return this;
	}

	@Override
	public DrawNodeAction undo() {
		if (setupAction != null) {
			setupAction.undo();
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public DrawNodeAction redo() {
		if (setupAction != null) {
			setupAction.redo();
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

	@Override
	public DrawNodeAction updateTranslation(double deltaX, double deltaY, double deltaZ) {
		startPoint.translate(deltaX, deltaY, deltaZ);
		return updateTransform();
	}
	public DrawNodeAction setTranslation(Vec3 delta) {
		startPoint.set(delta);
		return updateTransform();
	}

	@Override
	public DrawNodeAction updateTranslation(Vec3 delta) {
		startPoint.add(delta);
		return updateTransform();
	}


	protected DrawNodeAction updateTransform() {
		currPos.set(startPoint);
		idObject.setPivotPoint(currPos);
		return this;
	}
}
