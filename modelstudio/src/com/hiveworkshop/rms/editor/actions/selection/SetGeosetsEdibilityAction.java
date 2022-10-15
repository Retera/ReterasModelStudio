package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public final class SetGeosetsEdibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener changeListener;
	private final boolean editable;

	public SetGeosetsEdibilityAction(Boolean editable, ModelView modelViewManager,
	                                 ModelStructureChangeListener changeListener) {
		this.modelViewManager = modelViewManager;
		this.changeListener = changeListener;
		this.editable = editable;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setGeosetsEditable(!editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setGeosetsEditable(editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		if (editable) {
			return "enable edit geosets";
		}
		return "disable edit geosets";
	}

}
