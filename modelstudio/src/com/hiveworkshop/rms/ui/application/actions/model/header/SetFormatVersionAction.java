package com.hiveworkshop.rms.ui.application.actions.model.header;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

public class SetFormatVersionAction implements UndoAction {
	private final int prevVersion;
	private final int newVersion;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public SetFormatVersionAction(final int prevVersion, final int newVersion, final ModelViewManager modelViewManager,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevVersion = prevVersion;
		this.newVersion = newVersion;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().setFormatVersion(prevVersion);
		structureChangeListener.headerChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().setFormatVersion(newVersion);
		structureChangeListener.headerChanged();
	}

	@Override
	public String actionName() {
		return "set FormatVersion to " + newVersion;
	}

}
