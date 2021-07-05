package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddCameraAction implements UndoAction {
	private final EditableModel model;
	private final Camera camera;
	private final ModelStructureChangeListener changeListener;

	public AddCameraAction(EditableModel model, Camera camera, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.camera = camera;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.remove(camera);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(camera);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add Camera";
	}
}
