package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.actions.model.EditCommentAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentCommentPanel extends ComponentPanel<List<String>> {
	private final JTextPane textPane;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener changeListener;

	public ComponentCommentPanel(ModelHandler modelHandler,
	                             ModelStructureChangeListener changeListener) {
		this.modelHandler = modelHandler;
		this.changeListener = changeListener;

		textPane = new JTextPane();
		setLayout(new BorderLayout());
		add(textPane, BorderLayout.CENTER);
		textPane.addFocusListener(commentEditListener());
	}

//	private void commentEdited(){
//		final EditCommentAction editCommentAction = new EditCommentAction("ugg", "ugg", modelViewManager, changeListener);
//		editCommentAction.redo();
//		undoActionListener.pushAction(editCommentAction);
//	}

	// The type of this listener should maybe be something else, but still something that doesn't fire on every keypress
	private FocusListener commentEditListener() {
		return new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				final EditCommentAction editCommentAction = new EditCommentAction("ugg", "ugg", modelHandler.getModelView(), changeListener);
				editCommentAction.redo();
				modelHandler.getUndoManager().pushAction(editCommentAction);
			}
		};
	}

	@Override
	public void setSelectedItem(final List<String> headerComment) {
//		System.out.println(headerComment);

		final StringBuilder sb = new StringBuilder();
		for (final String line : headerComment) {
			sb.append(line);
			sb.append('\n');
		}
		textPane.setText(sb.toString());
		revalidate();
		repaint();
	}

	@Override
	public void save(final EditableModel model, final UndoManager undoManager,
	                 final ModelStructureChangeListener changeListener) {
//		model.setHeader(getCommentContents());
	}

	public ArrayList<String> getCommentContents() {
		String[] text = textPane.getText().split("\n");
		return new ArrayList<>(Arrays.asList(text));
	}
}
