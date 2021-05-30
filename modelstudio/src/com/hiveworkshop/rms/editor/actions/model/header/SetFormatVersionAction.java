package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetFormatVersionAction implements UndoAction {
	private final int prevVersion;
	private final int newVersion;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetFormatVersionAction(final int prevVersion, final int newVersion, final ModelView modelViewManager,
	                              final ModelStructureChangeListener structureChangeListener) {
		this.prevVersion = prevVersion;
		this.newVersion = newVersion;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.getModel().setFormatVersion(prevVersion);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().setFormatVersion(newVersion);
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set FormatVersion to " + newVersion;
	}

}
