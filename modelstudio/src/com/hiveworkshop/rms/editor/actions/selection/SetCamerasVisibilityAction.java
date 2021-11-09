package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class SetCamerasVisibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final Runnable refreshGUIRunnable;
	private final boolean visible;

	public SetCamerasVisibilityAction(Boolean visible, ModelView modelViewManager,
	                                  Runnable refreshGUIRunnable) {
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
		this.visible = visible;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setCamerasVisible(!visible);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setCamerasVisible(visible);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
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
