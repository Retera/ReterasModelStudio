package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class SetBlendTimeAction implements UndoAction {
	private final int prevBlendTime;
	private final int newBlendTime;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetBlendTimeAction(final int prevBlendTime, final int newBlendTime, final ModelViewManager modelViewManager,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevBlendTime = prevBlendTime;
		this.newBlendTime = newBlendTime;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().setBlendTime(prevBlendTime);
		structureChangeListener.headerChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().setBlendTime(newBlendTime);
		structureChangeListener.headerChanged();
	}

	@Override
	public String actionName() {
		return "set BlendTime to " + newBlendTime;
	}

}
