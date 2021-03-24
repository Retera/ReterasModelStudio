package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

class MultiAnimPanel extends AnimPanel {
	ModelHolderThing mht;

	JComboBox<String> importTypeBox = new JComboBox<>(AnimShell.ImportType.getDispList());

	public MultiAnimPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		selectedAnim = null;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addActionListener(e -> setInReverse());
//		inReverse.setEnabled(false);
		add(inReverse, "left, wrap");

		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this::showCorrectCard);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
//		importTypeBox.setEnabled(false);
		add(importTypeBox, "wrap");
	}

	private void setInReverse() {
		for (AnimShell animShell : mht.animJList.getSelectedValuesList()) {
			animShell.setReverse(inReverse.isSelected());
		}
	}

	public void updateMultiAnimPanel() {
		List<AnimShell> selectedValuesList = mht.animJList.getSelectedValuesList();

		AnimShell.ImportType firstImportStatus = selectedValuesList.get(0).getImportType();

		if (selectedValuesList.stream().anyMatch(as -> as.getImportType() != firstImportStatus)) {
			setMultiTypes();
		} else {
			importTypeBox.setSelectedIndex(firstImportStatus.ordinal());
		}
	}

	public void setMultiTypes() {
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem("Multiple selected");
		importTypeBox.setEditable(false);
	}

	private void showCorrectCard(ItemEvent e) {
		System.out.println("StateChange: " + e.getStateChange() + ", selected Index: " + importTypeBox.getSelectedIndex());
		for (AnimShell animShell : mht.animJList.getSelectedValuesList()) {
			animShell.setImportType(importTypeBox.getSelectedIndex());
		}
	}

}
