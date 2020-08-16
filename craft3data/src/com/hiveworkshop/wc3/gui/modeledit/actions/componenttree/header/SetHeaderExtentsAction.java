package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class SetHeaderExtentsAction implements UndoAction {
	private final ExtLog prevExtLog;
	private final ExtLog newExtLog;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetHeaderExtentsAction(final ExtLog prevExtLog, final ExtLog newExtLog,
			final ModelViewManager modelViewManager, final ModelStructureChangeListener structureChangeListener) {
		super();
		this.prevExtLog = prevExtLog;
		this.newExtLog = newExtLog;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().setExtents(prevExtLog);
		structureChangeListener.headerChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().setExtents(newExtLog);
		structureChangeListener.headerChanged();
	}

	@Override
	public String actionName() {
		return "set extents";
	}
}
