package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class SetGeosetsVisibilityAction implements UndoAction {
	private final ModelView modelViewManager;
	private final Runnable refreshGUIRunnable;
	private final boolean visible;

	public SetGeosetsVisibilityAction(Boolean visible, ModelView modelViewManager,
	                                  Runnable refreshGUIRunnable) {
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
		this.visible = visible;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.setGeosetsVisible(!visible);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.setGeosetsVisible(visible);
		if (refreshGUIRunnable != null) {
			refreshGUIRunnable.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		if (visible) {
			return "show geosets";
		}
		return "hide geosets";
	}

}
