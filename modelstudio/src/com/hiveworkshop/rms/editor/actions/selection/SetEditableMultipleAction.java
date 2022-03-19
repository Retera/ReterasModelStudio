package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;

public class SetEditableMultipleAction implements UndoAction {
	private Collection<Object> objects;
	private boolean editable;
	private ModelView modelView;
	private ModelStructureChangeListener changeListener;

	public SetEditableMultipleAction(Collection<Object> objects, boolean editable, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.objects = objects;
		this.editable = editable;
		this.modelView = modelView;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		for (Object object : objects) {
			modelView.makeEditable(object, !editable);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Object object : objects) {
			modelView.makeEditable(object, editable);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		String edNotEd = editable ? "editable" : "not editable";
		return "make " + objects.size() + " objects " + edNotEd;
	}
}
