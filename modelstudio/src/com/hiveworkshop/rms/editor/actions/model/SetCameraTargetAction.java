package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetCameraTargetAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Camera camera;
	private final Vec3 oldPosition;
	private final Vec3 newPosition;

	public SetCameraTargetAction(Camera camera, Vec3 newPosition, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.camera = camera;
		this.oldPosition = camera.getTargetPosition();
		this.newPosition = newPosition;
	}

	@Override
	public UndoAction undo() {
		camera.setTargetPosition(oldPosition);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		camera.setTargetPosition(newPosition);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set target for " + camera.getName();
	}
}
