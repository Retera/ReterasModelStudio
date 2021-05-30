package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetHeaderExtentsAction implements UndoAction {
	private final ExtLog prevExtLog;
	private final ExtLog newExtLog;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetHeaderExtentsAction(final ExtLog prevExtLog, final ExtLog newExtLog,
	                              final ModelView modelViewManager, final ModelStructureChangeListener structureChangeListener) {
		super();
		this.prevExtLog = prevExtLog;
		this.newExtLog = newExtLog;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.getModel().setExtents(prevExtLog);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().setExtents(newExtLog);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set extents";
	}
}
