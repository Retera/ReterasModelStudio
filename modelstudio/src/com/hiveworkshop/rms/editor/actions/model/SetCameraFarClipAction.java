package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetCameraFarClipAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Camera camera;
	private final double oldClip;
	private final double newClip;

	public SetCameraFarClipAction(Camera camera, double newClip, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.camera = camera;
		this.oldClip = camera.getFarClip();
		this.newClip = newClip;
	}

	@Override
	public UndoAction undo() {
		camera.setFarClip(oldClip);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		camera.setFarClip(newClip);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set far clip for " + camera.getName();
	}
}
