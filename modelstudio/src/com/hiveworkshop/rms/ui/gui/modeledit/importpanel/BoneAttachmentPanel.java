package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class BoneAttachmentPanel extends JPanel implements ActionListener, ListSelectionListener {
	JLabel title;

	// Old bone refs (matrices)
	JLabel oldBoneRefsLabel;
	DefaultListModel<MatrixShell> oldBoneRefs;
	JList<MatrixShell> oldBoneRefsList;
	JScrollPane oldBoneRefsPane;

	// New refs
	JLabel newRefsLabel;
	DefaultListModel<BoneShell> newRefs;
	JList<BoneShell> newRefsList;
	JScrollPane newRefsPane;
	JButton removeNewRef;
	JButton moveUp;
	JButton moveDown;

	// Bones (all available -- NEW AND OLD)
	JLabel bonesLabel;
	DefaultListModel<BoneShell> bones;
	JList<BoneShell> bonesList;
	JScrollPane bonesPane;
	JButton useBone;

	EditableModel model;
	Geoset geoset;
	MatrixShell currentMatrix = null;
	ModelHolderThing mht;

	public BoneAttachmentPanel(ModelHolderThing mht, final EditableModel model, final Geoset whichGeoset, final BoneShellListCellRenderer renderer) {
		this.mht = mht;
		this.model = model;
		geoset = whichGeoset;

		bonesLabel = new JLabel("Bones");
		updateBonesList();
		// Built before oldBoneRefs, so that the MatrixShells can default to
		// using New Refs with the same name as their first bone
		bonesList = new JList<>(bones);
		bonesList.setCellRenderer(renderer);
		bonesPane = new JScrollPane(bonesList);

		useBone = new JButton("Use Bone(s)", ImportPanel.greenArrowIcon);
		useBone.addActionListener(this);

		oldBoneRefsLabel = new JLabel("Old Bone References");
		buildOldRefsList();
		oldBoneRefsList = new JList<>(oldBoneRefs);
		oldBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		oldBoneRefsList.setCellRenderer(new MatrixShell2DListCellRenderer(new ModelViewManager(impPanel.currentModel),
//				new ModelViewManager(impPanel.importedModel)));
		oldBoneRefsList.addListSelectionListener(this);
		oldBoneRefsPane = new JScrollPane(oldBoneRefsList);

		newRefsLabel = new JLabel("New Refs");
		newRefs = new DefaultListModel<>();
		newRefsList = new JList<>(newRefs);
		newRefsList.setCellRenderer(renderer);
		newRefsPane = new JScrollPane(newRefsList);

		removeNewRef = new JButton("Remove", ImportPanel.redXIcon);
		removeNewRef.addActionListener(this);
		moveUp = new JButton(ImportPanel.moveUpIcon);
		moveUp.addActionListener(this);
		moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(this);

		buildLayout();

		refreshNewRefsList();
	}

	public void buildLayout() {
		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(oldBoneRefsLabel)
						.addComponent(oldBoneRefsPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(newRefsLabel)
						.addComponent(newRefsPane)
						.addComponent(removeNewRef))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(moveUp)
						.addComponent(moveDown))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(bonesLabel)
						.addComponent(bonesPane)
						.addComponent(useBone)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(oldBoneRefsLabel)
						.addComponent(newRefsLabel)
						.addComponent(bonesLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(oldBoneRefsPane)
						.addComponent(newRefsPane)
						.addGroup(layout.createSequentialGroup()
								.addComponent(moveUp).addGap(16)
								.addComponent(moveDown))
						.addComponent(bonesPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(removeNewRef)
						.addComponent(useBone)));
		setLayout(layout);
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		refreshLists();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == useBone) {
			for (final Object o : bonesList.getSelectedValuesList()) {
				if (!newRefs.contains(o)) {
					newRefs.addElement((BoneShell) o);
				}
			}
			refreshNewRefsList();
		} else if (e.getSource() == removeNewRef) {
			for (final Object o : newRefsList.getSelectedValuesList()) {
				int i = newRefsList.getSelectedIndex();
				newRefs.removeElement(o);
				if (i > (newRefs.size() - 1)) {
					i = newRefs.size() - 1;
				}
				newRefsList.setSelectedIndex(i);
			}
			refreshNewRefsList();
		} else if (e.getSource() == moveUp) {
			final int[] indices = newRefsList.getSelectedIndices();
			if ((indices != null) && (indices.length > 0)) {
				if (indices[0] > 0) {
					for (int i = 0; i < indices.length; i++) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] - 1, bs);
						indices[i] -= 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		} else if (e.getSource() == moveDown) {
			final int[] indices = newRefsList.getSelectedIndices();
			if ((indices != null) && (indices.length > 0)) {
				if (indices[indices.length - 1] < (newRefs.size() - 1)) {
					for (int i = indices.length - 1; i >= 0; i--) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] + 1, bs);
						indices[i] += 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		}
	}

	public void refreshLists() {
		updateBonesList();
		refreshNewRefsList();
	}

	public void refreshNewRefsList() {
		// Does save the currently constructed matrix
		final java.util.List<BoneShell> selection = newRefsList.getSelectedValuesList();
		if (currentMatrix != null) {
			currentMatrix.newBones.clear();
			for (final Object bs : newRefs.toArray()) {
				currentMatrix.newBones.add((BoneShell) bs);
			}
		}
		newRefs.clear();
		if (oldBoneRefsList.getSelectedValue() != null) {
			for (final BoneShell bs : oldBoneRefsList.getSelectedValue().newBones) {
				if (bones.contains(bs)) {
					newRefs.addElement(bs);
				}
			}
		}

		final int[] indices = new int[selection.size()];
		for (int i = 0; i < selection.size(); i++) {
			indices[i] = newRefs.indexOf(selection.get(i));
		}
		newRefsList.setSelectedIndices(indices);
		currentMatrix = oldBoneRefsList.getSelectedValue();
	}

	public void reloadNewRefsList() {
		// Does not save the currently constructed matrix
		final java.util.List<BoneShell> selection = newRefsList.getSelectedValuesList();
		newRefs.clear();
		if (oldBoneRefsList.getSelectedValue() != null) {
			for (final BoneShell bs : oldBoneRefsList.getSelectedValue().newBones) {
				if (bones.contains(bs)) {
					newRefs.addElement(bs);
				}
			}
		}

		final int[] indices = new int[selection.size()];
		for (int i = 0; i < selection.size(); i++) {
			indices[i] = newRefs.indexOf(selection.get(i));
		}
		newRefsList.setSelectedIndices(indices);
		currentMatrix = oldBoneRefsList.getSelectedValue();
	}

	public void buildOldRefsList() {
		if (oldBoneRefs == null) {
			oldBoneRefs = new DefaultListModel<>();
		} else {
			oldBoneRefs.clear();
		}
		for (final Matrix m : geoset.getMatrix()) {
			final MatrixShell ms = new MatrixShell(m);
			// For look to find similarly named stuff and add it
			for (final Object bs : bones.toArray()) {
				// try {
				for (final Bone b : m.getBones()) {
					final String mName = b.getName();
					if (((BoneShell) bs).bone == b)// .getName().equals(mName) )
					{
						ms.newBones.add((BoneShell) bs);
					}
				}
				// }
				// catch (NullPointerException e)
				// {
				// System.out.println("We have a null in a matrix process,
				// probably not good but it was assumed that this might
				// happen.");
				// }
			}
			oldBoneRefs.addElement(ms);
		}
	}

	public void resetMatrices() {
		for (int i = 0; i < oldBoneRefs.size(); i++) {
			final MatrixShell ms = oldBoneRefs.get(i);
			ms.newBones.clear();
			final Matrix m = ms.matrix;
			// For look to find right stuff and add it
			for (final Object bs : bones.toArray()) {
				for (final Bone b : m.getBones()) {
					if (((BoneShell) bs).bone == b)// .getName().equals(mName) )
					{
						ms.newBones.add((BoneShell) bs);
					}
				}
			}
		}
		reloadNewRefsList();
	}

	public void setMatricesToSimilarNames() {
		for (int i = 0; i < oldBoneRefs.size(); i++) {
			final MatrixShell ms = oldBoneRefs.get(i);
			ms.newBones.clear();
			final Matrix m = ms.matrix;
			// For look to find similarly named stuff and add it
			for (final Object bs : bones.toArray()) {
				for (final Bone b : m.getBones()) {
					final String mName = b.getName();
					if (((BoneShell) bs).bone.getName().equals(mName)) {
						ms.newBones.add((BoneShell) bs);
					}
				}
			}
		}
		reloadNewRefsList();
	}

	public void updateBonesList() {
		bones = mht.getFutureBoneList();
	}
}
