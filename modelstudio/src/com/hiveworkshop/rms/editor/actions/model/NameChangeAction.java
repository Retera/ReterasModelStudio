package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class NameChangeAction implements UndoAction {
	private final Named idObject;
	private final String oldName;
	private final String newName;
	private final ModelStructureChangeListener structureChangeListener;

	public NameChangeAction(Named idObject, String oldName, String newName,
	                        final ModelStructureChangeListener modelStructureChangeListener) {
		this.idObject = idObject;
		this.oldName = oldName;
		this.newName = newName;
		this.structureChangeListener = modelStructureChangeListener;
	}

	public NameChangeAction(Named idObject, String newName,
	                        final ModelStructureChangeListener modelStructureChangeListener) {
		this.idObject = idObject;
		this.oldName = idObject.getName();
		this.newName = newName;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		this.idObject.setName(oldName);
		structureChangeListener.nodeHierarchyChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		this.idObject.setName(newName);
		structureChangeListener.nodeHierarchyChanged();
		return this;
	}

	@Override
	public String actionName() {
		return oldName + " changed to " + newName;
	}
}
