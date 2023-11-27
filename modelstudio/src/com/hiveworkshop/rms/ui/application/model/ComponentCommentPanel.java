package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.EditCommentAction;
import com.hiveworkshop.rms.ui.application.model.editors.TwiFocusListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ComponentCommentPanel extends ComponentPanel<List<String>> {
	private final JTextPane textPane;

	public ComponentCommentPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel, new BorderLayout());

		textPane = new JTextPane();
		add(textPane, BorderLayout.CENTER);

		textPane.addFocusListener(new TwiFocusListener(textPane, this::editComment).setLastEditedExtend(20000));
	}

	private void editComment() {
		List<String> strings = Arrays.asList(textPane.getText().split("\n"));
		if (!model.getComments().equals(strings)) {
			final EditCommentAction editCommentAction = new EditCommentAction(strings, model, changeListener);
			undoManager.pushAction(editCommentAction.redo());
		}

	}

	@Override
	public ComponentPanel<List<String>> setSelectedItem(final List<String> headerComment) {

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
}
