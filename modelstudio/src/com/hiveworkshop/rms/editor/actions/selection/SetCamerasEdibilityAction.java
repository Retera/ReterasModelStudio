package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;


public final class SetCamerasEdibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final Runnable refreshGUIRunnable;
	private final boolean editable;

	public SetCamerasEdibilityAction(Boolean editable, ModelView modelViewManager,
	                                 Runnable refreshGUIRunnable) {
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
		this.editable = editable;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setCamerasEditable(!editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setCamerasEditable(editable);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		if (editable) {
			return "enable edit cameras";
		}
		return "disable edit cameras";
	}

}
