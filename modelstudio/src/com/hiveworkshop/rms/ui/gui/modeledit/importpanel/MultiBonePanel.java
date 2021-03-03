package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class MultiBonePanel extends BonePanel {
	JButton setAllParent;
	boolean listenForChange = true;

	public MultiBonePanel(final IterableListModel<BoneShell> existingBonesList, final BoneShellListCellRenderer renderer) {
		setLayout(new MigLayout("gap 0"));
		bone = null;
		existingBones = existingBonesList;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		add(importTypeBox, "wrap");

		boneList = new JList<>(existingBones);
		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boneList.setCellRenderer(renderer);
		boneListPane = new JScrollPane(boneList);

		add(importTypeBox);
		add(boneListPane);
		cardPanel = new JPanel(cards);
		cardPanel.add(boneListPane, "boneList");
		cardPanel.add(dummyPanel, "blank");
		boneList.setEnabled(false);
		cards.show(cardPanel, "blank");
		add(cardPanel, "wrap");

		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(e -> getImportPanel().setParentMultiBones());
		add(setAllParent, "wrap");
	}

	@Override
	public ImportPanel getImportPanel() {
		Container temp = getParent();
		while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
			temp = temp.getParent();
		}
		return (ImportPanel) temp;
	}

	@Override
	public void setSelectedIndex(final int index) {
		listenForChange = false;
		importTypeBox.setSelectedIndex(index);
		listenForChange = true;
	}

	@Override
	public void setSelectedValue(final String value) {
		listenForChange = false;
		importTypeBox.setSelectedItem(value);
		listenForChange = true;
	}

	public void setMultiTypes() {
		listenForChange = false;
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem("Multiple selected");
		importTypeBox.setEditable(false);
		// boneListPane.setVisible(false);
		// boneList.setVisible(false);
		cards.show(cardPanel, "blank");
		revalidate();
		listenForChange = true;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final long nanoStart = System.nanoTime();
		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
		if (listenForChange) {
			getImportPanel().setSelectedItem((String) importTypeBox.getSelectedItem());
		}
		final long nanoEnd = System.nanoTime();
		System.out.println("MultiBonePanel.actionPerformed() took " + (nanoEnd - nanoStart) + " ns");
	}
}
