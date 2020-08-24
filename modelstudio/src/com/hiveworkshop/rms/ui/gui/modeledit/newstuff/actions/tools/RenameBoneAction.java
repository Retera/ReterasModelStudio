package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.IdObject;

public final class RenameBoneAction implements UndoAction {
	private final String oldName;
	private final String newName;
	private final IdObject nodeToRename;

	public RenameBoneAction(final String oldName, final String newName, final IdObject nodeToRename) {
		this.oldName = oldName;
		this.newName = newName;
		this.nodeToRename = nodeToRename;
	}

	@Override
	public void undo() {
		nodeToRename.setName(oldName);
	}

	@Override
	public void redo() {
		nodeToRename.setName(newName);
	}

	@Override
	public String actionName() {
		return "rename " + oldName + " to " + newName;
	}
}
