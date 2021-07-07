package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class NameChangeAction implements UndoAction {
	private final Named idObject;
	private final String oldName;
	private final String newName;
	private final ModelStructureChangeListener changeListener;

	public NameChangeAction(Named idObject, String newName,
	                        ModelStructureChangeListener changeListener) {
		this.idObject = idObject;
		this.oldName = idObject.getName();
		this.newName = newName;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		this.idObject.setName(oldName);
		if (changeListener != null) {
			changeListener.nodeHierarchyChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		this.idObject.setName(newName);
		if (changeListener != null) {
			changeListener.nodeHierarchyChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return oldName + " changed to " + newName;
	}
}
