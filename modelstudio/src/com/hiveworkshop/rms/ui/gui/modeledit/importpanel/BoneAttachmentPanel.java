package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.MatrixShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MatrixShellListCellRenderer;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.SearchableTwiList;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

class BoneAttachmentPanel extends JPanel {

	// Old bone refs (matrices)
	private SearchableTwiList<MatrixShell> recModBoneRefsList;
	private final MatrixShellListCellRenderer matrixListCellRenderer = new MatrixShellListCellRenderer();

	// New refs
	private final IterableListModel<IdObjectShell<?>> newRefs = new IterableListModel<>();
	private TwiList<IdObjectShell<?>> newRefsList;

	// Bones (all available -- NEW AND OLD)
	private SearchableTwiList<IdObjectShell<?>> bonesList;

	private final ModelHolderThing mht;

	private GeosetShell selectedGeoset;
	private JCheckBox linkCheckBox;

	public BoneAttachmentPanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		setLayout(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow, 30%:32%:40%]10[grow, 30%:32%:40%]0[][grow, 30%:32%:40%]", "[grow]"));
		this.mht = mht;

		add(getOldBonesPanel(), "growy, growx");
		add(getNewBonesPanel(renderer), "growy, growx");
		add(getUpDownPanel(), "aligny center");
		add(getBonesPanel(renderer), "growy, growx");
	}

	private JPanel getOldBonesPanel() {
		JPanel oldBonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][][grow]"));
		oldBonesPanel.add(new JLabel("Old Bone References"), "wrap");

		recModBoneRefsList = new SearchableTwiList<>(this::matrixShellBoneNameFilter)
				.setRenderer(matrixListCellRenderer)
				.addSelectionListener(e -> refreshLists());
		recModBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		linkCheckBox = new JCheckBox("linked search");
		linkCheckBox.addActionListener(e -> queResetList());

		oldBonesPanel.add(recModBoneRefsList.getSearchField(), "grow, split");
		oldBonesPanel.add(linkCheckBox, "wrap");

		oldBonesPanel.add(recModBoneRefsList.getScrollableList(), "growy, growx");
		return oldBonesPanel;
	}

	private JPanel getUpDownPanel() {
		JPanel upDownPanel = new JPanel(new MigLayout("gap 0 0 0 0"));
		JButton moveUp = new JButton(RMSIcons.moveUpIcon);
		moveUp.addActionListener(e -> moveBone(-1));
		upDownPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(RMSIcons.moveDownIcon);
		moveDown.addActionListener(e -> moveBone(1));
		upDownPanel.add(moveDown, "wrap");
		return upDownPanel;
	}

	private JPanel getNewBonesPanel(BoneShellListCellRenderer renderer) {
		JPanel newBonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][grow][]"));
		newBonesPanel.add(new JLabel("New Refs"), "wrap");

		newRefsList = new TwiList<>();
		newRefsList.setCellRenderer(renderer);
		JScrollPane newRefsPane = new JScrollPane(newRefsList);
		newBonesPanel.add(newRefsPane, "growy, growx, wrap");

		JButton removeNewRef = new JButton("Remove", RMSIcons.redXIcon);
		removeNewRef.addActionListener(e -> removeNewRef());
		newBonesPanel.add(removeNewRef, "alignx center");
		return newBonesPanel;
	}

	private JPanel getBonesPanel(BoneShellListCellRenderer renderer) {
		JPanel bonesPanel = new JPanel(new MigLayout("gap 0 0 0 0, insets 0 0 0 0, fill", "[grow]", "[][][grow][]"));
		bonesPanel.add(new JLabel("Bones"), "wrap");

		bonesList = new SearchableTwiList<>(this::idObjectShellNameFilter).setRenderer(renderer);
		bonesPanel.add(bonesList.getSearchField(), "grow, wrap");
		bonesPanel.add(bonesList.getScrollableList(), "growy, growx, wrap");

		JButton useBone = new JButton("Use Bone(s)", RMSIcons.greenArrowIcon);
		useBone.addActionListener(e -> useBone());
		bonesPanel.add(useBone, "alignx center");
		return bonesPanel;
	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		bonesList.setList(mht.getFutureBoneList());
		recModBoneRefsList.setList(geosetShell.getMatrixShells());
		reloadNewRefsList();
	}

	public GeosetShell getSelectedGeoset() {
		return selectedGeoset;
	}

	private void moveBone(int dir) {
		int[] selected = newRefsList.getSelectedIndices();
		List<IdObjectShell<?>> selectedValuesList = newRefsList.getSelectedValuesList();

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
		for (IdObjectShell<?> bs : newRefsList.getSelectedValuesList()) {
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
			for (IdObjectShell<?> bs : bonesList.getSelectedValuesList()) {
				if (!selectedMatrix.getNewBones().contains(bs)) {
					selectedMatrix.addNewBone(bs);
				}
			}
		}
	}

	public void refreshLists() {
		reloadNewRefsList();
	}

	public void reloadNewRefsList() {
		MatrixShell selectedMatrix = recModBoneRefsList.getSelectedValue();
		if (selectedMatrix != null) {
			newRefsList.setModel(selectedMatrix.getNewBones());
		}
	}

	private void queResetList() {
		if (linkCheckBox.isSelected()) {
			bonesList.setFilterTextConsumer(this::linkSearch);
			recModBoneRefsList.setFilterTextConsumer(this::linkSearch);
		}
	}

	private void linkSearch(String filterText){
		if (!linkCheckBox.isSelected()) {
			bonesList.setFilterTextConsumer(null);
			recModBoneRefsList.setFilterTextConsumer(null);
			filterText = "";
		}

		if (recModBoneRefsList.getSearchField().hasFocus()
				&& !bonesList.getFilterText().equalsIgnoreCase(filterText)) {
			bonesList.setSearch(filterText);
		} else if (bonesList.getSearchField().hasFocus()
				&& !recModBoneRefsList.getFilterText().equalsIgnoreCase(filterText)) {
			recModBoneRefsList.setSearch(filterText);
		}
	}

	private boolean matrixShellBoneNameFilter(MatrixShell matrixShell, String filterText) {
		for (IdObjectShell<?> boneShell : matrixShell.getOrgBones()) {
			if (idObjectShellNameFilter(boneShell, filterText)) {
				return true;
			}
		}
		return false;
	}

	private boolean idObjectShellNameFilter(IdObjectShell<?> boneShell, String filterText) {
		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
