package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;

public final class MakeNotEditableAction implements UndoAction {
	private final EditabilityToggleHandler editabilityToggleHandler;
	private final Runnable truncateSelectionRunnable;
	private final Runnable unTruncateSelectionRunnable;
	private final Runnable refreshGUIRunnable;

	public MakeNotEditableAction(final EditabilityToggleHandler editabilityToggleHandler,
			final Runnable truncateSelectionRunnable, final Runnable unTruncateSelectionRunnable,
			final Runnable refreshGUIRunnable) {
		this.editabilityToggleHandler = editabilityToggleHandler;
		this.truncateSelectionRunnable = truncateSelectionRunnable;
		this.unTruncateSelectionRunnable = unTruncateSelectionRunnable;
		this.refreshGUIRunnable = refreshGUIRunnable;
	}

	@Override
	public void undo() {
		editabilityToggleHandler.makeEditable();
		unTruncateSelectionRunnable.run();
		refreshGUIRunnable.run();
	}

	@Override
	public void redo() {
		editabilityToggleHandler.makeNotEditable();
		truncateSelectionRunnable.run();
//		refreshGUIRunnable.run();
	}

	@Override
	public String actionName() {
		return "toggle visibility";
	}

}
