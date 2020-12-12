package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ComponentCommentPanel extends JPanel implements ComponentPanel<String> {
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
	public void setSelectedItem(String itemToSelect) {

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
