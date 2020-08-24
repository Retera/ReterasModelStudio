package com.hiveworkshop.rms.ui.gui.modeledit.components;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.editor.model.EditableModel;

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

	public List<String> getCommentContents() {
		return Arrays.asList(textPane.getText().split("\n"));
	}
}
