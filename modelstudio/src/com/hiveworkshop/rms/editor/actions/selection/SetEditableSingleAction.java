package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetEditableSingleAction implements UndoAction {
	private Object object;
	private String name;
	private boolean editable;
	private ModelView modelView;
	private ModelStructureChangeListener changeListener;

	public SetEditableSingleAction(Object object, boolean editable, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.object = object;
		this.editable = editable;
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
		modelView.makeEditable(object, !editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.makeEditable(object, editable);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		String edNotEd = editable ? " editable" : " not editable";
		return "make " + name + edNotEd;
	}
}
