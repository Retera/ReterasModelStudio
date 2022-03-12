package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.ActionEvent;

public class DocumentUndoManager extends UndoManager {
	private final JButton undoButton;
	private final JButton redoButton;
	private final AbstractAction undoAction;
	private final AbstractAction redoAction;

	public DocumentUndoManager(){
		KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
		undoAction = createUndoAction();
		undoAction.putValue(Action.ACCELERATOR_KEY, keyBindingPrefs.getKeyStroke(TextKey.UNDO));
		redoAction = createRedoAction();
		redoAction.putValue(Action.ACCELERATOR_KEY, keyBindingPrefs.getKeyStroke(TextKey.REDO));
		undoButton = new JButton(undoAction);
		redoButton = new JButton(redoAction);
		update();
	}
	public DocumentUndoManager(boolean isTesting){
		undoAction = createUndoAction();
		undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
		redoAction = createRedoAction();
		redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
		undoButton = new JButton(undoAction);
		redoButton = new JButton(redoAction);
		update();
	}

	public void update(){
		redoButton.setEnabled(canRedo());
		System.out.println("isInProgress: " + isInProgress());
		System.out.println("canUndo: " + canUndo());
		System.out.println("editToBeUndone: " + editToBeUndone());
		System.out.println("edits: " + this.edits);
		undoButton.setEnabled(canUndo());
	}

	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean b = super.addEdit(anEdit);
		System.out.println("edit");
		update();
		return b;
	}

	public JButton getUndoButton() {
		return undoButton;
	}

	public JButton getRedoButton() {
		return redoButton;
	}

	public AbstractAction getUndoAction(){
		return undoAction;
	}
	public AbstractAction getRedoAction(){
		return redoAction;
	}

	private AbstractAction createUndoAction(){
//		return new AbstractAction(TextKey.UNDO.toString()){
		return new AbstractAction(TextKey.UNDO.getDefaultTranslation()){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canUndo()) {
					undo();
					update();
				}
			}
		};
	}

	private AbstractAction createRedoAction(){
//		return new AbstractAction(TextKey.REDO.toString()){
		return new AbstractAction(TextKey.REDO.getDefaultTranslation()){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canRedo()) {
					redo();
					update();
				}
			}
		};
	}
}
