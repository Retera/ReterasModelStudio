package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public final class SetIdObjectsEdibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final boolean editable;
	private final ModelStructureChangeListener changeListener;
	public SetIdObjectsEdibilityAction(Boolean editable, ModelView modelViewManager,
	                                   ModelStructureChangeListener changeListener) {
		this.modelViewManager = modelViewManager;
		this.editable = editable;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setIdObjectsEditable(!editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setIdObjectsEditable(editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		if (editable) {
			return "enable edit nodes";
		}
		return "disable edit nodes";
	}

}
