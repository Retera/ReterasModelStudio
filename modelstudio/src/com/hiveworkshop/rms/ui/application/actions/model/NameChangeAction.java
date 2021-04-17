package com.hiveworkshop.rms.ui.application.actions.model;

import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

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
	public void undo() {
		this.idObject.setName(oldName);
		structureChangeListener.nodeHierarchyChanged();
	}

	@Override
	public void redo() {
		this.idObject.setName(newName);
		structureChangeListener.nodeHierarchyChanged();
	}

	@Override
	public String actionName() {
		return oldName + " changed to " + newName;
	}
}
