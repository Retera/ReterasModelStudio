package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class MultiBonePanel extends BonePanel {
	JButton setAllParent;
	boolean listenForChange = true;

	public MultiBonePanel(final DefaultListModel<BoneShell> existingBonesList, final BoneShellListCellRenderer renderer) {
		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(e -> getImportPanel().setParentMultiBones());
		bone = null;
		existingBones = existingBonesList;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));

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

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
						.addGroup(layout.createSequentialGroup().addComponent(importTypeBox).addComponent(cardPanel)
								.addComponent(setAllParent)))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(importTypeBox)
						.addComponent(cardPanel).addComponent(setAllParent)));
		setLayout(layout);
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
