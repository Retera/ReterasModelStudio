package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MatrixEditListRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The panel to handle re-assigning Matrices.
 *
 * Eric Theller 6/11/2012
 */

public class MatrixPopup extends JPanel {

	// New refs
	public IterableListModel<BoneShell> newRefs = new IterableListModel<>();
	JList<BoneShell> newRefsJList;

	// Bones (all available -- NEW AND OLD)
	IterableListModel<BoneShell> bones;
	IterableListModel<BoneShell> filteredBones = new IterableListModel<>();
	JList<BoneShell> bonesList;
	JTextField boneSearch;

	EditableModel model;

	Set<Bone> bonesInAll = new HashSet<>();
	Set<Bone> allBones = new HashSet<>();
	Set<BoneShell> bonesShellInAll = new HashSet<>();
	Set<BoneShell> bonesShellNotInAll = new HashSet<>();
	Set<BoneShell> allBonesShell = new HashSet<>();
	MatrixEditListRenderer renderer;

	public MatrixPopup(ModelHandler modelHandler) {
		setLayout(new MigLayout("gap 0", "[grow][][grow]", "[align center][grow][align center]"));
		this.model = modelHandler.getModel();

		renderer = new MatrixEditListRenderer(model, null).setShowClass(false);

		JCheckBox displayParents = new JCheckBox("Display parents", false);
		displayParents.addActionListener(e -> showParents(renderer, displayParents));


		JPanel bonePanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][][grow][]"));
		bonePanel.add(new JLabel("Bones"), "wrap");

		boneSearch = new JTextField();
		boneSearch.addCaretListener(e -> filterBones());
		bonePanel.add(boneSearch, "growx, wrap");

		// Built before oldBoneRefs, so that the MatrixShells can default to using New Refs with the same name as their first bone
		buildBonesList();
		getBoneLists(modelHandler.getModelView());

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

	private void showParents(MatrixEditListRenderer renderer, JCheckBox checkBox) {
		renderer.setShowParent(checkBox.isSelected());
		repaint();
	}

	private void getBoneLists(ModelView modelView) {
		Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
		if (!selectedVertices.isEmpty()) {
			bonesInAll.addAll(selectedVertices.stream().findFirst().get().getBones());
			for (GeosetVertex vertex : selectedVertices) {
				allBones.addAll(vertex.getBones());
				bonesInAll.removeIf(b -> !vertex.getBones().contains(b));
			}
		}

		for (BoneShell boneShell : bones) {
			if (allBones.contains(boneShell.getBone())) {
				newRefs.addElement(boneShell);
				allBonesShell.add(boneShell);
				if (bonesInAll.contains(boneShell.getBone())) {
//					renderer.addInAllBone(boneShell);
					bonesShellInAll.add(boneShell);
				}
			}
		}

		bonesShellNotInAll.addAll(allBonesShell);
		bonesShellNotInAll.removeAll(bonesShellInAll);

		renderer.addNotInAllBone(bonesShellNotInAll);
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
		allBonesShell.removeAll(newRefsJList.getSelectedValuesList());
		bonesShellInAll.removeAll(newRefsJList.getSelectedValuesList());
		renderer.removeNotInAllBone(newRefsJList.getSelectedValuesList());

		newRefs.removeAll(newRefsJList.getSelectedValuesList());


		repaint();
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
//		renderer.removeNotInAllBone(newRefsJList.getSelectedValuesList());
		for (final Object o : bonesList.getSelectedValuesList()) {
			renderer.removeNotInAllBone((BoneShell) o);
			if (!newRefs.contains(o)) {
				newRefs.addElement((BoneShell) o);
			}
			allBonesShell.add((BoneShell) o);
			bonesShellInAll.add((BoneShell) o);
			bonesShellNotInAll.remove(o);
		}
		repaint();
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

	public Set<Bone> getBonesInAll() {
		return bonesShellInAll.stream().map(BoneShell::getBone).collect(Collectors.toSet());
	}

	public Set<Bone> getBonesNotInAll() {
		return bonesShellNotInAll.stream().map(BoneShell::getBone).collect(Collectors.toSet());
	}
}