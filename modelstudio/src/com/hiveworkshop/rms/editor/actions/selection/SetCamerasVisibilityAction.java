package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public final class SetCamerasVisibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener changeListener;
	private final boolean visible;

	public SetCamerasVisibilityAction(Boolean visible, ModelView modelViewManager,
	                                  ModelStructureChangeListener changeListener) {
		this.modelViewManager = modelViewManager;
		this.changeListener = changeListener;
		this.visible = visible;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setCamerasVisible(!visible);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setCamerasVisible(visible);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		if (visible) {
			return "show cameras";
		}
		return "hide cameras";
	}

}
