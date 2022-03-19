package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class SetGeosetsEdibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final Runnable refreshGUIRunnable;
	private final boolean editable;

	public SetGeosetsEdibilityAction(Boolean editable, ModelView modelViewManager,
	                                 Runnable refreshGUIRunnable) {
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
		this.editable = editable;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setGeosetsEditable(!editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setGeosetsEditable(editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
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
