package com.hiveworkshop.rms.ui.application.edit;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.NoSuchElementException;

public final class UndoActionImplementation extends AbstractAction {

	public UndoActionImplementation(final String name) {
		super(name);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		undo();
	}

	public void undo() {
		final ModelPanel mpanel = ProgramGlobals.getCurrentModelPanel();
		final MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (mpanel != null) {
			try {
				mpanel.getUndoManager().undo();
			} catch (final NoSuchElementException exc) {
				JOptionPane.showMessageDialog(mainPanel, "Nothing to undo!");
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
			mpanel.repaintSelfAndRelatedChildren();
		}
		ProgramGlobals.getUndoHandler().refreshUndo();
		mainPanel.repaintSelfAndChildren();
	}
}