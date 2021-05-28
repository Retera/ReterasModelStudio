package com.hiveworkshop.rms.ui.application.actions.model.header;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetBlendTimeAction implements UndoAction {
	private final int prevBlendTime;
	private final int newBlendTime;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetBlendTimeAction(final int prevBlendTime, final int newBlendTime, final ModelView modelViewManager,
	                          final ModelStructureChangeListener structureChangeListener) {
		this.prevBlendTime = prevBlendTime;
		this.newBlendTime = newBlendTime;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.getModel().setBlendTime(prevBlendTime);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().setBlendTime(newBlendTime);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set BlendTime to " + newBlendTime;
	}

}
