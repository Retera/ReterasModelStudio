package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class SetIdObjectsEdibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final Runnable refreshGUIRunnable;
	private final boolean editable;

	public SetIdObjectsEdibilityAction(Boolean editable, ModelView modelViewManager,
	                                   Runnable refreshGUIRunnable) {
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
		this.editable = editable;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setIdObjectsEditable(!editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setIdObjectsEditable(editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
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
