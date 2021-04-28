package com.hiveworkshop.rms.ui.application.actions.model.header;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetNameAction(final String prevName, final String newName, final ModelView modelViewManager,
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
