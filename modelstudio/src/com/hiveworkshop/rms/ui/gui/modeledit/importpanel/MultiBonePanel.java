package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

class MultiBonePanel extends BonePanel {
	private JButton setAllParent;
	private List<IdObjectShell<?>> selectedValuesList;
	private JList<IdObjectShell<?>> donModBoneShellJList;

	public MultiBonePanel(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
		this.mht = mht;
		donModBoneShellJList = new JList<>(mht.donModBoneShells);
		setLayout(new MigLayout("gap 0"));
		selectedBone = null;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		this.renderer = renderer;
		importTypeBox.addItemListener(e -> showImportTypeCard(e));
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		add(importTypeBox, "wrap");

		cardPanel = new JPanel(cards);
		cardPanel.add(getMotionIntoBoneListPane(mht, renderer), "boneList");
		cardPanel.add(dummyPanel, "blank");

		cards.show(cardPanel, "blank");
		add(cardPanel, "wrap");

		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(e -> setParentMultiBones());
		add(setAllParent, "wrap");
	}

	private JScrollPane getMotionIntoBoneListPane(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
		importMotionIntoRecBoneList = new JList<>(mht.recModBoneShells);
		importMotionIntoRecBoneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importMotionIntoRecBoneList.setCellRenderer(renderer);
		importMotionIntoRecBoneList.setEnabled(false);
		return new JScrollPane(importMotionIntoRecBoneList);
	}

	public void setSelectedBones(List<IdObjectShell<?>> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;
		renderer.setSelectedBoneShell(null);
		IdObjectShell.ImportType firstImportStatus = selectedValuesList.get(0).getImportStatus();

		if (selectedValuesList.stream().anyMatch(bs -> bs.getImportStatus() != firstImportStatus)) {
			setMultiTypes();
		} else {
			importTypeBox.setSelectedIndex(firstImportStatus.ordinal());
		}
	}

	@Override
	public void setImportStatus(final int index) {
		if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.MOTION_FROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
	}

	private void showImportTypeCard(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED) {
			if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.MOTION_FROM.dispText) {
				cards.show(cardPanel, "boneList");
				importTypeForAll(IdObjectShell.ImportType.MOTION_FROM);
			} else if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.DONT_IMPORT.dispText) {
				cards.show(cardPanel, "blank");
				importTypeForAll(IdObjectShell.ImportType.DONT_IMPORT);
			} else if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.IMPORT.dispText) {
				cards.show(cardPanel, "blank");
				importTypeForAll(IdObjectShell.ImportType.IMPORT);
			} else {
				cards.show(cardPanel, "blank");
			}
		}
	}

	public void setMultiTypes() {
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem("Multiple selected");
		importTypeBox.setEditable(false);
	}

	public void importTypeForAll(IdObjectShell.ImportType type) {
		for (IdObjectShell boneShell : selectedValuesList) {
			boneShell.setImportStatus(type);
		}
	}


	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<IdObjectShell<?>> list = new JList<>(mht.getFutureBoneHelperList());
		list.setCellRenderer(mht.boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			for (IdObjectShell<?> temp : donModBoneShellJList.getSelectedValuesList()) {
				temp.setNewParentShell(list.getSelectedValue());
			}
		}
	}
}
