package com.hiveworkshop.rms.ui.application.actions.model;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ParentChangeAction implements UndoAction {
	private final IdObject oldParent;
	private final IdObject idObject;
	private final IdObject newParent;
	private final ModelStructureChangeListener structureChangeListener;

	public ParentChangeAction(IdObject idObject, IdObject oldParent, IdObject newParent,
	                          final ModelStructureChangeListener modelStructureChangeListener) {
		this.idObject = idObject;
		this.oldParent = oldParent;
		this.newParent = newParent;
		this.structureChangeListener = modelStructureChangeListener;
	}

	public ParentChangeAction(IdObject idObject, IdObject newParent,
	                          final ModelStructureChangeListener modelStructureChangeListener) {
		this.idObject = idObject;
		this.oldParent = idObject.getParent();
		this.newParent = newParent;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		this.idObject.setParent(oldParent);
		structureChangeListener.nodeHierarchyChanged();
	}

	@Override
	public void redo() {
		this.idObject.setParent(newParent);
		structureChangeListener.nodeHierarchyChanged();
	}

	@Override
	public String actionName() {
		return "changed parent for " + idObject.getName();
	}
}
