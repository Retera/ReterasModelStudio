package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.RedoActionImplementation;
import com.hiveworkshop.rms.ui.application.edit.UndoActionImplementation;

import javax.swing.*;

public class UndoHandler {
	UndoMenuItem undo;
	RedoMenuItem redo;

	AbstractAction undoAction;
	AbstractAction redoAction;

	public UndoHandler(MainPanel mainPanel) {
		undoAction = new UndoActionImplementation("Undo", mainPanel);
		redoAction = new RedoActionImplementation("Redo", mainPanel);

		undo = new UndoMenuItem(mainPanel, "Undo");
		undo.addActionListener(undoAction);
		undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
		undo.setEnabled(undo.funcEnabled());

		redo = new RedoMenuItem(mainPanel, "Redo");
		redo.addActionListener(redoAction);
		redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
		redo.setEnabled(redo.funcEnabled());
	}

	public void refreshUndo() {
		undo.setEnabled(undo.funcEnabled());
		redo.setEnabled(redo.funcEnabled());
	}

	public UndoMenuItem getUndo() {
		return undo;
	}

	public RedoMenuItem getRedo() {
		return redo;
	}

	public AbstractAction getUndoAction() {
		return undoAction;
	}

	public AbstractAction getRedoAction() {
		return redoAction;
	}

	static class UndoMenuItem extends JMenuItem {
		private final MainPanel mainPanel;

		public UndoMenuItem(MainPanel mainPanel, final String text) {
			super(text);
			this.mainPanel = mainPanel;
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Undo " + ProgramGlobals.getCurrentModelPanel().getUndoManager().getUndoText();// +"
				// Ctrl+Z";
			} else {
				return "Can't undo";// +" Ctrl+Z";
			}
		}

		public boolean funcEnabled() {
			try {
				return !ProgramGlobals.getCurrentModelPanel().getUndoManager().isUndoListEmpty();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}

	static class RedoMenuItem extends JMenuItem {
		private final MainPanel mainPanel;

		public RedoMenuItem(MainPanel mainPanel, final String text) {
			super(text);
			this.mainPanel = mainPanel;
		}

		@Override
		public String getText() {
			if (funcEnabled()) {
				return "Redo " + ProgramGlobals.getCurrentModelPanel().getUndoManager().getRedoText();// +"
				// Ctrl+Y";
			} else {
				return "Can't redo";// +" Ctrl+Y";
			}
		}

		public boolean funcEnabled() {
			try {
				return !ProgramGlobals.getCurrentModelPanel().getUndoManager().isRedoListEmpty();
			} catch (final NullPointerException e) {
				return false;
			}
		}
	}
}
