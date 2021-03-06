package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

class BoneAttachmentPanel extends JPanel {
	JLabel title;

	// Old bone refs (matrices)
	JList<MatrixShell> oldBoneRefsList;

	// New refs
	IterableListModel<BoneShell> newRefs = new IterableListModel<>();
	JList<BoneShell> newRefsList;

	// Bones (all available -- NEW AND OLD)
	IterableListModel<BoneShell> bones;
	JList<BoneShell> bonesList;
	JScrollPane bonesPane;

	MatrixShell currentMatrix = null;
	ModelHolderThing mht;

	GeosetShell selectedGeoset;

	public BoneAttachmentPanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		setLayout(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow][grow]0[][grow]", "[grow]"));
		this.mht = mht;

		JPanel oldBonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][grow]"));
		oldBonesPanel.add(new JLabel("Old Bone References"), "wrap");


		oldBoneRefsList = new JList<>();
		oldBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		oldBoneRefsList.setCellRenderer(renderer);
//		oldBoneRefsList.setCellRenderer(new MatrixShell2DListCellRenderer(new ModelViewManager(impPanel.currentModel), new ModelViewManager(impPanel.importedModel)));
		oldBoneRefsList.addListSelectionListener(e -> refreshLists());
		JScrollPane oldBoneRefsPane = new JScrollPane(oldBoneRefsList);
		oldBonesPanel.add(oldBoneRefsPane, "growy, growx");

		add(oldBonesPanel, "growy, growx");

		JPanel newBonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][grow][]"));
		newBonesPanel.add(new JLabel("New Refs"), "wrap");


		newRefsList = new JList<>();
		newRefsList.setCellRenderer(renderer);
		JScrollPane newRefsPane = new JScrollPane(newRefsList);
		newBonesPanel.add(newRefsPane, "growy, growx, wrap");

		JButton removeNewRef = new JButton("Remove", ImportPanel.redXIcon);
		removeNewRef.addActionListener(e -> removeNewRef());
		newBonesPanel.add(removeNewRef, "alignx center");

		add(newBonesPanel, "growy, growx");


		JPanel upDownPanel = new JPanel(new MigLayout("gap 0 0 0 0"));
		JButton moveUp = new JButton(ImportPanel.moveUpIcon);
//		moveUp.addActionListener(e -> moveUp());
		moveUp.addActionListener(e -> moveBone(-1));
		upDownPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(ImportPanel.moveDownIcon);
//		moveDown.addActionListener(e -> moveDown());
		moveDown.addActionListener(e -> moveBone(1));
		upDownPanel.add(moveDown, "wrap");

		add(upDownPanel, "aligny center");


		JPanel bonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "", "[][grow][]"));
		bonesPanel.add(new JLabel("Bones"), "wrap");

		// Built before oldBoneRefs, so that the MatrixShells can default to using New Refs with the same name as their first bone
		bonesList = new JList<>();
		bonesList.setCellRenderer(renderer);
		bonesPane = new JScrollPane(bonesList);

		bonesPanel.add(bonesPane, "growy, growx, wrap");

		JButton useBone = new JButton("Use Bone(s)", ImportPanel.greenArrowIcon);
		useBone.addActionListener(e -> useBone());
		bonesPanel.add(useBone, "alignx center");
		add(bonesPanel, "growy, growx");


	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		bones = mht.getFutureBoneList();
		bonesList.setModel(bones);
		oldBoneRefsList.setModel(geosetShell.getMatrixShells());
		reloadNewRefsList();
	}

	private void moveBone(int dir) {
		int[] selected = newRefsList.getSelectedIndices();
		List<BoneShell> selectedValuesList = newRefsList.getSelectedValuesList();

		int size = selectedValuesList.size();

		int start = Math.max(0, ((size - 1) * dir)); // moving down needs to start from bottom

		for (int i = 0; i < size; i++) {
			int index = start - (i * dir);
			selected[index] = oldBoneRefsList.getSelectedValue().moveBone(selectedValuesList.get(index), dir);
		}
		newRefsList.setSelectedIndices(selected);

	}

	private void removeNewRef() {
		int i = newRefsList.getSelectedIndex() - newRefsList.getSelectedValuesList().size();
		for (BoneShell bs : newRefsList.getSelectedValuesList()) {
			oldBoneRefsList.getSelectedValue().removeNewBone(bs);
		}
		if (i >= (newRefs.size())) {
			i = newRefs.size() - 1;
		} else if (i < 0) {
			i = 0;
		}
		newRefsList.setSelectedIndex(i);
		reloadNewRefsList();
	}

	private void useBone() {
		MatrixShell selectedMatrix = oldBoneRefsList.getSelectedValue();
		if (selectedMatrix != null) {
			for (BoneShell bs : bonesList.getSelectedValuesList()) {
				if (!selectedMatrix.getNewBones().contains(bs)) {
					selectedMatrix.addNewBone(bs);
				}
			}
		}
	}

	public void refreshLists() {
		bones = mht.getFutureBoneList();
		reloadNewRefsList();
	}

	public void reloadNewRefsList() {
		MatrixShell selectedMatrix = oldBoneRefsList.getSelectedValue();
		if (selectedMatrix != null) {
			newRefsList.setModel(selectedMatrix.getNewBones());
		}
	}
}
