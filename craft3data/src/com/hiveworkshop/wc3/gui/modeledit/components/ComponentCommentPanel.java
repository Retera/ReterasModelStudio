package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.EditableModel;

public class ComponentCommentPanel extends JPanel implements ComponentPanel {
	private final JTextPane textPane;

	public ComponentCommentPanel() {
		textPane = new JTextPane();
		setLayout(new BorderLayout());
		add(textPane, BorderLayout.CENTER);
	}

	public void setCommentContents(final Iterable<String> headerComment) {
		final StringBuilder sb = new StringBuilder();
		for (final String line : headerComment) {
			sb.append(line);
			sb.append('\n');
		}
		textPane.setText(sb.toString());
	}

	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
			final ModelStructureChangeListener changeListener) {
		model.setHeader(getCommentContents());
	}

	public ArrayList<String> getCommentContents() {
		return new ArrayList<>(Arrays.asList(textPane.getText().split("\n")));
	}
}
