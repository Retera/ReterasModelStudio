package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public final class RenameBoneAction implements UndoAction {
	private final String oldName;
	private final String newName;
	private final IdObject nodeToRename;

	public RenameBoneAction(String newName, IdObject nodeToRename) {
		this.oldName = nodeToRename.getName();
		this.newName = newName;
		this.nodeToRename = nodeToRename;
	}

	@Override
	public UndoAction undo() {
		nodeToRename.setName(oldName);
		return this;
	}

	@Override
	public UndoAction redo() {
		nodeToRename.setName(newName);
		return this;
	}

	@Override
	public String actionName() {
		return "rename " + oldName + " to " + newName;
	}
}
