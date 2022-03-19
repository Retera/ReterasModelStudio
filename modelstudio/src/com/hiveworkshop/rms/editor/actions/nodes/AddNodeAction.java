package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddNodeAction implements UndoAction {
	private final EditableModel model;
	private final IdObject node;
	private final ModelStructureChangeListener changeListener;

	public AddNodeAction(EditableModel model, IdObject node, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.node = node;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.remove(node);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(node);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add " + node.getClass().getSimpleName();
	}
}
