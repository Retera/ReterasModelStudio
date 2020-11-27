package com.hiveworkshop.rms.ui.application.edit;

import java.awt.event.ActionEvent;
import java.util.NoSuchElementException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

public final class UndoActionImplementation extends AbstractAction {
	private final MainPanel mainPanel;

	public UndoActionImplementation(final String name, final MainPanel mainPanel) {
		super(name);
		this.mainPanel = mainPanel;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final ModelPanel mpanel = mainPanel.currentModelPanel();
		if (mpanel != null) {
			try {
				mpanel.getUndoManager().undo();
			} catch (final NoSuchElementException exc) {
				JOptionPane.showMessageDialog(mainPanel, "Nothing to undo!");
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
		}
		mainPanel.refreshUndo();
		MainPanel.repaintSelfAndChildren(mainPanel);
		mpanel.repaintSelfAndRelatedChildren();
	}
}