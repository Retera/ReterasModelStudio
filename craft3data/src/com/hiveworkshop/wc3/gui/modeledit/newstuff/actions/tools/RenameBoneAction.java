package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.IdObject;
import hiveworkshop.localizationmanager.localizationmanager;

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
		return LocalizationManager.getInstance().get("string.renameboneaction_actionname_rename") + oldName + LocalizationManager.getInstance().get("string.renameboneaction_actionname_to") + newName;
	}
}
