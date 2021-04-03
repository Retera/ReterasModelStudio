package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MultiBonePanel extends BonePanel {
	JButton setAllParent;
	ModelHolderThing mht;

	BoneShellMotionListCellRenderer oneShellRenderer;

	public MultiBonePanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		BoneShellMotionListCellRenderer oneShellRenderer = new BoneShellMotionListCellRenderer(mht.recModelManager, mht.donModelManager);
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		selectedBone = null;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		importTypeBox.addActionListener(e -> showImportTypeCard());
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		add(importTypeBox, "wrap");

		importMotionIntoRecBoneList = new JList<>(mht.recModBoneShells);
		importMotionIntoRecBoneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importMotionIntoRecBoneList.setCellRenderer(renderer);
		JScrollPane boneListPane = new JScrollPane(importMotionIntoRecBoneList);

		cardPanel = new JPanel(cards);
		cardPanel.add(boneListPane, "boneList");
		cardPanel.add(dummyPanel, "blank");
		importMotionIntoRecBoneList.setEnabled(false);

		cards.show(cardPanel, "blank");
		add(cardPanel, "wrap");

		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(e -> setParentMultiBones());
		add(setAllParent, "wrap");
	}

	public void updateMultiBonePanel(){
		List<BoneShell> selectedValuesList = mht.donModBoneShellJList.getSelectedValuesList();

		BoneShell.ImportType firstImportStatus = selectedValuesList.get(0).getImportStatus();

		if (selectedValuesList.stream().anyMatch(bs -> bs.getImportStatus() != firstImportStatus)) {
			setMultiTypes();
		} else {
			importTypeBox.setSelectedIndex(firstImportStatus.ordinal());
		}
	}

	@Override
	public void setSelectedIndex(final int index) {
		if (importTypeBox.getSelectedItem() == BoneShell.ImportType.MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
	}

	private void showImportTypeCard() {
		if (importTypeBox.getSelectedItem() == BoneShell.ImportType.MOTIONFROM) {
			cards.show(cardPanel, "boneList");
			importTypeForAll();
		} else if (importTypeBox.getSelectedItem() == BoneShell.ImportType.DONTIMPORT) {
			cards.show(cardPanel, "blank");
			importTypeForAll();
		} else if (importTypeBox.getSelectedItem() == BoneShell.ImportType.IMPORT) {
			cards.show(cardPanel, "blank");
			importTypeForAll();
		} else {
			cards.show(cardPanel, "blank");
		}
	}

	public void setMultiTypes() {
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem("Multiple selected");
		importTypeBox.setEditable(false);
	}

	public void importTypeForAll() {
		for (BoneShell temp : mht.donModBoneShellJList.getSelectedValuesList()) {
			temp.setImportStatus(importTypeBox.getSelectedIndex());
		}
	}


	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<BoneShell> list = new JList<>(mht.getFutureBoneListExtended(true));
		list.setCellRenderer(mht.boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			for (BoneShell temp : mht.donModBoneShellJList.getSelectedValuesList()) {
				temp.setNewParentBs(list.getSelectedValue());
			}
		}
	}
}
