package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.EditCommentAction;
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

	public ComponentCommentPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);

		textPane = new JTextPane();
		setLayout(new BorderLayout());
		add(textPane, BorderLayout.CENTER);
		textPane.addFocusListener(commentEditListener());
	}

	// The type of this listener should maybe be something else, but still something that doesn't fire on every keypress
	private FocusListener commentEditListener() {
		return new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				final EditCommentAction editCommentAction = new EditCommentAction(textPane.getText(), model, changeListener);
				undoManager.pushAction(editCommentAction.redo());
			}
		};
	}

	@Override
	public ComponentPanel<List<String>> setSelectedItem(final List<String> headerComment) {
//		System.out.println(headerComment);

		final StringBuilder sb = new StringBuilder();
		for (final String line : headerComment) {
			sb.append(line);
			sb.append('\n');
		}
		textPane.setText(sb.toString());
		revalidate();
		repaint();
		return this;
	}

	public ArrayList<String> getCommentContents() {
		String[] text = textPane.getText().split("\n");
		return new ArrayList<>(Arrays.asList(text));
	}
}
