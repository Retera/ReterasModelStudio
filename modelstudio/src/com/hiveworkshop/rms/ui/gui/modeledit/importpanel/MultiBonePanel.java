package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class MultiBonePanel extends BonePanel {
	JButton setAllParent;
	boolean listenForChange = true;
	ModelHolderThing mht;

	public MultiBonePanel(ModelHolderThing mht, final IterableListModel<BoneShell> existingBonesList, final BoneShellListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		bone = null;
		existingBones = existingBonesList;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(e -> selectSimilarOrSomething());
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
		setAllParent.addActionListener(e -> setParentMultiBones());
		add(setAllParent, "wrap");
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

	private void selectSimilarOrSomething() {
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
			setSelectedItem((String) importTypeBox.getSelectedItem());
		}
		final long nanoEnd = System.nanoTime();
		System.out.println("MultiBonePanel.actionPerformed() took " + (nanoEnd - nanoStart) + " ns");
	}

	public void setSelectedItem(final String what) {
		for (BonePanel temp : mht.boneTabs.getSelectedValuesList()) {
			temp.setSelectedValue(what);
		}
	}


	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<BoneShell> list = new JList<>(mht.getFutureBoneListExtended(true));
		list.setCellRenderer(mht.boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(null, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			for (BonePanel temp : mht.boneTabs.getSelectedValuesList()) {
				temp.setParent(list.getSelectedValue());
			}
		}
	}
}
