package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.List;

class MultiBonePanel extends BonePanel {
	private JButton setAllParent;
	private List<IdObjectShell<?>> selectedValuesList;
	private JList<IdObjectShell<?>> donModBoneShellJList;

	public MultiBonePanel(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
		super(mht, "Multiple Selected", renderer);
		donModBoneShellJList = new JList<>(mht.donModBoneShells);
		setLayout(new MigLayout("gap 0"));
		selectedBone = null;
		add(title, "align center, wrap");

		importTypeBox = getImportTypeComboBox();
		add(importTypeBox, "wrap");

		cardPanel = new JPanel(cards);
//		cardPanel.add(new JPanel(), IdObjectShell.ImportType.MOTION_FROM.name());
//		cardPanel.add(getMotionIntoBoneListPane(mht, renderer), IdObjectShell.ImportType.MOTION_FROM.name());
		cardPanel.add(getReciveMotionFromPanel(), IdObjectShell.ImportType.RECEIVE_MOTION.name());
		cardPanel.add(dummyPanel, "blank");

		cards.show(cardPanel, "blank");
		add(cardPanel, "wrap");

		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(e -> setParentMultiBones());
		add(setAllParent, "wrap");
	}

//	private JScrollPane getMotionIntoBoneListPane(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
//		recModMotionDestBoneList = new JList<>(mht.recModBoneShells);
//		recModMotionDestBoneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		recModMotionDestBoneList.setCellRenderer(renderer);
//		recModMotionDestBoneList.setEnabled(false);
//		return new JScrollPane(recModMotionDestBoneList);
//	}

//	protected JPanel getImportMotionIntoPanel() {
//		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
//		importMotionIntoPanel.add(new JLabel("Bones to receive motion"), "wrap");
//		motionDestList = new SearchableList<>(this::idObjectShellNameFilter)
//				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
//				.addListSelectionListener(this::motionDestChosen)
//				.setRenderer(motionReceiveRenderer);
//		importMotionIntoPanel.add(motionDestList.getSearchField(), "grow, wrap");
//		importMotionIntoPanel.add(motionDestList.getScrollableList(), "spanx, growx, growy");
//		return importMotionIntoPanel;
//	}

	protected JPanel getReciveMotionFromPanel() {
		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
		importMotionIntoPanel.add(new JLabel("Bone to receive motion from"), "wrap");
		motionSrcList = new SearchableList<>(this::idObjectShellNameFilter)
				.addSelectionListener(this::motionSrcChosen)
				.setRenderer(motionReceiveRenderer);
		motionSrcList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		importMotionIntoPanel.add(motionSrcList.getSearchField(), "grow, wrap");
		importMotionIntoPanel.add(motionSrcList.getScrollableList(), "spanx, growx, growy");
		return importMotionIntoPanel;
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

	protected void motionSrcChosen(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			IdObjectShell<?> bs = motionSrcList.getSelectedValue();
			if (bs != null) {
				for(IdObjectShell<?> o : selectedValuesList){
					bs.addMotionDest(o);
				}
			}
			motionSrcList.setSelectedValue(null, false);
		}
	}

	@Override
	public void setImportStatus(IdObjectShell.ImportType type) {
		if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.MOTION_FROM) {
			cards.show(cardPanel, IdObjectShell.ImportType.MOTION_FROM.name());
		} else if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.RECEIVE_MOTION) {
			cards.show(cardPanel, IdObjectShell.ImportType.RECEIVE_MOTION.name());
		} else {
			cards.show(cardPanel, "blank");
		}
	}

	@Override
	protected void showImportTypeCard(IdObjectShell.ImportType type) {
		if (type != null) {
			if (type == IdObjectShell.ImportType.MOTION_FROM) {
				cards.show(cardPanel, IdObjectShell.ImportType.MOTION_FROM.name());
				importTypeForAll(IdObjectShell.ImportType.MOTION_FROM);
			} else if (type == IdObjectShell.ImportType.DONT_IMPORT) {
				cards.show(cardPanel, "blank");
				importTypeForAll(IdObjectShell.ImportType.DONT_IMPORT);
			} else if (type == IdObjectShell.ImportType.IMPORT) {
				cards.show(cardPanel, "blank");
				importTypeForAll(IdObjectShell.ImportType.IMPORT);
			}else {
				cards.show(cardPanel, "blank");
			}
		}
	}

	public void setMultiTypes() {
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem(null);
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
