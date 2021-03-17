package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The panel to handle re-assigning Matrices.
 *
 * Eric Theller 6/11/2012
 */

public class MatrixPopup extends JPanel {

	// New refs
	public IterableListModel<BoneShell> newRefs;
	JList<BoneShell> newRefsJList;

	// Bones (all available -- NEW AND OLD)
	IterableListModel<BoneShell> bones;
	IterableListModel<BoneShell> filteredBones = new IterableListModel<>();
	JList<BoneShell> bonesList;
	JTextField boneSearch;

	EditableModel model;

	public MatrixPopup(final EditableModel model) {
		setLayout(new MigLayout("gap 0", "[grow][][grow]", "[align center][grow][align center]"));
		this.model = model;

		final ModelView disp = new ModelViewManager(model);
		BoneShellListCellRenderer renderer = new BoneShellListCellRenderer(disp, null).setShowClass(false);

		JCheckBox displayParents = new JCheckBox("Display parents", false);
		displayParents.addActionListener(e -> showParents(renderer, displayParents));


		JPanel bonePanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][][grow][]"));
		bonePanel.add(new JLabel("Bones"), "wrap");

		boneSearch = new JTextField();
		boneSearch.addCaretListener(e -> filterBones());
		bonePanel.add(boneSearch, "growx, wrap");

		// Built before oldBoneRefs, so that the MatrixShells can default to using New Refs with the same name as their first bone
		buildBonesList();

		bonesList = new JList<>(bones);
		bonesList.setCellRenderer(renderer);
		JScrollPane bonesPane = new JScrollPane(bonesList);
		bonesPane.setPreferredSize(new Dimension(400, 500));

		bonePanel.add(bonesPane, "wrap");

		JButton useBone = new JButton("Use Bone(s)", ImportPanel.greenArrowIcon);
		useBone.addActionListener(e -> useBone());

		bonePanel.add(useBone, "wrap");

		JPanel newRefsPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][grow][]"));
		newRefsPanel.add(new JLabel("New Refs"), "wrap");

		newRefs = new IterableListModel<>();
		newRefsJList = new JList<>(newRefs);
		newRefsJList.setCellRenderer(renderer);
		JScrollPane newRefsPane = new JScrollPane(newRefsJList);
		newRefsPane.setPreferredSize(new Dimension(400, 500));

		newRefsPanel.add(newRefsPane, "wrap");

		JButton removeNewRef = new JButton("Remove", ImportPanel.redXIcon);
		removeNewRef.addActionListener(e -> removeNewRef());

		newRefsPanel.add(removeNewRef, "wrap");

		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0", "[]5", "[align center]16[align center]"));

		JButton moveUp = new JButton(ImportPanel.moveUpIcon);
		moveUp.addActionListener(e -> moveUp());
		arrowPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(e -> moveDown());
		arrowPanel.add(moveDown, "wrap");

		add(displayParents, "wrap, spanx, align center");
		add(newRefsPanel);
		add(arrowPanel);
		add(bonePanel, "wrap");

	}

	private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox) {
		renderer.setShowParent(checkBox.isSelected());
		repaint();
	}


	private void moveDown() {
		final int[] indices = newRefsJList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			if (indices[indices.length - 1] < newRefs.size() - 1) {
				for (int i = indices.length - 1; i >= 0; i--) {
					final BoneShell bs = newRefs.get(indices[i]);
					newRefs.removeElement(bs);
					newRefs.add(indices[i] + 1, bs);
					indices[i] += 1;
				}
			}
			newRefsJList.setSelectedIndices(indices);
		}
	}

	private void moveUp() {
		final int[] indices = newRefsJList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			if (indices[0] > 0) {
				for (int i = 0; i < indices.length; i++) {
					final BoneShell bs = newRefs.get(indices[i]);
					newRefs.removeElement(bs);
					newRefs.add(indices[i] - 1, bs);
					indices[i] -= 1;
				}
			}
			newRefsJList.setSelectedIndices(indices);
		}
	}

	private void removeNewRef() {
		newRefs.removeAll(newRefsJList.getSelectedValuesList());
	}

	private void filterBones() {
		String filterText = boneSearch.getText();
		if (!filterText.equals("")) {
			filteredBones.clear();
			for (BoneShell boneShell : bones) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredBones.addElement(boneShell);
				}
			}
			bonesList.setModel(filteredBones);
		} else {
			bonesList.setModel(bones);
		}
	}

	private void useBone() {
		for (final Object o : bonesList.getSelectedValuesList()) {
			if (!newRefs.contains(o)) {
				newRefs.addElement((BoneShell) o);
			}
		}
	}

	public void buildBonesList() {
		bones = new IterableListModel<>();
		final List<Bone> modelBones = model.getBones();
		for (final Bone b : modelBones) {
			bones.addElement(new BoneShell(b));
		}
	}

	public List<Bone> getNewBoneList() {
		final List<Bone> bones = new ArrayList<>();
		for (final BoneShell bs : newRefs) {
			bones.add(bs.getBone());
		}
		return bones;
	}
}