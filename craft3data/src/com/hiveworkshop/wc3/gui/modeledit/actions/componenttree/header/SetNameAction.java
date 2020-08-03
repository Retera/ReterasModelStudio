package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class SetNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetNameAction(final String prevName, final String newName, final ModelViewManager modelViewManager,
			final ModelStructureChangeListener structureChangeListener) {
		super();
		this.prevName = prevName;
		this.newName = newName;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().setName(prevName);
		structureChangeListener.headerChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().setName(newName);
		structureChangeListener.headerChanged();
	}

	@Override
	public String actionName() {
		return "set name to \"" + newName + "\"";
	}

}
