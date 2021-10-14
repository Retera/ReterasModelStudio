package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetCameraFoVAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Camera camera;
	private final double oldFoV;
	private final double newFoV;

	public SetCameraFoVAction(Camera camera, double FoV, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.camera = camera;
		this.oldFoV = camera.getFieldOfView();
		this.newFoV = FoV;
	}

	@Override
	public UndoAction undo() {
		camera.setFieldOfView(oldFoV);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		camera.setFieldOfView(newFoV);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set FoV for " + camera.getName();
	}
}
