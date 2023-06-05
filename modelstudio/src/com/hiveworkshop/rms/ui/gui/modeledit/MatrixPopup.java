package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MatrixEditListRenderer;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * The panel to handle re-assigning Matrices.
 *
 * Eric Theller 6/11/2012
 */

public class MatrixPopup extends JPanel {

	private final SearchableList<Bone> newRefsJList;
	private final SearchableList<Bone> bonesList;
	private final Set<Bone> bonesNotInAll = new HashSet<>();
	private final Set<Bone> bonesInAll = new HashSet<>();
	private final Set<Bone> allBones = new HashSet<>();
	private final MatrixEditListRenderer renderer;

	public MatrixPopup(ModelHandler modelHandler) {
		this(modelHandler.getModel(), modelHandler.getModelView().getSelectedVertices());
	}
	public MatrixPopup(EditableModel model, Collection<GeosetVertex> vertices) {
		super(new MigLayout("gap 0", "[grow][][grow]", "[align center][grow][align center]"));

		renderer = new MatrixEditListRenderer(model, null).setShowClass(false);

		JCheckBox displayParents = new JCheckBox("Display parents", false);
		displayParents.addActionListener(e -> showParents(renderer, displayParents));


		JPanel bonePanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][][grow][]"));
		bonePanel.add(new JLabel("Bones"), "wrap");

		bonesList = new SearchableList<>(this::filterBones).addAll(model.getBones());
		bonesList.setCellRenderer(renderer);
		bonePanel.add(bonesList.getSearchField(), "growx, wrap");


		JScrollPane bonesPane = bonesList.getScrollableList();
		bonesPane.setPreferredSize(new Dimension(400, 500));
		bonePanel.add(bonesPane, "wrap");

		JButton useBone = new JButton("Use Bone(s)", RMSIcons.greenArrowIcon);
		useBone.addActionListener(e -> useBone());
		bonePanel.add(useBone, "wrap");

		JPanel newRefsPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][grow][]"));
		newRefsPanel.add(new JLabel("New Refs"), "wrap");

		newRefsJList = getNewRefsLists(vertices);
		newRefsJList.setCellRenderer(renderer);
		JScrollPane newRefsPane = newRefsJList.getScrollableList();
		newRefsPane.setPreferredSize(new Dimension(400, 500));
		newRefsPanel.add(newRefsPane, "wrap");

		JButton removeNewRef = new JButton("Remove", RMSIcons.redXIcon);
		removeNewRef.addActionListener(e -> removeNewRef());
		newRefsPanel.add(removeNewRef, "wrap");

		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0", "[]5", "[align center]16[align center]"));

		JButton moveUp = new JButton(RMSIcons.moveUpIcon);
		moveUp.addActionListener(e -> moveUp());
		arrowPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(RMSIcons.moveDownIcon);
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

	private SearchableList<Bone> getNewRefsLists(Collection<GeosetVertex> vertices) {
		setUpBoneLists(vertices);
		SearchableList<Bone> newRefsJList = new SearchableList<>(this::filterBones);
		for(Bone bone : bonesList.getFullListModel()){
			// To keep the bones in the correct order
			if(allBones.contains(bone)){
				newRefsJList.add(bone);
			}
		}
		return newRefsJList;
	}
	private void setUpBoneLists(Collection<GeosetVertex> vertices) {
		vertices.forEach(vertex -> allBones.addAll(vertex.getBones()));
		bonesInAll.addAll(allBones);
		vertices.forEach(vertex -> bonesInAll.removeIf(b -> !vertex.getBones().contains(b)));

		bonesNotInAll.addAll(allBones);
		bonesNotInAll.removeAll(bonesInAll);

		renderer.addNotInAllBone(bonesNotInAll);
	}


	private void moveDown() {
		final int[] indices = newRefsJList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			if (indices[indices.length - 1] < newRefsJList.listSize() - 1) {
				for (int i = indices.length - 1; i >= 0; i--) {
					final Bone bs = newRefsJList.get(indices[i]);
					newRefsJList.remove(bs);
					newRefsJList.add(indices[i] + 1, bs);
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
					final Bone bs = newRefsJList.get(indices[i]);
					newRefsJList.remove(bs);
					newRefsJList.add(indices[i] - 1, bs);
					indices[i] -= 1;
				}
			}
			newRefsJList.setSelectedIndices(indices);
		}
	}

	private void removeNewRef() {
		newRefsJList.getSelectedValuesList().forEach(allBones::remove);
		newRefsJList.getSelectedValuesList().forEach(bonesInAll::remove);
		renderer.removeNotInAllBone(newRefsJList.getSelectedValuesList());

		newRefsJList.removeAll(newRefsJList.getSelectedValuesList());


		repaint();
	}

	private void useBone() {
		for (Bone shell : bonesList.getSelectedValuesList()) {
			renderer.removeNotInAllBone(shell);
			if (!newRefsJList.contains(shell)) {
				newRefsJList.add(shell);
			}
			allBones.add(shell);
			bonesInAll.add(shell);
			bonesNotInAll.remove(shell);
		}
		repaint();
	}

	private boolean filterBones(Bone shell, String filterText) {
		return shell.getName().toLowerCase().contains(filterText.toLowerCase());
	}

	public List<Bone> getNewBoneList() {
		final List<Bone> bones = new ArrayList<>();
		for (final Bone bs : newRefsJList.getFullListModel()) {
			bones.add(bs);
		}
		return bones;
	}

	public Set<Bone> getBonesInAll() {
		return bonesInAll;
	}

	public Set<Bone> getBonesNotInAll() {
		return bonesNotInAll;
	}
}