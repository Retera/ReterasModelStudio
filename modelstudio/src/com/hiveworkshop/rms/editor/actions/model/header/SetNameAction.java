package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

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
	public UndoAction undo() {
		modelViewManager.getModel().setName(prevName);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().setName(newName);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set name to \"" + newName + "\"";
	}

}
