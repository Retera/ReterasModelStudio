package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.util.List;
import java.util.function.Consumer;

class BoneAttachmentPanel extends JPanel {
	JLabel title;

	// Old bone refs (matrices)
	JList<MatrixShell> recModBoneRefsList;
	IterableListModel<MatrixShell> recModMatrixShells;
	IterableListModel<MatrixShell> filteredRecModMatrixShells = new IterableListModel<>();

	// New refs
	IterableListModel<BoneShell> newRefs = new IterableListModel<>();
	JList<BoneShell> newRefsList;

	// Bones (all available -- NEW AND OLD)
	IterableListModel<BoneShell> futureBones;
	IterableListModel<BoneShell> filteredFutureBones = new IterableListModel<>();
	JList<BoneShell> bonesList;
	JScrollPane bonesPane;

	ModelHolderThing mht;

	GeosetShell selectedGeoset;
	JCheckBox linkBox;

	JTextField leftSearchField;
	JTextField rightSearchField;
	boolean listResetQued = false;

	public BoneAttachmentPanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		setLayout(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow, 30%:32%:40%]10[grow, 30%:32%:40%]0[][grow, 30%:32%:40%]", "[grow]"));
		this.mht = mht;

		JPanel oldBonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][][grow]"));
		oldBonesPanel.add(new JLabel("Old Bone References"), "wrap");

		leftSearchField = new JTextField();
		rightSearchField = new JTextField();

		leftSearchField.addCaretListener(getCaretListener(leftSearchField, rightSearchField, this::setRecModMatrixFilter));

		rightSearchField.addCaretListener(getCaretListener(rightSearchField, leftSearchField, this::setFutureBonesFilter));

		linkBox = new JCheckBox("linked search");
		linkBox.addActionListener(e -> queResetList());

		oldBonesPanel.add(leftSearchField, "grow, split");
		oldBonesPanel.add(linkBox, "wrap");


		recModBoneRefsList = new JList<>();
		recModBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		oldBoneRefsList.setCellRenderer(renderer);
//		oldBoneRefsList.setCellRenderer(new MatrixShell2DListCellRenderer(new ModelViewManager(impPanel.currentModel), new ModelViewManager(impPanel.importedModel)));
		recModBoneRefsList.addListSelectionListener(e -> refreshLists());
		JScrollPane oldBoneRefsPane = new JScrollPane(recModBoneRefsList);
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
		moveUp.addActionListener(e -> moveBone(-1));
		upDownPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(e -> moveBone(1));
		upDownPanel.add(moveDown, "wrap");

		add(upDownPanel, "aligny center");


		JPanel bonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][][grow][]"));
		bonesPanel.add(new JLabel("Bones"), "wrap");

		bonesPanel.add(rightSearchField, "grow, wrap");

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
		futureBones = mht.getFutureBoneList();
		bonesList.setModel(futureBones);
		recModMatrixShells = geosetShell.getMatrixShells();
		recModBoneRefsList.setModel(recModMatrixShells);
		reloadNewRefsList();
	}

	private void moveBone(int dir) {
		int[] selected = newRefsList.getSelectedIndices();
		List<BoneShell> selectedValuesList = newRefsList.getSelectedValuesList();

		int size = selectedValuesList.size();

		int start = Math.max(0, ((size - 1) * dir)); // moving down needs to start from bottom

		for (int i = 0; i < size; i++) {
			int index = start - (i * dir);
			selected[index] = recModBoneRefsList.getSelectedValue().moveBone(selectedValuesList.get(index), dir);
		}
		newRefsList.setSelectedIndices(selected);

	}

	private void removeNewRef() {
		int i = newRefsList.getSelectedIndex() - newRefsList.getSelectedValuesList().size();
		for (BoneShell bs : newRefsList.getSelectedValuesList()) {
			recModBoneRefsList.getSelectedValue().removeNewBone(bs);
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
		MatrixShell selectedMatrix = recModBoneRefsList.getSelectedValue();
		if (selectedMatrix != null) {
			for (BoneShell bs : bonesList.getSelectedValuesList()) {
				if (!selectedMatrix.getNewBones().contains(bs)) {
					selectedMatrix.addNewBone(bs);
				}
			}
		}
	}

	public void refreshLists() {
		futureBones = mht.getFutureBoneList();
		reloadNewRefsList();
	}

	public void reloadNewRefsList() {
		MatrixShell selectedMatrix = recModBoneRefsList.getSelectedValue();
		if (selectedMatrix != null) {
			newRefsList.setModel(selectedMatrix.getNewBones());
		}
	}

	private void queResetList() {
		if (!linkBox.isSelected()) {
			listResetQued = true;
		}
	}

	private CaretListener getCaretListener(JTextField activeSearchField, JTextField inActiveSearchField, Consumer<String> listModelFunction) {
		return e -> {
			if (linkBox.isSelected()) {
				inActiveSearchField.setText(activeSearchField.getText());
			}
			String filterText = activeSearchField.getText();
			listModelFunction.accept(filterText);
		};
	}

	private void setFutureBonesFilter(String filterText) {
		applyFutureBonesListModel(filterText);
		if (linkBox.isSelected()) {
			applyRecModMatrixFilteredListModel(filterText);
		} else if (listResetQued) {
			listResetQued = false;
			setImportIntoListModel(recModMatrixShells);
			leftSearchField.setText("");
		}
	}

	private void applyFutureBonesListModel(String filterText) {
		if (!filterText.equals("")) {
			filteredFutureBones.clear();
			for (BoneShell boneShell : futureBones) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredFutureBones.addElement(boneShell);
				}
			}
			setFutureBoneListModel(filteredFutureBones);
		} else {
			setFutureBoneListModel(futureBones);
		}
	}

	private void setRecModMatrixFilter(String filterText) {
		applyRecModMatrixFilteredListModel(filterText);
		if (linkBox.isSelected()) {
			applyFutureBonesListModel(filterText);
		} else if (listResetQued) {
			listResetQued = false;
			setFutureBoneListModel(futureBones);
			rightSearchField.setText("");
		}
	}

	private void applyRecModMatrixFilteredListModel(String filterText) {
		if (!filterText.equals("")) {
			filteredRecModMatrixShells.clear();
			for (MatrixShell matrixShell : recModMatrixShells) {
				for (BoneShell boneShell : matrixShell.getOrgBones()){
					if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
						filteredRecModMatrixShells.addElement(matrixShell);
						break;
					}
				}
			}
			setImportIntoListModel(filteredRecModMatrixShells);
		} else {
			setImportIntoListModel(recModMatrixShells);
		}
	}

	public void setFutureBoneListModel(IterableListModel<BoneShell> model) {
		bonesList.setModel(model);
	}


	private void setImportIntoListModel(IterableListModel<MatrixShell> model) {
		recModBoneRefsList.setModel(model);
	}
}
