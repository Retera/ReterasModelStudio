package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ShowHideSingleAction implements UndoAction {
	private Object object;
	private String name;
	private boolean visible;
	private ModelView modelView;
	private ModelStructureChangeListener changeListener;

	public ShowHideSingleAction(Object object, boolean visible, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.object = object;
		this.visible = visible;
		this.modelView = modelView;
		this.changeListener = changeListener;
		if (object instanceof Named) {
			name = ((Named) object).getName();
		} else {
			name = "object";
		}
	}

	@Override
	public UndoAction undo() {
		modelView.makeVisible(object, !visible);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.makeVisible(object, visible);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		String showHide = visible ? "show " : "hide ";
		return showHide + name;
	}
}
