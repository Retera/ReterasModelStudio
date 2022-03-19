package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetCameraNearClipAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Camera camera;
	private final double oldClip;
	private final double newClip;

	public SetCameraNearClipAction(Camera camera, double newClip, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.camera = camera;
		this.oldClip = camera.getNearClip();
		this.newClip = newClip;
	}

	@Override
	public UndoAction undo() {
		camera.setNearClip(oldClip);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		camera.setNearClip(newClip);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set near clip for " + camera.getName();
	}
}
