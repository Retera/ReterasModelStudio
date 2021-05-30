package com.hiveworkshop.rms.ui.gui.modeledit.listener;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClonedNodeNamePicker {
	private final MainPanel mainPanel;

	public ClonedNodeNamePicker(final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public Map<IdObject, String> pickNames(final Collection<IdObject> clonedNodes) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		final Map<JTextField, IdObject> textFieldToObject = new HashMap<>();
		for (final IdObject object : clonedNodes) {
			final JTextField textField = new JTextField(object.getName() + " copy");
			final JLabel oldNameLabel = new JLabel("Enter name for clone of \"" + object.getName() + "\":");
			panel.add(oldNameLabel);
			panel.add(textField);
			textFieldToObject.put(textField, object);
		}
		final JPanel dumbPanel = new JPanel();
		dumbPanel.add(panel);
		final JScrollPane scrollPane = new JScrollPane(dumbPanel);
		scrollPane.setPreferredSize(new Dimension(450, 300));
		final int x = JOptionPane.showConfirmDialog(mainPanel, scrollPane, "Choose Node Names",
				JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return null;
		}
		final Map<IdObject, String> objectToName = new HashMap<>();
		for (final JTextField field : textFieldToObject.keySet()) {
			final IdObject idObject = textFieldToObject.get(field);
			objectToName.put(idObject, field.getText());
		}
		return objectToName;
	}
}
